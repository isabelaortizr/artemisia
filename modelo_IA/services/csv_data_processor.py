"""
CSV-based DataProcessor replacement.
Reads CSVs produced by `tools/export_training_data.py` in `modelo_IA/data_exports/`.
Provides a subset of the original DataProcessor interface used by ModelTrainer:
  - get_all_users_data(limit)
  - get_available_products()
  - get_training_data_from_db()

This allows training using CSVs instead of the DB.
"""
import os
import csv
import json
import logging
from io import StringIO
from typing import List, Dict, Any, Optional

PACKAGE_ROOT = os.path.dirname(os.path.dirname(__file__))
DEFAULT_EXPORT_DIR = os.path.join(PACKAGE_ROOT, 'data_exports')

logger = logging.getLogger(__name__)


class CSVDataProcessor:
    def __init__(self, export_dir: Optional[str] = None):
        self.export_dir = os.path.abspath(export_dir or DEFAULT_EXPORT_DIR)

    def _read_csv(self, name: str) -> List[Dict[str, Any]]:
        path = os.path.join(self.export_dir, name)
        if not os.path.exists(path):
            return []
        # Try a few common encodings (utf-8, utf-8-sig, cp1252) and fall back to a
        # best-effort decode if none succeed. Use newline='' which is recommended
        # for csv module to handle line endings correctly.
        tried = []
        for enc in ('utf-8', 'utf-8-sig', 'cp1252'):
            try:
                with open(path, 'r', encoding=enc, newline='') as f:
                    reader = csv.DictReader(f)
                    rows = [row for row in reader]
                    if rows is not None:
                        if enc != 'utf-8':
                            logger.debug(f"_read_csv: opened %s with encoding=%s (rows=%d)", path, enc, len(rows))
                        return rows
            except UnicodeDecodeError as e:
                tried.append(enc)
                logger.debug("_read_csv: UnicodeDecodeError with %s for file %s: %s", enc, path, e)
                continue
            except Exception as e:
                logger.warning("_read_csv: error reading %s with encoding %s: %s", path, enc, e)
                return []

        # Last resort: read bytes and decode with replacement to avoid crashing.
        try:
            with open(path, 'rb') as bf:
                raw = bf.read()
            text = raw.decode('utf-8', errors='replace')
            logger.debug("_read_csv: fallback binary decode for %s (tried: %s)", path, ','.join(tried))
            reader = csv.DictReader(StringIO(text))
            return [row for row in reader]
        except Exception as e:
            logger.error("_read_csv: failed to read %s: %s", path, e)
            return []

    def get_all_users_data(self, limit: int = 1000) -> List[Dict[str, Any]]:
        users = self._read_csv('users.csv')
        # normalize column names: email may be 'email' or 'mail'
        out = []
        for u in users[:limit]:
            out.append({
                'id': int(u.get('id')) if u.get('id') else None,
                'name': u.get('name') or u.get('username'),
                'email': u.get('email') or u.get('mail'),
                'status': u.get('status'),
                'role': u.get('role')
            })
        return out

    def get_available_products(self) -> List[Dict[str, Any]]:
        rows = self._read_csv('products.csv')
        out = []
        for r in rows:
            techniques = r.get('techniques')
            categories = r.get('categories')
            # csv may represent arrays like {"tech":"x"} or Postgres array string
            def parse_arr(v):
                if v is None:
                    return []
                v = v.strip()
                # Postgres array format: {a,b}
                if v.startswith('{') and v.endswith('}'):
                    elems = [e.strip() for e in v[1:-1].split(',') if e.strip()]
                    elems = [clean_item(x) for x in elems]
                    return elems
                # JSON array with double quotes
                try:
                    parsed = json.loads(v)
                    if isinstance(parsed, list):
                        return [clean_item(str(x)) for x in parsed]
                    # if it's a single value, wrap
                    return [clean_item(str(parsed))]
                except Exception:
                    # fallback: Python-list-like string: ['A','B'] or plain comma-separated
                    # remove surrounding brackets
                    s = v
                    if s.startswith('[') and s.endswith(']'):
                        s = s[1:-1]
                    parts = [x.strip() for x in s.split(',') if x.strip()]
                    return [clean_item(x) for x in parts]

            def clean_item(x: str) -> str:
                # strip surrounding quotes and brackets and whitespace
                if x is None:
                    return ''
                x = x.strip()
                # remove surrounding single or double quotes
                if len(x) >= 2 and ((x[0] == x[-1]) and x[0] in "'\""):
                    x = x[1:-1]
                # remove stray brackets
                x = x.strip('[](){}')
                return x.strip()

            out.append({
                'id': int(r.get('id')) if r.get('id') else None,
                'name': r.get('name'),
                'materials': r.get('materials'),
                'description': r.get('description'),
                'price': float(r.get('price')) if r.get('price') else None,
                'stock': int(r.get('stock')) if r.get('stock') else None,
                'status': r.get('status'),
                'image_url': r.get('image_url'),
                'techniques': parse_arr(techniques),
                'categories': parse_arr(categories)
            })
        return out

    def get_training_data_from_db(self) -> List[Dict[str, Any]]:
        # Compose a training dataset merging users, interactions and product info
        # The training pipeline expects a list of user-centric dicts with keys:
        #   'user_id', 'purchase_history' (list of purchases), 'total_purchases', 'avg_purchase_value'
        users = {u['id']: u for u in self.get_all_users_data(100000)}
        products = {p['id']: p for p in self.get_available_products()}

        # Diagnostic logging to help debug DB-based training failures
        try:
            logger.info(f"DBDataProcessor: users={len(users)}, products={len(products)}")
            interactions_preview = []
            interactions = self._read_csv('interactions.csv') or self._read_csv('purchases.csv')
            if interactions:
                interactions_preview = interactions[:5]
            logger.info(f"DBDataProcessor: interactions_count={len(interactions) if interactions is not None else 0}; preview={interactions_preview}")
        except Exception:
            # non-fatal diagnostic
            pass

        # support multiple possible interaction filenames (interactions.csv or purchases.csv)
        interactions = self._read_csv('interactions.csv')
        if not interactions:
            interactions = self._read_csv('purchases.csv')

        # group by buyer_id
        grouped = {}
        for row in interactions:
            try:
                buyer_id = int(row.get('buyer_id') or row.get('user_id') or row.get('buyer') or 0)
            except Exception:
                continue
            try:
                product_id = int(row.get('product_id') or row.get('item_id') or 0)
            except Exception:
                product_id = None

            if not buyer_id:
                continue

            qty = 0
            try:
                qty = int(row.get('quantity') or row.get('qty') or 0)
            except Exception:
                qty = 0

            total = 0.0
            try:
                total = float(row.get('total_paid') or row.get('total') or row.get('amount') or 0.0)
            except Exception:
                total = 0.0

            product = products.get(product_id, {}) if product_id else {}

            purchase = {
                'product_id': product_id,
                'quantity': qty,
                'total': total,
                'categories': product.get('categories', []),
                'techniques': product.get('techniques', []),
                'purchase_date': row.get('purchase_date') or row.get('date')
            }

            lst = grouped.setdefault(buyer_id, [])
            lst.append(purchase)

        out = []
        for buyer_id, purchases in grouped.items():
            tot = sum(p.get('total', 0.0) for p in purchases)
            num = len(purchases)
            avg = (tot / num) if num else 0.0
            out.append({
                'user_id': buyer_id,
                'purchase_history': purchases,
                'total_purchases': num,
                'avg_purchase_value': avg,
                'user': users.get(buyer_id, {})
            })

        # Also include users that have explicit preference vectors in user_preferences.csv
        prefs_path = os.path.join(self.export_dir, 'user_preferences.csv')
        if os.path.exists(prefs_path):
            try:
                with open(prefs_path, 'r', encoding='utf-8') as pf:
                    pref_reader = csv.DictReader(pf)
                    for row in pref_reader:
                        try:
                            uid = int(row.get('user_id') or row.get('id'))
                        except Exception:
                            continue
                        # parse vector_kv which may be a Python-list-like string
                        vraw = row.get('vector_kv')
                        pref_map = {}
                        if vraw:
                            try:
                                # try JSON first
                                parsed = json.loads(vraw)
                                if isinstance(parsed, list):
                                    for item in parsed:
                                        if isinstance(item, str) and ':' in item:
                                            k, v = item.split(':', 1)
                                            pref_map[k.strip()] = float(v)
                                elif isinstance(parsed, dict):
                                    for k, v in parsed.items():
                                        pref_map[str(k)] = float(v)
                            except Exception:
                                # fallback: Python list string like "['k:0.1','k2:0.2']"
                                try:
                                    import ast
                                    parsed = ast.literal_eval(vraw)
                                    if isinstance(parsed, (list, tuple)):
                                        for item in parsed:
                                            if isinstance(item, str) and ':' in item:
                                                k, v = item.split(':', 1)
                                                pref_map[k.strip()] = float(v)
                                except Exception:
                                    # last resort: split by commas
                                    s = vraw.strip()
                                    if s.startswith('[') and s.endswith(']'):
                                        s = s[1:-1]
                                    parts = [p.strip().strip("'\"") for p in s.split(',') if p.strip()]
                                    for item in parts:
                                        if ':' in item:
                                            k, v = item.split(':', 1)
                                            try:
                                                pref_map[k.strip()] = float(v)
                                            except Exception:
                                                pass

                        # Only add if not already present (preferences may duplicate purchases)
                        if not any(u.get('user_id') == uid for u in out):
                            out.append({
                                'user_id': uid,
                                'preference_vector': pref_map,
                                'purchase_history': users.get(uid, {}).get('purchase_history', []) if users.get(uid) else [],
                                'total_purchases': len(users.get(uid, {}).get('purchase_history', [])) if users.get(uid) else 0,
                                'avg_purchase_value': 0.0,
                                'user': users.get(uid, {})
                            })
            except Exception:
                pass

        return out

    def _get_user_purchase_history(self, user_id: int) -> List[Dict[str, Any]]:
        """Return the list of purchases for a given user by reading interactions/purchases CSV."""
        interactions = self._read_csv('interactions.csv')
        if not interactions:
            interactions = self._read_csv('purchases.csv')

        products = {p['id']: p for p in self.get_available_products()}
        out = []
        for row in interactions:
            try:
                buyer_id = int(row.get('buyer_id') or row.get('user_id') or row.get('buyer') or 0)
            except Exception:
                continue
            if buyer_id != user_id:
                continue
            try:
                product_id = int(row.get('product_id') or row.get('item_id') or 0)
            except Exception:
                product_id = None

            qty = 0
            try:
                qty = int(row.get('quantity') or row.get('qty') or 0)
            except Exception:
                qty = 0

            total = 0.0
            try:
                total = float(row.get('total_paid') or row.get('total') or row.get('amount') or 0.0)
            except Exception:
                total = 0.0

            product = products.get(product_id, {}) if product_id else {}
            purchase = {
                'product_id': product_id,
                'quantity': qty,
                'total': total,
                'categories': product.get('categories', []),
                'techniques': product.get('techniques', []),
                'purchase_date': row.get('purchase_date') or row.get('date')
            }
            out.append(purchase)
        return out

    def get_user_data(self, user_id: int) -> Dict[str, Any]:
        """Return a combined user record with purchase_history and optional preference_vector."""
        users = self._read_csv('users.csv')
        user_row = None
        for u in users:
            try:
                if int(u.get('id') or 0) == int(user_id):
                    user_row = u
                    break
            except Exception:
                continue

        result = {
            'user_id': int(user_id),
            'user': user_row or {},
            'purchase_history': self._get_user_purchase_history(int(user_id))
        }

        # attach preference vector if present
        prefs_path = os.path.join(self.export_dir, 'user_preferences.csv')
        if os.path.exists(prefs_path):
            try:
                with open(prefs_path, 'r', encoding='utf-8') as pf:
                    pref_reader = csv.DictReader(pf)
                    for row in pref_reader:
                        try:
                            uid = int(row.get('user_id') or row.get('id'))
                        except Exception:
                            continue
                        if uid != int(user_id):
                            continue
                        vraw = row.get('vector_kv')
                        pref_map = {}
                        if vraw:
                            try:
                                parsed = json.loads(vraw)
                                if isinstance(parsed, list):
                                    for item in parsed:
                                        if isinstance(item, str) and ':' in item:
                                            k, v = item.split(':', 1)
                                            pref_map[k.strip()] = float(v)
                                elif isinstance(parsed, dict):
                                    for k, v in parsed.items():
                                        pref_map[str(k)] = float(v)
                            except Exception:
                                try:
                                    import ast
                                    parsed = ast.literal_eval(vraw)
                                    if isinstance(parsed, (list, tuple)):
                                        for item in parsed:
                                            if isinstance(item, str) and ':' in item:
                                                k, v = item.split(':', 1)
                                                pref_map[k.strip()] = float(v)
                                except Exception:
                                    s = vraw.strip()
                                    if s.startswith('[') and s.endswith(']'):
                                        s = s[1:-1]
                                    parts = [p.strip().strip("'\"") for p in s.split(',') if p.strip()]
                                    for item in parts:
                                        if ':' in item:
                                            k, v = item.split(':', 1)
                                            try:
                                                pref_map[k.strip()] = float(v)
                                            except Exception:
                                                pass
                        result['preference_vector'] = pref_map
                        break
            except Exception:
                pass

        return result


# convenience factory to keep previous import style
def DataProcessor(*args, **kwargs):
    """Factory: return a DB-backed processor when DB is configured, else the CSV processor.

    It tries to use the DatabaseConnection helper (lazy import) and falls back to CSV on
    any error. Callers should import `DataProcessor` and not `CSVDataProcessor` directly
    if they want automatic DB usage.
    """
    # Try to use the DB connection if available
    try:
        from ..config.database import db, DatabaseUnavailable  # lazy import
        # attempt to get a connection; DatabaseUnavailable will be raised if creds missing
        conn = db.get_connection()
        # If we have a working connection, return DBDataProcessor
        return DBDataProcessor(conn, *args, **kwargs)
    except Exception:
        # Any problem -> fall back to CSV-based processor
        return CSVDataProcessor(*args, **kwargs)


class DBDataProcessor:
    """DB-backed data processor. Minimal implementation mirroring the CSVDataProcessor
    interface used by the training and serving code.

    It expects a psycopg2 connection (RealDictCursor compatible) and will execute
    queries similar to `tools/export_training_data.py`.
    """
    def __init__(self, conn, export_dir: Optional[str] = None):
        self.conn = conn
        # keep export_dir for compatibility with callers that pass it
        self.export_dir = os.path.abspath(export_dir) if export_dir else None

    def _rows_to_list(self, query, params=None):
        cur = self.conn.cursor()
        cur.execute(query, params or ())
        rows = cur.fetchall()
        # psycopg2 RealDictCursor gives dict-like rows
        out = []
        for r in rows:
            try:
                out.append(dict(r))
            except Exception:
                # tuple -> list
                out.append(r)
        return out

    def get_all_users_data(self, limit: int = 1000) -> List[Dict[str, Any]]:
        q = """
        SELECT id, name, mail as email, status, role, created_date, modified_date
        FROM users
        LIMIT %s
        """
        # Diagnostic: log the query and examine returned rows to detect DB-side filtering
        try:
            rows = self._rows_to_list(q, (limit,))
            try:
                # Rows may be list of dicts or tuples; extract ids where possible
                ids = []
                for r in rows[:200]:
                    try:
                        if isinstance(r, dict):
                            ids.append(r.get('id'))
                        else:
                            # tuple-like
                            ids.append(r[0])
                    except Exception:
                        ids.append(None)
                logger.debug("DBDataProcessor.get_all_users_data: query returned %d rows; sample ids=%s", len(rows), ids[:50])
            except Exception:
                logger.debug("DBDataProcessor.get_all_users_data: query returned %d rows (could not extract ids sample)", len(rows))
        except Exception as e:
            logger.warning("DBDataProcessor.get_all_users_data: error executing query: %s", e)
            rows = []
        out = []
        for u in rows:
            out.append({
                'id': int(u.get('id')) if u.get('id') else None,
                'name': u.get('name') or u.get('username'),
                'email': u.get('email') or u.get('mail'),
                'status': u.get('status'),
                'role': u.get('role')
            })
        return out

    def get_available_products(self) -> List[Dict[str, Any]]:
        q = """
        SELECT p.id, p.name, p.materials, p.description, p.price, p.stock, p.status, p.image_url,
               COALESCE(array_agg(DISTINCT pt.technique) FILTER (WHERE pt.technique IS NOT NULL), ARRAY[]::text[]) AS techniques,
               COALESCE(array_agg(DISTINCT pc.category) FILTER (WHERE pc.category IS NOT NULL), ARRAY[]::text[]) AS categories,
               p.created_date
        FROM product p
        LEFT JOIN product_techniques pt ON pt.product_id = p.id
        LEFT JOIN product_categories pc ON pc.product_id = p.id
        GROUP BY p.id, p.name, p.materials, p.description, p.price, p.stock, p.status, p.image_url, p.created_date
        """
        rows = self._rows_to_list(q)
        out = []
        for r in rows:
            # techniques/categories may already be Python lists
            techniques = r.get('techniques') or []
            categories = r.get('categories') or []
            # normalize elements to strings
            techniques = [str(x) for x in techniques]
            categories = [str(x) for x in categories]
            out.append({
                'id': int(r.get('id')) if r.get('id') else None,
                'name': r.get('name'),
                'materials': r.get('materials'),
                'description': r.get('description'),
                'price': float(r.get('price')) if r.get('price') is not None else None,
                'stock': int(r.get('stock')) if r.get('stock') is not None else None,
                'status': r.get('status'),
                'image_url': r.get('image_url'),
                'techniques': techniques,
                'categories': categories
            })
        return out

    def get_training_data_from_db(self) -> List[Dict[str, Any]]:
        # Build user-centric training records similar to CSV version
        users = {u['id']: u for u in self.get_all_users_data(100000)}
        products = {p['id']: p for p in self.get_available_products()}

        # interactions
        q = """
        SELECT d.id as detail_id, n.id as nota_id, n.user_id as buyer_id, d.seller_id as seller_id,
               d.product_id as product_id, d.product_name, d.quantity, d.total as total_paid,
               n.estado_venta as venta_state, n.date as purchase_date
        FROM detail d
        JOIN nota_venta n ON d.group_id = n.id
        """
        interactions = self._rows_to_list(q)

        grouped = {}
        for row in interactions:
            try:
                buyer_id = int(row.get('buyer_id') or row.get('user_id') or 0)
            except Exception:
                continue
            try:
                product_id = int(row.get('product_id') or 0)
            except Exception:
                product_id = None

            if not buyer_id:
                continue

            qty = 0
            try:
                qty = int(row.get('quantity') or 0)
            except Exception:
                qty = 0

            total = 0.0
            try:
                total = float(row.get('total_paid') or row.get('total') or 0.0)
            except Exception:
                total = 0.0

            product = products.get(product_id, {}) if product_id else {}

            purchase = {
                'product_id': product_id,
                'quantity': qty,
                'total': total,
                'categories': product.get('categories', []),
                'techniques': product.get('techniques', []),
                'purchase_date': row.get('purchase_date') or row.get('date')
            }

            lst = grouped.setdefault(buyer_id, [])
            lst.append(purchase)

        out = []
        for buyer_id, purchases in grouped.items():
            tot = sum(p.get('total', 0.0) for p in purchases)
            num = len(purchases)
            avg = (tot / num) if num else 0.0
            out.append({
                'user_id': buyer_id,
                'purchase_history': purchases,
                'total_purchases': num,
                'avg_purchase_value': avg,
                'user': users.get(buyer_id, {})
            })

        # user preferences (optional)
        qprefs = """
        SELECT up.user_id, up.last_updated, array_agg( (v.feature || ':' || v.weight) ) as vector_kv
        FROM user_preferences up
        LEFT JOIN user_preference_vectors v ON v.user_preference_id = up.id
        GROUP BY up.user_id, up.last_updated
        """
        try:
            prefs = self._rows_to_list(qprefs)
            for row in prefs:
                try:
                    uid = int(row.get('user_id') or 0)
                except Exception:
                    continue
                vlist = row.get('vector_kv') or []
                pref_map = {}
                for item in vlist:
                    if isinstance(item, str) and ':' in item:
                        k, v = item.split(':', 1)
                        try:
                            pref_map[k.strip()] = float(v)
                        except Exception:
                            pass
                if not any(u.get('user_id') == uid for u in out):
                    out.append({
                        'user_id': uid,
                        'preference_vector': pref_map,
                        'purchase_history': users.get(uid, {}).get('purchase_history', []) if users.get(uid) else [],
                        'total_purchases': len(users.get(uid, {}).get('purchase_history', [])) if users.get(uid) else 0,
                        'avg_purchase_value': 0.0,
                        'user': users.get(uid, {})
                    })
        except Exception:
            # ignore prefs if table absent
            pass

        return out

    def _get_user_purchase_history(self, user_id: int) -> List[Dict[str, Any]]:
        q = """
        SELECT d.id as detail_id, n.id as nota_id, n.user_id as buyer_id, d.seller_id as seller_id,
               d.product_id as product_id, d.product_name, d.quantity, d.total as total_paid,
               n.estado_venta as venta_state, n.date as purchase_date
        FROM detail d
        JOIN nota_venta n ON d.group_id = n.id
        WHERE n.user_id = %s
        """
        rows = self._rows_to_list(q, (user_id,))
        products = {p['id']: p for p in self.get_available_products()}
        out = []
        for row in rows:
            try:
                product_id = int(row.get('product_id') or 0)
            except Exception:
                product_id = None
            qty = 0
            try:
                qty = int(row.get('quantity') or 0)
            except Exception:
                qty = 0
            total = 0.0
            try:
                total = float(row.get('total_paid') or row.get('total') or 0.0)
            except Exception:
                total = 0.0
            product = products.get(product_id, {}) if product_id else {}
            purchase = {
                'product_id': product_id,
                'quantity': qty,
                'total': total,
                'categories': product.get('categories', []),
                'techniques': product.get('techniques', []),
                'purchase_date': row.get('purchase_date') or row.get('date')
            }
            out.append(purchase)
        return out

    def get_user_data(self, user_id: int) -> Dict[str, Any]:
        users = {u['id']: u for u in self.get_all_users_data(100000)}
        result = {
            'user_id': int(user_id),
            'user': users.get(int(user_id), {}),
            'purchase_history': self._get_user_purchase_history(int(user_id))
        }

        # try to fetch preference vector
        try:
            q = """
            SELECT array_agg( (v.feature || ':' || v.weight) ) as vector_kv
            FROM user_preferences up
            LEFT JOIN user_preference_vectors v ON v.user_preference_id = up.id
            WHERE up.user_id = %s
            GROUP BY up.user_id
            """
            rows = self._rows_to_list(q, (int(user_id),))
            if rows:
                vlist = rows[0].get('vector_kv') or []
                pref_map = {}
                for item in vlist:
                    if isinstance(item, str) and ':' in item:
                        k, v = item.split(':', 1)
                        try:
                            pref_map[k.strip()] = float(v)
                        except Exception:
                            pass
                result['preference_vector'] = pref_map
        except Exception:
            pass

        return result
