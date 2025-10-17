import os
import logging

logger = logging.getLogger(__name__)

class DatabaseUnavailable(Exception):
    pass


class DatabaseConnection:
    """Simple wrapper that only attempts a DB connection when full credentials are present.

    This prevents accidental imports from failing in CSV-only workflows.
    """
    def __init__(self):
        self._conn = None

    def get_connection(self):
        # lazily import psycopg2 only when needed
        from .settings import config

        if not all([config.DB_HOST, config.DB_NAME, config.DB_USER, config.DB_PASSWORD]):
            raise DatabaseUnavailable('Database not configured in .env (DB_HOST/DB_NAME/DB_USER/DB_PASSWORD)')

        if self._conn is None:
            try:
                import psycopg2
                from psycopg2.extras import RealDictCursor
                self._conn = psycopg2.connect(
                    host=config.DB_HOST,
                    port=config.DB_PORT or 5432,
                    database=config.DB_NAME,
                    user=config.DB_USER,
                    password=config.DB_PASSWORD,
                    cursor_factory=RealDictCursor
                )
                logger.info('DB connection established')
            except Exception as e:
                logger.error(f'Error establishing DB connection: {e}')
                raise
        return self._conn

    def close_connection(self):
        if self._conn:
            try:
                self._conn.close()
            except Exception:
                pass
            self._conn = None


# global instance
db = DatabaseConnection()
