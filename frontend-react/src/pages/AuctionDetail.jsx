// src/pages/AuctionDetail.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import ImageModal from '../components/ImageModal';
import auctionService from '../services/auctionService';
import addressService from '../services/addressService';
import notaVentaService from '../services/notaVentaService';

function formatDate(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('es-BO', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit',
    });
}

function formatPrice(amount) {
    return new Intl.NumberFormat('es-BO', { style: 'currency', currency: 'BOB' })
        .format(amount ?? 0)
        .replace('Bs', 'Bs.');
}

const STATUS_LABEL = {
    ACTIVE: { text: 'Activa', color: 'bg-green-100 text-green-700' },
    FINISHED: { text: 'Finalizada', color: 'bg-yellow-100 text-yellow-700' },
    CANCELLED: { text: 'Cancelada', color: 'bg-gray-100 text-gray-600' },
    COMPLETED: { text: 'Completada', color: 'bg-blue-100 text-blue-700' },
};

export default function AuctionDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const userId = Number(localStorage.getItem('userId'));

    const [auction, setAuction] = useState(null);
    const [bids, setBids] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Bid form
    const [bidAmount, setBidAmount] = useState('');
    const [bidLoading, setBidLoading] = useState(false);
    const [bidError, setBidError] = useState('');
    const [bidSuccess, setBidSuccess] = useState('');

    // Close auction
    const [closeLoading, setCloseLoading] = useState(false);
    const [closeError, setCloseError] = useState('');

    // Image modal
    const [showImageModal, setShowImageModal] = useState(false);

    // Confirm purchase
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [addresses, setAddresses] = useState([]);
    const [selectedAddress, setSelectedAddress] = useState('');
    const [confirmLoading, setConfirmLoading] = useState(false);
    const [confirmError, setConfirmError] = useState('');

    // Payment simulation
    const [showPaymentModal, setShowPaymentModal] = useState(false);
    const [timeLeft, setTimeLeft] = useState(600);
    const [isExpired, setIsExpired] = useState(false);
    const [payLoading, setPayLoading] = useState(false);
    const [payError, setPayError] = useState('');

    useEffect(() => {
        if (!showPaymentModal || timeLeft <= 0) return;
        const timer = setInterval(() => {
            setTimeLeft(prev => {
                if (prev <= 1) { setIsExpired(true); return 0; }
                return prev - 1;
            });
        }, 1000);
        return () => clearInterval(timer);
    }, [showPaymentModal, timeLeft]);

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
    };

    const load = useCallback(() => {
        return Promise.all([
            auctionService.getAuctionById(id),
            auctionService.getAuctionBids(id, 0, 20),
        ]).then(([auc, bidsData]) => {
            setAuction(auc);
            setBids(bidsData.content ?? []);
        });
    }, [id]);

    useEffect(() => {
        if (!userId) { navigate('/login'); return; }
        setLoading(true);
        load()
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [load, userId, navigate]);

    const handleBid = async (e) => {
        e.preventDefault();
        setBidError('');
        setBidSuccess('');
        const amount = Number(bidAmount);
        if (!amount || amount <= (auction?.currentPrice ?? 0)) {
            setBidError(`La puja debe ser mayor al precio actual: ${formatPrice(auction?.currentPrice)}`);
            return;
        }
        setBidLoading(true);
        try {
            await auctionService.placeBid(id, amount);
            setBidSuccess('¡Puja registrada!');
            setBidAmount('');
            await load();
        } catch (err) {
            setBidError(err.message);
        } finally {
            setBidLoading(false);
        }
    };

    const handleClose = async () => {
        if (!window.confirm('¿Cerrar la subasta anticipadamente?')) return;
        setCloseLoading(true);
        setCloseError('');
        try {
            await auctionService.closeAuction(id);
            await load();
        } catch (err) {
            setCloseError(err.message);
        } finally {
            setCloseLoading(false);
        }
    };

    const openConfirmModal = async () => {
        setConfirmError('');
        setSelectedAddress('');
        try {
            const addrs = await addressService.getAddressesByUser(userId);
            setAddresses(addrs ?? []);
        } catch {
            setAddresses([]);
        }
        setShowConfirmModal(true);
    };

    const handleConfirmPurchase = async () => {
        if (!selectedAddress) {
            setConfirmError('Seleccioná una dirección de envío.');
            return;
        }
        setConfirmLoading(true);
        setConfirmError('');
        try {
            await auctionService.confirmPurchase(id, Number(selectedAddress));
            setShowConfirmModal(false);
            setTimeLeft(600);
            setIsExpired(false);
            setPayError('');
            setShowPaymentModal(true);
        } catch (err) {
            setConfirmError(err.message);
        } finally {
            setConfirmLoading(false);
        }
    };

    const handleSimulatePayment = async () => {
        setPayLoading(true);
        setPayError('');
        try {
            const res = await notaVentaService.simulatePayment();
            navigate('/orderReceipt', { state: { notaVentaId: res.notaVentaId } });
        } catch (err) {
            setPayError(err.message || 'Error al confirmar pago');
        } finally {
            setPayLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center">
                <div className="animate-spin rounded-full h-14 w-14 border-t-4 border-white border-opacity-40" />
            </div>
        );
    }

    if (error || !auction) {
        return (
            <div className="min-h-screen bg-black text-white flex flex-col items-center justify-center gap-4">
                <p className="text-red-400">{error || 'Subasta no encontrada.'}</p>
                <Link to="/auctions" className="text-gray-300 hover:underline text-sm">← Volver a subastas</Link>
            </div>
        );
    }

    const isSeller = auction.sellerId === userId;
    const isWinner = auction.winnerId === userId;
    const imageSrc = auction.productImage
        ? (auction.productImage.startsWith('http') ? auction.productImage : `data:image/jpeg;base64,${auction.productImage}`)
        : 'https://via.placeholder.com/600x400';
    const statusInfo = STATUS_LABEL[auction.status] ?? { text: auction.status, color: 'bg-gray-100 text-gray-600' };

    return (
        <div className="min-h-screen flex flex-col bg-black text-white">
            <Navbar showSignUpButton={false} />

            <main className="flex-1 max-w-5xl mx-auto w-full px-6 py-24">
                <Link to="/auctions" className="text-gray-400 hover:text-white text-sm mb-6 inline-block">
                    ← Volver a subastas
                </Link>

                <div className="grid md:grid-cols-2 gap-10">
                    {/* Imagen */}
                    <div
                        className="rounded-2xl overflow-hidden shadow-xl cursor-zoom-in"
                        onClick={() => setShowImageModal(true)}
                    >
                        <img src={imageSrc} alt={auction.productName} className="w-full h-80 object-cover hover:opacity-90 transition-opacity" />
                    </div>

                    {/* Info principal */}
                    <div className="flex flex-col gap-4">
                        <div className="flex items-start justify-between gap-2">
                            <h1 className="text-2xl font-bold leading-snug">{auction.productName}</h1>
                            <span className={`text-xs font-semibold px-3 py-1 rounded-full whitespace-nowrap ${statusInfo.color}`}>
                                {statusInfo.text}
                            </span>
                        </div>

                        <p className="text-gray-400 text-sm">Vendedor: <span className="text-white">{auction.sellerName}</span></p>
                        {auction.winnerName && (
                            <p className="text-sm text-yellow-400 font-medium">
                                Ganador: {auction.winnerName} {isWinner && '(tú)'}
                            </p>
                        )}

                        <div className="bg-white/5 rounded-xl p-4 space-y-2">
                            <div className="flex justify-between text-sm">
                                <span className="text-gray-400">Precio inicial</span>
                                <span>{formatPrice(auction.startingPrice)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-gray-400">Puja actual</span>
                                <span className="text-xl font-bold">{formatPrice(auction.currentPrice)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-gray-400">Inicio</span>
                                <span>{formatDate(auction.startDate)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-gray-400">Cierre</span>
                                <span>{formatDate(auction.endDate)}</span>
                            </div>
                        </div>

                        {/* Acción: vendedor puede cerrar */}
                        {isSeller && auction.status === 'ACTIVE' && (
                            <div>
                                <button
                                    onClick={handleClose}
                                    disabled={closeLoading}
                                    className="w-full py-2 rounded-lg border border-red-500 text-red-400 hover:bg-red-500 hover:text-white transition text-sm disabled:opacity-50"
                                >
                                    {closeLoading ? 'Cerrando…' : 'Cerrar subasta anticipadamente'}
                                </button>
                                {closeError && <p className="text-red-400 text-sm mt-1">{closeError}</p>}
                            </div>
                        )}

                        {/* Acción: comprador puede pujar */}
                        {!isSeller && auction.status === 'ACTIVE' && (
                            <form onSubmit={handleBid} className="space-y-3">
                                <div className="flex gap-2">
                                    <input
                                        type="number"
                                        min={auction.currentPrice + 0.01}
                                        step="0.01"
                                        value={bidAmount}
                                        onChange={e => setBidAmount(e.target.value)}
                                        placeholder={`> ${formatPrice(auction.currentPrice)}`}
                                        className="flex-1 bg-white/10 border border-white/20 rounded-lg px-3 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-white/30"
                                    />
                                    <button
                                        type="submit"
                                        disabled={bidLoading}
                                        className="px-5 py-2 bg-white text-black rounded-lg text-sm font-semibold hover:bg-gray-200 transition disabled:opacity-50"
                                    >
                                        {bidLoading ? '…' : 'Pujar'}
                                    </button>
                                </div>
                                {bidError && <p className="text-red-400 text-sm">{bidError}</p>}
                                {bidSuccess && <p className="text-green-400 text-sm">{bidSuccess}</p>}
                            </form>
                        )}

                        {/* Acción: ganador puede confirmar compra */}
                        {isWinner && auction.status === 'FINISHED' && (
                            <div>
                                <button
                                    onClick={openConfirmModal}
                                    className="w-full py-2 rounded-lg bg-yellow-400 text-black text-sm font-bold hover:bg-yellow-300 transition"
                                >
                                    Confirmar compra y pagar
                                </button>
                                <p className="text-xs text-gray-400 mt-1">
                                    Seleccioná tu dirección de envío para confirmar y proceder al pago.
                                </p>
                            </div>
                        )}

                        {auction.status === 'CANCELLED' && (
                            <p className="text-gray-400 text-sm">
                                Esta subasta fue cancelada (no hubo pujas o fue cerrada sin participantes).
                            </p>
                        )}

                        {auction.status === 'COMPLETED' && (
                            <p className="text-green-400 text-sm font-medium">
                                Subasta completada. El ganador ya confirmó y pagó la compra.
                            </p>
                        )}
                    </div>
                </div>

                {/* Ranking de pujas */}
                <section className="mt-12">
                    <h2 className="text-xl font-semibold mb-4">Ranking de pujas</h2>
                    {bids.length === 0 ? (
                        <p className="text-gray-400 text-sm">Todavía no hay pujas en esta subasta.</p>
                    ) : (
                        <div className="bg-white/5 rounded-xl overflow-hidden">
                            <table className="w-full text-sm">
                                <thead>
                                    <tr className="border-b border-white/10 text-gray-400">
                                        <th className="text-left px-4 py-3">#</th>
                                        <th className="text-left px-4 py-3">Participante</th>
                                        <th className="text-right px-4 py-3">Puja</th>
                                        <th className="text-right px-4 py-3">Fecha</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {bids.map((b, i) => (
                                        <tr
                                            key={b.id}
                                            className={`border-b border-white/5 ${i === 0 ? 'text-yellow-400 font-semibold' : 'text-white'}`}
                                        >
                                            <td className="px-4 py-3">{i + 1}</td>
                                            <td className="px-4 py-3">
                                                {b.participantName}
                                                {b.participantId === userId && ' (tú)'}
                                            </td>
                                            <td className="px-4 py-3 text-right">{formatPrice(b.bidAmount)}</td>
                                            <td className="px-4 py-3 text-right text-gray-400">{formatDate(b.bidDate)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            </main>

            <Footer />

            {/* Modal imagen */}
            {showImageModal && (
                <ImageModal
                    imageSrc={imageSrc}
                    alt={auction.productName}
                    onClose={() => setShowImageModal(false)}
                />
            )}

            {/* Modal pago simulado */}
            {showPaymentModal && (
                <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white text-black max-w-md w-full rounded-2xl p-6 relative shadow-xl">
                        <h2 className="text-2xl font-bold mb-4 text-center">Finaliza tu compra</h2>

                        <div className="text-center mb-4">
                            <p className="text-gray-600 mb-3">Escanea este código QR para pagar</p>
                            <div className="border-2 border-gray-200 rounded-lg p-4 inline-block bg-white">
                                <img
                                    src={`https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=ARTEMISIA-AUCTION|${auction.currentPrice.toFixed(2)}|BOB`}
                                    alt="QR de pago"
                                    width={160}
                                    height={160}
                                />
                            </div>
                        </div>

                        <div className="text-center mb-4">
                            <p className="text-gray-600 mb-1">Monto a pagar</p>
                            <p className="text-3xl font-bold text-gray-900">{formatPrice(auction.currentPrice)}</p>
                        </div>

                        <div className="text-center mb-4">
                            <div className={`inline-flex items-center px-4 py-2 rounded-full ${timeLeft < 60 ? 'bg-red-100 text-red-800' : 'bg-blue-100 text-blue-800'}`}>
                                <span className="font-bold text-lg">{formatTime(timeLeft)}</span>
                            </div>
                            <p className="text-sm text-gray-500 mt-1">Tiempo restante para completar el pago</p>
                            {isExpired && <p className="text-red-500 text-sm mt-1">El tiempo ha expirado. Por favor, inicia una nueva transacción.</p>}
                        </div>

                        <button
                            onClick={handleSimulatePayment}
                            disabled={payLoading || isExpired}
                            className="w-full py-3 bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white font-semibold rounded-lg transition mb-2"
                        >
                            {payLoading ? 'Procesando...' : isExpired ? 'Tiempo Expirado' : 'Ya pagué'}
                        </button>
                        <p className="text-xs text-gray-500 text-center">Escanea el QR con tu aplicación de pagos y luego haz clic en "Ya pagué"</p>

                        {payError && <p className="mt-4 text-red-500 text-center text-sm">Error: {payError}</p>}
                    </div>
                </div>
            )}

            {/* Modal confirmar compra */}
            {showConfirmModal && (
                <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white text-black rounded-2xl max-w-md w-full p-6 relative">
                        <button
                            onClick={() => setShowConfirmModal(false)}
                            className="absolute top-4 right-4 text-gray-500 hover:text-red-600 text-2xl leading-none"
                        >
                            ×
                        </button>
                        <h2 className="text-xl font-bold mb-1">Confirmar compra</h2>
                        <p className="text-gray-500 text-sm mb-4">
                            "{auction.productName}" — {formatPrice(auction.currentPrice)}
                        </p>

                        {addresses.length === 0 ? (
                            <div className="text-center py-4">
                                <p className="text-gray-600 text-sm mb-3">No tenés direcciones guardadas.</p>
                                <Link
                                    to="/profile"
                                    className="text-black underline text-sm"
                                    onClick={() => setShowConfirmModal(false)}
                                >
                                    Ir a mi perfil para agregar una dirección
                                </Link>
                            </div>
                        ) : (
                            <div className="space-y-3">
                                <label className="block text-sm font-medium">Dirección de envío</label>
                                <select
                                    value={selectedAddress}
                                    onChange={e => setSelectedAddress(e.target.value)}
                                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black"
                                >
                                    <option value="">Seleccioná una dirección…</option>
                                    {addresses.map(addr => (
                                        <option key={addr.address_id} value={addr.address_id}>
                                            {addr.street} {addr.house_number}, {addr.city}, {addr.country}
                                        </option>
                                    ))}
                                </select>

                                {confirmError && <p className="text-red-500 text-sm">{confirmError}</p>}

                                <button
                                    onClick={handleConfirmPurchase}
                                    disabled={confirmLoading || !selectedAddress}
                                    className="w-full py-2 mt-2 rounded-lg bg-black text-white text-sm font-semibold hover:bg-gray-800 transition disabled:opacity-50"
                                >
                                    {confirmLoading ? 'Procesando…' : 'Confirmar y proceder al pago'}
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
