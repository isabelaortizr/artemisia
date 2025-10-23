import numpy as np
from sklearn.cluster import DBSCAN
from sklearn.metrics import silhouette_score
import logging
from typing import Dict, List, Any

logger = logging.getLogger(__name__)

class UserClusteringModel:
    def __init__(self):
        self.cluster_labels = {}
        self.cluster_profiles = {}
        
    def find_user_segments(self, user_vectors: Dict[int, Dict[str, float]]) -> Dict[int, int]:
        """Encuentra segmentos de usuarios usando DBSCAN"""
        try:
            if len(user_vectors) < 10:
                logger.warning("Insuficientes usuarios para clustering")
                return {}
            
            # Convertir a matriz
            user_ids = list(user_vectors.keys())
            features = list(user_vectors.values())[0].keys()
            X = np.array([[vec.get(f, 0) for f in features] for vec in user_vectors.values()])
            
            # DBSCAN para encontrar clusters naturales
            clustering = DBSCAN(eps=0.5, min_samples=3).fit(X)
            labels = clustering.labels_
            
            # Asignar labels
            for user_id, label in zip(user_ids, labels):
                self.cluster_labels[user_id] = label
            
            # Crear perfiles de cluster
            self._build_cluster_profiles(user_vectors)
            
            logger.info(f"✅ Segmentación completada: {len(set(labels))} clusters encontrados")
            return self.cluster_labels
            
        except Exception as e:
            logger.error(f"Error en segmentación: {e}")
            return {}
    
    def _build_cluster_profiles(self, user_vectors: Dict[int, Dict[str, float]]):
        """Construye perfiles descriptivos para cada cluster"""
        clusters = {}
        
        for user_id, cluster_id in self.cluster_labels.items():
            if cluster_id not in clusters:
                clusters[cluster_id] = []
            clusters[cluster_id].append(user_vectors[user_id])
        
        for cluster_id, vectors in clusters.items():
            if cluster_id == -1:  # Noise
                continue
                
            # Calcular promedios por característica
            avg_vector = {}
            for feature in vectors[0].keys():
                values = [vec[feature] for vec in vectors if feature in vec]
                avg_vector[feature] = np.mean(values) if values else 0
            
            self.cluster_profiles[cluster_id] = avg_vector
        
        logger.info(f"📊 Perfiles de cluster construidos: {len(self.cluster_profiles)}")