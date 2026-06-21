// src/components/WorkCard.jsx
import React, { useState } from 'react';
import productService from '../services/productService';
import ImageModal from './ImageModal';

// Función para formatear el texto
const formatText = (text) => {
    if (!text) return '';
    return text.toLowerCase().replace(/_/g, ' ')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
};

export default function WorkCard({ work, isExpanded, onToggleExpand, onEdit, onAuction }) {
    const [showImageModal, setShowImageModal] = useState(false);

    const imageSrc = work.image
        ? (work.image.startsWith('http') ? work.image : `data:image/jpeg;base64,${work.image}`)
        : 'https://via.placeholder.com/300x200';

    const handleImageClick = (e) => {
        e.stopPropagation();
        setShowImageModal(true);
    };

    const handleToggle = async (e) => {
        e.stopPropagation();

        // Si el usuario está expandiendo (viendo detalles), trackear la vista
        if (!isExpanded) {
            try {
                await productService.trackProductView(work.productId || work.id);
                console.log(`Tracked view for product: ${work.name}`);
            } catch (error) {
                console.error('Error tracking product view:', error);
                // No mostramos error al usuario para no interrumpir la experiencia
            }
        }

        onToggleExpand();
    };

    const handleEdit = (e) => {
        e.stopPropagation();
        onEdit();
    };

    return (
        <>
            <div className="border rounded-xl p-4 shadow-md flex flex-col hover:shadow-lg bg-white text-black transition-all duration-300">
                {/* Imagen clickeable */}
                <div
                    className="w-full h-48 overflow-hidden rounded-lg cursor-zoom-in hover:opacity-90 transition-opacity"
                    onClick={handleImageClick}
                >
                    <img
                        src={imageSrc}
                        alt={work.name}
                        className="w-full h-full object-cover"
                    />
                </div>

                <h3 className="mt-4 font-semibold text-lg">{work.name}</h3>
                <p className="text-gray-800 font-bold mt-1">
                    {new Intl.NumberFormat('es-BO', { style: 'currency', currency: 'BOB' }).format(work.price)}
                </p>

                {/* Contenido expandible */}
                {isExpanded && (
                    <div className="mt-3 bg-gray-100 rounded-lg p-3 text-sm text-black border border-gray-200">
                        <p><strong>Techniques:</strong> {work.techniques ? work.techniques.map(formatText).join(', ') : 'N/A'}</p>
                        <p><strong>Categories:</strong> {work.categories ? work.categories.map(formatText).join(', ') : 'N/A'}</p>
                        <p><strong>Materials:</strong> {work.materials || 'N/A'}</p>
                        <p><strong>Stock:</strong> {work.stock}</p>
                        <p><strong>Status:</strong> {
                            work.status === 'AVAILABLE' ? 'Available'
                            : work.status === 'ON_AUCTION' ? 'On Auction'
                            : 'Unavailable'
                        }</p>
                        <p className="mt-2 text-gray-700 italic">{work.description || 'No description.'}</p>
                    </div>
                )}

                <div className="mt-auto pt-4 space-y-2">
                    <div className="flex justify-between items-center">
                        <button
                            onClick={handleToggle}
                            className="text-black hover:underline text-sm focus:outline-none transition-colors"
                        >
                            {isExpanded ? 'See less ▲' : 'See details ▼'}
                        </button>
                        <div className="flex gap-2">
                            {work.status === 'AVAILABLE' && onAuction && (
                                <button
                                    onClick={e => { e.stopPropagation(); onAuction(); }}
                                    className="bg-yellow-400 text-black py-2 px-3 rounded-full hover:bg-yellow-300 text-xs font-semibold focus:outline-none transition-colors"
                                >
                                    Auction
                                </button>
                            )}
                            {work.status === 'ON_AUCTION' && (
                                <span className="text-xs font-semibold px-3 py-1.5 rounded-full bg-orange-100 text-orange-600">
                                    On Auction
                                </span>
                            )}
                            <button
                                onClick={handleEdit}
                                className="bg-black text-white py-2 px-4 rounded-full hover:bg-gray-800 text-sm focus:outline-none transition-colors"
                            >
                                Edit
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Modal de imagen */}
            {showImageModal && (
                <ImageModal
                    imageSrc={imageSrc}
                    alt={work.name}
                    onClose={() => setShowImageModal(false)}
                />
            )}
        </>
    );
}