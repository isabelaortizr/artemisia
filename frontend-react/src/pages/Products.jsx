// src/pages/Products.jsx
import { useState, useEffect } from 'react';
import productService from '../services/productService';
import { Link } from 'react-router-dom';

const Products = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading]   = useState(true);
    const [error, setError]       = useState(null);

    useEffect(() => {
        productService.getProducts()
            .then(data => {
                setProducts(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message || 'Error al cargar productos');
                setLoading(false);
            });
    }, []);

    if (loading) return <p className="text-center mt-10">Cargando productos...</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">{error}</p>;

    return (
        <div className="max-w-7xl mx-auto p-6">
            <h2 className="text-3xl font-semibold mb-8 text-center">Catálogo de Productos</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                {products.map(prod => (
                    <div
                        key={prod.id}
                        className="border rounded-xl p-4 shadow-md hover:shadow-xl transition"
                    >
                        <img
                            src={prod.imageUrl || 'https://via.placeholder.com/300x200'}
                            alt={prod.name}
                            className="w-full h-48 object-cover rounded-lg"
                        />
                        <h3 className="mt-4 font-semibold text-lg">{prod.name}</h3>
                        <p className="text-gray-500 mt-1">${prod.price.toFixed(2)}</p>
                        <Link
                            to={`/products/${prod.id}`}
                            className="mt-4 block w-full text-center bg-indigo-600 text-white py-2 rounded hover:bg-indigo-700 transition"
                        >
                            Ver detalle
                        </Link>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Products;
