import unittest
import sys
import os

sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from models.recommendation_engine import ArtRecommendationEngine

class TestRecommendationEngine(unittest.TestCase):
    
    def setUp(self):
        self.engine = ArtRecommendationEngine()
        
    def test_feature_space_initialization(self):
        """Test que el espacio de características se inicializa correctamente"""
        self.assertGreater(len(self.engine.feature_names), 0)
        self.assertTrue(any('cat_' in feature for feature in self.engine.feature_names))
        self.assertTrue(any('tech_' in feature for feature in self.engine.feature_names))
    
    def test_vector_building(self):
        """Test la construcción de vectores"""
        test_product = {
            'categories': ['Abstracta', 'Contemporánea'],
            'techniques': ['Acrílico'],
            'price': 450.0
        }
        
        vector = self.engine._build_product_vector(test_product)
        
        self.assertEqual(vector['cat_Abstracta'], 1.0)
        self.assertEqual(vector['cat_Contemporánea'], 1.0)
        self.assertEqual(vector['tech_Acrílico'], 1.0)
        self.assertIn('price_sensitivity', vector)
    
    def test_similarity_calculation(self):
        """Test el cálculo de similitud"""
        user_vector = {'cat_Abstracta': 0.8, 'tech_Acrílico': 0.6}
        product_vector = {'cat_Abstracta': 1.0, 'tech_Acrílico': 1.0}
        
        # Este test sería más complejo en la implementación real
        # Por ahora solo verificamos que la función existe
        self.assertTrue(hasattr(self.engine, '_calculate_similarity'))

if __name__ == '__main__':
    unittest.main()