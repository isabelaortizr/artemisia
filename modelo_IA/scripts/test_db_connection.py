#!/usr/bin/env python3
"""
Small smoke-test script to verify DB connectivity and DataProcessor access.
Runs using the project package layout (adds project root to PYTHONPATH).

It will:
 - try to connect to the DB via the DatabaseConnection wrapper
 - attempt to instantiate the project's DataProcessor (DB preferred) and fetch one product
 - print clear diagnostic information and exit with 0 on success, non-zero on failure
"""
import sys
import os
import logging
import traceback
import json

# Ensure project root (parent of the 'modelo_IA' package) is on sys.path so imports work
# e.g. workspace/.../artemisia should be on sys.path so `import modelo_IA` succeeds
ROOT = os.path.dirname(os.path.dirname(__file__))
PARENT = os.path.dirname(ROOT)
if PARENT not in sys.path:
    sys.path.insert(0, PARENT)
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

logging.basicConfig(level=logging.INFO, format='%(levelname)s: %(message)s')
logger = logging.getLogger('test_db_connection')

results = {
    'db_connection': False,
    'db_error': None,
    'data_processor': False,
    'data_processor_error': None,
    'sample_product': None
}

try:
    # Try DB connection (this will only attempt if DB env vars present)
    from modelo_IA.config.database import db as db_wrapper
    try:
        conn = db_wrapper.get_connection()
        results['db_connection'] = True
        logger.info('DB connection succeeded (returned connection object)')
    except Exception as e:
        results['db_error'] = str(e)
        logger.warning(f'DB connection failed: {e}')
except Exception as e:
    results['db_error'] = f'Could not import DatabaseConnection: {e}'
    logger.warning(results['db_error'])

# Try to instantiate DataProcessor (DB-preferred implementation should be used by the factory)
try:
    # Try common export names used across the project
    try:
        from modelo_IA.services.csv_data_processor import DataProcessor
    except Exception:
        from modelo_IA.services.csv_data_processor import CSVDataProcessor as DataProcessor

    dp = DataProcessor()
    results['data_processor'] = True
    logger.info('DataProcessor instantiated')

    # Attempt to fetch available products (limited)
    try:
        products = dp.get_available_products()
        n = len(products) if products is not None else 0
        logger.info(f'Products fetched: {n}')
        results['sample_product'] = products[0] if n > 0 else None
    except Exception as e:
        results['data_processor_error'] = f'Error fetching products: {e}\n{traceback.format_exc()}'
        logger.warning(results['data_processor_error'])

except Exception as e:
    results['data_processor_error'] = f'Could not import or instantiate DataProcessor: {e}\n{traceback.format_exc()}'
    logger.warning(results['data_processor_error'])

# Print JSON summary
print('\n===== TEST SUMMARY =====')
print(json.dumps(results, indent=2, ensure_ascii=False))

# Exit status: success if data_processor succeeded and either DB connected or CSV fallback worked
ok = results['data_processor'] and (results['db_connection'] or results['sample_product'] is not None)
sys.exit(0 if ok else 2)
