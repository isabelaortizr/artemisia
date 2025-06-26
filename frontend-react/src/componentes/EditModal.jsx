// src/components/EditModal.jsx
import React, { useState } from 'react';
import { paintingTechniques, paintingCategories } from '../constants/painting';
import productService from '../services/productService';

export default function EditModal({ work, onClose, onSave }) {
    const [form, setForm]       = useState({ ...work });
    const [loading, setLoading] = useState(false);
    const [error, setError]     = useState('');

    const handleChange = e => {
        const { name, value } = e.target;
        setForm(f => ({ ...f, [name]: value }));
    };

    const handleSubmit = async e => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const updated = await productService.updateProduct(work.id, {
                sellerId: Number(localStorage.getItem('userId')),
                ...form
            });
            onSave(updated);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <form
                onSubmit={handleSubmit}
                className="bg-white p-6 rounded-lg shadow-lg w-full max-w-lg max-h-[90vh] overflow-auto space-y-4 text-gray-800"
            >
                <h3 className="text-xl font-bold mb-2 text-center">
                    Editar “{work.name}”
                </h3>
                {error && <p className="text-red-500 text-sm">{error}</p>}

                {/* Nombre */}
                <div>
                    <label className="block text-gray-700">Nombre</label>
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        placeholder="Nombre de la obra"
                        required
                        className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black placeholder-gray-400"
                    />
                </div>

                {/* Técnica */}
                <div>
                    <label className="block text-gray-700">Técnica</label>
                    <select
                        name="technique"
                        value={form.technique}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black"
                    >
                        {paintingTechniques.map(t => (
                            <option key={t} value={t}>{t}</option>
                        ))}
                    </select>
                </div>

                {/* Categoría */}
                <div>
                    <label className="block text-gray-700">Categoría</label>
                    <select
                        name="category"
                        value={form.category}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black"
                    >
                        {paintingCategories.map(c => (
                            <option key={c} value={c}>{c}</option>
                        ))}
                    </select>
                </div>

                {/* Materiales */}
                <div>
                    <label className="block text-gray-700">Materiales</label>
                    <input
                        name="materials"
                        value={form.materials}
                        onChange={handleChange}
                        placeholder="Papel, Lienzo…"
                        className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black placeholder-gray-400"
                    />
                </div>

                {/* Descripción */}
                <div>
                    <label className="block text-gray-700">Descripción</label>
                    <textarea
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        rows={3}
                        placeholder="Añade detalles…"
                        className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black placeholder-gray-400"
                    />
                </div>

                {/* Precio y Stock */}
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-gray-700">Precio (USD)</label>
                        <input
                            name="price"
                            type="number"
                            step="0.01"
                            value={form.price}
                            onChange={handleChange}
                            required
                            className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black placeholder-gray-400"
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700">Stock</label>
                        <input
                            name="stock"
                            type="number"
                            value={form.stock}
                            onChange={handleChange}
                            required
                            className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black placeholder-gray-400"
                        />
                    </div>
                </div>

                {/* Estado */}
                <div>
                    <label className="block text-gray-700">Estado</label>
                    <select
                        name="status"
                        value={form.status}
                        onChange={handleChange}
                        className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black"
                    >
                        <option value="AVAILABLE">Disponible</option>
                        <option value="UNAVAILABLE">No disponible</option>
                    </select>
                </div>

                {/* URL Imagen */}
                <div>
                    <label className="block text-gray-700">URL de la Imagen</label>
                    <input
                        name="image"
                        value={form.image}
                        onChange={handleChange}
                        placeholder="https://miimagen.jpg"
                        className="w-full mt-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black placeholder-gray-400"
                    />
                </div>

                {/* Botones */}
                <div className="flex justify-end space-x-2 mt-4">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={loading}
                        className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 transition"
                    >
                        Cancelar
                    </button>
                    <button
                        type="submit"
                        disabled={loading}
                        className={`px-4 py-2 rounded text-white ${
                            loading ? 'bg-gray-400' : 'bg-indigo-600 hover:bg-indigo-700'
                        }`}
                    >
                        {loading ? 'Guardando…' : 'Guardar Cambios'}
                    </button>
                </div>
            </form>
        </div>
    );
}
