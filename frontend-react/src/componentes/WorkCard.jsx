// src/components/WorkCard.jsx
import React, { useState } from 'react';

export default function WorkCard({ work, onEdit }) {
    const [expanded, setExpanded] = useState(false);

    return (
        <div className="border rounded-xl p-4 shadow-md flex flex-col hover:shadow-lg">
            <img src={work.image || 'https://via.placeholder.com/300x200'}
                 alt={work.name}
                 className="w-full h-48 object-cover rounded-lg" />
            <h3 className="mt-4 font-semibold text-lg">{work.name}</h3>
            <p className="text-gray-500 mt-1">${work.price.toFixed(2)}</p>

            {expanded && (
                <div className="mt-4 space-y-2 text-gray-700 text-sm">
                    <p><strong>Técnica:</strong> {work.technique}</p>
                    <p><strong>Categoría:</strong> {work.category}</p>
                    <p><strong>Materiales:</strong> {work.materials}</p>
                    <p><strong>Descripción:</strong> {work.description}</p>
                    <p><strong>Stock:</strong> {work.stock}</p>
                    <p><strong>Estado:</strong> {work.status === 'AVAILABLE' ? 'Disponible' : 'No disponible'}</p>
                </div>
            )}

            <div className="mt-auto flex justify-between items-center pt-4">
                <button
                    onClick={() => setExpanded(e => !e)}
                    className="text-indigo-600 hover:underline"
                >
                    {expanded ? 'Ver menos ▲' : 'Ver detalle ▼'}
                </button>
                <button
                    onClick={onEdit}
                    className="text-green-600 hover:underline"
                >Editar</button>
            </div>
        </div>
    );
}
