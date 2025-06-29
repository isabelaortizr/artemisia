// src/components/EditModal.jsx
import React, { useState } from 'react';
import { paintingTechniques, paintingCategories } from '../constants/painting';
import productService from '../services/productService';
import imageService   from '../services/imageService';  // ← importamos el service de imágenes

export default function EditModal({ work, onClose, onSave }) {
    const [form, setForm]       = useState({ ...work });
    const [fileData, setFileData] = useState({ fileName: '', base64Image: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError]     = useState('');

    const handleChange = e => {
        const { name, value } = e.target;
        setForm(f => ({ ...f, [name]: value }));
    };

    const handleFileChange = e => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = () => {
            const base64 = reader.result.split(',')[1];
            setFileData({ fileName: file.name, base64Image: base64 });
        };
        reader.readAsDataURL(file);
    };

    const handleSubmit = async e => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            // 1) Actualizamos datos del producto
            const updated = await productService.updateProduct(work.productId, {
                sellerId: Number(localStorage.getItem('userId')),
                name:        form.name,
                technique:   form.technique,
                category:    form.category,
                materials:   form.materials,
                description: form.description,
                price:       parseFloat(form.price),
                stock:       parseInt(form.stock, 10),
                status:      form.status
            });

            // 2) Si el usuario seleccionó una nueva imagen, la subimos
            if (fileData.base64Image) {
                await imageService.uploadImage({
                    productId:   work.productId,
                    fileName:    fileData.fileName,
                    base64Image: fileData.base64Image
                });
                // opcional: podrías refrescar updated.image con el nuevo Base64
                updated.image = fileData.base64Image;
            }

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

                {/* — Nombre — */}
                <div>
                    <label className="block text-gray-700">Nombre</label>
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded focus:ring-2 focus:ring-indigo-500 text-black"
                    />
                </div>

                {/* — Técnica — */}
                <div>
                    <label className="block text-gray-700">Técnica</label>
                    <select
                        name="technique"
                        value={form.technique}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded focus:ring-2 focus:ring-indigo-500 text-black"
                    >
                        {paintingTechniques.map(t => (
                            <option key={t} value={t}>{t}</option>
                        ))}
                    </select>
                </div>

                {/* — Categoría — */}
                <div>
                    <label className="block text-gray-700">Categoría</label>
                    <select
                        name="category"
                        value={form.category}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded focus:ring-2 focus:ring-indigo-500 text-black"
                    >
                        {paintingCategories.map(c => (
                            <option key={c} value={c}>{c}</option>
                        ))}
                    </select>
                </div>

                {/* — Materiales — */}
                <div>
                    <label className="block text-gray-700">Materiales</label>
                    <input
                        name="materials"
                        value={form.materials}
                        onChange={handleChange}
                        className="w-full mt-1 px-3 py-2 border rounded focus:ring-2 focus:ring-indigo-500 text-black"
                    />
                </div>

                {/* — Descripción — */}
                <div>
                    <label className="block text-gray-700">Descripción</label>
                    <textarea
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        rows={3}
                        className="w-full mt-1 px-3 py-2 border rounded focus:ring-2 focus:ring-indigo-500 text-black"
                    />
                </div>

                {/* — Precio y Stock — */}
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
                            className="w-full mt-1 px-3 py-2 border rounded focus:ring-2 focus:ring-indigo-500 text-black"
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
                            className="w-full mt-1 px-3 py-2 border rounded focus:ring-2 focus:ring-indigo-500 text-black"
                        />
                    </div>
                </div>

                {/* — Estado — */}
                <div>
                    <label className="block text-gray-700">Estado</label>
                    <select
                        name="status"
                        value={form.status}
                        onChange={handleChange}
                        className="w-full mt-1 px-3 py-2 border rounded focus:ring-2 focus:ring-indigo-500 text-black"
                    >
                        <option value="AVAILABLE">Disponible</option>
                        <option value="UNAVAILABLE">No disponible</option>
                    </select>
                </div>

                {/* — Imagen: botón de file upload — */}
                <div>
                    <label className="block text-gray-700 mb-1">Imagen de la Obra</label>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="w-full mt-1 border-2 border-dashed border-gray-300 p-2 rounded cursor-pointer"
                    />
                    {fileData.fileName && (
                        <p className="mt-1 text-sm text-gray-600">
                            Seleccionado: {fileData.fileName}
                        </p>
                    )}
                </div>

                {/* — Botones — */}
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
