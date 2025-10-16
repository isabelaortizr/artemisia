#!/usr/bin/env python3
"""
Script para entrenar el modelo de recomendación
"""
import sys
import os
import logging

# Añadir directorio raíz al path
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from services.model_trainer import ModelTrainer
from config.settings import config

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('training.log')
    ]
)

logger = logging.getLogger(__name__)

def main():
    """Función principal de entrenamiento"""
    logger.info("🎨 Iniciando entrenamiento del sistema de recomendación...")
    
    try:
        trainer = ModelTrainer()
        success = trainer.train_model_async()
        
        if success:
            logger.info("🚀 Entrenamiento iniciado en segundo plano")
            logger.info("📋 Ver training.log para progreso detallado")
        else:
            logger.error("❌ No se pudo iniciar el entrenamiento")
            sys.exit(1)
            
    except Exception as e:
        logger.error(f"💥 Error fatal: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()