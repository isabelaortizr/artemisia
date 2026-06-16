// src/pages/MyAuctions.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import auctionService from '../services/auctionService';

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
    ACTIVE:    { text: 'Activa',      color: 'bg-green-100 text-green-700' },
    FINISHED:  { text: 'Finalizada',  color: 'bg-yellow-100 text-yellow-700' },
    CANCELLED: { text: 'Cancelada',   color: 'bg-gray-100 text-gray-500' },
    COMPLETED: { text: 'Completada',  color: 'bg-blue-100 text-blue-700' },
};

function AuctionRow({ auction, onClose, showCloseBtn, showWonBadge }) {
    const imageSrc = auction.productImage
        ? (auction.productImage.startsWith('http') ? auction.productImage : `data:image/jpeg;base64,${auction.productImage}`)
        : 'https://via.placeholder.com/80x80';
    const statusInfo = STATUS_LABEL[auction.status] ?? { text: auction.status, color: 'bg-gray-100 text-gray-600' };

    return (
        <div className="bg-white/5 rounded-xl p-4 flex flex-col sm:flex-row gap-4 items-start sm:items-center">
            <img src={imageSrc} alt={auction.productName} className="w-20 h-20 object-cover rounded-lg flex-shrink-0" />

            <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                    <Link
                        to={`/auctions/${auction.id}`}
                        className="font-semibold text-white hover:underline line-clamp-1"
                    >
                        {auction.productName}
                    </Link>
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusInfo.color}`}>
                        {statusInfo.text}
                    </span>
                    {showWonBadge && auction.status === 'FINISHED' && (
                        <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-yellow-400 text-black">
                            Pendiente de confirmar
                        </span>
                    )}
                </div>
                <p className="text-gray-400 text-sm mt-0.5">
                    Puja actual: <span className="text-white font-medium">{formatPrice(auction.currentPrice)}</span>
                    {auction.winnerName && (
                        <> · Ganador: <span className="text-yellow-400">{auction.winnerName}</span></>
                    )}
                </p>
                <p className="text-gray-500 text-xs mt-0.5">
                    Cierra: {formatDate(auction.endDate)}
                </p>
            </div>

            <div className="flex gap-2 flex-shrink-0">
                <Link
                    to={`/auctions/${auction.id}`}
                    className="px-3 py-1.5 border border-white/20 rounded-lg text-xs text-white hover:bg-white/10 transition"
                >
                    Ver detalle
                </Link>
                {showCloseBtn && auction.status === 'ACTIVE' && (
                    <button
                        onClick={() => onClose(auction.id)}
                        className="px-3 py-1.5 border border-red-500 rounded-lg text-xs text-red-400 hover:bg-red-500 hover:text-white transition"
                    >
                        Cerrar
                    </button>
                )}
                {showWonBadge && auction.status === 'FINISHED' && (
                    <Link
                        to={`/auctions/${auction.id}`}
                        className="px-3 py-1.5 rounded-lg text-xs bg-yellow-400 text-black font-semibold hover:bg-yellow-300 transition"
                    >
                        Confirmar compra
                    </Link>
                )}
            </div>
        </div>
    );
}

export default function MyAuctions() {
    const navigate = useNavigate();
    const userRole = localStorage.getItem('userRole');
    const isSeller = userRole === 'SELLER';

    const [tab, setTab] = useState(isSeller ? 'mine' : 'won');
    const [myAuctions, setMyAuctions] = useState([]);
    const [wonAuctions, setWonAuctions] = useState([]);
    const [myPage, setMyPage] = useState(0);
    const [wonPage, setWonPage] = useState(0);
    const [myTotal, setMyTotal] = useState(1);
    const [wonTotal, setWonTotal] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [closeError, setCloseError] = useState('');

    const loadMine = useCallback(() => {
        setLoading(true);
        return auctionService.getMyAuctions(myPage, 10)
            .then(data => {
                setMyAuctions(data.content ?? []);
                setMyTotal(data.totalPages ?? 1);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [myPage]);

    const loadWon = useCallback(() => {
        setLoading(true);
        return auctionService.getWonAuctions(wonPage, 10)
            .then(data => {
                setWonAuctions(data.content ?? []);
                setWonTotal(data.totalPages ?? 1);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [wonPage]);

    useEffect(() => {
        if (!localStorage.getItem('userId')) { navigate('/login'); return; }
    }, [navigate]);

    useEffect(() => {
        if (tab === 'mine') loadMine();
    }, [tab, loadMine]);

    useEffect(() => {
        if (tab === 'won') loadWon();
    }, [tab, loadWon]);

    const handleClose = async (auctionId) => {
        if (!window.confirm('¿Cerrar esta subasta anticipadamente?')) return;
        setCloseError('');
        try {
            await auctionService.closeAuction(auctionId);
            await loadMine();
        } catch (err) {
            setCloseError(err.message);
        }
    };

    const currentList = tab === 'mine' ? myAuctions : wonAuctions;
    const currentTotal = tab === 'mine' ? myTotal : wonTotal;
    const currentPage = tab === 'mine' ? myPage : wonPage;
    const setCurrentPage = tab === 'mine' ? setMyPage : setWonPage;

    return (
        <div className="min-h-screen flex flex-col bg-black text-white">
            <Navbar showSignUpButton={false} />

            <main className="flex-1 max-w-4xl mx-auto w-full px-6 py-24">
                <h1 className="text-3xl font-bold mb-8">My Auctions</h1>

                {/* Tabs */}
                <div className="flex gap-1 mb-8 bg-white/5 p-1 rounded-xl w-fit">
                    {isSeller && (
                        <button
                            onClick={() => setTab('mine')}
                            className={`px-5 py-2 rounded-lg text-sm font-medium transition ${
                                tab === 'mine' ? 'bg-white text-black' : 'text-gray-400 hover:text-white'
                            }`}
                        >
                            Mis subastas
                        </button>
                    )}
                    <button
                        onClick={() => setTab('won')}
                        className={`px-5 py-2 rounded-lg text-sm font-medium transition ${
                            tab === 'won' ? 'bg-white text-black' : 'text-gray-400 hover:text-white'
                        }`}
                    >
                        Ganadas
                    </button>
                </div>

                {closeError && (
                    <p className="text-red-400 text-sm mb-4">{closeError}</p>
                )}

                {loading ? (
                    <div className="flex justify-center items-center h-40">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-white border-opacity-40" />
                    </div>
                ) : error ? (
                    <p className="text-red-400 text-sm">{error}</p>
                ) : currentList.length === 0 ? (
                    <div className="text-center py-16">
                        <p className="text-gray-400">
                            {tab === 'mine'
                                ? 'No creaste subastas todavía. Podés subastar tus obras desde "My Works".'
                                : 'No ganaste ninguna subasta todavía. ¡Participá en las activas!'}
                        </p>
                        <Link
                            to={tab === 'mine' ? '/myworks' : '/auctions'}
                            className="mt-4 inline-block text-sm text-gray-300 hover:text-white underline"
                        >
                            {tab === 'mine' ? 'Ir a Mis Obras' : 'Ver subastas activas'}
                        </Link>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {currentList.map(a => (
                            <AuctionRow
                                key={a.id}
                                auction={a}
                                onClose={handleClose}
                                showCloseBtn={tab === 'mine'}
                                showWonBadge={tab === 'won'}
                            />
                        ))}
                    </div>
                )}

                {currentTotal > 1 && (
                    <div className="mt-8 flex justify-center items-center gap-4 text-gray-300">
                        <button
                            disabled={currentPage === 0}
                            onClick={() => setCurrentPage(p => p - 1)}
                            className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded disabled:opacity-50 transition text-sm"
                        >
                            Previous
                        </button>
                        <span className="text-sm">Page {currentPage + 1} of {currentTotal}</span>
                        <button
                            disabled={currentPage + 1 >= currentTotal}
                            onClick={() => setCurrentPage(p => p + 1)}
                            className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded disabled:opacity-50 transition text-sm"
                        >
                            Next
                        </button>
                    </div>
                )}
            </main>

            <Footer />
        </div>
    );
}
