// src/components/WorkCard.jsx
import React, { useState } from 'react';

// Función para formatear el texto
const formatText = (text) => {
    if (!text) return '';
    return text.toLowerCase().replace(/_/g, ' ')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
};

export default function WorkCard({ work, onEdit }) {
    const [expanded, setExpanded] = useState(false);

    const imageSrc = work.image
        ? `data:image/jpeg;base64,${work.image}`
        : 'https://via.placeholder.com/300x200';

    const toggleExpand = () => {
        setExpanded(!expanded);
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

            {expanded && (
                <div className="mt-4 space-y-2 text-gray-700 text-sm animate-fadeIn">
                    <p><strong>Techniques:</strong> {work.techniques ? work.techniques.map(formatText).join(', ') : 'N/A'}</p>
                    <p><strong>Categories:</strong> {work.categories ? work.categories.map(formatText).join(', ') : 'N/A'}</p>
                    <p><strong>Materials:</strong> {work.materials || 'N/A'}</p>
                    <p><strong>Description:</strong> {work.description || 'N/A'}</p>
                    <p><strong>Stock:</strong> {work.stock}</p>
                    <p><strong>Status:</strong> {work.status === 'AVAILABLE' ? 'Available' : 'Unavailable'}</p>
                </div>
            )}

            <div className="mt-auto flex justify-between items-center pt-4">
                <button
                    onClick={toggleExpand}
                    className="text-blue-600 hover:underline text-sm focus:outline-none"
                >
                    {expanded ? 'See less ▲' : 'See details ▼'}
                </button>
                <button
                    onClick={onEdit}
                    className="bg-black text-white py-2 px-4 rounded-full hover:bg-gray-800 text-sm focus:outline-none transition-colors"
                >
                    Edit
                </button>
            </div>
        </div>
    );
}