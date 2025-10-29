"""
FastAPI wrapper to serve the trained recommendation model.

Endpoints:
 - GET /recommendations/{user_id}?top_n=10
 - GET /similar_users/{user_id}?limit=5

Run with:
 uvicorn modelo_IA.tools.recommender_api:APP --host 0.0.0.0 --port 8000

Ensure the Python environment has required packages (fastapi, uvicorn, joblib, pandas, numpy, scikit-learn).
"""
import os
import sys
import joblib
from typing import List, Dict, Any, Optional
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
import traceback
import logging

# Prefer package-relative imports when used as a package. When running this file
# directly (script mode), fall back to ensuring the project root is on sys.path
# and import using the package name. This makes the module runnable both via
# `uvicorn modelo_IA.tools.recommender_api:APP` and `python modelo_IA/tools/recommender_api.py`.
try:
    # when imported as a package (recommended)
    from ..services.csv_data_processor import DataProcessor
    from ..models.recommendation_engine import ArtRecommendationEngine
    from ..services.model_trainer import ModelTrainer
    from ..config.settings import config
    from ..services.preference_updater import update_user_preferences_from_product, update_user_preferences_from_purchase, create_user_pref
except Exception:
    # fallback for script execution: add project root to sys.path and import by package
    PROJ_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
    if PROJ_ROOT not in sys.path:
        sys.path.insert(0, PROJ_ROOT)
    from modelo_IA.services.csv_data_processor import DataProcessor
    from modelo_IA.models.recommendation_engine import ArtRecommendationEngine
    from modelo_IA.services.model_trainer import ModelTrainer
    from modelo_IA.config.settings import config
    from modelo_IA.services.preference_updater import update_user_preferences_from_product, update_user_preferences_from_purchase, create_user_pref

APP = FastAPI(title="Artemisia Recommender API")
# provide lowercase alias `app` so `uvicorn module:app` works (common convention)
app = APP

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

# Determine project root (two levels up from this file)
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
if not os.path.isabs(MODEL_PATH):
    MODEL_PATH = os.path.abspath(os.path.join(PROJECT_ROOT, MODEL_PATH))

# data_exports lives under the package directory
DATA_DIR = os.path.abspath(os.path.join(PROJECT_ROOT, 'modelo_IA', 'data_exports'))


def load_model() -> Any:
    # Use the engine's loader which can trigger initial CSV-only training when missing
    model = ArtRecommendationEngine.load_model(MODEL_PATH)
    return model


def load_products(sample_limit: int = 500) -> List[Dict[str, Any]]:
    dp = DataProcessor(export_dir=DATA_DIR)
    products = dp.get_available_products()
    if not products:
        return []

    # Only return products marked as AVAILABLE (case-insensitive)
    available = [p for p in products if str(p.get('status', '')).upper() == 'AVAILABLE']
    return available[:sample_limit]


@APP.get('/health')
def health():
    return {'status': 'ok'}


@APP.get('/recommendations/{user_id}')
def recommendations(user_id: int, top_n: int = 10, limit: int = None):
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
    n = limit if (limit is not None) else top_n
    recs = model.get_recommendations(user_vector, products, top_n=n)
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
            # collect diagnostic info to return to caller
            diag = {}
            try:
                data_sample = trainer.data_processor.get_training_data_from_db() or []
                diag['training_records_returned'] = len(data_sample)
                diag['sample'] = data_sample[:5]
            except Exception as e:
                diag['error_collecting_data_sample'] = str(e)
            raise HTTPException(status_code=500, detail={'message': 'Training reported failure', 'diagnostic': diag})
        return {'status': 'ok', 'source': source}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def _verify_api_key_in_payload(payload: Optional[Dict[str, Any]]):
    """Verify API key provided inside JSON payload under 'api_key' if config requires it.

    If no API key is configured in `config.RECOMMENDER_API_KEY`, this is a no-op.
    """
    expected = getattr(config, 'RECOMMENDER_API_KEY', None)
    if expected:
        if not payload or payload.get('api_key') != expected:
            raise HTTPException(status_code=401, detail='Invalid API key in payload')


@APP.post('/update-view')
def update_view(payload: Dict[str, Any], background: BackgroundTasks):
    """Notify the recommender about a product view by a user.

    Payload: { "user_id": int, "product_id": int, "event_weight": float (optional) }
    This schedules a background update to the user's preference vector.
    """
    # verify api key passed inside JSON payload (if configured)
    _verify_api_key_in_payload(payload)
    user_id = payload.get('user_id')
    product_id = payload.get('product_id')
    event_weight = payload.get('event_weight', None)

    if user_id is None or product_id is None:
        raise HTTPException(status_code=400, detail='user_id and product_id are required')

    try:
        # Schedule background update to avoid blocking the caller
        background.add_task(update_user_preferences_from_product, int(user_id), int(product_id), 0.05, event_weight)
        return {'status': 'accepted', 'user_id': int(user_id), 'product_id': int(product_id)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@APP.post('/update-purchase')
def update_purchase(payload: Dict[str, Any], background: BackgroundTasks):
    """Notify the recommender about a purchase (one or more products) by a user.

    Payload: { "user_id": int, "product_ids": [int, ...] }
    This schedules a background heavier update to the user's preferences (beta ~0.5).
    """
    # verify api key passed inside JSON payload (if configured)
    _verify_api_key_in_payload(payload)
    user_id = payload.get('user_id')
    product_ids = payload.get('product_ids')

    if user_id is None or not product_ids:
        raise HTTPException(status_code=400, detail='user_id and product_ids are required')

    try:
        background.add_task(update_user_preferences_from_purchase, int(user_id), list(product_ids), 0.5)
        return {'status': 'accepted', 'user_id': int(user_id), 'product_ids': product_ids}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



@APP.post('/register_user')
def register_user(payload: Dict[str, Any], background: BackgroundTasks):
    """Create an empty preference entry for a newly created user.

    Payload: { 'user_id': int }
    This will create a user_preferences entry (CSV or DB) without retraining the model.
    """
    _verify_api_key_in_payload(payload)
    user_id = payload.get('user_id')
    if user_id is None:
        raise HTTPException(status_code=400, detail='user_id is required')

    logger = logging.getLogger(__name__)
    # Try synchronous creation first so caller gets immediate diagnostic info.
    try:
        pref_id = create_user_pref(int(user_id))
        if pref_id:
            logger.info(f"Created preference entry for user {user_id} (pref_id={pref_id})")
            return {'status': 'ok', 'user_id': int(user_id), 'pref_id': pref_id}

        # create_user_pref returned falsy (None/False) — schedule background task and return diagnostic info
        logger.warning(f"create_user_pref returned falsy for user {user_id}; scheduling background task")
        try:
            background.add_task(create_user_pref, int(user_id))
        except Exception as be:
            logger.exception("Failed to schedule background create_user_pref task")
            raise

        diag = {
            'message': 'create_user_pref returned falsy; background task scheduled',
            'db_configured': getattr(config, 'DB_HOST', None) is not None and config.db_is_configured(),
            'data_dir': DATA_DIR,
            'prefs_csv_exists': os.path.exists(os.path.join(DATA_DIR, 'user_preferences.csv'))
        }
        return {'status': 'accepted', 'user_id': int(user_id), 'diagnostic': diag}

    except Exception as e:
        # Log full exception and return diagnostic details to caller for debugging
        logger.exception(f"Exception while creating preference for user {user_id}: {e}")
        diag = {
            'exception': str(e),
            'traceback': traceback.format_exc(),
            'db_configured': getattr(config, 'DB_HOST', None) is not None and config.db_is_configured(),
            'data_dir': DATA_DIR,
            'prefs_csv_exists': os.path.exists(os.path.join(DATA_DIR, 'user_preferences.csv'))
        }
        raise HTTPException(status_code=500, detail={'message': 'Error creating user preference', 'diagnostic': diag})


if __name__ == '__main__':
    # Allow running this file directly for development convenience.
    # Example: python modelo_IA\tools\recommender_api.py
    import uvicorn
    uvicorn.run(APP, host='127.0.0.1', port=8000, log_level='info')
