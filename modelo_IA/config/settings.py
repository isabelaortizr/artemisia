import os
from dotenv import load_dotenv

load_dotenv()


def _get_env(key: str, default: str = None) -> str:
    """Helper to fetch env var and strip surrounding quotes and whitespace."""
    val = os.getenv(key, default)
    if isinstance(val, str):
        # remove surrounding spaces and quotes (single or double)
        val = val.strip()
        if len(val) >= 2 and ((val[0] == val[-1]) and val[0] in "'\""):
            val = val[1:-1]
        return val
    return val


class Config:
    # Database (optional, used only by export tool)
    DB_HOST = _get_env('DB_HOST', None)
    DB_PORT = int(_get_env('DB_PORT', '5432')) if _get_env('DB_PORT', None) else None
    DB_NAME = _get_env('DB_NAME', None)
    DB_USER = _get_env('DB_USER', None)
    DB_PASSWORD = _get_env('DB_PASSWORD', None)

    # ML Model path
    MODEL_PATH = _get_env('MODEL_PATH', 'models/trained/recommendation_model.pkl')

    # Training
    try:
        MIN_USERS_FOR_TRAINING = int(_get_env('MIN_USERS_FOR_TRAINING', '50'))
    except ValueError:
        MIN_USERS_FOR_TRAINING = 50

    try:
        NUM_CLUSTERS = int(_get_env('NUM_CLUSTERS', '5'))
    except ValueError:
        NUM_CLUSTERS = 5

    # KMeans / clustering tuning
    try:
        KMEANS_MAX_ITER = int(_get_env('KMEANS_MAX_ITER', '300'))
    except ValueError:
        KMEANS_MAX_ITER = 300

    try:
        KMEANS_N_INIT = int(_get_env('KMEANS_N_INIT', '10'))
    except ValueError:
        KMEANS_N_INIT = 10

    # Features
    CATEGORIES = [
        'Realista', 'Abstracta', 'Expresionista', 'Impresionista', 'Surrealista',
        'Conceptual', 'Religiosa', 'Histórica', 'Decorativa', 'Contemporánea'
    ]

    TECHNIQUES = [
        'Óleo', 'Acrílico', 'Acuarela', 'Temple', 'Fresco',
        'Gouache', 'Tinta', 'Mixta', 'Spray', 'Digital'
    ]

    # Local cache directory for computed artifacts (product matrix, etc.)
    CACHE_DIR = _get_env('CACHE_DIR', '.cache')

    def get(self, key: str, default=None):
        """Small helper to mimic dict-like access used in some modules."""
        return getattr(self, key, default)


config = Config()