#!/usr/bin/env python3
"""
Genera datos de entrenamiento sintéticos para desarrollo
"""
import sys
import os
import pandas as pd
import numpy as np
import logging

sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from services.model_trainer import ModelTrainer
from config.settings import config

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def generate_synthetic_dataset(num_users=1000, num_products=500):
    """Genera dataset sintético completo"""
    logger.info(f"🏭 Generando dataset sintético: {num_users} usuarios, {num_products} productos")
    
    # Generar productos
    products = generate_products(num_products)
    
    # Generar usuarios y sus compras
    users = generate_users_with_purchases(num_users, products)
    
    # Guardar dataset
    save_synthetic_dataset(users, products)
    
    logger.info("✅ Dataset sintético generado exitosamente")

def generate_products(num_products):
    """Genera productos sintéticos"""
    products = []
    
    categories = config.CATEGORIES
    techniques = config.TECHNIQUES
    
    for i in range(num_products):
        # Asignar categorías y técnicas aleatorias
        num_cats = np.random.randint(1, 4)
        num_techs = np.random.randint(1, 3)
        
        product_cats = np.random.choice(categories, num_cats, replace=False).tolist()
        product_techs = np.random.choice(techniques, num_techs, replace=False).tolist()
        
        # Precio basado en categorías y técnicas
        base_price = 50
        if 'Óleo' in product_techs:
            base_price += 200
        if 'Realista' in product_cats:
            base_price += 150
        if 'Abstracta' in product_cats:
            base_price += 100
            
        price = np.random.normal(base_price, base_price * 0.3)
        price = max(20, min(2000, price))  # Limitar rango
        
        products.append({
            'product_id': i + 1,
            'name': f"Obra de Arte {i+1}",
            'categories': product_cats,
            'techniques': product_techs,
            'price': round(price, 2),
            'stock': np.random.randint(1, 20)
        })
    
    return products

def generate_users_with_purchases(num_users, products):
    """Genera usuarios con historial de compras"""
    users = []
    
    # Patrones de usuario
    patterns = [
        {'name': 'Traditionalist', 'pref_cats': ['Realista', 'Histórica'], 'pref_techs': ['Óleo'], 'budget': (300, 1200)},
        {'name': 'Modern_Lover', 'pref_cats': ['Abstracta', 'Contemporánea'], 'pref_techs': ['Acrílico', 'Digital'], 'budget': (100, 600)},
        {'name': 'Eclectic', 'pref_cats': ['Impresionista', 'Decorativa'], 'pref_techs': ['Acuarela', 'Óleo'], 'budget': (150, 800)},
        {'name': 'Budget', 'pref_cats': ['Decorativa'], 'pref_techs': ['Tinta', 'Acuarela'], 'budget': (50, 300)}
    ]
    
    for i in range(num_users):
        pattern = patterns[i % len(patterns)]
        
        # Generar compras
        num_purchases = np.random.poisson(3) + 1  # Mínimo 1 compra
        purchases = []
        total_spent = 0
        
        for _ in range(num_purchases):
            # Seleccionar producto que coincida con preferencias
            matching_products = [p for p in products 
                               if any(cat in p['categories'] for cat in pattern['pref_cats'])]
            
            if not matching_products:
                matching_products = products  # Fallback a todos los productos
                
            product = np.random.choice(matching_products)
            quantity = np.random.randint(1, 3)
            total = product['price'] * quantity
            
            purchases.append({
                'product_id': product['product_id'],
                'quantity': quantity,
                'total': total,
                'categories': product['categories'],
                'techniques': product['techniques']
            })
            total_spent += total
        
        users.append({
            'user_id': i + 1,
            'user_pattern': pattern['name'],
            'purchase_history': purchases,
            'total_purchases': num_purchases,
            'total_spent': total_spent,
            'avg_purchase_value': total_spent / num_purchases
        })
    
    return users

def save_synthetic_dataset(users, products):
    """Guarda dataset sintético"""
    os.makedirs('data/synthetic', exist_ok=True)
    
    # Guardar usuarios
    users_df = pd.DataFrame(users)
    users_df.to_csv('data/synthetic/synthetic_users.csv', index=False)
    
    # Guardar productos
    products_df = pd.DataFrame(products)
    products_df.to_csv('data/synthetic/synthetic_products.csv', index=False)
    
    # Guardar resumen
    summary = {
        'total_users': len(users),
        'total_products': len(products),
        'total_purchases': sum(len(u['purchase_history']) for u in users),
        'avg_purchases_per_user': np.mean([len(u['purchase_history']) for u in users])
    }
    
    import json
    with open('data/synthetic/dataset_summary.json', 'w') as f:
        json.dump(summary, f, indent=2)

if __name__ == '__main__':
    generate_synthetic_dataset(1000, 500)