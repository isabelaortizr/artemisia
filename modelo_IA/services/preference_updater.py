import logging
from services.vector_builder import product_to_vector, dict_to_array, FEATURE_NAMES, l2_normalize
import numpy as np
from datetime import datetime
import logging

# Note: we import db lazily inside functions to avoid requiring psycopg2 at module import time

logger = logging.getLogger(__name__)


def fetch_product(product_id: int):
    from config.database import db
    conn = db.get_connection()
    with conn.cursor() as cur:
        cur.execute("""
            SELECT p.id, p.price,
                   ARRAY_AGG(DISTINCT pc.category) as categories,
                   ARRAY_AGG(DISTINCT pt.technique) as techniques
            FROM product p
            LEFT JOIN product_categories pc ON p.id = pc.product_id
            LEFT JOIN product_techniques pt ON p.id = pt.product_id
            WHERE p.id = %s
            GROUP BY p.id, p.price
        """, (product_id,))
        row = cur.fetchone()
        if not row:
            return None
        return {
            'product_id': row['id'],
            'price': float(row['price']) if row['price'] is not None else None,
            'categories': row['categories'] or [],
            'techniques': row['techniques'] or []
        }


def fetch_user_pref(user_id: int):
    from config.database import db
    conn = db.get_connection()
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM user_preferences WHERE user_id = %s", (user_id,))
        row = cur.fetchone()
        if not row:
            return None, {}
        pref_id = row['id']
        cur.execute("SELECT feature, weight FROM user_preference_vectors WHERE user_preference_id = %s", (pref_id,))
        rows = cur.fetchall()
        pref_map = {r['feature']: float(r['weight']) for r in rows}
        return pref_id, pref_map


def create_user_pref(user_id: int):
    from config.database import db
    conn = db.get_connection()
    with conn.cursor() as cur:
        cur.execute("INSERT INTO user_preferences (user_id, last_updated) VALUES (%s, %s) RETURNING id", (user_id, datetime.utcnow()))
        row = cur.fetchone()
        conn.commit()
        return row['id']


def write_user_pref_map(pref_id: int, pref_map: dict):
    from config.database import db
    conn = db.get_connection()
    with conn.cursor() as cur:
        # delete existing
        cur.execute("DELETE FROM user_preference_vectors WHERE user_preference_id = %s", (pref_id,))
        # insert new
        params = [(pref_id, k, float(v)) for k, v in pref_map.items()]
        if params:
            cur.executemany("INSERT INTO user_preference_vectors (user_preference_id, feature, weight) VALUES (%s, %s, %s)", params)
        # update timestamp
        cur.execute("UPDATE user_preferences SET last_updated = %s WHERE id = %s", (datetime.utcnow(), pref_id))
        conn.commit()


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

    # Transactional update: try to use preference_accum & weight_sum columns for O(1) update
    from config.database import db
    conn = db.get_connection()
    try:
        with conn.cursor() as cur:
            # try to select user_preferences row for update
            cur.execute("SELECT id, preference_accum, weight_sum FROM user_preferences WHERE user_id = %s FOR UPDATE", (user_id,))
            row = cur.fetchone()
            if not row:
                # create preference row
                cur.execute("INSERT INTO user_preferences (user_id, last_updated, preference_accum, weight_sum) VALUES (%s, now(), %s, %s) RETURNING id, preference_accum, weight_sum", (user_id, None, 0.0))
                row = cur.fetchone()

            pref_id = row['id']

            # If preference_accum exists and is not null, use it; otherwise reconstruct from map
            accum = row.get('preference_accum')
            wsum = row.get('weight_sum')
            if accum is not None and isinstance(accum, dict):
                # accum stored as map feature->value
                old_arr = np.zeros(len(FEATURE_NAMES), dtype=float)
                for i, fname in enumerate(FEATURE_NAMES):
                    old_arr[i] = float(accum.get(fname, 0.0))
                weight_sum = float(wsum or 0.0)
            else:
                # fallback: read element collection table
                cur.execute("SELECT feature, weight FROM user_preference_vectors WHERE user_preference_id = %s", (pref_id,))
                pref_rows = cur.fetchall()
                old_map = {r['feature']: float(r['weight']) for r in pref_rows} if pref_rows else {}
                old_arr = np.zeros(len(FEATURE_NAMES), dtype=float)
                for i, fname in enumerate(FEATURE_NAMES):
                    old_arr[i] = float(old_map.get(fname, 0.0))
                weight_sum = 0.0

            # Update accumulators
            new_accum = old_arr + (beta * pvec)
            new_weight_sum = weight_sum + beta

            # compute normalized vector
            if new_weight_sum == 0:
                raw = new_accum
            else:
                raw = new_accum / new_weight_sum
            new_norm = l2_normalize(raw)

            # persist: update preference_accum (jsonb), weight_sum, preference_json, and element collection
            # preference_accum stored as jsonb map
            accum_map = {FEATURE_NAMES[i]: float(new_accum[i]) for i in range(len(FEATURE_NAMES))}
            pref_json = {FEATURE_NAMES[i]: float(new_norm[i]) for i in range(len(FEATURE_NAMES))}

            # update accum & weight_sum & last_updated
            try:
                cur.execute("UPDATE user_preferences SET preference_accum = %s, weight_sum = %s, preference_json = %s, last_updated = now() WHERE id = %s",
                            (accum_map, new_weight_sum, pref_json, pref_id))
            except Exception:
                # If DB doesn't have these columns, fall back to deleting/inserting element collection
                cur.execute("DELETE FROM user_preference_vectors WHERE user_preference_id = %s", (pref_id,))
                params = [(pref_id, k, float(pref_json[k])) for k in pref_json.keys()]
                if params:
                    cur.executemany("INSERT INTO user_preference_vectors (user_preference_id, feature, weight) VALUES (%s, %s, %s)", params)
                cur.execute("UPDATE user_preferences SET last_updated = now() WHERE id = %s", (pref_id,))

            conn.commit()
            return True
    except Exception as e:
        logger.error(f"Error updating user preference for user {user_id}: {e}")
        try:
            conn.rollback()
        except Exception:
            pass
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

    # Use similar transactional pattern as product update
    from config.database import db
    conn = db.get_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT id, preference_accum, weight_sum FROM user_preferences WHERE user_id = %s FOR UPDATE", (user_id,))
            row = cur.fetchone()
            if not row:
                cur.execute("INSERT INTO user_preferences (user_id, last_updated, preference_accum, weight_sum) VALUES (%s, now(), %s, %s) RETURNING id, preference_accum, weight_sum", (user_id, None, 0.0))
                row = cur.fetchone()

            pref_id = row['id']
            accum = row.get('preference_accum')
            wsum = row.get('weight_sum')
            if accum is not None and isinstance(accum, dict):
                old_arr = np.zeros(len(FEATURE_NAMES), dtype=float)
                for i, fname in enumerate(FEATURE_NAMES):
                    old_arr[i] = float(accum.get(fname, 0.0))
                weight_sum = float(wsum or 0.0)
            else:
                cur.execute("SELECT feature, weight FROM user_preference_vectors WHERE user_preference_id = %s", (pref_id,))
                pref_rows = cur.fetchall()
                old_map = {r['feature']: float(r['weight']) for r in pref_rows} if pref_rows else {}
                old_arr = np.zeros(len(FEATURE_NAMES), dtype=float)
                for i, fname in enumerate(FEATURE_NAMES):
                    old_arr[i] = float(old_map.get(fname, 0.0))
                weight_sum = 0.0

            new_accum = old_arr + (beta * combined)
            new_weight_sum = weight_sum + beta
            if new_weight_sum == 0:
                raw = new_accum
            else:
                raw = new_accum / new_weight_sum
            new_norm = l2_normalize(raw)

            accum_map = {FEATURE_NAMES[i]: float(new_accum[i]) for i in range(len(FEATURE_NAMES))}
            pref_json = {FEATURE_NAMES[i]: float(new_norm[i]) for i in range(len(FEATURE_NAMES))}

            try:
                cur.execute("UPDATE user_preferences SET preference_accum = %s, weight_sum = %s, preference_json = %s, last_updated = now() WHERE id = %s",
                            (accum_map, new_weight_sum, pref_json, pref_id))
            except Exception:
                cur.execute("DELETE FROM user_preference_vectors WHERE user_preference_id = %s", (pref_id,))
                params = [(pref_id, k, float(pref_json[k])) for k in pref_json.keys()]
                if params:
                    cur.executemany("INSERT INTO user_preference_vectors (user_preference_id, feature, weight) VALUES (%s, %s, %s)", params)
                cur.execute("UPDATE user_preferences SET last_updated = now() WHERE id = %s", (pref_id,))

            conn.commit()
            return True
    except Exception as e:
        logger.error(f"Error updating preferences from purchase for user {user_id}: {e}")
        try:
            conn.rollback()
        except Exception:
            pass
        return False
