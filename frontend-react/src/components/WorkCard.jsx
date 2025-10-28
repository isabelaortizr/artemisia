// src/components/WorkCard.jsx
import React from 'react';
import productService from '../services/productService';

// Función para formatear el texto
const formatText = (text) => {
    if (!text) return '';
    return text.toLowerCase().replace(/_/g, ' ')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
};

export default function WorkCard({ work, isExpanded, onToggleExpand, onEdit }) {
    const imageSrc = work.image
        ? `data:image/jpeg;base64,${work.image}`
        : 'https://via.placeholder.com/300x200';

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
        <div className="border rounded-xl p-4 shadow-md flex flex-col hover:shadow-lg bg-white text-black transition-all duration-300">
            <img
                src={imageSrc}
                alt={work.name}
                className="w-full h-48 object-cover rounded-lg" />
            <h3 className="mt-4 font-semibold text-lg">{work.name}</h3>
            <p className="text-gray-800 font-bold mt-1">
                {new Intl.NumberFormat('es-BO', { style: 'currency', currency: 'BOB' }).format(work.price)}
            </p>

            {/* Contenido expandible */}
            <div className={`overflow-hidden transition-all duration-300 ${
                isExpanded ? 'max-h-96 opacity-100 mt-4' : 'max-h-0 opacity-0'
            }`}>
                <div className="space-y-2 text-gray-700 text-sm">
                    <p><strong>Techniques:</strong> {work.techniques ? work.techniques.map(formatText).join(', ') : 'N/A'}</p>
                    <p><strong>Categories:</strong> {work.categories ? work.categories.map(formatText).join(', ') : 'N/A'}</p>
                    <p><strong>Materials:</strong> {work.materials || 'N/A'}</p>
                    <p><strong>Description:</strong> {work.description || 'N/A'}</p>
                    <p><strong>Stock:</strong> {work.stock}</p>
                    <p><strong>Status:</strong> {work.status === 'AVAILABLE' ? 'Available' : 'Unavailable'}</p>
                </div>
            </div>

            <div className="mt-auto flex justify-between items-center pt-4">
                <button
                    onClick={handleToggle}
                    className="text-black hover:underline text-sm focus:outline-none transition-colors"
                >
                    {isExpanded ? 'See less ▲' : 'See details ▼'}
                </button>
                <button
                    onClick={handleEdit}
                    className="bg-black text-white py-2 px-4 rounded-full hover:bg-gray-800 text-sm focus:outline-none transition-colors"
                >
                    Edit
                </button>
            </div>
        </div>
    );
}