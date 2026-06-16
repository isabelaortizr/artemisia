// src/components/CreateAuctionModal.jsx
import React, { useState } from 'react';
import auctionService from '../services/auctionService';

export default function CreateAuctionModal({ work, onClose, onSuccess }) {
    const [startingPrice, setStartingPrice] = useState('');
    const [endDate, setEndDate] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const minEndDate = new Date(Date.now() + 60 * 60 * 1000)
        .toISOString()
        .slice(0, 16);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (!startingPrice || Number(startingPrice) <= 0) {
            setError('El precio inicial debe ser mayor a 0.');
            return;
        }
        if (!endDate) {
            setError('La fecha de fin es obligatoria.');
            return;
        }
        setLoading(true);
        try {
            await auctionService.createAuction({
                productId: work.productId ?? work.id,
                startingPrice: Number(startingPrice),
                endDate: endDate + ':00',
            });
            onSuccess();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl max-w-md w-full p-6 relative text-black">
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-gray-500 hover:text-red-600 text-2xl leading-none"
                >
                    ×
                </button>
                <h2 className="text-xl font-bold mb-1">Subastar obra</h2>
                <p className="text-sm text-gray-500 mb-4">"{work.name}"</p>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium mb-1">
                            Precio inicial (Bs.)
                        </label>
                        <input
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={startingPrice}
                            onChange={e => setStartingPrice(e.target.value)}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black"
                            placeholder="Ej: 100.00"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium mb-1">
                            Fecha y hora de cierre
                        </label>
                        <input
                            type="datetime-local"
                            value={endDate}
                            min={minEndDate}
                            onChange={e => setEndDate(e.target.value)}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black"
                            required
                        />
                    </div>

                    {error && (
                        <p className="text-red-500 text-sm">{error}</p>
                    )}

                    <div className="flex gap-3 pt-2">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 py-2 rounded-lg border border-gray-300 text-sm hover:bg-gray-50 transition"
                        >
                            Cancelar
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 py-2 rounded-lg bg-black text-white text-sm hover:bg-gray-800 transition disabled:opacity-50"
                        >
                            {loading ? 'Creando…' : 'Crear subasta'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
