import logging
import threading
import time
from typing import Dict, List, Any
from ..models.recommendation_engine import ArtRecommendationEngine
from ..services.vector_builder import VectorBuilder, FEATURE_NAMES

# Delay importing DataProcessor (it depends on psycopg2). Import lazily.
from ..services.csv_data_processor import CSVDataProcessor
from ..config.settings import config
import os

logger = logging.getLogger(__name__)

class ModelTrainer:
    def __init__(self):
        self.data_processor = CSVDataProcessor()
        self.vector_builder = VectorBuilder()
        self.is_training = False
        self.last_training_time = None
        self.training_stats = {}
    
    def train_model_async(self) -> bool:
        """Entrena el modelo en segundo plano"""
        if self.is_training:
            logger.warning("⚠️  Entrenamiento ya en progreso")
            return False
        
        def training_thread():
            self.is_training = True
            try:
                logger.info("🚀 Iniciando entrenamiento asíncrono...")
                success = self._train_model()
                self.is_training = False
                self.last_training_time = time.time()
                
                if success:
                    logger.info("✅ Entrenamiento completado exitosamente")
                else:
                    logger.error("❌ Entrenamiento falló")
                    
            except Exception as e:
                self.is_training = False
                logger.error(f"💥 Error en entrenamiento: {e}")
        
        thread = threading.Thread(target=training_thread)
        thread.daemon = True
        thread.start()
        
        return True
    
    def _train_model(self) -> bool:
        """Lógica principal de entrenamiento"""
        try:
            # 1. Obtener datos de entrenamiento
            logger.info("📥 Obteniendo datos de entrenamiento...")
            # If the DataProcessor isn't available (e.g., missing psycopg2) or returns
            # no/insufficient data, fall back to synthetic data so training can proceed.
            training_data = []

            if self.data_processor is None:
                logger.warning("⚠️  DataProcessor no disponible (posible falta de psycopg2). Usando datos sintéticos.")
                training_data = self._generate_synthetic_data(max(config.MIN_USERS_FOR_TRAINING, 200))
            else:
                try:
                    training_data = self.data_processor.get_training_data_from_db() or []
                except Exception as e:
                    logger.warning(f"⚠️  Error obteniendo datos reales: {e}. Usando datos sintéticos.")
                    training_data = []

            # Option: if environment requests CSV-only training, do not generate synthetic users
            csv_only = os.getenv('CSV_ONLY_TRAINING', '0') in ('1', 'true', 'True')
            if len(training_data) < config.MIN_USERS_FOR_TRAINING:
                if csv_only:
                    logger.error(f"❌ Datos insuficientes ({len(training_data)} usuarios) y CSV_ONLY_TRAINING habilitado. Abortando entrenamiento.")
                    return False
                logger.warning(f"⚠️  Datos insuficientes ({len(training_data)} usuarios). Añadiendo sintéticos para llegar al mínimo.")
                need = max(0, config.MIN_USERS_FOR_TRAINING - len(training_data))
                synthetic_data = self._generate_synthetic_data(need + 100)
                training_data.extend(synthetic_data)
                logger.info(f"➕ Añadidos {len(synthetic_data)} usuarios sintéticos")
            
            # 2. Preparar datos para el modelo
            logger.info("🔧 Preparando datos para entrenamiento...")
            model_training_data = []
            
            for user_data in training_data:
                prepared_data = self._prepare_user_data(user_data)
                if prepared_data:
                    model_training_data.append(prepared_data)
            
            if not model_training_data:
                logger.error("❌ No se pudieron preparar datos para entrenamiento")
                return False
            
            # 3. Entrenar modelo
            logger.info(f"🏋️‍♂️ Entrenando con {len(model_training_data)} usuarios...")
            model = ArtRecommendationEngine()
            success = model.train(model_training_data)
            
            if success:
                # 4. Guardar modelo
                model.save_model(config.MODEL_PATH)
                
                # 5. Guardar estadísticas
                self._save_training_stats(model, len(model_training_data))
                
                return True
            else:
                return False
                
        except Exception as e:
            logger.error(f"Error en entrenamiento: {e}")
            return False
    
    def _prepare_user_data(self, user_data: Dict) -> Dict[str, Any]:
        """Prepara datos de usuario para entrenamiento.

        Usa `preference_vector` si está presente en el registro (proviene de
        user_preferences.csv). Si no está, construye el vector desde el
        `purchase_history` usando VectorBuilder.
        """
        try:
            user_id = user_data.get('user_id')
            if user_id is None:
                return None

            # Prefer explicit preference vector when present
            pref = user_data.get('preference_vector') or user_data.get('preferences')
            if pref and isinstance(pref, dict) and len(pref) > 0:
                # If pref keys match FEATURE_NAMES directly, keep them. Otherwise
                # try to build a compatible vector via VectorBuilder helper that
                # maps free-form keys to model features.
                vec = {k: float(v) for k, v in pref.items() if k in FEATURE_NAMES}
                if not vec:
                    try:
                        vec = self.vector_builder.build_vector_from_pref_map(pref)
                    except Exception:
                        vec = {}
                if vec:
                    return {
                        'user_id': int(user_id),
                        'vector': vec,
                        'purchase_history': user_data.get('purchase_history', []),
                        'total_purchases': user_data.get('total_purchases', 0),
                        'avg_purchase_value': user_data.get('avg_purchase_value', 0),
                        'raw': user_data
                    }

            # Fallback: build from purchase history
            purchase_history = user_data.get('purchase_history', []) or []
            if purchase_history:
                user_vector = self.vector_builder.build_user_vector_from_history(purchase_history)
                return {
                    'user_id': int(user_id),
                    'vector': user_vector,
                    'purchase_history': purchase_history,
                    'total_purchases': user_data.get('total_purchases', 0),
                    'avg_purchase_value': user_data.get('avg_purchase_value', 0),
                    'raw': user_data
                }

            # Last resort: check embedded user row for vector_kv or preference_vector
            user_row = user_data.get('user') or {}
            upr = None
            if isinstance(user_row, dict):
                upr = user_row.get('preference_vector') or user_row.get('vector_kv')
            if upr and isinstance(upr, dict):
                vec = {k: float(v) for k, v in upr.items() if k in FEATURE_NAMES}
                if vec:
                    return {
                        'user_id': int(user_id),
                        'vector': vec,
                        'purchase_history': purchase_history,
                        'total_purchases': user_data.get('total_purchases', 0),
                        'avg_purchase_value': user_data.get('avg_purchase_value', 0),
                        'raw': user_data
                    }

            return None

        except Exception as e:
            logger.error(f"Error preparando datos de usuario {user_data.get('user_id')}: {e}")
            return None
    
    def _generate_synthetic_data(self, num_users: int) -> List[Dict]:
        """Genera datos sintéticos para entrenamiento"""
        synthetic_data = []
        
        # Patrones de usuario predefinidos
        user_patterns = [
            # Traditional Collector
            {
                'preferred_categories': ['Realista', 'Histórica', 'Religiosa'],
                'preferred_techniques': ['Óleo', 'Temple'],
                'price_range': (500, 2000),
                'purchase_frequency': 0.3
            },
            # Modern Art Lover
            {
                'preferred_categories': ['Abstracta', 'Contemporánea', 'Conceptual'],
                'preferred_techniques': ['Acrílico', 'Mixta', 'Digital'],
                'price_range': (200, 800),
                'purchase_frequency': 0.7
            },
            # Eclectic Buyer
            {
                'preferred_categories': ['Impresionista', 'Decorativa', 'Expresionista'],
                'preferred_techniques': ['Acuarela', 'Óleo', 'Acrílico'],
                'price_range': (100, 600),
                'purchase_frequency': 0.5
            },
            # Budget Shopper
            {
                'preferred_categories': ['Decorativa', 'Realista'],
                'preferred_techniques': ['Acuarela', 'Tinta'],
                'price_range': (50, 300),
                'purchase_frequency': 0.8
            }
        ]
        
        base_user_id = 100000  # IDs altos para usuarios sintéticos
        
        for i in range(num_users):
            pattern = user_patterns[i % len(user_patterns)]
            user_data = self._generate_synthetic_user(base_user_id + i, pattern)
            synthetic_data.append(user_data)
        
        return synthetic_data
    
    def _generate_synthetic_user(self, user_id: int, pattern: Dict) -> Dict:
        """Genera un usuario sintético"""
        import random
        
        purchases = []
        num_purchases = max(1, int(random.gauss(5, 3)))  # Media 5 compras
        
        for _ in range(num_purchases):
            purchase = {
                'product_id': random.randint(1, 1000),
                'quantity': random.randint(1, 3),
                'total': random.uniform(pattern['price_range'][0], pattern['price_range'][1]),
                'categories': random.sample(pattern['preferred_categories'], 
                                          k=random.randint(1, len(pattern['preferred_categories']))),
                'techniques': random.sample(pattern['preferred_techniques'],
                                          k=random.randint(1, len(pattern['preferred_techniques'])))
            }
            purchases.append(purchase)
        
        return {
            'user_id': user_id,
            'purchase_history': purchases,
            'total_purchases': num_purchases,
            'avg_purchase_value': sum(p['total'] for p in purchases) / num_purchases
        }
    
    def _save_training_stats(self, model: ArtRecommendationEngine, num_users: int):
        """Guarda estadísticas del entrenamiento"""
        self.training_stats = {
            'last_training_time': time.time(),
            'num_users_trained': num_users,
            'num_clusters': len(set(model.user_clusters.values())),
            'feature_dimensions': len(model.feature_names),
            'pca_variance_explained': model.pca_model.explained_variance_ratio_.sum() if model.pca_model else 0
        }
        
        logger.info(f"📊 Estadísticas de entrenamiento: {self.training_stats}")
    
    def get_training_status(self) -> Dict[str, Any]:
        """Obtiene estado actual del entrenamiento"""
        return {
            'is_training': self.is_training,
            'last_training_time': self.last_training_time,
            'training_stats': self.training_stats
        }