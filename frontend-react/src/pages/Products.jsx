// src/pages/Products.jsx
import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import notaVentaService from '../services/notaVentaService';
import productService   from '../services/productService';
import cartIcon         from '../assets/cart-icon.png';
import logoutIcon       from '../assets/logout-icon.png'; // tu icono de logout
import authService      from '../services/authService';

const Products = () => {
    const [products, setProducts]     = useState([]);
    const [loading, setLoading]       = useState(true);
    const [error, setError]           = useState(null);
    const [expanded, setExpanded]     = useState([]);
    const [page, setPage]             = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [toastMessage, setToastMessage] = useState('');
    const navigate = useNavigate();


    useEffect(() => {
        const userId = localStorage.getItem("userId");
        if (!userId) {
            navigate("/login");
            return;
        }
        setLoading(true);
        productService.getProducts(page, 9)
            .then(({ items, totalPages }) => {
                setProducts(items);
                setTotalPages(totalPages);
            })
            .catch(err => setError(err.message || 'Error al cargar productos'))
            .finally(() => setLoading(false));
    }, [page]);

    const handleLogout = () => {
        authService.logout();
        navigate('/login');
    };

    const toggleExpand = (id) => {
        setExpanded(prev =>
            prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
        );
    };

    const handleAddToCart = async (prod) => {
        try {
            await notaVentaService.addToCart({
                productId: prod.productId,
                quantity:  1
            });
            setToastMessage(`"${prod.name}" añadido al carrito`);
            setTimeout(() => setToastMessage(''), 3000);
        } catch (err) {
            console.error(err);
            alert(err.message);
        }
    };

    if (loading) return <p className="text-center mt-10">Cargando productos...</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">{error}</p>;

    return (
        <div className="relative max-w-7xl mx-auto p-6">

            {/* Botón logout */}
            <button
                onClick={handleLogout}
                className="absolute top-6 left-6">
                <img
                    src={logoutIcon}
                    alt="Cerrar sesión"
                    className="w-10 h-10 hover:opacity-80 transition"
                />
            </button>

            {/* Botón carrito */}
            <Link to="/cart" className="absolute top-6 right-6">
                <img
                    src={cartIcon}
                    alt="Ir al carrito"
                    className="w-10 h-10 hover:opacity-80 transition"
                />
            </Link>

            {/* Toast centradо abajo */}
            {toastMessage && (
                <div className="fixed bottom-6 left-1/2 transform -translate-x-1/2 bg-green-600 text-white px-4 py-2 rounded shadow-lg z-50">
                    {toastMessage}
                </div>
            )}

            <h2 className="text-3xl font-semibold mb-8 text-center">Catálogo de Productos</h2>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                {products.map(prod => {
                    const isExpanded = expanded.includes(prod.productId);
                    return (
                        <div
                            key={prod.productId}
                            className={`border rounded-xl p-4 shadow-md transition-transform duration-200 ${
                                isExpanded ? 'scale-105' : 'hover:shadow-xl'
                            }`}
                        >
                            <img
                                src={prod.imageUrl || 'https://via.placeholder.com/300x200'}
                                alt={prod.name}
                                className="w-full h-48 object-cover rounded-lg"
                            />
                            <h3 className="mt-4 font-semibold text-lg">{prod.name}</h3>
                            <p className="text-gray-500 mt-1">${prod.price.toFixed(2)}</p>

                            <button
                                onClick={() => toggleExpand(prod.productId)}
                                className="mt-4 text-indigo-600 hover:underline"
                            >
                                {isExpanded ? 'Ver menos ▲' : 'Ver más ▼'}
                            </button>

                            {isExpanded && (
                                <div className="mt-4 text-sm text-gray-700 space-y-2">
                                    <p><strong>Técnica:</strong> {prod.technique}</p>
                                    {prod.category    && <p><strong>Categoría:</strong> {prod.category}</p>}
                                    {prod.description && <p><strong>Descripción:</strong> {prod.description}</p>}
                                </div>
                            )}

                            <button
                                onClick={() => handleAddToCart(prod)}
                                className="mt-6 w-full bg-indigo-600 text-white py-2 rounded hover:bg-indigo-700 transition"
                            >
                                Añadir al carrito
                            </button>
                        </div>
                    );
                })}
            </div>

            {/* Paginación */}
            <div className="flex justify-center items-center space-x-4 mt-8">
                <button
                    onClick={() => setPage(p => Math.max(p - 1, 0))}
                    disabled={page === 0}
                    className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
                >
                    Anterior
                </button>
                <span>Página {page + 1} de {totalPages}</span>
                <button
                    onClick={() => setPage(p => Math.min(p + 1, totalPages - 1))}
                    disabled={page + 1 >= totalPages}
                    className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
                >
                    Siguiente
                </button>
            </div>
        </div>
    );
};

export default Products;
