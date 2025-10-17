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
    # Database
    DB_HOST = _get_env('DB_HOST', 'localhost')
    DB_PORT = int(_get_env('DB_PORT', '5432'))
    DB_NAME = _get_env('DB_NAME', 'artemisia_db')
    DB_USER = _get_env('DB_USER', 'postgres')
    DB_PASSWORD = _get_env('DB_PASSWORD', 'password')

    # Java API (optional, used by some processors)
    JAVA_API_URL = _get_env('JAVA_API_URL', 'http://localhost:8081/api')

    # ML Model
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


config = Config()