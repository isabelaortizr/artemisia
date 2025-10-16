import psycopg2
from psycopg2.extras import RealDictCursor
from config.settings import config
import logging

logger = logging.getLogger(__name__)

class DatabaseConnection:
    def __init__(self):
        self.connection = None
        
    def get_connection(self):
        if self.connection is None or self.connection.closed:
            try:
                self.connection = psycopg2.connect(
                    host=config.DB_HOST,
                    port=config.DB_PORT,
                    database=config.DB_NAME,
                    user=config.DB_USER,
                    password=config.DB_PASSWORD,
                    cursor_factory=RealDictCursor
                )
                logger.info("✅ Conexión a base de datos establecida")
            except Exception as e:
                logger.error(f"❌ Error conectando a la base de datos: {e}")
                raise e
        return self.connection
    
    def close_connection(self):
        if self.connection and not self.connection.closed:
            self.connection.close()
            self.connection = None

# Instancia global
db = DatabaseConnection()