import numpy as np
import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity
import logging
import os
from ..services.vector_builder import VectorBuilder
import joblib
from ..config.settings import config

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


def load_product_matrix(limit: int = 10000):
    """Load product feature matrix using the DataProcessor factory (DB preferred).

    Returns (matrix, product_ids) or (None, None) on failure.
    """
    try:
        from ..services.csv_data_processor import DataProcessor
        dp = DataProcessor()
        products = dp.get_available_products()
        if not products:
            logger.warning("No products returned from DataProcessor")
            return None, None

        products = products[:limit] if limit else products

        vb = VectorBuilder()
        product_vectors = []
        product_ids = []
        from ..services.vector_builder import dict_to_array

        for p in products:
            # Build product vector dict
            vec = vb.build_product_vector(p)
            arr = dict_to_array(vec)
            product_vectors.append(arr)
            product_ids.append(p.get('id'))

        if not product_vectors:
            return None, None

        features_matrix = np.vstack(product_vectors)
        return features_matrix, product_ids
    except Exception as e:
        logger.warning(f"Could not load product matrix from DataProcessor: {e}")
        return None, None
