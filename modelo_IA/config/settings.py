import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    # Database
    DB_HOST = os.getenv('DB_HOST', 'localhost')
    DB_PORT = os.getenv('DB_PORT', '5432')
    DB_NAME = os.getenv('DB_NAME', 'artemisia_db')
    DB_USER = os.getenv('DB_USER', 'postgres')
    DB_PASSWORD = os.getenv('DB_PASSWORD', 'password')
    
    # Java API
    JAVA_API_URL = os.getenv('JAVA_API_URL', 'http://localhost:8080/api')
    
    # ML Model
    MODEL_PATH = os.getenv('MODEL_PATH', 'models/trained/recommendation_model.pkl')
    
    # Training
    MIN_USERS_FOR_TRAINING = int(os.getenv('MIN_USERS_FOR_TRAINING', '50'))
    NUM_CLUSTERS = int(os.getenv('NUM_CLUSTERS', '5'))
    
    # Features
    CATEGORIES = [
        'Realista', 'Abstracta', 'Expresionista', 'Impresionista', 'Surrealista',
        'Conceptual', 'Religiosa', 'Histórica', 'Decorativa', 'Contemporánea'
    ]
    
    TECHNIQUES = [
        'Óleo', 'Acrílico', 'Acuarela', 'Temple', 'Fresco',
        'Gouache', 'Tinta', 'Mixta', 'Spray', 'Digital'
    ]

config = Config()