#!/usr/bin/env python3
"""
Script para recolectar y preparar datos de entrenamiento
"""
import sys
import os
import pandas as pd
import logging

sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from services.data_processor import DataProcessor
from config.settings import config

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def collect_training_data():
    """Recolecta datos para entrenamiento"""
    logger.info("📥 Recolectando datos de entrenamiento...")
    
    processor = DataProcessor()
    
    # 1. Datos de usuarios
    users_data = processor.get_all_users_data(limit=2000)
    logger.info(f"👥 Usuarios obtenidos: {len(users_data)}")
    
    # 2. Datos de productos
    products_data = processor.get_available_products()
    logger.info(f"🛍️  Productos obtenidos: {len(products_data)}")
    
    # 3. Datos externos
    external_data = processor.load_external_art_data()
    logger.info(f"🌐 Datos externos: {external_data.shape if not external_data.empty else 0} registros")
    
    # 4. Guardar datos para análisis
    save_analysis_data(users_data, products_data, external_data)
    
    logger.info("✅ Recolección de datos completada")

def save_analysis_data(users_data, products_data, external_data):
    """Guarda datos para análisis posterior"""
    os.makedirs('data/analysis', exist_ok=True)
    
    # Estadísticas de usuarios
    user_stats = {
        'total_users': len(users_data),
        'users_with_purchases': len([u for u in users_data if u.get('purchase_history')]),
        'avg_purchases_per_user': np.mean([len(u.get('purchase_history', [])) for u in users_data])
    }
    
    with open('data/analysis/user_stats.json', 'w') as f:
        import json
        json.dump(user_stats, f, indent=2)
    
    # Datos de productos
    if products_data:
        df_products = pd.DataFrame(products_data)
        df_products.to_csv('data/analysis/products_analysis.csv', index=False)
    
    logger.info("💾 Datos de análisis guardados")

if __name__ == '__main__':
    collect_training_data()