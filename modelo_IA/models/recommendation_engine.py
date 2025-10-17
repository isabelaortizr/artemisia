import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler
import pandas as pd
import joblib
from typing import List, Dict, Any, Tuple
import os
import logging
from ..config.settings import config

logger = logging.getLogger(__name__)

class ArtRecommendationEngine:
    def __init__(self):
        self.user_vectors = {}
        self.product_vectors = {}
        self.user_clusters = {}
        self.kmeans_model = None
        self.pca_model = None
        self.scaler = StandardScaler()
        self.feature_names = []
        self.build_feature_space()
        
    def build_feature_space(self):
        """Construye el espacio de características completo"""
        # Características básicas
        categories_features = [f'cat_{cat}' for cat in config.CATEGORIES]
        techniques_features = [f'tech_{tech}' for tech in config.TECHNIQUES]
        
        # Características derivadas
        derived_features = [
            'price_sensitivity', 'style_preference', 'color_intensity', 
            'modern_traditional', 'complexity_preference', 'emotional_intensity',
            'purchase_frequency', 'avg_purchase_value', 'category_diversity',
            'technique_exploration'
        ]
        
        self.feature_names = categories_features + techniques_features + derived_features
        logger.info(f"🔧 Espacio de características construido: {len(self.feature_names)} dimensiones")
        
    def train(self, training_data: List[Dict[str, Any]]) -> bool:
        """Entrena el modelo con datos de usuarios"""
        try:
            logger.info(f"🏋️‍♂️ Iniciando entrenamiento con {len(training_data)} usuarios...")
            
            if len(training_data) < config.MIN_USERS_FOR_TRAINING:
                logger.warning(f"⚠️  Datos insuficientes. Mínimo requerido: {config.MIN_USERS_FOR_TRAINING}")
                return False
            
            # Construir matriz de usuarios
            user_matrix = []
            user_ids = []
            
            for user_data in training_data:
                user_id = user_data['user_id']
                user_vector = self._build_complete_user_vector(user_data)
                
                if user_vector:
                    vector_array = np.array([user_vector.get(feature, 0.0) for feature in self.feature_names])
                    user_matrix.append(vector_array)
                    user_ids.append(user_id)
                    self.user_vectors[user_id] = user_vector
            
            if not user_matrix:
                logger.error("❌ No se pudieron construir vectores de usuario")
                return False
                
            user_matrix = np.array(user_matrix)
            logger.info(f"📊 Matriz de entrenamiento: {user_matrix.shape}")
            
            # Escalar datos
            user_matrix_scaled = self.scaler.fit_transform(user_matrix)
            
            # Reducción de dimensionalidad con PCA
            n_components = min(10, len(user_matrix) - 1, len(self.feature_names))
            self.pca_model = PCA(n_components=n_components, random_state=42)
            user_matrix_reduced = self.pca_model.fit_transform(user_matrix_scaled)
            
            logger.info(f"📉 Varianza explicada por PCA: {self.pca_model.explained_variance_ratio_.sum():.3f}")
            
            # Clustering con K-means (tunable via env)
            n_clusters = min(config.NUM_CLUSTERS, len(user_matrix))
            self.kmeans_model = KMeans(
                n_clusters=n_clusters,
                random_state=42,
                n_init=config.KMEANS_N_INIT,
                max_iter=config.KMEANS_MAX_ITER
            )
            clusters = self.kmeans_model.fit_predict(user_matrix_reduced)
            
            # Asignar clusters a usuarios
            for user_id, cluster in zip(user_ids, clusters):
                self.user_clusters[user_id] = cluster
            
            # Estadísticas de clusters
            cluster_counts = {}
            for cluster in clusters:
                cluster_counts[cluster] = cluster_counts.get(cluster, 0) + 1
            
            logger.info(f"✅ Modelo entrenado exitosamente")
            logger.info(f"🎯 Clusters creados: {cluster_counts}")
            
            return True
            
        except Exception as e:
            logger.error(f"❌ Error en entrenamiento: {e}")
            return False
    
    def _build_complete_user_vector(self, user_data: Dict[str, Any]) -> Dict[str, float]:
        """Construye vector completo de usuario con características derivadas"""
        try:
            vector = {feature: 0.0 for feature in self.feature_names}
            base_vector = user_data.get('vector', {})
            purchase_history = user_data.get('purchase_history', [])
            
            # Características base del vector
            for feature, value in base_vector.items():
                if feature in vector:
                    vector[feature] = value
            
            # Características derivadas de comportamiento
            self._add_behavioral_features(vector, purchase_history, user_data)
            
            return vector
            
        except Exception as e:
            logger.error(f"Error construyendo vector de usuario: {e}")
            return {}
    
    def _add_behavioral_features(self, vector: Dict[str, float], purchases: List, user_data: Dict):
        """Añade características derivadas del comportamiento"""
        if not purchases:
            return
            
        # Frecuencia de compra
        total_purchases = len(purchases)
        vector['purchase_frequency'] = min(total_purchases / 10.0, 1.0)  # Normalizado
        
        # Valor promedio de compra
        total_value = sum(p.get('total', 0) for p in purchases)
        avg_value = total_value / total_purchases if total_purchases > 0 else 0
        vector['avg_purchase_value'] = min(avg_value / 1000.0, 1.0)  # Normalizado
        
        # Sensibilidad al precio
        if avg_value < 300:
            vector['price_sensitivity'] = 0.8  # Alto
        elif avg_value < 700:
            vector['price_sensitivity'] = 0.5  # Medio
        else:
            vector['price_sensitivity'] = 0.2  # Bajo
        
        # Diversidad de categorías y técnicas
        all_categories = set()
        all_techniques = set()
        
        for purchase in purchases:
            all_categories.update(purchase.get('categories', []))
            all_techniques.update(purchase.get('techniques', []))
        
        vector['category_diversity'] = len(all_categories) / len(config.CATEGORIES)
        vector['technique_exploration'] = len(all_techniques) / len(config.TECHNIQUES)
        
        # Preferencia moderno/tradicional
        modern_cats = {'Abstracta', 'Conceptual', 'Contemporánea', 'Surrealista'}
        traditional_cats = {'Realista', 'Religiosa', 'Histórica', 'Impresionista'}
        
        modern_count = len(all_categories.intersection(modern_cats))
        traditional_count = len(all_categories.intersection(traditional_cats))
        
        if modern_count + traditional_count > 0:
            vector['modern_traditional'] = modern_count / (modern_count + traditional_count)
        else:
            vector['modern_traditional'] = 0.5
    
    def get_recommendations(self, user_vector: Dict[str, float], products: List[Dict], top_n: int = 10) -> List[Dict]:
        """Obtiene recomendaciones personalizadas"""
        try:
            if not products:
                return []
                
            # Si no hay modelo entrenado, usar fallback
            if not self.kmeans_model:
                return self._get_fallback_recommendations(products, top_n)
            
            user_array = np.array([user_vector.get(feature, 0.0) for feature in self.feature_names]).reshape(1, -1)
            
            # Calcular similitud con cada producto
            scored_products = []
            for product in products:
                product_vector = self._build_product_vector(product)
                similarity = self._calculate_similarity(user_array, product_vector)
                
                # Añadir score de novedad para nuevos usuarios
                novelty_score = self._calculate_novelty_score(product, user_vector)
                final_score = 0.7 * similarity + 0.3 * novelty_score
                
                scored_products.append((product, final_score))
            
            # Ordenar y devolver top N
            scored_products.sort(key=lambda x: x[1], reverse=True)
            return [product for product, score in scored_products[:top_n]]
            
        except Exception as e:
            logger.error(f"Error en recomendaciones: {e}")
            return self._get_fallback_recommendations(products, top_n)
    
    def _calculate_similarity(self, user_array: np.ndarray, product_vector: Dict[str, float]) -> float:
        """Calcula similitud coseno entre usuario y producto"""
        try:
            product_array = np.array([product_vector.get(feature, 0.0) for feature in self.feature_names]).reshape(1, -1)
            similarity = cosine_similarity(user_array, product_array)[0][0]
            return max(0.0, min(1.0, similarity))
        except:
            return 0.0
    
    def _calculate_novelty_score(self, product: Dict, user_vector: Dict[str, float]) -> float:
        """Calcula score de novedad para evitar recomendar siempre lo mismo"""
        # Productos con categorías poco exploradas por el usuario tienen mayor novedad
        explored_categories = {cat.replace('cat_', '') for cat, weight in user_vector.items() 
                              if cat.startswith('cat_') and weight > 0.1}
        
        product_categories = set(product.get('categories', []))
        new_categories = product_categories - explored_categories
        
        if explored_categories:
            return len(new_categories) / len(product_categories) if product_categories else 0.5
        else:
            return 0.5  # Neutral para nuevos usuarios
    
    def _build_product_vector(self, product: Dict) -> Dict[str, float]:
        """Construye vector de características para producto"""
        vector = {feature: 0.0 for feature in self.feature_names}
        
        # Características de categoría
        for category in product.get('categories', []):
            vector[f'cat_{category}'] = 1.0
            
        # Características de técnica
        for technique in product.get('techniques', []):
            vector[f'tech_{technique}'] = 1.0
            
        # Características derivadas del producto
        price = product.get('price', 0)
        vector['price_sensitivity'] = self._map_price_to_sensitivity(price)
        vector['modern_traditional'] = self._calculate_style_score(product)
        
        return vector
    
    def _map_price_to_sensitivity(self, price: float) -> float:
        """Mapea precio a sensibilidad"""
        if price < 100: return 0.8
        elif price < 300: return 0.6
        elif price < 600: return 0.4
        else: return 0.2
    
    def _calculate_style_score(self, product: Dict) -> float:
        """Calcula score de estilo moderno/tradicional"""
        modern_cats = {'Abstracta', 'Conceptual', 'Contemporánea', 'Surrealista'}
        traditional_cats = {'Realista', 'Religiosa', 'Histórica', 'Impresionista'}
        
        product_cats = set(product.get('categories', []))
        modern_count = len(product_cats.intersection(modern_cats))
        traditional_count = len(product_cats.intersection(traditional_cats))
        
        if modern_count + traditional_count == 0:
            return 0.5
            
        return modern_count / (modern_count + traditional_count)
    
    def find_similar_users(self, user_id: int, limit: int = 5) -> List[int]:
        """Encuentra usuarios similares"""
        try:
            if user_id not in self.user_vectors:
                return []
                
            target_vector = self.user_vectors[user_id]
            target_array = np.array([target_vector.get(feature, 0.0) for feature in self.feature_names]).reshape(1, -1)
            
            similarities = []
            for other_user_id, other_vector in self.user_vectors.items():
                if other_user_id == user_id:
                    continue
                    
                other_array = np.array([other_vector.get(feature, 0.0) for feature in self.feature_names]).reshape(1, -1)
                similarity = cosine_similarity(target_array, other_array)[0][0]
                similarities.append((other_user_id, similarity))
            
            similarities.sort(key=lambda x: x[1], reverse=True)
            return [user_id for user_id, sim in similarities[:limit]]
            
        except Exception as e:
            logger.error(f"Error encontrando usuarios similares: {e}")
            return []
    
    def _get_fallback_recommendations(self, products: List[Dict], top_n: int) -> List[Dict]:
        """Recomendaciones de fallback"""
        # Estrategia 1: Productos más populares (por precio accesible y disponibilidad)
        scored_products = []
        for product in products:
            score = 0.0
            price = product.get('price', 0)
            stock = product.get('stock', 0)
            
            # Preferir productos con buen precio y stock
            if price > 0 and stock > 0:
                price_score = max(0, 1 - (price / 2000))  # Normalizar precio
                stock_score = min(1, stock / 10)  # Normalizar stock
                score = 0.6 * price_score + 0.4 * stock_score
                
            scored_products.append((product, score))
        
        scored_products.sort(key=lambda x: x[1], reverse=True)
        return [product for product, score in scored_products[:top_n]]
    
    def save_model(self, filepath: str):
        """Guarda el modelo entrenado"""
        try:
            os.makedirs(os.path.dirname(filepath), exist_ok=True)
            joblib.dump(self, filepath)
            abs_path = os.path.abspath(filepath)
            logger.info(f"💾 Modelo guardado en: {abs_path}")
        except Exception as e:
            logger.error(f"Error guardando modelo: {e}")
    
    @classmethod
    def load_model(cls, filepath: str) -> 'ArtRecommendationEngine':
        """Carga modelo desde archivo"""
        try:
            if os.path.exists(filepath):
                model = joblib.load(filepath)
                logger.info(f"📂 Modelo cargado desde: {filepath}")
                return model
            else:
                logger.warning(f"Modelo no encontrado en: {filepath}")
                return cls()
        except Exception as e:
            logger.error(f"Error cargando modelo: {e}")
            return cls()