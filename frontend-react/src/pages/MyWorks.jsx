// src/pages/MyWorks.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, Link }        from 'react-router-dom';
import productService               from '../services/productService';
import WorkCard                     from '../componentes/WorkCard';
import EditModal                    from '../componentes/EditModal';
import backIcon                     from '../assets/back-icon.png';

const MyWorks = () => {
    const [works,    setWorks]    = useState([]);
    const [loading,  setLoading]  = useState(true);
    const [error,    setError]    = useState(null);
    const [page,     setPage]     = useState(0);
    const [totalPages, setTotal]  = useState(0);
    const [editingWork, setEdit]  = useState(null);
    const navigate = useNavigate();

    // 1) fetch inicial
    useEffect(() => {
        const role     = localStorage.getItem('userRole');
        const sellerId = localStorage.getItem('userId');
        if (role !== 'SELLER' || !sellerId) {
            navigate('/products', { replace: true });
            return;
        }
        setLoading(true);
        productService
            .getProductsBySeller(sellerId, page, 9)
            .then(({ items, totalPages }) => {
                // normaliza cada item: si viene productId ponlo en id
                const normalized = items.map(item => ({
                    ...item,
                    id: item.id ?? item.productId
                }));
                setWorks(normalized);
                setTotal(totalPages);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [page, navigate]);

    if (loading) return <p className="text-center mt-10">Cargando tus obras…</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">Error: {error}</p>;

    return (
        <div className="relative max-w-7xl mx-auto p-6">
            {/* Volver */}
            <Link to="/menu" className="absolute top-6 left-6">
                <img src={backIcon} alt="Volver" className="w-8 h-8" />
            </Link>

            <h2 className="text-3xl font-semibold mb-8 text-center">Mis Obras</h2>

            {works.length === 0
                ? <p className="text-center text-gray-600">No tienes obras aún.</p>
                : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                        {works.map(work => (
                            <WorkCard
                                key={work.id}
                                work={work}
                                onEdit={() => setEdit(work)}
                            />
                        ))}
                    </div>
                )
            }

            {/* Paginación */}
            {totalPages > 1 && (
                <div className="flex justify-center items-center space-x-4 mt-8">
                    <button
                        onClick={() => setPage(p => Math.max(p - 1, 0))}
                        disabled={page === 0}
                        className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
                    >Anterior</button>
                    <span>Página {page + 1} de {totalPages}</span>
                    <button
                        onClick={() => setPage(p => Math.min(p + 1, totalPages - 1))}
                        disabled={page + 1 >= totalPages}
                        className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
                    >Siguiente</button>
                </div>
            )}

            {/* Modal de edición */}
            {editingWork &&
                <EditModal
                    work={editingWork}
                    onClose={() => setEdit(null)}
                    onSave={(updated) => {
                        const u = { ...updated, id: updated.id ?? updated.productId };
                        setWorks(ws => ws.map(w => w.id===u.id ? u : w));
                        setEdit(null);
                    }}
                />
            }
        </div>
    );
};

export default MyWorks;
