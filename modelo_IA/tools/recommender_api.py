"""
FastAPI wrapper to serve the trained recommendation model.

Endpoints:
 - GET /recommendations/{user_id}?top_n=10
 - GET /similar_users/{user_id}?limit=5

Run with:
 uvicorn modelo_IA.tools.recommender_api:app --host 0.0.0.0 --port 8000

Ensure the Python environment has required packages (fastapi, uvicorn, joblib, pandas, numpy, scikit-learn).
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import joblib
import os
from typing import List, Dict, Any
from modelo_IA.services.csv_data_processor import DataProcessor
from modelo_IA.models.recommendation_engine import ArtRecommendationEngine
from modelo_IA.services.model_trainer import ModelTrainer
from modelo_IA.config.settings import config

APP = FastAPI(title="Artemisia Recommender API")

# Allow local requests from Spring Boot / browser while developing
APP.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Prefer configured MODEL_PATH; fall back to package-relative path
MODEL_PATH = config.MODEL_PATH or os.path.join('models', 'trained', 'recommendation_model.pkl')
# Normalize to package root so relative paths work when running as script
PKG_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
if not os.path.isabs(MODEL_PATH):
    MODEL_PATH = os.path.abspath(os.path.join(PKG_ROOT, MODEL_PATH))

DATA_DIR = os.path.abspath(os.path.join(PKG_ROOT, 'modelo_IA', 'data_exports'))


def load_model() -> Any:
    # Use the engine's loader which can trigger initial CSV-only training when missing
    model = ArtRecommendationEngine.load_model(MODEL_PATH)
    return model


def load_products(sample_limit: int = 500) -> List[Dict[str, Any]]:
    dp = DataProcessor(export_dir=DATA_DIR)
    prods = dp.get_available_products()
    # Only return products marked as AVAILABLE (case-insensitive) to ensure we
    # recommend only actual items in the product catalog that are visible.
    available = [p for p in prods if str(p.get('status', '')).upper() == 'AVAILABLE']
    return available[:sample_limit]


@APP.get('/health')
def health():
    return {'status': 'ok'}


@APP.get('/recommendations/{user_id}')
def recommendations(user_id: int, top_n: int = 10):
    model = load_model()

    # Ensure the requested user exists in the canonical data source (DB preferred)
    dp = DataProcessor(export_dir=DATA_DIR)
    user_row = dp.get_user_data(user_id)
    if not user_row or not user_row.get('user'):
        raise HTTPException(status_code=404, detail=f'user_id {user_id} not found in DB')

    # user_vectors must only contain DB users; check model
    user_vector = model.user_vectors.get(user_id)
    if not user_vector:
        raise HTTPException(status_code=404, detail=(f'user_id {user_id} not present in trained model. '
                                                     'Re-train model from DB to include this user.'))

    products = load_products(1000)
    recs = model.get_recommendations(user_vector, products, top_n=top_n)
    return recs


@APP.get('/similar_users/{user_id}')
def similar_users(user_id: int, limit: int = 5):
    model = load_model()

    # Verify user exists in DB
    dp = DataProcessor(export_dir=DATA_DIR)
    user_row = dp.get_user_data(user_id)
    if not user_row or not user_row.get('user'):
        raise HTTPException(status_code=404, detail=f'user_id {user_id} not found in DB')

    if user_id not in model.user_vectors:
        raise HTTPException(status_code=404, detail=(f'user_id {user_id} not found in trained model. '
                                                     'Re-train model from DB to include this user.'))

    # Find similar users and ensure they are in DB
    similar = model.find_similar_users(user_id, limit)
    # Filter similar users to only those present in DB
    db_similar = []
    for uid in similar:
        row = dp.get_user_data(uid)
        if row and row.get('user'):
            db_similar.append(uid)

    return {'user_id': user_id, 'similar_users': db_similar}



@APP.post('/train')
def train(training_data: List[Dict[str, Any]]):
    """Endpoint para re-entrenar el modelo con payload desde Spring Boot.
    Espera una lista de dicts con al menos {'user_id': int, 'vector': {feature: weight}, 'purchase_history': [...]}
    """
    try:
        # Crear un motor nuevo o cargar existente
        model = ArtRecommendationEngine()

        # Intentar entrenar con el payload
        ok = model.train(training_data)
        if not ok:
            raise RuntimeError('Training failed or insufficient data')

        # Guardar modelo
        model.save_model(MODEL_PATH)
        return {'status': 'ok', 'message': f'model trained and saved to {MODEL_PATH}'}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



@APP.post('/train_trigger')
def train_trigger(source: str = 'db'):
    """Trigger training from an external caller.

    source: 'csv' to run initial CSV-only training (synchronous), 'db' to train from DB.
    """
    try:
        trainer = ModelTrainer()
        if source == 'csv':
            ok = trainer.train_from_csv()
        else:
            # run DB-based training synchronously
            ok = trainer._train_model()

        if not ok:
            raise RuntimeError('Training reported failure')
        return {'status': 'ok', 'source': source}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
