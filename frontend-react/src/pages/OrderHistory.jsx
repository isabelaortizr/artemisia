// src/pages/OrderHistory.jsx
import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import { assets } from '../assets/assets';
import notaVentaService from '../services/notaVentaService';

export default function OrderHistory() {
    const [expandedId, setExpandedId] = useState(null);
    const [orders,     setOrders]     = useState([]);
    const [loading,    setLoading]    = useState(true);
    const [error,      setError]      = useState(null);

    useEffect(() => {
        const userId = Number(localStorage.getItem('userId'));
        setLoading(true);
        notaVentaService.getHistory(userId, 0, 20)
            .then(page => {
                setOrders(page.content || []);
            })
            .catch(err => {
                setError(err.message || 'Error al cargar órdenes');
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    const toggleExpand = id => {
        setExpandedId(prev => (prev === id ? null : id));
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <p className="text-white">Cargando órdenes…</p>
            </div>
        );
    }
    if (error) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <p className="text-red-500">{error}</p>
            </div>
        );
    }

    return (
        <div
            className="min-h-screen bg-cover bg-center relative"
            style={{ backgroundImage: `url(${assets.register_img})` }}
        >
            <div className="absolute inset-0 bg-black/60 backdrop-blur-sm z-0" />
            <Navbar showSignUpButton={false} />

            <div className="relative z-10 max-w-4xl mx-auto px-6 pt-32 text-white">
                <h1 className="text-3xl font-bold mb-8 text-center">My Orders</h1>

                <div className="space-y-6">
                    {orders.map(order => {
                        const fecha = new Date(order.date).toLocaleDateString(undefined, {
                            year: 'numeric', month: 'long', day: 'numeric'
                        });
                        return (
                            <div
                                key={order.id}
                                className="bg-zinc-900 bg-opacity-90 rounded-xl p-6 shadow-xl border border-white/10"
                            >
                                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                                    <div>
                                        <p className="text-sm text-gray-400">Order ID</p>
                                        <p className="font-medium">#{order.id}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-400">Date</p>
                                        <p>{fecha}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-400">Total</p>
                                        <p className="font-bold text-lg">
                                            Bs. {order.totalGlobal.toFixed(2)}
                                        </p>
                                    </div>
                                    <button
                                        onClick={() => toggleExpand(order.id)}
                                        className="self-start sm:self-center bg-white text-black px-4 py-2 rounded-full text-sm hover:bg-gray-200 transition"
                                    >
                                        {expandedId === order.id ? 'Hide Details' : 'View Details'}
                                    </button>
                                </div>

                                {expandedId === order.id && (
                                    <div className="mt-6 border-t border-white/10 pt-4 space-y-2 text-sm">
                                        {order.detalles.map(item => (
                                            <div key={item.id} className="flex justify-between">
                        <span className="text-gray-300">
                          {item.productName} ×{item.quantity}
                        </span>
                                                <span>
                          Bs. {item.total.toFixed(2)}
                        </span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}
