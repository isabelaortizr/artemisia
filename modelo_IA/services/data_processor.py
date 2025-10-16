import pandas as pd
import numpy as np
import requests
import json
import logging
from typing import Dict, List, Any, Optional
from config.database import db
from config.settings import config

logger = logging.getLogger(__name__)

class DataProcessor:
    def __init__(self):
        self.java_api_url = config.JAVA_API_URL
        
    def get_user_data(self, user_id: int) -> Optional[Dict]:
        """Obtiene datos completos de usuario desde la base de datos"""
        try:
            connection = db.get_connection()
            with connection.cursor() as cursor:
                # Datos básicos del usuario
                cursor.execute("""
                    SELECT id, name, mail, created_date 
                    FROM users WHERE id = %s
                """, (user_id,))
                user = cursor.fetchone()
                
                if not user:
                    return None
                
                # Historial de compras
                cursor.execute("""
                    SELECT nv.id, nv.total_global, nv.date, 
                           od.product_id, od.quantity, od.total,
                           p.categories, p.techniques, p.price
                    FROM nota_venta nv
                    JOIN order_detail od ON nv.id = od.group_id
                    JOIN product p ON od.product_id = p.id
                    WHERE nv.buyer_id = %s AND nv.estado_venta = 'PAYED'
                    ORDER BY nv.date DESC
                """, (user_id,))
                purchases = cursor.fetchall()
                
                # Vector de preferencias si existe
                cursor.execute("""
                    SELECT feature, weight 
                    FROM user_preference_vectors upv
                    JOIN user_preferences up ON upv.user_preference_id = up.id
                    WHERE up.user_id = %s
                """, (user_id,))
                preference_rows = cursor.fetchall()
                
                preference_vector = {row['feature']: row['weight'] for row in preference_rows}
                
                return {
                    'user_id': user_id,
                    'user_data': dict(user),
                    'purchase_history': [dict(purchase) for purchase in purchases],
                    'preference_vector': preference_vector
                }
                
        except Exception as e:
            logger.error(f"Error obteniendo datos de usuario {user_id}: {e}")
            return None
    
    def get_all_users_data(self, limit: int = 1000) -> List[Dict]:
        """Obtiene datos de todos los usuarios para entrenamiento"""
        try:
            connection = db.get_connection()
            with connection.cursor() as cursor:
                cursor.execute("""
                    SELECT id FROM users 
                    WHERE status = 'ACTIVE' 
                    ORDER BY id 
                    LIMIT %s
                """, (limit,))
                user_ids = [row['id'] for row in cursor.fetchall()]
            
            users_data = []
            for user_id in user_ids:
                user_data = self.get_user_data(user_id)
                if user_data:
                    users_data.append(user_data)
            
            logger.info(f"📥 Obtenidos datos de {len(users_data)} usuarios")
            return users_data
            
        except Exception as e:
            logger.error(f"Error obteniendo datos de usuarios: {e}")
            return []
    
    def get_available_products(self) -> List[Dict]:
        """Obtiene productos disponibles desde la base de datos"""
        try:
            connection = db.get_connection()
            with connection.cursor() as cursor:
                cursor.execute("""
                    SELECT p.id as product_id, p.name, p.price, p.stock,
                           ARRAY_AGG(DISTINCT pc.category) as categories,
                           ARRAY_AGG(DISTINCT pt.technique) as techniques
                    FROM product p
                    LEFT JOIN product_categories pc ON p.id = pc.product_id
                    LEFT JOIN product_techniques pt ON p.id = pt.product_id
                    WHERE p.status = 'AVAILABLE' AND p.stock > 0
                    GROUP BY p.id, p.name, p.price, p.stock
                    ORDER BY p.id
                """)
                products = cursor.fetchall()
                
                # Convertir a formato estándar
                formatted_products = []
                for product in products:
                    formatted_products.append({
                        'product_id': product['product_id'],
                        'name': product['name'],
                        'price': float(product['price']),
                        'stock': product['stock'],
                        'categories': product['categories'] or [],
                        'techniques': product['techniques'] or []
                    })
                
                logger.info(f"🛍️  Obtenidos {len(formatted_products)} productos disponibles")
                return formatted_products
                
        except Exception as e:
            logger.error(f"Error obteniendo productos: {e}")
            return self._get_sample_products()
    
    def get_training_data_from_db(self) -> List[Dict]:
        """Obtiene datos de entrenamiento directamente de la base de datos"""
        try:
            connection = db.get_connection()
            with connection.cursor() as cursor:
                # Consulta optimizada para datos de entrenamiento
                cursor.execute("""
                    SELECT 
                        u.id as user_id,
                        COUNT(DISTINCT nv.id) as total_purchases,
                        AVG(nv.total_global) as avg_purchase_value,
                        ARRAY_AGG(DISTINCT pc.category) as purchased_categories,
                        ARRAY_AGG(DISTINCT pt.technique) as purchased_techniques,
                        MAX(nv.date) as last_purchase_date
                    FROM users u
                    LEFT JOIN nota_venta nv ON u.id = nv.buyer_id AND nv.estado_venta = 'PAYED'
                    LEFT JOIN order_detail od ON nv.id = od.group_id
                    LEFT JOIN product p ON od.product_id = p.id
                    LEFT JOIN product_categories pc ON p.id = pc.product_id
                    LEFT JOIN product_techniques pt ON p.id = pt.product_id
                    WHERE u.status = 'ACTIVE'
                    GROUP BY u.id
                    HAVING COUNT(DISTINCT nv.id) > 0
                    ORDER BY u.id
                """)
                
                training_data = []
                for row in cursor.fetchall():
                    user_data = {
                        'user_id': row['user_id'],
                        'total_purchases': row['total_purchases'],
                        'avg_purchase_value': float(row['avg_purchase_value'] or 0),
                        'purchased_categories': row['purchased_categories'] or [],
                        'purchased_techniques': row['purchased_techniques'] or [],
                        'purchase_history': self._get_user_purchase_history(row['user_id'])
                    }
                    training_data.append(user_data)
                
                return training_data
                
        except Exception as e:
            logger.error(f"Error obteniendo datos de entrenamiento: {e}")
            return []
    
    def _get_user_purchase_history(self, user_id: int) -> List[Dict]:
        """Obtiene historial detallado de compras"""
        try:
            connection = db.get_connection()
            with connection.cursor() as cursor:
                cursor.execute("""
                    SELECT 
                        p.id as product_id,
                        p.name,
                        p.price,
                        od.quantity,
                        od.total,
                        ARRAY_AGG(DISTINCT pc.category) as categories,
                        ARRAY_AGG(DISTINCT pt.technique) as techniques
                    FROM order_detail od
                    JOIN product p ON od.product_id = p.id
                    JOIN nota_venta nv ON od.group_id = nv.id
                    LEFT JOIN product_categories pc ON p.id = pc.product_id
                    LEFT JOIN product_techniques pt ON p.id = pt.product_id
                    WHERE nv.buyer_id = %s AND nv.estado_venta = 'PAYED'
                    GROUP BY p.id, p.name, p.price, od.quantity, od.total
                """, (user_id,))
                
                return [dict(row) for row in cursor.fetchall()]
                
        except Exception as e:
            logger.error(f"Error obteniendo historial de usuario {user_id}: {e}")
            return []
    
    def _get_sample_products(self) -> List[Dict]:
        """Productos de ejemplo para desarrollo"""
        return [
            {
                'product_id': 1,
                'name': 'Paisaje Abstracto',
                'categories': ['Abstracta', 'Contemporánea'],
                'techniques': ['Acrílico', 'Mixta'],
                'price': 450.0,
                'stock': 5
            },
            {
                'product_id': 2,
                'name': 'Retrato Realista',
                'categories': ['Realista'],
                'techniques': ['Óleo'],
                'price': 800.0,
                'stock': 3
            }
        ]
    
    def load_external_art_data(self) -> pd.DataFrame:
        """Carga datos externos de arte"""
        try:
            # Simular datos de movimientos artísticos
            art_data = {
                'movement': ['Renaissance', 'Baroque', 'Romanticism', 'Impressionism', 
                           'Expressionism', 'Cubism', 'Surrealism', 'Abstract', 'Contemporary'],
                'color_intensity': [0.6, 0.8, 0.7, 0.9, 0.8, 0.5, 0.7, 0.4, 0.6],
                'emotional_intensity': [0.5, 0.8, 0.9, 0.7, 0.9, 0.6, 0.8, 0.7, 0.5],
                'modernity_score': [0.1, 0.2, 0.4, 0.7, 0.6, 0.8, 0.8, 0.9, 1.0],
                'complexity': [0.9, 0.8, 0.7, 0.6, 0.7, 0.8, 0.8, 0.6, 0.5]
            }
            return pd.DataFrame(art_data)
        except Exception as e:
            logger.error(f"Error cargando datos externos: {e}")
            return pd.DataFrame()