import numpy as np
from typing import Dict, List, Any
import logging
from config.settings import config

logger = logging.getLogger(__name__)

class VectorBuilder:
    def __init__(self):
        self.feature_names = []
        self._initialize_features()
    
    def _initialize_features(self):
        """Inicializa todas las características del espacio vectorial"""
        categories_features = [f'cat_{cat}' for cat in config.CATEGORIES]
        techniques_features = [f'tech_{tech}' for tech in config.TECHNIQUES]
        behavioral_features = [
            'price_sensitivity', 'style_preference', 'purchase_frequency',
            'avg_purchase_value', 'category_diversity', 'technique_exploration',
            'modern_traditional', 'color_preference', 'complexity_preference'
        ]
        
        self.feature_names = categories_features + techniques_features + behavioral_features
        logger.info(f"🔧 Vector builder inicializado con {len(self.feature_names)} características")
    
    def build_user_vector_from_history(self, purchase_history: List[Dict]) -> Dict[str, float]:
        """Construye vector de usuario basado en historial de compras"""
        vector = {feature: 0.0 for feature in self.feature_names}
        
        if not purchase_history:
            return self._get_default_vector()
        
        total_spent = 0
        all_categories = set()
        all_techniques = set()
        purchase_count = len(purchase_history)
        
        for purchase in purchase_history:
            total_spent += purchase.get('total', 0)
            
            # Procesar categorías
            categories = purchase.get('categories', [])
            for category in categories:
                if category in config.CATEGORIES:
                    vector[f'cat_{category}'] += purchase.get('quantity', 1) * 0.1
                    all_categories.add(category)
            
            # Procesar técnicas
            techniques = purchase.get('techniques', [])
            for technique in techniques:
                if technique in config.TECHNIQUES:
                    vector[f'tech_{technique}'] += purchase.get('quantity', 1) * 0.1
                    all_techniques.add(technique)
        
        # Añadir características de comportamiento
        self._add_behavioral_features(vector, purchase_history, all_categories, all_techniques, total_spent, purchase_count)
        
        # Normalizar vector
        vector = self._normalize_vector(vector)
        
        return vector
    
    def _add_behavioral_features(self, vector: Dict[str, float], purchases: List[Dict], 
                               all_categories: set, all_techniques: set, total_spent: float, purchase_count: int):
        """Añade características de comportamiento al vector"""
        # Frecuencia y valor de compra
        if purchase_count > 0:
            avg_purchase = total_spent / purchase_count
            vector['purchase_frequency'] = min(purchase_count / 20.0, 1.0)
            vector['avg_purchase_value'] = min(avg_purchase / 2000.0, 1.0)
            
            # Sensibilidad al precio
            if avg_purchase < 200:
                vector['price_sensitivity'] = 0.9
            elif avg_purchase < 500:
                vector['price_sensitivity'] = 0.6
            elif avg_purchase < 1000:
                vector['price_sensitivity'] = 0.3
            else:
                vector['price_sensitivity'] = 0.1
        
        # Diversidad
        vector['category_diversity'] = len(all_categories) / len(config.CATEGORIES)
        vector['technique_exploration'] = len(all_techniques) / len(config.TECHNIQUES)
        
        # Preferencia de estilo
        modern_cats = {'Abstracta', 'Conceptual', 'Contemporánea', 'Surrealista'}
        traditional_cats = {'Realista', 'Religiosa', 'Histórica', 'Impresionista'}
        
        modern_count = len(all_categories.intersection(modern_cats))
        traditional_count = len(all_categories.intersection(traditional_cats))
        
        if modern_count + traditional_count > 0:
            vector['modern_traditional'] = modern_count / (modern_count + traditional_count)
        else:
            vector['modern_traditional'] = 0.5
        
        # Preferencia de color (simulada basada en técnicas)
        bright_tech = {'Acrílico', 'Acuarela', 'Spray', 'Digital'}
        muted_tech = {'Óleo', 'Temple', 'Fresco'}
        
        bright_count = len(all_techniques.intersection(bright_tech))
        muted_count = len(all_techniques.intersection(muted_tech))
        
        if bright_count + muted_count > 0:
            vector['color_preference'] = bright_count / (bright_count + muted_count)
        else:
            vector['color_preference'] = 0.5
    
    def build_product_vector(self, product: Dict) -> Dict[str, float]:
        """Construye vector de características para producto"""
        vector = {feature: 0.0 for feature in self.feature_names}
        
        # Características de categoría
        for category in product.get('categories', []):
            if category in config.CATEGORIES:
                vector[f'cat_{category}'] = 1.0
        
        # Características de técnica
        for technique in product.get('techniques', []):
            if technique in config.TECHNIQUES:
                vector[f'tech_{technique}'] = 1.0
        
        # Características derivadas del producto
        price = product.get('price', 0)
        vector['price_sensitivity'] = self._map_price_to_feature(price)
        vector['modern_traditional'] = self._calculate_product_style(product)
        
        return vector
    
    def _map_price_to_feature(self, price: float) -> float:
        """Mapea precio a característica de sensibilidad"""
        if price <= 0:
            return 0.5
            
        if price < 100:
            return 0.9  # Muy sensible al precio
        elif price < 300:
            return 0.7
        elif price < 600:
            return 0.5
        elif price < 1000:
            return 0.3
        else:
            return 0.1  # Poco sensible al precio
    
    def _calculate_product_style(self, product: Dict) -> float:
        """Calcula score de estilo moderno/tradicional para producto"""
        modern_cats = {'Abstracta', 'Conceptual', 'Contemporánea', 'Surrealista'}
        traditional_cats = {'Realista', 'Religiosa', 'Histórica', 'Impresionista'}
        
        product_cats = set(product.get('categories', []))
        modern_count = len(product_cats.intersection(modern_cats))
        traditional_count = len(product_cats.intersection(traditional_cats))
        
        if modern_count + traditional_count == 0:
            return 0.5
            
        return modern_count / (modern_count + traditional_count)
    
    def _normalize_vector(self, vector: Dict[str, float]) -> Dict[str, float]:
        """Normaliza el vector para que la suma sea 1"""
        total = sum(vector.values())
        if total > 0:
            return {k: v / total for k, v in vector.items()}
        return vector
    
    def _get_default_vector(self) -> Dict[str, float]:
        """Vector por defecto para nuevos usuarios"""
        vector = {feature: 0.01 for feature in self.feature_names}  # Pequeños valores por defecto
        return self._normalize_vector(vector)


# --- Compatibility helpers (numpy-based) ---------------------------------
_VB = VectorBuilder()
FEATURE_NAMES = _VB.feature_names
DIM = len(FEATURE_NAMES)


def dict_to_array(d: Dict[str, float]) -> "np.ndarray":
    """Convert a feature dict (name->value) to a numpy array following FEATURE_NAMES order."""
    import numpy as _np
    arr = _np.zeros(DIM, dtype=float)
    for i, fname in enumerate(FEATURE_NAMES):
        arr[i] = float(d.get(fname, 0.0))
    return arr


def product_to_vector(product: Dict) -> "np.ndarray":
    """Wrapper: build product vector (numpy array) from product dict."""
    pdict = _VB.build_product_vector(product)
    return dict_to_array(pdict)


def l2_normalize(x: "np.ndarray") -> "np.ndarray":
    import numpy as _np
    norm = _np.linalg.norm(x)
    if norm == 0:
        return x
    return x / norm


def update_user_accum(preference_accum: "np.ndarray", weight_sum: float,
                      product_vec: "np.ndarray", w: float):
    """Update running accumulator and weight sum and return normalized vector.

    Returns (new_accum, new_weight_sum, normalized_vector)
    """
    import numpy as _np
    if preference_accum is None:
        preference_accum = _np.zeros(DIM, dtype=float)
        weight_sum = 0.0

    new_accum = preference_accum + (w * product_vec)
    new_weight_sum = weight_sum + w

    if new_weight_sum == 0:
        normalized = l2_normalize(new_accum)
    else:
        raw = new_accum / new_weight_sum
        normalized = l2_normalize(raw)

    return new_accum, new_weight_sum, normalized
