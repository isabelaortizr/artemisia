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
import argparse

# When running this script directly (python modelo_IA\tools\smoke_test_recommender.py)
# ensure the repository root is on sys.path so imports like `from modelo_IA...` work.
# If you run this as a module (python -m modelo_IA.tools.smoke_test_recommender)
# this adjustment is not necessary but harmless.
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

from modelo_IA.models.recommendation_engine import ArtRecommendationEngine
from modelo_IA.services.csv_data_processor import DataProcessor
from modelo_IA.services.vector_builder import VectorBuilder

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)

MODEL_PATH = os.path.join('models', 'trained', 'recommendation_model.pkl')
DATA_DIR = os.path.join('modelo_IA', 'data_exports')


def precision_at_k(recs, ground_truth, k):
    if not recs:
        return 0.0
    hits = 0
    for r in recs[:k]:
        if r.get('id') in ground_truth:
            hits += 1
    return hits / k


def recall_at_k(recs, ground_truth, k):
    if not ground_truth:
        return 0.0
    hits = 0
    for r in recs[:k]:
        if r.get('id') in ground_truth:
            hits += 1
    return hits / len(ground_truth)


def reciprocal_rank(recs, ground_truth):
    for i, r in enumerate(recs, start=1):
        if r.get('id') in ground_truth:
            return 1.0 / i
    return 0.0


def dcg(rels):
    import numpy as _np
    return sum((2**r - 1) / _np.log2(i + 2) for i, r in enumerate(rels))


def ndcg_at_k(recs, ground_truth, k):
    import numpy as _np
    rels = [1 if r.get('id') in ground_truth else 0 for r in recs[:k]]
    ideal = sorted(rels, reverse=True)
    denom = dcg(ideal)
    if denom == 0:
        return 0.0
    return dcg(rels) / denom


def load_products(sample_n=100):
    dp = DataProcessor(export_dir=DATA_DIR)
    products = dp.get_available_products()
    # products viene como lista; limitar a sample_n para rapidez
    return products[:sample_n]


def main(sample_n=100, top_k=10):
    print('🔎 Smoke test + evaluación: cargar modelo y medir métricas básicas')

    # Cargar modelo
    model = ArtRecommendationEngine.load_model(MODEL_PATH)
    if model is None:
        print('⚠️  No se pudo cargar el modelo. Asegúrate de que existe en', MODEL_PATH)
        return

    # Cargar productos
    products = load_products(1000)
    print(f'📦 Productos cargados: {len(products)}')

    # Cargar usuarios / historial desde DataProcessor
    dp = DataProcessor(export_dir=DATA_DIR)
    try:
        users = dp.get_training_data_from_db()
    except Exception:
        # fallback a lista vacía
        users = []

    # diagnostics
    try:
        total_users = len(users) if users else 0
        users_with_ph = sum(1 for u in users if u.get('purchase_history')) if users else 0
        users_with_prefvec = sum(1 for u in users if u.get('preference_vector')) if users else 0
        users_in_model = sum(1 for u in users if (u.get('user_id') or u.get('id')) in model.user_vectors) if users else 0
    except Exception:
        total_users = users_with_ph = users_with_prefvec = users_in_model = 0

    print(f'🔎 Diagnóstico usuarios desde DataProcessor: total={total_users}, with_history={users_with_ph}, with_prefvec={users_with_prefvec}, present_in_model_vectors={users_in_model}')

    if not users:
        print('⚠️  No se encontraron usuarios/historial para evaluación en DataProcessor. Intentando evaluar múltiples usuarios desde el modelo...')
        # fallback: take multiple users from model.user_vectors
        user_ids = list(model.user_vectors.keys())
        if not user_ids:
            print('⚠️  El modelo no tiene user_vectors cargados. Asegúrate de que se entrenó correctamente.')
            return

        # limit to sample_n
        user_ids = user_ids[:sample_n]

        import time
        from statistics import mean

        precisions = []
        recalls = []
        mrrs = []
        ndcgs = []
        latencies = []
        covered = set()
        users_eval = 0

        vb = VectorBuilder()
        for user_id in user_ids:
            print(f'👤 Evaluando user_id: {user_id}')
            # try to load full user data (to extract ground-truth) via DataProcessor if available
            try:
                user_record = dp.get_user_data(user_id)
            except Exception:
                user_record = None

            # determine ground truth if possible
            ph = []
            if user_record:
                ph = user_record.get('purchase_history') or []

            gt = [p.get('product_id') or p.get('id') for p in ph[-1:]] if ph else []

            user_vector = model.user_vectors.get(user_id)
            if user_vector is None:
                # try to build from purchase history if available
                if ph:
                    try:
                        user_vector = vb.build_user_vector_from_history(ph)
                        print(' - vector construido desde historial')
                    except Exception:
                        user_vector = None
                if user_vector is None:
                    # skip generation if no vector present
                    print(f' - sin vector de usuario disponible para {user_id}; se salta métricas, pero generará top-k si se puede.')
                    continue

            start = time.time()
            try:
                recs = model.get_recommendations(user_vector, products, top_n=top_k)
            except Exception as e:
                logger.warning(f'Error generando recomendaciones para user {user_id}: {e}')
                continue
            elapsed_ms = (time.time() - start) * 1000.0
            latencies.append(elapsed_ms)

            # if we have ground truth, compute metrics
            if gt:
                users_eval += 1
                precisions.append(precision_at_k(recs, set(gt), top_k))
                recalls.append(recall_at_k(recs, set(gt), top_k))
                mrrs.append(reciprocal_rank(recs, set(gt)))
                ndcgs.append(ndcg_at_k(recs, set(gt), top_k))

            for r in recs:
                covered.add(r.get('id'))

            # show top-3 for quick inspection
            print(' - Top 3:')
            for i, prod in enumerate(recs[:3], 1):
                title = prod.get('title') or prod.get('name') or prod.get('product_name') or prod.get('id')
                print(f'   {i}. {title} (id={prod.get("id")})')

        if users_eval == 0:
            print('\n⚠️  Ningún usuario tenía ground-truth disponible; métricas agregadas no calculadas.')
        else:
            print('\n=== RESUMEN DE EVALUACIÓN (SMOKE, desde user_vectors) ===')
            print(f'Usuarios evaluados (con GT): {users_eval}')
            print(f'Precision@{top_k}: {mean(precisions):.4f}')
            print(f'Recall@{top_k}: {mean(recalls):.4f}')
            print(f'MRR: {mean(mrrs):.4f}')
            print(f'NDCG@{top_k}: {mean(ndcgs):.4f}')
            print(f'Median latency (ms): {sorted(latencies)[len(latencies)//2]:.2f}')
            try:
                import numpy as _np
                print(f'P95 latency (ms): {_np.percentile(latencies, 95):.2f}')
            except Exception:
                pass
            print(f'Coverage (recommended products / catalog): {len(covered)}/{len(products)} = {len(covered)/len(products):.4f}')

        return

    # filtrar usuarios con historial suficiente y crear sample
    candidates = [u for u in users if u.get('purchase_history') and len(u.get('purchase_history')) >= 2]
    if not candidates:
        print('⚠️  Ningún usuario con historial suficiente para evaluación (need >=2).')
        return

    candidates = candidates[:sample_n]

    import time
    from statistics import mean

    precisions = []
    recalls = []
    mrrs = []
    ndcgs = []
    latencies = []
    covered = set()
    users_eval = 0

    for u in candidates:
        user_id = u.get('user_id') or u.get('id')
        ph = u.get('purchase_history') or []
        if not ph:
            continue
        # ground truth: last purchase
        gt = [p.get('product_id') or p.get('id') for p in ph[-1:]]

        # try get vector from model, then from user record
        user_vector = None
        try:
            user_vector = model.user_vectors.get(user_id)
        except Exception:
            user_vector = None

        if user_vector is None:
            # try preference_vector first
            user_vector = u.get('preference_vector')
            # try to build from purchase history if still None
            if user_vector is None and ph:
                try:
                    vb = VectorBuilder()
                    user_vector = vb.build_user_vector_from_history(ph)
                except Exception:
                    user_vector = None
            if user_vector is None:
                # skip if no vector available
                continue

        users_eval += 1

        start = time.time()
        try:
            recs = model.get_recommendations(user_vector, products, top_n=top_k)
        except Exception as e:
            logger.warning(f'Error generando recomendaciones para user {user_id}: {e}')
            continue
        elapsed_ms = (time.time() - start) * 1000.0
        latencies.append(elapsed_ms)

        precisions.append(precision_at_k(recs, set(gt), top_k))
        recalls.append(recall_at_k(recs, set(gt), top_k))
        mrrs.append(reciprocal_rank(recs, set(gt)))
        ndcgs.append(ndcg_at_k(recs, set(gt), top_k))

        for r in recs:
            covered.add(r.get('id'))

    if users_eval == 0:
        print('⚠️  No se pudo evaluar a ningún usuario (no había vectores/groud-truth).')
        return

    print('\n=== RESUMEN DE EVALUACIÓN (SMOKE) ===')
    print(f'Usuarios evaluados: {users_eval}')
    print(f'Precision@{top_k}: {mean(precisions):.4f}')
    print(f'Recall@{top_k}: {mean(recalls):.4f}')
    print(f'MRR: {mean(mrrs):.4f}')
    print(f'NDCG@{top_k}: {mean(ndcgs):.4f}')
    print(f'Median latency (ms): {sorted(latencies)[len(latencies)//2]:.2f}')
    try:
        import numpy as _np
        print(f'P95 latency (ms): {_np.percentile(latencies, 95):.2f}')
    except Exception:
        pass
    print(f'Coverage (recommended products / catalog): {len(covered)}/{len(products)} = {len(covered)/len(products):.4f}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Smoke test y evaluación rápida del recomendador')
    parser.add_argument('--sample', type=int, default=100, help='Número de usuarios a muestrear')
    parser.add_argument('--top-k', type=int, default=10, help='Top-K para métricas y ranking')
    args = parser.parse_args()

    main(sample_n=args.sample, top_k=args.top_k)
