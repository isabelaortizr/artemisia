// src/components/EditModal.jsx
import React, { useState } from 'react';
import { paintingTechniques, paintingCategories } from '../constants/painting';
import productService from '../services/productService';
import imageService from '../services/imageService';

export default function EditModal({ work, onClose, onSave }) {
    const [form, setForm] = useState({
        ...work,
        techniques: Array.isArray(work.techniques) ? work.techniques : [work.technique].filter(Boolean),
        categories: Array.isArray(work.categories) ? work.categories : [work.category].filter(Boolean)
    });
    const [fileData, setFileData] = useState({ fileName: '', base64Image: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = e => {
        const { name, value } = e.target;
        setForm(f => ({ ...f, [name]: value }));
    };

    // Manejar checkboxes para técnicas
    const handleTechniqueToggle = (technique) => {
        setForm(f => ({
            ...f,
            techniques: f.techniques.includes(technique)
                ? f.techniques.filter(t => t !== technique)
                : [...f.techniques, technique]
        }));
    };

    // Manejar checkboxes para categorías
    const handleCategoryToggle = (category) => {
        setForm(f => ({
            ...f,
            categories: f.categories.includes(category)
                ? f.categories.filter(c => c !== category)
                : [...f.categories, category]
        }));
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
            const productData = {
                sellerId: Number(localStorage.getItem('userId')),
                name: form.name,
                techniques: form.techniques,
                categories: form.categories,
                materials: form.materials,
                description: form.description,
                price: parseFloat(form.price),
                stock: parseInt(form.stock, 10),
                status: form.status
            };

            const updated = await productService.updateProduct(work.productId, productData);

            if (fileData.base64Image) {
                await imageService.uploadImage({
                    productId: work.productId,
                    fileName: fileData.fileName,
                    base64Image: fileData.base64Image
                });
                updated.image = fileData.base64Image;
            }

            onSave(updated);
        } catch (err) {
            setError(err.message || 'Error al actualizar el producto');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            {/* Fondo que previene el scroll del body */}
            <div
                className="absolute inset-0 bg-black/50 backdrop-blur-sm"
                onClick={onClose}
            />

            {/* Modal */}
            <form
                onSubmit={handleSubmit}
                className="relative z-10 bg-white text-black p-6 rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto"
            >
                <h3 className="text-xl font-bold mb-6 text-center">
                    Edit "{work.name}"
                </h3>

                {error && <p className="text-red-500 text-sm mb-4 p-2 bg-red-50 rounded">{error}</p>}

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* — Nombre — */}
                    <div className="md:col-span-2">
                        <label className="block text-gray-700 font-medium mb-2">Name</label>
                        <input
                            name="name"
                            value={form.name}
                            onChange={handleChange}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-black"
                        />
                    </div>

                    {/* — Técnicas (Checkboxes) — */}
                    <div>
                        <label className="block text-gray-700 font-medium mb-3">Techniques</label>
                        <div className="space-y-2 max-h-48 overflow-y-auto p-2 border border-gray-200 rounded-lg">
                            {paintingTechniques.map(technique => (
                                <label key={technique} className="flex items-center space-x-3 cursor-pointer hover:bg-gray-50 p-2 rounded">
                                    <input
                                        type="checkbox"
                                        checked={form.techniques.includes(technique)}
                                        onChange={() => handleTechniqueToggle(technique)}
                                        className="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
                                    />
                                    <span className="text-gray-700">{technique}</span>
                                </label>
                            ))}
                        </div>
                        <p className="text-xs text-gray-500 mt-2">
                            {form.techniques.length} selected
                        </p>
                    </div>

                    {/* — Categorías (Checkboxes) — */}
                    <div>
                        <label className="block text-gray-700 font-medium mb-3">Categories</label>
                        <div className="space-y-2 max-h-48 overflow-y-auto p-2 border border-gray-200 rounded-lg">
                            {paintingCategories.map(category => (
                                <label key={category} className="flex items-center space-x-3 cursor-pointer hover:bg-gray-50 p-2 rounded">
                                    <input
                                        type="checkbox"
                                        checked={form.categories.includes(category)}
                                        onChange={() => handleCategoryToggle(category)}
                                        className="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
                                    />
                                    <span className="text-gray-700">{category}</span>
                                </label>
                            ))}
                        </div>
                        <p className="text-xs text-gray-500 mt-2">
                            {form.categories.length} selected
                        </p>
                    </div>

                    {/* — Materiales — */}
                    <div className="md:col-span-2">
                        <label className="block text-gray-700 font-medium mb-2">Materials</label>
                        <input
                            name="materials"
                            value={form.materials}
                            onChange={handleChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-black"
                            placeholder="List the materials used..."
                        />
                    </div>

                    {/* — Descripción — */}
                    <div className="md:col-span-2">
                        <label className="block text-gray-700 font-medium mb-2">Description</label>
                        <textarea
                            name="description"
                            value={form.description}
                            onChange={handleChange}
                            rows={3}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-black"
                            placeholder="Describe your artwork..."
                        />
                    </div>

                    {/* — Precio y Stock — */}
                    <div>
                        <label className="block text-gray-700 font-medium mb-2">Price (Bs.)</label>
                        <input
                            name="price"
                            type="number"
                            step="0.01"
                            min="0"
                            value={form.price}
                            onChange={handleChange}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-black"
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700 font-medium mb-2">Stock</label>
                        <input
                            name="stock"
                            type="number"
                            min="0"
                            value={form.stock}
                            onChange={handleChange}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-black"
                        />
                    </div>

                    {/* — Estado — */}
                    <div className="md:col-span-2">
                        <label className="block text-gray-700 font-medium mb-2">Status</label>
                        <select
                            name="status"
                            value={form.status}
                            onChange={handleChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-black"
                        >
                            <option value="AVAILABLE">Available</option>
                            <option value="UNAVAILABLE">Unavailable</option>
                        </select>
                    </div>

                    {/* — Imagen — */}
                    <div className="md:col-span-2">
                        <label className="block text-gray-700 font-medium mb-2">Picture</label>
                        <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center">
                            <input
                                type="file"
                                accept="image/*"
                                onChange={handleFileChange}
                                className="hidden"
                                id="file-upload"
                            />
                            <label htmlFor="file-upload" className="cursor-pointer">
                                <div className="flex flex-col items-center justify-center">
                                    <svg className="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                    </svg>
                                    <span className="text-sm text-gray-600">
                                        {fileData.fileName ? `Selected: ${fileData.fileName}` : 'Click to upload new image'}
                                    </span>
                                    <span className="text-xs text-gray-500 mt-1">
                                        PNG, JPG, JPEG up to 10MB
                                    </span>
                                </div>
                            </label>
                        </div>
                        {fileData.fileName && (
                            <p className="mt-2 text-sm text-green-600 text-center">
                                ✅ {fileData.fileName}
                            </p>
                        )}
                    </div>
                </div>

                {/* — Botones — */}
                <div className="flex justify-end space-x-3 mt-8 pt-6 border-t border-gray-200">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={loading}
                        className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors disabled:opacity-50 font-medium"
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        disabled={loading}
                        className={`px-6 py-3 rounded-lg text-white font-medium transition-colors ${
                            loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-black hover:bg-gray-800'
                        }`}
                    >
                        {loading ? 'Saving...' : 'Save Changes'}
                    </button>
                </div>
            </form>
        </div>
    );
}