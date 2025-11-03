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
import numpy as np

# Ensure simple console logging is enabled at DEBUG level for diagnostics when
# uvicorn or the environment did not already configure handlers. This makes
# logger.debug/info/warning calls from our modules visible during development.
try:
    root_logger = logging.getLogger()
    # If no handlers configured (common in some startup flows), set a basic config
    if not root_logger.handlers:
        logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)s %(name)s: %(message)s')
    # Always make sure our package logs at DEBUG during troubleshooting
    logging.getLogger('modelo_IA').setLevel(logging.DEBUG)
    root_logger.setLevel(logging.DEBUG)
except Exception:
    # If logging setup fails for any reason, don't crash the app startup
    pass

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
    from ..services.vector_builder import VectorBuilder
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
    from modelo_IA.services.vector_builder import VectorBuilder

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

    # Diagnostic: log model coverage
    try:
        logger = logging.getLogger(__name__)
        model_user_count = len(getattr(model, 'user_vectors', {}) or {})
        logger.debug("recommendations: model_user_count=%d; requesting_user=%s", model_user_count, user_id)
    except Exception:
        pass

    # Try to get the user vector from the trained model first
    user_vector = None
    try:
        user_vector = model.user_vectors.get(user_id)
    except Exception:
        user_vector = None

    vector_used = 'model'
    # If there is an explicit stored preference vector in the canonical data
    # source (DB or CSV), prefer it because it reflects the latest events even
    # when the in-memory trained model has an older cached vector.
    try:
        pref_map = user_row.get('preference_vector') or user_row.get('user', {}).get('preference_vector')
        if pref_map and isinstance(pref_map, dict) and len(pref_map) > 0:
            vb = VectorBuilder()
            try:
                pv = vb.build_vector_from_pref_map(pref_map)
                # override the model vector for this request
                user_vector = pv
                vector_used = 'preference_map'
            except Exception:
                # if conversion fails, fall back to model/user-built vector
                pass
    except Exception:
        pass
    # If the user is not present in the trained model, build a vector on-the-fly
    if not user_vector:
        try:
            logger = logging.getLogger(__name__)
            logger.info(f"User {user_id} not present in trained model; building on-the-fly vector")
            vb = VectorBuilder()
            # Prefer explicit preference_vector from canonical source
            pref = user_row.get('preference_vector') or user_row.get('user', {}).get('preference_vector')
            if pref and isinstance(pref, dict) and len(pref) > 0:
                user_vector = vb.build_vector_from_pref_map(pref)
            else:
                # Build from purchase history if available
                ph = user_row.get('purchase_history') or []
                if ph:
                    user_vector = vb.build_user_vector_from_history(ph)
                else:
                    # Last resort: default vector
                    user_vector = vb._get_default_vector()
                    # introduce a tiny deterministic per-user perturbation so cold-start
                    # default vectors are not identical across users (improves diversity)
                    try:
                        import random
                        rnd = random.Random(int(user_id))
                        # perturb each feature slightly and renormalize
                        for k in list(user_vector.keys()):
                            user_vector[k] = max(0.0, float(user_vector.get(k, 0.0)) + rnd.uniform(-1e-3, 1e-3))
                        # renormalize to sum 1
                        s = sum(user_vector.values())
                        if s > 0:
                            for k in user_vector:
                                user_vector[k] = float(user_vector[k]) / s
                    except Exception:
                        pass
            vector_used = 'on_the_fly'
        except Exception as e:
            logging.getLogger(__name__).exception(f"Failed to build on-the-fly vector for user {user_id}: {e}")
            raise HTTPException(status_code=500, detail='Could not construct user vector')

    else:
        # If we did retrieve a vector from the saved model, detect whether it's a
        # near-uniform / cold vector (this happens when many users had only a
        # default vector at training time). In that case, rebuild an on-the-fly
        # vector from canonical data (preferences or purchase history) so each
        # user gets a slightly different starting vector and recommendations
        # diversify.
        try:
            vals = np.array([float(user_vector.get(f, 0.0)) for f in model.feature_names])
            max_val = float(np.max(vals)) if vals.size else 0.0
            ptp = float(np.ptp(vals)) if vals.size else 0.0
            if max_val < 0.05 or ptp < 1e-3:
                logger = logging.getLogger(__name__)
                logger.info(f"Model vector for user {user_id} appears cold (max={max_val:.6f} ptp={ptp:.6f}); rebuilding on-the-fly vector to improve diversity")
                vb = VectorBuilder()
                # Prefer explicit stored preference_vector
                pref = user_row.get('preference_vector') or user_row.get('user', {}).get('preference_vector')
                new_vec = None
                if pref and isinstance(pref, dict) and len(pref) > 0:
                    new_vec = vb.build_vector_from_pref_map(pref)
                else:
                    ph = user_row.get('purchase_history') or []
                    if ph:
                        new_vec = vb.build_user_vector_from_history(ph)
                    else:
                        new_vec = vb._get_default_vector()
                        # deterministic per-user perturbation so defaults differ
                        try:
                            import random
                            rnd = random.Random(int(user_id))
                            for k in list(new_vec.keys()):
                                new_vec[k] = max(0.0, float(new_vec.get(k, 0.0)) + rnd.uniform(-1e-3, 1e-3))
                            s = sum(new_vec.values())
                            if s > 0:
                                for k in new_vec:
                                    new_vec[k] = float(new_vec[k]) / s
                        except Exception:
                            pass

                # Replace the model vector with the rebuilt one for this request only
                if new_vec:
                    user_vector = new_vec
                    vector_used = 'on_the_fly_from_model'
        except Exception:
            # if anything goes wrong here, fall back to the original model vector
            pass

    # Load candidate products and filter / compute recommendations
    products = load_products(1000)
    # Remove products the user already bought to improve novelty
    try:
        purchased_ids = {p.get('product_id') for p in (user_row.get('purchase_history') or []) if p.get('product_id')}
        candidates = [p for p in products if p.get('id') not in purchased_ids]
    except Exception:
        candidates = products

    # Detailed per-candidate diagnostics to understand ranking behavior
    try:
        ua = np.array([user_vector.get(f, 0.0) for f in model.feature_names]).reshape(1, -1)
        cand_details = []
        for p in candidates[:500]:
            try:
                pv = model._build_product_vector(p)
                sim = model._calculate_similarity(ua, pv)
                nov = model._calculate_novelty_score(p, user_vector)
                score = 0.7 * sim + 0.3 * nov
                top_feats = sorted(list(pv.items()), key=lambda x: -abs(float(x[1])))[:6]
                cand_details.append({'id': p.get('id'), 'name': p.get('name'), 'similarity': float(sim), 'novelty': float(nov), 'score': float(score), 'top_product_features': top_feats})
            except Exception as e:
                logging.getLogger(__name__).debug(f"Error computing score for product {p.get('id')}: {e}")
        # log top candidates by computed score for debugging
        try:
            sorted_c = sorted(cand_details, key=lambda x: -x['score'])
            logging.getLogger(__name__).debug("recommendations: detailed_candidates_top20=%s", sorted_c[:20])
        except Exception:
            logging.getLogger(__name__).debug("recommendations: cand_details_count=%d", len(cand_details))
    except Exception as e:
        logging.getLogger(__name__).exception(f"Error while preparing candidate diagnostics: {e}")

    n = limit if (limit is not None) else top_n
    recs = model.get_recommendations(user_vector, candidates, top_n=n)
    # Log vector source for diagnostics but keep response the original list format
    try:
        logging.getLogger(__name__).debug("recommendations: vector_source=%s; returned=%d items", vector_used, len(recs or []))
    except Exception:
        pass
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

    # Optional synchronous mode for debugging/testing: if payload contains
    # {'sync': True} we will perform the update synchronously and return
    # updated recommendations for the user. Default behaviour remains
    # background update to avoid latency in production.
    sync = bool(payload.get('sync', False))
    top_n = int(payload.get('top_n', 10))

    try:
        if sync:
            # perform update immediately and then return recommendations
            ok = update_user_preferences_from_product(int(user_id), int(product_id), 0.05, event_weight)
            if not ok:
                # fall back to scheduling background task but report failure
                background.add_task(update_user_preferences_from_product, int(user_id), int(product_id), 0.05, event_weight)
                return {'status': 'accepted_with_warning', 'user_id': int(user_id), 'product_id': int(product_id), 'message': 'sync update failed, scheduled background task'}

            # Reuse the recommendations endpoint logic to return updated list
            try:
                recs = recommendations(int(user_id), top_n=top_n)
                return {'status': 'ok', 'user_id': int(user_id), 'product_id': int(product_id), 'recommendations': recs}
            except Exception as e:
                # If something goes wrong building recs, still return success for update
                return {'status': 'ok', 'user_id': int(user_id), 'product_id': int(product_id), 'message': 'update applied but failed to compute recommendations', 'error': str(e)}
        else:
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
