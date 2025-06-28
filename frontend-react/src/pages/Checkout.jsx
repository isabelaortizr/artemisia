// src/pages/Checkout.jsx
import { useState }          from 'react';
import { useLocation, Link } from 'react-router-dom';
import backIcon              from '../assets/back-icon.png';

export default function Checkout() {
    const { state }    = useLocation();
    const tx           = state?.transaction;
    const addressId    = state?.addressId;

    // Si no vienen los datos, redirigimos al carrito
    if (!tx || !addressId) {
        return (
            <div className="max-w-md mx-auto p-6 text-center">
                <p className="text-red-500">No se encontró la transacción. Volviendo al carrito…</p>
                <Link to="/cart" className="text-indigo-600 hover:underline">Ir al Carrito</Link>
            </div>
        );
    }

    const paymentLink = tx.payment_link;

    return (
        <div className="relative max-w-xl mx-auto p-6">
            {/* Botón de volver */}
            <Link to="/cart" className="absolute top-6 left-6">
                <img src={backIcon} alt="Volver" className="w-10 h-10 hover:opacity-80 transition" />
            </Link>

            <h2 className="text-2xl font-bold mb-4 text-center">Finaliza tu compra</h2>
            <p className="mb-6 text-center">
                Escanea el QR o interactúa con la pantalla de pago embebida a continuación:
            </p>

            <div className="flex justify-center mb-6">
                <iframe
                    src={paymentLink}
                    title="Stereum Pay"
                    allow="clipboard-read; clipboard-write"
                    allowFullScreen
                    loading="lazy"
                    className="w-full max-w-2xl h-[600px] border rounded-lg shadow"
                />
            </div>

            <p className="text-center">
                Si por alguna razón no se carga bien aquí,{' '}
                <a
                    href={paymentLink}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-indigo-600 hover:underline"
                >
                    haz clic aquí para ir a la página de pago
                </a>.
            </p>
        </div>
    );
}
