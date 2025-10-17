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


def load_product_matrix_from_csv(limit: int = 10000):
    """Load product features from CSV files in data_exports directory.
    Returns a tuple of (product_matrix, product_ids) where product_matrix is a numpy array
    of feature vectors and product_ids is a list of corresponding product IDs.
    """
    try:
        # prefer repo-local path under modelo_IA/data_exports, fallback to top-level data_exports
        csv_path = os.path.join('modelo_IA', 'data_exports', 'products.csv')
        if not os.path.exists(csv_path):
            csv_path = os.path.join('data_exports', 'products.csv')
        if not os.path.exists(csv_path):
            logger.warning(f"Products CSV not found at {csv_path}")
            return None, None
            
        df = pd.read_csv(csv_path)
        if len(df) == 0:
            return None, None
            
        if limit:
            df = df.head(limit)
            
        # Assuming we have feature columns in the CSV; if not, compute on-the-fly
        feature_cols = [col for col in df.columns if col.startswith('feature_')]

        product_ids = df['id'].tolist()

        # Optional cache to speed up repeated loads
        cache_dir = config.get('CACHE_DIR') or '.cache'
        os.makedirs(cache_dir, exist_ok=True)
        cache_path = os.path.join(cache_dir, f'product_matrix_limit_{limit}.joblib')

        if feature_cols:
            features_matrix = df[feature_cols].values
            return features_matrix, product_ids

        # Try loading from cache first
        try:
            if os.path.exists(cache_path):
                logger.info(f"Loading cached product matrix from {cache_path}")
                cached = joblib.load(cache_path)
                return cached.get('matrix'), cached.get('ids')
        except Exception:
            logger.debug("Failed to load product matrix cache, will rebuild")

        # Build product vectors on-the-fly using VectorBuilder
        vb = VectorBuilder()
        product_vectors = []
        for _, row in df.iterrows():
            product = {
                'id': row.get('id'),
                'categories': [],
                'techniques': []
            }
            # parse categories/techniques from likely fields
            if 'categories' in row and not pd.isna(row['categories']):
                try:
                    import ast
                    parsed = ast.literal_eval(row['categories']) if isinstance(row['categories'], str) else row['categories']
                    if isinstance(parsed, (list, tuple)):
                        product['categories'] = parsed
                except Exception:
                    # fallback: comma-separated string
                    product['categories'] = [s.strip() for s in str(row['categories']).split(',') if s.strip()]

            if 'techniques' in row and not pd.isna(row['techniques']):
                try:
                    import ast
                    parsed = ast.literal_eval(row['techniques']) if isinstance(row['techniques'], str) else row['techniques']
                    if isinstance(parsed, (list, tuple)):
                        product['techniques'] = parsed
                except Exception:
                    product['techniques'] = [s.strip() for s in str(row['techniques']).split(',') if s.strip()]

            vec_dict = vb.build_product_vector(product)
            # convert dict to ordered array matching VectorBuilder.FEATURE_NAMES
            from ..services.vector_builder import FEATURE_NAMES, dict_to_array
            arr = dict_to_array(vec_dict)
            product_vectors.append(arr)

        if not product_vectors:
            return None, None

        features_matrix = np.vstack(product_vectors)

        # Save cache
        try:
            joblib.dump({'matrix': features_matrix, 'ids': product_ids}, cache_path)
        except Exception:
            logger.debug("Could not write product matrix cache")

        return features_matrix, product_ids
    except Exception as e:
        logger.warning(f"Could not load product matrix from CSV: {e}")
        return None, None
