// src/pages/MyWorks.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import productService from '../services/productService';
import WorkCard from '../components/WorkCard';
import EditModal from '../components/EditModal';
import AddArt from './AddArt';

const MyWorks = () => {
    const [works, setWorks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotal] = useState(0);
    const [editing, setEditing] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);
    const [expandedWorkId, setExpandedWorkId] = useState(null); // Nueva state para controlar qué tarjeta está expandida
    const navigate = useNavigate();
    const sellerId = localStorage.getItem('userId');

    const normalize = items =>
        items.map(item => ({ ...item, id: item.id ?? item.productId }));

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

    useEffect(() => {
        const role = localStorage.getItem('userRole');
        if (role !== 'SELLER' || !sellerId) {
            navigate('/products', { replace: true });
            return;
        }
        loadWorks();
    }, [loadWorks, navigate, sellerId]);

    const handleAddSuccess = () => {
        setShowAddModal(false);
        loadWorks();
    };

    // Función para manejar la expansión/contracción
    const handleToggleExpand = (workId) => {
        setExpandedWorkId(prev => prev === workId ? null : workId);
    };

    if (loading) return <p className="text-center mt-10">Cargando tus obras…</p>;
    if (error) return <p className="text-center mt-10 text-red-500">Error: {error}</p>;

    return (
        <div className="relative max-w-7xl mx-auto p-6">
            <h2 className="text-3xl font-semibold mb-8 text-center">My pieces</h2>

            {/* Botón para agregar nueva obra */}
            <div className="mb-8 flex justify-center">
                <button
                    onClick={() => setShowAddModal(true)}
                    className="bg-white text-black hover:bg-gray-100 font-semibold py-3 px-6 rounded-xl border border-white/20 transition duration-200 flex items-center gap-2"
                >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                    Add a new piece
                </button>
            </div>

            {/* Grid de obras */}
            {works.length === 0 ? (
                <div className="text-center py-12">
                    <p className="text-white text-lg mb-4">You don't have pieces yet</p>
                    <p className="text-gray-400">Click "Add a new piece" to get started</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                    {works.map(w => (
                        <WorkCard
                            key={w.id}
                            work={w}
                            isExpanded={expandedWorkId === w.id} // Pasamos si está expandida
                            onToggleExpand={() => handleToggleExpand(w.id)} // Pasamos la función para toggle
                            onEdit={() => setEditing(w)}
                        />
                    ))}
                </div>
            )}

            {/* Paginación */}
            {totalPages > 1 && (
                <div className="flex justify-center items-center space-x-4 mt-8">
                    <button onClick={() => setPage(p => Math.max(p-1,0))}
                            disabled={page===0}
                            className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50">
                        Previous
                    </button>
                    <span className="text-white">Página {page+1} de {totalPages}</span>
                    <button onClick={() => setPage(p => Math.min(p+1, totalPages-1))}
                            disabled={page+1 >= totalPages}
                            className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50">
                        Next
                    </button>
                </div>
            )}

            {/* Modal para agregar nueva obra */}
            {showAddModal && (
                <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto relative">
                        <button
                            className="absolute top-4 right-4 text-black text-2xl hover:text-red-600 z-10 bg-white rounded-full w-8 h-8 flex items-center justify-center shadow-md"
                            onClick={() => setShowAddModal(false)}
                        >
                            ×
                        </button>
                        <AddArt
                            embedded
                            onSuccess={handleAddSuccess}
                            onCancel={() => setShowAddModal(false)}
                        />
                    </div>
                </div>
            )}

            {/* Modal para editar obra */}
            {editing && (
                <EditModal
                    work={editing}
                    onClose={() => setEditing(null)}
                    onSave={async updated => {
                        setEditing(null);
                        await loadWorks();
                    }}
                />
            )}
        </div>
    );
};

export default MyWorks;