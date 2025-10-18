import logging
from ..services.vector_builder import product_to_vector, dict_to_array, FEATURE_NAMES, l2_normalize
import numpy as np
from datetime import datetime
import os
import pandas as pd
import json

# CSV directory where exported data lives
CSV_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'data_exports')

# Note: we import db lazily inside functions to avoid requiring psycopg2 at module import time

logger = logging.getLogger(__name__)


def fetch_product(product_id: int):
    """Fetch product information (DB preferred via DataProcessor, fallback to CSV)."""
    # Try DataProcessor (DB preferred)
    try:
        from .csv_data_processor import DataProcessor
        dp = DataProcessor()
        products = dp.get_available_products()
        for p in products:
            if int(p.get('id') or 0) == int(product_id):
                return {
                    'product_id': p.get('id'),
                    'price': p.get('price'),
                    'categories': p.get('categories', []),
                    'techniques': p.get('techniques', [])
                }
    except Exception:
        # fallthrough to CSV fallback
        pass

    # CSV fallback
    try:
        products_path = os.path.join(CSV_DIR, 'products.csv')
        if not os.path.exists(products_path):
            logger.warning(f"Products CSV not found at {products_path}")
            return None

        df = pd.read_csv(products_path)
        product = df[df['id'] == product_id].iloc[0] if len(df[df['id'] == product_id]) > 0 else None

        if product is None:
            return None

        # Parse categories and techniques from JSON strings
        try:
            categories = json.loads(product['categories']) if pd.notna(product['categories']) else []
            techniques = json.loads(product['techniques']) if pd.notna(product['techniques']) else []
        except Exception:
            categories, techniques = [], []

        return {
            'product_id': product['id'],
            'price': float(product['price']) if pd.notna(product['price']) else None,
            'categories': categories,
            'techniques': techniques
        }
    except Exception as e:
        logger.error(f"Error fetching product {product_id} from CSV: {e}")
        return None


def fetch_user_pref(user_id: int):
    """Fetch user preferences from user_preferences.csv"""
    try:
        prefs_path = os.path.join(CSV_DIR, 'user_preferences.csv')
        if not os.path.exists(prefs_path):
            return None, {}
            
        df = pd.read_csv(prefs_path)
        user_pref = df[df['user_id'] == user_id].iloc[0] if len(df[df['user_id'] == user_id]) > 0 else None
        
        if user_pref is None:
            return None, {}
            
        try:
            # Expecting vector_kv to be a JSON string of feature:weight pairs
            vector_pairs = json.loads(user_pref['vector_kv'])
            pref_map = {k: float(v) for k, v in [pair.split(':') for pair in vector_pairs]}
            return user_pref['user_id'], pref_map
        except:
            return None, {}
    except Exception as e:
        logger.error(f"Error fetching preferences for user {user_id} from CSV: {e}")
        return None, {}


def create_user_pref(user_id: int):
    """Create new user preference entry in user_preferences.csv"""
    try:
        prefs_path = os.path.join(CSV_DIR, 'user_preferences.csv')
        
        # Create empty CSV if it doesn't exist
        if not os.path.exists(prefs_path):
            df = pd.DataFrame(columns=['user_id', 'last_updated', 'vector_kv'])
        else:
            df = pd.read_csv(prefs_path)
            
        # Add new user preference
        new_pref = pd.DataFrame([{
            'user_id': user_id,
            'last_updated': datetime.utcnow().isoformat(),
            'vector_kv': '[]'  # empty vector
        }])
        
        df = pd.concat([df, new_pref], ignore_index=True)
        df.to_csv(prefs_path, index=False)
        
        return user_id  # using user_id as pref_id for simplicity in CSV version
    except Exception as e:
        logger.error(f"Error creating preferences for user {user_id} in CSV: {e}")
        return None


def write_user_pref_map(pref_id: int, pref_map: dict):
    """Update user preferences in user_preferences.csv"""
    try:
        prefs_path = os.path.join(CSV_DIR, 'user_preferences.csv')
        if not os.path.exists(prefs_path):
            return
            
        df = pd.read_csv(prefs_path)
        
        # Convert preference map to vector_kv format
        vector_kv = json.dumps([f"{k}:{v}" for k, v in pref_map.items()])
        
        # Update existing preference
        mask = df['user_id'] == pref_id  # using user_id as pref_id in CSV version
        if any(mask):
            df.loc[mask, 'vector_kv'] = vector_kv
            df.loc[mask, 'last_updated'] = datetime.utcnow().isoformat()
            df.to_csv(prefs_path, index=False)
    except Exception as e:
        logger.error(f"Error writing preferences for user {pref_id} to CSV: {e}")
        return


def update_user_preferences_from_product(user_id: int, product_id: int, beta: float = 0.05, event_weight: float = None):
    """Fetch product and user pref, compute product vector and update user preference.

    The update is performed transactionally using SELECT ... FOR UPDATE when possible.
    If event_weight is provided (0..1), effective beta = beta * event_weight.
    """
    prod = fetch_product(product_id)
    if prod is None:
        logger.warning(f"Product {product_id} not found")
        return False

    pvec = product_to_vector(prod)

    # adjust beta by event weight if provided
    if event_weight is not None:
        try:
            ew = float(event_weight)
            ew = max(0.0, min(1.0, ew))
            beta = beta * ew
        except Exception:
            pass

    # CSV-based update: read existing preferences, update vector, write back
    try:
        pref_id, existing_map = fetch_user_pref(user_id)
        if pref_id is None:
            # create an entry
            create_user_pref(user_id)
            pref_id, existing_map = fetch_user_pref(user_id)

        # Convert existing_map to array
        old_arr = np.zeros(len(FEATURE_NAMES), dtype=float)
        for i, fname in enumerate(FEATURE_NAMES):
            old_arr[i] = float(existing_map.get(fname, 0.0)) if existing_map else 0.0

        weight_sum = 1.0 if existing_map else 0.0

        new_accum = old_arr + (beta * pvec)
        new_weight_sum = weight_sum + beta

        if new_weight_sum == 0:
            raw = new_accum
        else:
            raw = new_accum / new_weight_sum

        new_norm = l2_normalize(raw)

        pref_json = {FEATURE_NAMES[i]: float(new_norm[i]) for i in range(len(FEATURE_NAMES))}

        # write back
        write_user_pref_map(pref_id or user_id, pref_json)
        return True
    except Exception as e:
        logger.error(f"Error updating user preference (CSV) for user {user_id}: {e}")
        return False


def update_user_preferences_from_purchase(user_id: int, product_ids: list, beta: float = 0.5):
    """Apply heavier update using the set of product_ids (or list of items). Combines product vectors as average then updates with beta.

    Transactional: uses preference_accum/weight_sum when available.
    """
    vecs = []
    for pid in product_ids:
        prod = fetch_product(pid)
        if prod:
            vecs.append(product_to_vector(prod))
    if not vecs:
        logger.warning("No valid products found for purchase update")
        return False

    combined = np.mean(np.stack(vecs, axis=0), axis=0)

    # CSV-based batch update: combine product vectors then reuse single-update logic
    try:
        vecs = []
        for pid in product_ids:
            prod = fetch_product(pid)
            if prod:
                vecs.append(product_to_vector(prod))
        if not vecs:
            logger.warning("No valid products found for purchase update")
            return False

        combined = np.mean(np.stack(vecs, axis=0), axis=0)

        # reuse single product update logic but apply combined vector
        # emulate pvec input by calling internal steps
        pref_id, existing_map = fetch_user_pref(user_id)
        if pref_id is None:
            create_user_pref(user_id)
            pref_id, existing_map = fetch_user_pref(user_id)

        old_arr = np.zeros(len(FEATURE_NAMES), dtype=float)
        for i, fname in enumerate(FEATURE_NAMES):
            old_arr[i] = float(existing_map.get(fname, 0.0)) if existing_map else 0.0

        weight_sum = 1.0 if existing_map else 0.0

        new_accum = old_arr + (beta * combined)
        new_weight_sum = weight_sum + beta

        if new_weight_sum == 0:
            raw = new_accum
        else:
            raw = new_accum / new_weight_sum

        new_norm = l2_normalize(raw)
        pref_json = {FEATURE_NAMES[i]: float(new_norm[i]) for i in range(len(FEATURE_NAMES))}

        write_user_pref_map(pref_id or user_id, pref_json)
        return True
    except Exception as e:
        logger.error(f"Error updating preferences from purchase (CSV) for user {user_id}: {e}")
        return False
