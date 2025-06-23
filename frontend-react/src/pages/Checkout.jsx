// src/pages/Checkout.jsx
import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import paymentService from '../services/paymentService';

const Checkout = () => {
    const navigate = useNavigate();
    const { state } = useLocation();
    const [qrUrl, setQrUrl]     = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState('');

    useEffect(() => {
        // 1) Recupera el orderId (puede venir en state o en localStorage)
        const orderId = state?.orderId || localStorage.getItem('orderId');
        if (!orderId) {
            // si no hay orden, volvemos al carrito
            navigate('/cart');
            return;
        }

        // opcional: guardarlo para refrescos de página
        localStorage.setItem('orderId', orderId);

        // 2) Llamamos al back para traer el QR
        paymentService.getPaymentQr(orderId)
            .then(url => setQrUrl(url))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [navigate, state]);

    if (loading) return <p className="text-center mt-10">Generando tu QR de pago…</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">{error}</p>;

    return (
        <div className="relative max-w-md mx-auto p-6">
            <h2 className="text-2xl font-bold mb-6 text-center">Escanea para pagar</h2>
            <div className="flex justify-center">
                <img
                    src={qrUrl}
                    alt="QR de pago"
                    className="w-64 h-64 object-contain"
                />
            </div>
            <p className="mt-4 text-center text-gray-600">
                Abre la app de tu banco o wallet y escanea el código.
            </p>
        </div>
    );
};

export default Checkout;
