import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from config.settings import config
from config.database import db
import logging

logger = logging.getLogger(__name__)


def recommend_by_similarity(user_vector: np.ndarray, product_matrix: np.ndarray, product_ids: list, k: int = 10):
    """Return top-k product_ids ordered by cosine similarity to user_vector.
    product_matrix: n_products x dim
    product_ids: list of corresponding product ids
    """
    if user_vector is None or product_matrix is None or len(product_ids) == 0:
        return []

    # ensure dims compatible
    u = user_vector.reshape(1, -1)
    sims = cosine_similarity(u, product_matrix).reshape(-1)
    top_idx = np.argsort(-sims)[:k]
    return [product_ids[i] for i in top_idx]


def load_product_matrix_from_db(limit: int = 10000):
    """Example helper that tries to load product features from DB. The implementation assumes a table
    or a materialized view with product feature vectors available as JSON or arrays. This is an optional helper
    and may need adaptation to your schema.
    """
    try:
        conn = db.get_connection()
        cur = conn.cursor()
        # This SQL expects a table 'product_features' with columns product_id and feature_json (jsonb array)
        cur.execute("SELECT product_id, feature_json FROM product_features LIMIT %s", (limit,))
        rows = cur.fetchall()
        product_ids = []
        mats = []
        for r in rows:
            pid = r[0]
            f = r[1]
            arr = np.array(f, dtype=float)
            product_ids.append(pid)
            mats.append(arr)
        if len(mats) == 0:
            return None, None
        return np.vstack(mats), product_ids
    except Exception as e:
        logger.warning(f"Could not load product matrix from DB: {e}")
        return None, None
