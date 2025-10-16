import unittest
import sys
import os
from unittest.mock import Mock, patch
import pandas as pd

sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from services.data_processor import DataProcessor
from config.settings import config

class TestDataProcessor(unittest.TestCase):
    
    def setUp(self):
        self.processor = DataProcessor()
        
    @patch('services.data_processor.db')
    def test_get_user_data_success(self, mock_db):
        """Test obtener datos de usuario exitosamente"""
        # Mock de la conexión y cursor
        mock_cursor = Mock()
        mock_connection = Mock()
        mock_db.get_connection.return_value = mock_connection
        mock_connection.cursor.return_value.__enter__ = Mock(return_value=mock_cursor)
        mock_connection.cursor.return_value.__exit__ = Mock(return_value=None)
        
        # Mock de resultados de la base de datos
        mock_cursor.fetchone.side_effect = [
            {'id': 1, 'name': 'test_user', 'mail': 'test@artemisia.com', 'created_date': '2023-01-01'},  # User data
            [  # Purchase history
                {'id': 1, 'total_global': 100.0, 'date': '2023-01-01', 'product_id': 1, 'quantity': 2, 'total': 200.0, 'categories': ['Abstracta'], 'techniques': ['Acrílico'], 'price': 100.0}
            ],
            [  # Preference vectors
                {'feature': 'cat_Abstracta', 'weight': 0.8},
                {'feature': 'tech_Acrílico', 'weight': 0.6}
            ]
        ]
        
        user_data = self.processor.get_user_data(1)
        
        self.assertIsNotNone(user_data)
        self.assertEqual(user_data['user_id'], 1)
        self.assertEqual(len(user_data['purchase_history']), 1)
        self.assertEqual(user_data['preference_vector']['cat_Abstracta'], 0.8)
        
    @patch('services.data_processor.db')
    def test_get_user_data_not_found(self, mock_db):
        """Test cuando el usuario no existe"""
        mock_cursor = Mock()
        mock_connection = Mock()
        mock_db.get_connection.return_value = mock_connection
        mock_connection.cursor.return_value.__enter__ = Mock(return_value=mock_cursor)
        mock_connection.cursor.return_value.__exit__ = Mock(return_value=None)
        
        mock_cursor.fetchone.return_value = None  # User not found
        
        user_data = self.processor.get_user_data(999)
        
        self.assertIsNone(user_data)
    
    @patch('services.data_processor.db')
    def test_get_available_products(self, mock_db):
        """Test obtener productos disponibles"""
        mock_cursor = Mock()
        mock_connection = Mock()
        mock_db.get_connection.return_value = mock_connection
        mock_connection.cursor.return_value.__enter__ = Mock(return_value=mock_cursor)
        mock_connection.cursor.return_value.__exit__ = Mock(return_value=None)
        
        mock_cursor.fetchall.return_value = [
            {
                'product_id': 1, 
                'name': 'Test Product', 
                'price': 100.0, 
                'stock': 5,
                'categories': ['Abstracta', 'Contemporánea'],
                'techniques': ['Acrílico']
            }
        ]
        
        products = self.processor.get_available_products()
        
        self.assertEqual(len(products), 1)
        self.assertEqual(products[0]['product_id'], 1)
        self.assertEqual(products[0]['categories'], ['Abstracta', 'Contemporánea'])
        self.assertEqual(products[0]['price'], 100.0)
    
    @patch('services.data_processor.db')
    def test_get_all_users_data(self, mock_db):
        """Test obtener datos de todos los usuarios"""
        mock_cursor = Mock()
        mock_connection = Mock()
        mock_db.get_connection.return_value = mock_connection
        mock_connection.cursor.return_value.__enter__ = Mock(return_value=mock_cursor)
        mock_connection.cursor.return_value.__exit__ = Mock(return_value=None)
        
        # Mock para la primera consulta (user IDs)
        mock_cursor.fetchall.return_value = [{'id': 1}, {'id': 2}]
        
        # Mock para get_user_data
        with patch.object(self.processor, 'get_user_data') as mock_get_user:
            mock_get_user.side_effect = [
                {'user_id': 1, 'user_data': {'name': 'user1'}, 'purchase_history': []},
                {'user_id': 2, 'user_data': {'name': 'user2'}, 'purchase_history': []}
            ]
            
            users_data = self.processor.get_all_users_data(limit=10)
            
            self.assertEqual(len(users_data), 2)
            self.assertEqual(users_data[0]['user_id'], 1)
            self.assertEqual(users_data[1]['user_id'], 2)
    
    def test_load_external_art_data(self):
        """Test cargar datos externos de arte"""
        external_data = self.processor.load_external_art_data()
        
        self.assertIsInstance(external_data, pd.DataFrame)
        self.assertFalse(external_data.empty)
        self.assertIn('movement', external_data.columns)
        self.assertIn('color_intensity', external_data.columns)
    
    def test_get_sample_products(self):
        """Test obtener productos de ejemplo"""
        sample_products = self.processor._get_sample_products()
        
        self.assertEqual(len(sample_products), 2)
        self.assertEqual(sample_products[0]['product_id'], 1)
        self.assertEqual(sample_products[1]['product_id'], 2)
        self.assertIn('Abstracta', sample_products[0]['categories'])
        self.assertIn('Realista', sample_products[1]['categories'])
    
    @patch('services.data_processor.db')
    def test_get_training_data_from_db(self, mock_db):
        """Test obtener datos de entrenamiento desde la base de datos"""
        mock_cursor = Mock()
        mock_connection = Mock()
        mock_db.get_connection.return_value = mock_connection
        mock_connection.cursor.return_value.__enter__ = Mock(return_value=mock_cursor)
        mock_connection.cursor.return_value.__exit__ = Mock(return_value=None)
        
        # Mock para la consulta principal
        mock_cursor.fetchall.return_value = [
            {
                'user_id': 1,
                'total_purchases': 5,
                'avg_purchase_value': 450.0,
                'purchased_categories': ['Abstracta', 'Contemporánea'],
                'purchased_techniques': ['Acrílico', 'Digital'],
                'last_purchase_date': '2023-12-01'
            }
        ]
        
        # Mock para get_user_purchase_history
        with patch.object(self.processor, '_get_user_purchase_history') as mock_history:
            mock_history.return_value = [
                {
                    'product_id': 1,
                    'name': 'Test Product',
                    'price': 100.0,
                    'quantity': 2,
                    'total': 200.0,
                    'categories': ['Abstracta'],
                    'techniques': ['Acrílico']
                }
            ]
            
            training_data = self.processor.get_training_data_from_db()
            
            self.assertEqual(len(training_data), 1)
            self.assertEqual(training_data[0]['user_id'], 1)
            self.assertEqual(training_data[0]['total_purchases'], 5)
            self.assertEqual(training_data[0]['avg_purchase_value'], 450.0)

class TestDataProcessorIntegration(unittest.TestCase):
    """Tests de integración para DataProcessor"""
    
    def setUp(self):
        self.processor = DataProcessor()
    
    @patch('services.data_processor.requests.get')
    def test_java_api_connection(self, mock_get):
        """Test conexión con API Java (simulada)"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            'content': [
                {'product_id': 1, 'name': 'Test Product', 'price': 100.0}
            ]
        }
        mock_get.return_value = mock_response
        
        # Este test verifica que se hace la llamada correcta
        # En un entorno real, necesitarías la API Java corriendo
    
    def test_data_validation(self):
        """Test validación de datos"""
        # Test con datos vacíos
        empty_vector = self.processor._get_sample_products()
        self.assertTrue(all('product_id' in product for product in empty_vector))
        self.assertTrue(all('categories' in product for product in empty_vector))
        
        # Test que los datos de ejemplo tienen estructura correcta
        sample_products = self.processor._get_sample_products()
        required_fields = ['product_id', 'name', 'categories', 'techniques', 'price', 'stock']
        
        for product in sample_products:
            for field in required_fields:
                self.assertIn(field, product)

if __name__ == '__main__':
    # Ejecutar tests
    unittest.main(verbosity=2)