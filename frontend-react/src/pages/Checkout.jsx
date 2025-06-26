// src/pages/Checkout.jsx
import { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import notaVentaService from '../services/notaVentaService';
import backIcon from '../assets/back-icon.png';

const Checkout = () => {
    const [paymentLink, setPaymentLink] = useState('');
    const [loading, setLoading]         = useState(true);
    const [error, setError]             = useState(null);
    const navigate = useNavigate();
    const didRequest = useRef(false);

    useEffect(() => {
        if (didRequest.current) return;
        didRequest.current = true;

        const userId = localStorage.getItem('userId');
        if (!userId) {
            navigate('/login');
            return;
        }

        notaVentaService
            .createTransaction({
                userId,
                currency: 'BOB',
                chargeReason: 'Compra en Artemisia',
                network: 'BISA',
                country: 'BO'
            })
            .then(data => {
                // extraemos el enlace real de la respuesta
                const { payment_link } = data;
                setPaymentLink(payment_link);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [navigate]);

    if (loading) return <p className="text-center mt-10">Preparando tu pago…</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">Error: {error}</p>;

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

            {/* Iframe embebida */}
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

            {/* Enlace externo de respaldo */}
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
};

export default Checkout;
