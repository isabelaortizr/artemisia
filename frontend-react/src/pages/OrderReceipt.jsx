// src/pages/OrderReceipt.jsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { assets } from '../assets/assets';
import notaVentaService from '../services/notaVentaService';

export default function OrderReceipt() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const notaVentaId = state?.notaVentaId;

  const [loading, setLoading]     = useState(true);
  const [error,   setError]       = useState(null);
  const [receipt, setReceipt]     = useState(null);

  useEffect(() => {
    if (!notaVentaId) {
      // si no hay ID, volvemos al carrito
      navigate('/', { replace: true });
      return;
    }
    notaVentaService.getNotaVentaById(notaVentaId)
        .then(data => setReceipt(data))
        .catch(err => setError(err.message || 'Error al cargar recibo'))
        .finally(() => setLoading(false));
  }, [notaVentaId, navigate]);

  if (loading) return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-white">Cargando recibo…</p>
      </div>
  );
  if (error) return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-red-500">{error}</p>
      </div>
  );

  // formatear fecha
  const dateStr = new Date(receipt.date)
      .toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });

  return (
      <div
          className="min-h-screen bg-cover bg-center relative"
          style={{ backgroundImage: `url(${assets.register_img})` }}
      >
        <div className="absolute inset-0 bg-black/60 backdrop-blur-sm z-0" />
        <Navbar showSignUpButton={false} />
        <div className="relative z-10 max-w-3xl mx-auto p-6 pt-32 text-white">
          <div className="bg-zinc-900 bg-opacity-90 p-8 rounded-2xl shadow-xl border border-white/10">
            <h1 className="text-3xl font-bold text-center mb-4">¡Gracias por tu compra!</h1>
            <p className="text-center text-gray-300 mb-8">
              Hemos recibido tu orden y se está procesando.
            </p>

            {/* Datos de la orden */}
            <div className="mb-6 space-y-3">
              <div className="flex justify-between border-b border-white/10 pb-2">
                <span className="text-gray-400">Número de orden:</span>
                <span className="font-semibold">#{receipt.id}</span>
              </div>
              <div className="flex justify-between border-b border-white/10 pb-2">
                <span className="text-gray-400">Fecha:</span>
                <span>{dateStr}</span>
              </div>
              <div className="flex justify-between border-b border-white/10 pb-2">
                <span className="text-gray-400">ID Transacción:</span>
                <span className="truncate max-w-xs">{receipt.idTransaccion}</span>
              </div>
              <div className="flex justify-between border-b border-white/10 pb-2">
                <span className="text-gray-400">Total:</span>
                <span className="font-bold text-lg">{receipt.totalGlobal.toFixed(2)}</span>
              </div>
            </div>

            {/* Resumen de productos */}
            <div className="mb-8">
              <h2 className="text-xl font-semibold mb-4">Resumen</h2>
              <ul className="space-y-2">
                {receipt.detalles.map(item => (
                    <li key={item.id} className="flex justify-between">
                  <span className="text-gray-300">
                    {item.productName} × {item.quantity}
                  </span>
                      <span>${item.total.toFixed(2)}</span>
                    </li>
                ))}
              </ul>
            </div>

            {/* Acciones */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link
                  to="/products"
                  className="w-full sm:w-auto text-center bg-white text-black px-6 py-3 rounded-full hover:bg-gray-200 transition"
              >
                Seguir comprando
              </Link>
              <Link
                  to="/orderHistory"
                  className="w-full sm:w-auto text-center bg-black border border-white px-6 py-3 rounded-full hover:bg-white hover:text-black transition"
              >
                Ver mis órdenes
              </Link>
            </div>
          </div>
        </div>
      </div>
  );
}
