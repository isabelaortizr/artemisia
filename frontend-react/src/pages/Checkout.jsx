// src/pages/Checkout.jsx
import React, { useState }           from 'react';
import { useLocation, Link }         from 'react-router-dom';
import backIcon                       from '../assets/back-icon.png';
import notaVentaService               from '../services/notaVentaService';

export default function Checkout() {
    const { state }         = useLocation();
    const tx                = state?.transaction;
    const addressId         = state?.addressId;
    const userId            = Number(localStorage.getItem("userId"));

    const [verifyLoading,   setVerifyLoading]   = useState(false);
    const [verifyResult,    setVerifyResult]    = useState(null);
    const [verifyError,     setVerifyError]     = useState(null);

    // si no vienen tx o address, redirigimos al carrito
    if (!tx || !addressId) {
        return (
            <div className="min-h-screen flex items-center justify-center p-6 text-center">
                <div>
                    <p className="text-red-500 mb-4">
                        No se encontró la transacción. Volviendo al carrito…
                    </p>
                    <Link to="/cart" className="text-indigo-600 hover:underline">
                        Ir al Carrito
                    </Link>
                </div>
            </div>
        );
    }

    const paymentLink = tx.payment_link;

    const handleVerify = async () => {
        setVerifyLoading(true);
        setVerifyError(null);
        try {
            const res = await notaVentaService.verifyTransaction(userId);
            console.log('Verificación de pago:', res);
            setVerifyResult(res);
        } catch (err) {
            console.error(err);
            setVerifyError(err.message || 'Error al verificar');
        } finally {
            setVerifyLoading(false);
        }
    };

    return (
        <div className="h-screen bg-black/10 flex items-center justify-center">
            <div
                className="relative max-w-xl w-full mx-auto p-6 pt-12
                   overflow-y-auto max-h-[calc(100vh-2rem)] bg-white rounded-xl"
            >
                {/* Volver */}
                <Link to="/cart" className="absolute top-6 left-6 z-10">
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
                        className="w-full h-[600px] border rounded-lg shadow"
                    />
                </div>

                <p className="text-center mb-8">
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

                {/* ——— NUEVO: Verificar pago ——— */}
                <div className="text-center mb-12">
                    <button
                        onClick={handleVerify}
                        disabled={verifyLoading}
                        className="px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-full transition disabled:opacity-50"
                    >
                        {verifyLoading ? 'Verificando…' : 'Verificar pago'}
                    </button>

                    {verifyResult && (
                        <div className="mt-4">
                            <p>Estado: <strong>{verifyResult.estado}</strong></p>
                            <p>Nota de venta: <strong>{verifyResult.notaVentaId}</strong></p>
                        </div>
                    )}

                    {verifyError && (
                        <p className="mt-4 text-red-500">
                            Error: {verifyError}
                        </p>
                    )}
                </div>
            </div>
        </div>
    );
}
