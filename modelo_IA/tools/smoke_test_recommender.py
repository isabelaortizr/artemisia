"""Smoke test para el modelo de recomendación.

Carga el modelo entrenado en models/trained/recommendation_model.pkl,
carga algunos productos desde modelo_IA/data_exports/products.csv y
pide recomendaciones para un user_id de ejemplo.

Ejecutar desde la raíz del repo:
C:/Users/.../python.exe modelo_IA\tools\smoke_test_recommender.py
"""
import os
import sys
import logging

# When running this script directly (python modelo_IA\tools\smoke_test_recommender.py)
# ensure the repository root is on sys.path so imports like `from modelo_IA...` work.
# If you run this as a module (python -m modelo_IA.tools.smoke_test_recommender)
# this adjustment is not necessary but harmless.
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

from modelo_IA.models.recommendation_engine import ArtRecommendationEngine
from modelo_IA.services.csv_data_processor import DataProcessor

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)

MODEL_PATH = os.path.join('models', 'trained', 'recommendation_model.pkl')
DATA_DIR = os.path.join('modelo_IA', 'data_exports')


def load_products(sample_n=100):
    dp = DataProcessor(export_dir=DATA_DIR)
    products = dp.get_available_products()
    # products viene como lista; limitar a sample_n para rapidez
    return products[:sample_n]


def main():
    print('🔎 Smoke test: cargar modelo y pedir recomendaciones')

    # Cargar modelo
    model = ArtRecommendationEngine.load_model(MODEL_PATH)

    # Cargar productos
    products = load_products(200)
    print(f'📦 Productos cargados: {len(products)}')

    # Seleccionar un user_id de ejemplo
    user_ids = list(model.user_vectors.keys())
    if not user_ids:
        print('⚠️  El modelo no tiene user_vectors cargados. Asegúrate de que se entrenó correctamente.')
        return

    user_id = user_ids[0]
    print(f'👤 Usando user_id de ejemplo: {user_id}')

    user_vector = model.user_vectors.get(user_id, {})

    # Obtener recomendaciones
    recs = model.get_recommendations(user_vector, products, top_n=10)
    print(f'🏆 Top {len(recs)} recomendaciones:')
    for i, prod in enumerate(recs, 1):
        title = prod.get('title') or prod.get('name') or prod.get('product_name') or prod.get('id')
        price = prod.get('price', 'N/A')
        print(f'{i}. {title} — price: {price}')

    # Usuarios similares
    similar = model.find_similar_users(user_id, limit=5)
    print(f'🤝 Usuarios similares a {user_id}: {similar}')


if __name__ == '__main__':
    main()
