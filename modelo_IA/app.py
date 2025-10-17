from flask import Flask, request, jsonify
from flask_cors import CORS
import logging
import os
from datetime import datetime

from .models.recommendation_engine import ArtRecommendationEngine
from .services.model_trainer import ModelTrainer
from .services.preference_updater import update_user_preferences_from_product, update_user_preferences_from_purchase
from .config.settings import config

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('recommendation_service.log')
    ]
)

logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# Inicializar servicios
model_trainer = ModelTrainer()

# Cargar modelo al inicio
recommendation_engine = ArtRecommendationEngine.load_model(config.MODEL_PATH)

# API key for internal endpoints
INTERNAL_API_KEY = os.getenv('RECOMMENDER_API_KEY')


def require_api_key(f):
    def wrapper(*args, **kwargs):
        if INTERNAL_API_KEY:
            key = request.headers.get('X-API-Key') or request.args.get('api_key')
            if not key or key != INTERNAL_API_KEY:
                return jsonify({'error': 'forbidden'}), 403
        return f(*args, **kwargs)
    wrapper.__name__ = f.__name__
    return wrapper

@app.route('/recommendations/<int:user_id>', methods=['GET'])
def get_recommendations(user_id):
    """Obtiene recomendaciones para un usuario"""
    try:
        limit = request.args.get('limit', 10, type=int)
        
        # Obtener vector de usuario
        from services.csv_data_processor import CSVDataProcessor
        data_processor = CSVDataProcessor()
        user_data = data_processor.get_user_data(user_id)
        if not user_data:
            return jsonify({"error": "User not found"}), 404

        user_vector = user_data.get('preference_vector', {})
        if not user_vector:
            # Construir vector desde historial
            from services.vector_builder import VectorBuilder
            vector_builder = VectorBuilder()
            user_vector = vector_builder.build_user_vector_from_history(
                user_data.get('purchase_history', [])
            )
        
        # Obtener productos disponibles
        products = data_processor.get_available_products()
            
        # Obtener recomendaciones
        recommendations = recommendation_engine.get_recommendations(
            user_vector, products, top_n=limit
        )
        
        return jsonify([prod['product_id'] for prod in recommendations])
        
    except Exception as e:
        logger.error(f"Error getting recommendations for user {user_id}: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/similar-users/<int:user_id>', methods=['GET'])
def get_similar_users(user_id):
    """Encuentra usuarios similares"""
    try:
        limit = request.args.get('limit', 5, type=int)
        similar_users = recommendation_engine.find_similar_users(user_id, limit)
        return jsonify(similar_users)
    except Exception as e:
        logger.error(f"Error finding similar users for {user_id}: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/train', methods=['POST'])
def train_model():
    """Inicia entrenamiento del modelo"""
    try:
        success = model_trainer.train_model_async()
        
        if success:
            return jsonify({
                "message": "Training started successfully",
                "timestamp": datetime.now().isoformat()
            })
        else:
            return jsonify({"error": "Training already in progress"}), 409
            
    except Exception as e:
        logger.error(f"Error starting training: {e}")
        return jsonify({"error": "Internal server error"}), 500


@app.route('/update-view', methods=['POST'])
@require_api_key
def receive_view_event():
    """Endpoint para recibir vistas de productos y actualizar preferencias (incremental)."""
    try:
        payload = request.get_json()
        user_id = payload.get('user_id')
        product_id = payload.get('product_id')
        duration = payload.get('duration')

        if not user_id or not product_id:
            return jsonify({'error': 'user_id and product_id required'}), 400

        # Compute event weight from duration (simple heuristic)
        duration = payload.get('duration')
        event_weight = None
        try:
            if duration:
                # normalize duration to 0..1 using a cap of 300s
                event_weight = min(1.0, float(duration) / 300.0)
        except Exception:
            event_weight = None

        # For views we use a small beta
        success = update_user_preferences_from_product(int(user_id), int(product_id), beta=0.05, event_weight=event_weight)
        if success:
            return jsonify({'status': 'updated'}), 200
        else:
            return jsonify({'status': 'failed'}), 500

    except Exception as e:
        logger.error(f"Error processing view event: {e}")
        return jsonify({'error': 'internal error'}), 500


@app.route('/update-purchase', methods=['POST'])
@require_api_key
def receive_purchase_event():
    """Endpoint para recibir compras completadas y actualizar preferencias (fuerte)."""
    try:
        payload = request.get_json()
        user_id = payload.get('user_id')
        product_ids = payload.get('product_ids')

        if not user_id or not product_ids:
            return jsonify({'error': 'user_id and product_ids required'}), 400

        # purchases are strong signals; allow optional event_weight multiplier
        event_weight = payload.get('event_weight')
        if event_weight is not None:
            try:
                ew = float(event_weight)
                ew = max(0.0, min(1.0, ew))
            except Exception:
                ew = 1.0
        else:
            ew = 1.0

        success = update_user_preferences_from_purchase(int(user_id), [int(x) for x in product_ids], beta=0.5 * ew)
        if success:
            return jsonify({'status': 'updated'}), 200
        else:
            return jsonify({'status': 'failed'}), 500

    except Exception as e:
        logger.error(f"Error processing purchase event: {e}")
        return jsonify({'error': 'internal error'}), 500

@app.route('/training-status', methods=['GET'])
def get_training_status():
    """Obtiene estado del entrenamiento"""
    status = model_trainer.get_training_status()
    return jsonify(status)

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "service": "Artemisia Recommendation Engine",
        "timestamp": datetime.now().isoformat(),
        "model_loaded": recommendation_engine.kmeans_model is not None,
        "users_in_model": len(recommendation_engine.user_vectors)
    })

@app.route('/products', methods=['GET'])
def get_products():
    """Obtiene productos disponibles"""
    try:
        from services.csv_data_processor import CSVDataProcessor
        data_processor = CSVDataProcessor()
        products = data_processor.get_available_products()
        return jsonify(products)
    except Exception as e:
        logger.error(f"Error getting products: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/user/<int:user_id>/vector', methods=['GET'])
def get_user_vector(user_id):
    """Obtiene vector de preferencias de usuario"""
    try:
        from services.csv_data_processor import CSVDataProcessor
        data_processor = CSVDataProcessor()
        user_data = data_processor.get_user_data(user_id)
        if not user_data:
            return jsonify({"error": "User not found"}), 404
        
        vector = user_data.get('preference_vector', {})
        return jsonify(vector)
    except Exception as e:
        logger.error(f"Error getting user vector for {user_id}: {e}")
        return jsonify({"error": "Internal server error"}), 500

if __name__ == '__main__':
    logger.info("🚀 Starting Artemisia Recommendation Service...")
    logger.info(f"🔧 Model path: {config.MODEL_PATH}")
    
    app.run(host='0.0.0.0', port=5000, debug=True)