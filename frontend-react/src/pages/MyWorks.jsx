// src/pages/MyWorks.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import productService from '../services/productService';
import WorkCard       from '../components/WorkCard';
import EditModal      from '../components/EditModal';

const MyWorks = () => {
    const [works,      setWorks]    = useState([]);
    const [loading,    setLoading]  = useState(true);
    const [error,      setError]    = useState(null);
    const [page,       setPage]     = useState(0);
    const [totalPages, setTotal]    = useState(0);
    const [editing,    setEditing]  = useState(null);
    const navigate = useNavigate();
    const sellerId = localStorage.getItem('userId');

    const normalize = items =>
        items.map(item => ({ ...item, id: item.id ?? item.productId }));

    // 1) función de carga
    const loadWorks = useCallback(() => {
        setLoading(true);
        productService
            .getProductsBySeller(sellerId, page, 9)
            .then(({ items, totalPages }) => {
                setWorks(normalize(items));
                setTotal(totalPages);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [page, sellerId]);

    // 2) useEffect que dispara la carga
    useEffect(() => {
        const role = localStorage.getItem('userRole');
        if (role !== 'SELLER' || !sellerId) {
            navigate('/products', { replace: true });
            return;
        }
        loadWorks();
    }, [loadWorks, navigate, sellerId]);

    if (loading) return <p className="text-center mt-10">Cargando tus obras…</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">Error: {error}</p>;

    return (
        <div className="relative max-w-7xl mx-auto p-6">

            <h2 className="text-3xl font-semibold mb-8 text-center">My pieces</h2>

            {works.length === 0
                ? <p className="text-center text-white">You dont have pieces yet</p>
                : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                        {works.map(w => (
                            <WorkCard key={w.id} work={w} onEdit={() => setEditing(w)} />
                        ))}
                    </div>
                )
            }

            {totalPages > 1 && (
                <div className="flex justify-center items-center space-x-4 mt-8">
                    <button onClick={() => setPage(p => Math.max(p-1,0))}
                            disabled={page===0}
                            className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50">
                        Previous
                    </button>
                    <span>Página {page+1} de {totalPages}</span>
                    <button onClick={() => setPage(p => Math.min(p+1, totalPages-1))}
                            disabled={page+1 >= totalPages}
                            className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50">
                        Next
                    </button>
                </div>
            )}

            {editing && (
                <EditModal
                    work={editing}
                    onClose={() => setEditing(null)}
                    onSave={async updated => {
                        setEditing(null);
                        await loadWorks();       // <-- recargas la lista
                    }}
                />
            )}
        </div>
    );
};

export default MyWorks;
