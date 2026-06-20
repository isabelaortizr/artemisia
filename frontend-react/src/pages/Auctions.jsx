// src/pages/Auctions.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
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

function AuctionCard({ auction }) {
    const imageSrc = auction.productImage
        ? (auction.productImage.startsWith('http') ? auction.productImage : `data:image/jpeg;base64,${auction.productImage}`)
        : 'https://via.placeholder.com/400x300';

    const timeLeft = () => {
        const diff = new Date(auction.endDate) - new Date();
        if (diff <= 0) return 'Finalizada';
        const h = Math.floor(diff / 3600000);
        const m = Math.floor((diff % 3600000) / 60000);
        if (h > 24) return `${Math.floor(h / 24)}d ${h % 24}h`;
        return `${h}h ${m}m`;
    };

    return (
        <Link
            to={`/auctions/${auction.id}`}
            className="bg-white rounded-2xl shadow-lg hover:shadow-2xl transition duration-300 overflow-hidden text-black flex flex-col group"
        >
            <div className="overflow-hidden h-52">
                <img
                    src={imageSrc}
                    alt={auction.productName}
                    className="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                />
            </div>
            <div className="p-4 flex flex-col flex-1">
                <h3 className="font-semibold text-base line-clamp-1">{auction.productName}</h3>
                <p className="text-gray-500 text-sm mt-0.5">por {auction.sellerName}</p>

                <div className="mt-3 flex justify-between items-end flex-1">
                    <div>
                        <p className="text-xs text-gray-400 uppercase tracking-wide">Puja actual</p>
                        <p className="text-lg font-bold">{formatPrice(auction.currentPrice)}</p>
                    </div>
                    <div className="text-right">
                        <p className="text-xs text-gray-400 uppercase tracking-wide">Cierra en</p>
                        <p className="text-sm font-medium text-orange-500">{timeLeft()}</p>
                    </div>
                </div>

                <p className="text-xs text-gray-400 mt-2">Cierre: {formatDate(auction.endDate)}</p>
            </div>
        </Link>
    );
}

export default function Auctions() {
    const [auctions, setAuctions] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        if (!localStorage.getItem('userId')) {
            navigate('/login');
            return;
        }
        setLoading(true);
        auctionService.getActiveAuctions(page, 12)
            .then(data => {
                setAuctions(data.content ?? []);
                setTotalPages(data.totalPages ?? 1);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [page, navigate]);

    return (
        <div className="min-h-screen flex flex-col bg-black text-white">
            <Navbar showSignUpButton={false} />

            <section
                className="h-[40vh] bg-cover bg-center flex items-center justify-center text-center relative"
                style={{ backgroundImage: "url('/header_img.png')" }}
            >
                <div className="absolute inset-0 bg-black/40" />
                <div className="relative z-10">
                    <h1 className="text-4xl sm:text-5xl font-bold mb-3">Live Auctions</h1>
                    <p className="text-gray-200 text-lg max-w-xl mx-auto">
                        Puja por obras únicas de artistas. La puja más alta gana.
                    </p>
                </div>
            </section>

            <main className="flex-1 px-6 sm:px-10 py-12">
                {loading ? (
                    <div className="flex justify-center items-center h-60">
                        <div className="animate-spin rounded-full h-14 w-14 border-t-4 border-white border-opacity-40" />
                    </div>
                ) : error ? (
                    <p className="text-center text-red-400">{error}</p>
                ) : auctions.length === 0 ? (
                    <div className="text-center py-20">
                        <p className="text-gray-400 text-lg">No hay subastas activas en este momento.</p>
                        <p className="text-gray-500 text-sm mt-2">Vuelve pronto o revisa tu historial en "My Auctions".</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                        {auctions.map(a => (
                            <AuctionCard key={a.id} auction={a} />
                        ))}
                    </div>
                )}

                {totalPages > 1 && (
                    <div className="mt-10 flex justify-center items-center gap-4 text-gray-300">
                        <button
                            disabled={page === 0}
                            onClick={() => setPage(p => p - 1)}
                            className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded disabled:opacity-50 transition"
                        >
                            Previous
                        </button>
                        <span className="text-sm">Page {page + 1} of {totalPages}</span>
                        <button
                            disabled={page + 1 >= totalPages}
                            onClick={() => setPage(p => p + 1)}
                            className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded disabled:opacity-50 transition"
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
