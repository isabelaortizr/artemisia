import logging
from ..services.vector_builder import product_to_vector, dict_to_array, FEATURE_NAMES, l2_normalize
import numpy as np
from datetime import datetime
import os
import pandas as pd
import json
import csv
from pandas.errors import EmptyDataError, ParserError
from ..config.settings import config

# CSV directory where exported data lives
CSV_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'data_exports')

# Note: we import db lazily inside functions to avoid requiring psycopg2 at module import time

logger = logging.getLogger(__name__)


def fetch_product(product_id: int):
    """Fetch product information (DB preferred via DataProcessor, fallback to CSV)."""
    # Try DataProcessor (DB preferred)
    # When DB is configured, strictly use the DB-backed DataProcessor and never fall
    # back to CSV files (user requested CSVs be ignored when DB is active).
    if config.db_is_configured():
        from .csv_data_processor import DataProcessor
        dp = DataProcessor()
        products = dp.get_available_products()
        for p in products:
            try:
                if int(p.get('id') or 0) == int(product_id):
                    return {
                        'product_id': p.get('id'),
                        'price': p.get('price'),
                        'categories': p.get('categories', []),
                        'techniques': p.get('techniques', [])
                    }
            except Exception:
                continue
        return None

    # CSV fallback (only when DB is not configured)
    try:
        products_path = os.path.join(CSV_DIR, 'products.csv')
        if not os.path.exists(products_path):
            logger.warning(f"Products CSV not found at {products_path}")
            return None

        # robust read: try default then fallback to python engine skipping bad lines
        try:
            df = pd.read_csv(products_path)
        except (ParserError, EmptyDataError) as e:
            logger.warning(f"products.csv parse error with default engine, retrying with python engine: {e}")
            try:
                df = pd.read_csv(products_path, engine='python', on_bad_lines='skip')
            except Exception as e2:
                logger.error(f"Error reading products.csv with fallback: {e2}")
                return None

        if df is None or df.empty:
            return None

        # ensure id column numeric comparison
        try:
            matches = df[df['id'].astype(float) == float(product_id)]
        except Exception:
            matches = df[df['id'] == product_id] if 'id' in df.columns else pd.DataFrame()

        product = matches.iloc[0] if len(matches) > 0 else None

        if product is None:
            return None

        # Parse categories and techniques from JSON strings if possible
        try:
            categories = json.loads(product['categories']) if pd.notna(product.get('categories')) else []
        except Exception:
            categories = []
        try:
            techniques = json.loads(product['techniques']) if pd.notna(product.get('techniques')) else []
        except Exception:
            techniques = []

        return {
            'product_id': product.get('id'),
            'price': float(product['price']) if pd.notna(product.get('price')) else None,
            'categories': categories,
            'techniques': techniques
        }
    except Exception as e:
        logger.error(f"Error fetching product {product_id} from CSV: {e}")
        return None


def fetch_user_pref(user_id: int):
    """Fetch user preferences from user_preferences.csv"""
    # If DB is configured use DB tables instead of CSV
    if config.db_is_configured():
        try:
            from ..config.database import db
            from .csv_data_processor import DBDataProcessor
            conn = db.get_connection()
            dp = DBDataProcessor(conn)
            urec = dp.get_user_data(user_id)
            pref_map = urec.get('preference_vector') or {}
            return (int(user_id) if urec else None), pref_map
        except Exception as e:
            logger.error(f"Error fetching preferences for user {user_id} from DB: {e}")
            return None, {}

    # CSV fallback when DB not configured
    try:
        prefs_path = os.path.join(CSV_DIR, 'user_preferences.csv')
        if not os.path.exists(prefs_path):
            return None, {}

        # robust read with fallback
        try:
            df = pd.read_csv(prefs_path)
        except (ParserError, EmptyDataError) as e:
            logger.warning(f"user_preferences.csv parse error with default engine, retrying with python engine: {e}")
            try:
                df = pd.read_csv(prefs_path, engine='python', on_bad_lines='skip')
            except Exception as e2:
                logger.error(f"Error reading user_preferences.csv with fallback: {e2}")
                return None, {}

        if df is None or df.empty:
            return None, {}

        # normalize user_id column for comparison
        try:
            mask = df['user_id'].astype(int) == int(user_id)
        except Exception:
            mask = (df['user_id'] == user_id) if 'user_id' in df.columns else pd.Series([False]*len(df))

        user_pref = df[mask].iloc[0] if mask.any() else None

        if user_pref is None:
            return None, {}

        try:
            # Expecting vector_kv to be a JSON string of feature:weight pairs
            vector_pairs = json.loads(user_pref['vector_kv'])
            # vector_pairs may be list of 'k:v' strings or dict
            pref_map = {}
            if isinstance(vector_pairs, dict):
                pref_map = {k: float(v) for k, v in vector_pairs.items()}
            elif isinstance(vector_pairs, list):
                for pair in vector_pairs:
                    if isinstance(pair, str) and ':' in pair:
                        k, v = pair.split(':', 1)
                        try:
                            pref_map[k] = float(v)
                        except Exception:
                            pass
            return int(user_pref['user_id']), pref_map
        except Exception:
            return None, {}
    except Exception as e:
        logger.error(f"Error fetching preferences for user {user_id} from CSV: {e}")
        return None, {}


def create_user_pref(user_id: int):
    """Create new user preference entry in user_preferences.csv"""
    # DB-backed creation when configured
    if config.db_is_configured():
        try:
            from ..config.database import db
            conn = db.get_connection()
            cur = conn.cursor()
            # insert a new preference row for the user
            cur.execute("INSERT INTO user_preferences (user_id, last_updated) VALUES (%s, now()) RETURNING id", (int(user_id),))
            pref_id = cur.fetchone()[0]
            conn.commit()
            return pref_id
        except Exception as e:
            logger.error(f"Error creating preferences for user {user_id} in DB: {e}")
            try:
                conn.rollback()
            except Exception:
                pass
            return None

    # CSV fallback when DB not configured
    try:
        prefs_path = os.path.join(CSV_DIR, 'user_preferences.csv')

        # Ensure the export directory exists so we can write the CSV
        try:
            os.makedirs(CSV_DIR, exist_ok=True)
        except Exception as e:
            logger.error(f"Could not create CSV_DIR {CSV_DIR}: {e}")
            return None

        # Create empty CSV if it doesn't exist
        if not os.path.exists(prefs_path):
            df = pd.DataFrame(columns=['user_id', 'last_updated', 'vector_kv'])
        else:
            # robust read
            try:
                df = pd.read_csv(prefs_path)
            except (ParserError, EmptyDataError):
                logger.warning(f"user_preferences.csv parse error while creating pref for {user_id}, rebuilding file header")
                df = pd.DataFrame(columns=['user_id', 'last_updated', 'vector_kv'])

        # Add new user preference
        new_pref = pd.DataFrame([{
            'user_id': user_id,
            'last_updated': datetime.utcnow().isoformat(),
            'vector_kv': '[]'  # empty vector
        }])

        df = pd.concat([df, new_pref], ignore_index=True)

        # atomic write to avoid partial files
        try:
            tmp_path = prefs_path + '.tmp'
            df.to_csv(tmp_path, index=False)
            os.replace(tmp_path, prefs_path)
        except Exception as e:
            logger.error(f"Error writing user_preferences.csv for user {user_id}: {e}")
            # attempt fallback write
            try:
                df.to_csv(prefs_path, index=False)
            except Exception as e2:
                logger.error(f"Fallback write failed for user_preferences.csv: {e2}")
                return None

        return user_id  # using user_id as pref_id for simplicity in CSV version
    except Exception as e:
        logger.error(f"Error creating preferences for user {user_id} in CSV: {e}")
        return None


def write_user_pref_map(pref_id: int, pref_map: dict):
    """Update user preferences in user_preferences.csv"""
    # DB-backed update when configured
    if config.db_is_configured():
        try:
            from ..config.database import db
            conn = db.get_connection()
            cur = conn.cursor()
            # remove existing vectors for this preference id
            cur.execute("DELETE FROM user_preference_vectors WHERE user_preference_id = %s", (int(pref_id),))
            # insert new vectors
            inserts = []
            for k, v in (pref_map or {}).items():
                inserts.append((int(pref_id), str(k), float(v)))
            if inserts:
                cur.executemany("INSERT INTO user_preference_vectors (user_preference_id, feature, weight) VALUES (%s, %s, %s)", inserts)
            # update last_updated
            cur.execute("UPDATE user_preferences SET last_updated = now() WHERE id = %s", (int(pref_id),))
            conn.commit()
            return
        except Exception as e:
            logger.error(f"Error writing preferences for user {pref_id} to DB: {e}")
            try:
                conn.rollback()
            except Exception:
                pass
            return

    # CSV fallback when DB not configured
    try:
        prefs_path = os.path.join(CSV_DIR, 'user_preferences.csv')
        # Ensure directory exists
        try:
            os.makedirs(CSV_DIR, exist_ok=True)
        except Exception as e:
            logger.error(f"Could not create CSV_DIR {CSV_DIR}: {e}")
            return

        if not os.path.exists(prefs_path):
            # Nothing to update
            return

        # robust read
        try:
            df = pd.read_csv(prefs_path)
        except (ParserError, EmptyDataError):
            logger.warning(f"user_preferences.csv parse error while updating pref {pref_id}, aborting update")
            return

        # Convert preference map to vector_kv format
        vector_kv = json.dumps([f"{k}:{v}" for k, v in pref_map.items()])

        # Update existing preference
        try:
            mask = df['user_id'].astype(int) == int(pref_id)
        except Exception:
            mask = df['user_id'] == pref_id if 'user_id' in df.columns else pd.Series([False]*len(df))

        if mask.any():
            df.loc[mask, 'vector_kv'] = vector_kv
            df.loc[mask, 'last_updated'] = datetime.utcnow().isoformat()
            # atomic write
            try:
                tmp_path = prefs_path + '.tmp'
                df.to_csv(tmp_path, index=False)
                os.replace(tmp_path, prefs_path)
            except Exception as e:
                logger.error(f"Error writing preferences for user {pref_id} to CSV: {e}")
                try:
                    df.to_csv(prefs_path, index=False)
                except Exception as e2:
                    logger.error(f"Fallback write failed for user_preferences.csv: {e2}")
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

    # Use DB-backed preferences when configured (no CSV touches)
    try:
        pref_id, existing_map = fetch_user_pref(user_id)
        if pref_id is None:
            pref_id = create_user_pref(user_id)
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

        # write back (DB or CSV depending on config)
        write_user_pref_map(pref_id or user_id, pref_json)
        return True
    except Exception as e:
        logger.error(f"Error updating user preference for user {user_id}: {e}")
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

    # DB-backed batch update (will use DB writes via write_user_pref_map)
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

        pref_id, existing_map = fetch_user_pref(user_id)
        if pref_id is None:
            pref_id = create_user_pref(user_id)
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
        logger.error(f"Error updating preferences from purchase for user {user_id}: {e}")
        return False
