// src/pages/MyWorks.jsx
import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import productService from '../services/productService';
import backIcon       from '../assets/back-icon.png';

const MyWorks = () => {
    const [works, setWorks]         = useState([]);
    const [loading, setLoading]     = useState(true);
    const [error, setError]         = useState(null);
    const [page, setPage]           = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const navigate = useNavigate();

    useEffect(() => {
        const role = localStorage.getItem('userRole');
        const sellerId = localStorage.getItem('userId');
        if (role !== 'SELLER' || !sellerId) {
            navigate('/products', { replace: true });
            return;
        }
        setLoading(true);
        productService.getProductsBySeller(sellerId, page, 9)
            .then(({ items, totalPages }) => {
                setWorks(items);
                setTotalPages(totalPages);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [page, navigate]);

    if (loading) return <p className="text-center mt-10">Cargando tus obras…</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">{error}</p>;

    return (
        <div className="relative max-w-7xl mx-auto p-6">
            {/* Volver al menú */}
            <Link to="/seller/menu" className="absolute top-6 left-6">
                <img src={backIcon} alt="Volver" className="w-8 h-8 hover:opacity-80 transition" />
            </Link>

            <h2 className="text-3xl font-semibold mb-8 text-center">Mis Obras</h2>
            {works.length === 0 ? (
                <p className="text-center text-gray-600">Aún no tienes obras publicadas.</p>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                    {works.map(work => (
                        <div key={work.id} className="border rounded-xl p-4 shadow-md">
                            <img
                                src={work.image || 'https://via.placeholder.com/300x200'}
                                alt={work.name}
                                className="w-full h-48 object-cover rounded-lg"
                            />
                            <h3 className="mt-4 font-semibold text-lg">{work.name}</h3>
                            <p className="text-gray-500 mt-1">${work.price.toFixed(2)}</p>
                        </div>
                    ))}
                </div>
            )}

            {/* Paginación */}
            {totalPages > 1 && (
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
            )}
        </div>
    );
};

export default MyWorks;
