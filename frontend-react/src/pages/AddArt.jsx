// src/pages/AddArt.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import productService from '../services/productService';
import imageService from '../services/imageService';
import toastIcon from '../assets/toast-icon.png';

// Definir las opciones disponibles EXACTAMENTE como están en el backend
const TECHNIQUE_OPTIONS = [
    'Óleo',
    'Acrílico',
    'Acuarela',
    'Temple',
    'Fresco',
    'Gouache',
    'Tinta',
    'Mixta',
    'Spray',
    'Digital'
];

const CATEGORY_OPTIONS = [
    'Realista',
    'Abstracta',
    'Expresionista',
    'Impresionista',
    'Surrealista',
    'Conceptual',
    'Religiosa',
    'Histórica',
    'Decorativa',
    'Contemporánea'
];

const AddArt = ({ embedded, dark, onSuccess, onCancel }) => {
    const [form, setForm] = useState({
        name: '',
        materials: '',
        description: '',
        price: '',
        stock: '',
        status: 'AVAILABLE'
    });

    // Estados para técnicas y categorías como arrays (checkboxes)
    const [selectedTechniques, setSelectedTechniques] = useState([]);
    const [selectedCategories, setSelectedCategories] = useState([]);

    const [fileData, setFileData] = useState({ fileName: '', base64Image: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();
    const sellerId = Number(localStorage.getItem('userId'));

    const handleChange = e => {
        const { name, value } = e.target;
        setForm(f => ({ ...f, [name]: value }));
    };

    // Manejar checkboxes para técnicas
    const handleTechniqueToggle = (technique) => {
        setSelectedTechniques(prev =>
            prev.includes(technique)
                ? prev.filter(t => t !== technique)
                : [...prev, technique]
        );
    };

    // Manejar checkboxes para categorías
    const handleCategoryToggle = (category) => {
        setSelectedCategories(prev =>
            prev.includes(category)
                ? prev.filter(c => c !== category)
                : [...prev, category]
        );
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
        setError('');
        setSuccess('');

        // Validaciones
        if (selectedTechniques.length === 0) {
            setError('Selecciona al menos una técnica.');
            return;
        }

        if (selectedCategories.length === 0) {
            setError('Selecciona al menos una categoría.');
            return;
        }

        const priceNum = parseFloat(form.price);
        const stockNum = parseInt(form.stock, 10);
        if (isNaN(priceNum) || priceNum < 0) {
            setError('El precio no puede ser negativo.');
            return;
        }
        if (isNaN(stockNum) || stockNum < 0) {
            setError('El stock no puede ser negativo.');
            return;
        }

        if (!sellerId) {
            setError('Debes iniciar sesión como vendedor');
            return;
        }

        setLoading(true);
        try {
            // Preparar datos para el backend
            const productData = {
                sellerId,
                name: form.name,
                materials: form.materials,
                description: form.description,
                price: parseFloat(form.price),
                stock: parseInt(form.stock, 10),
                status: form.status,
                techniques: selectedTechniques,
                categories: selectedCategories
            };

            console.log('Enviando datos al backend:', productData);

            // 1) Crear el producto
            const newProd = await productService.createProduct(productData);

            // 2) Subir imagen si existe
            if (fileData.base64Image) {
                await imageService.uploadImage({
                    productId: newProd.productId,
                    fileName: fileData.fileName,
                    base64Image: fileData.base64Image
                });
            }

            setSuccess('¡Obra creada exitosamente!');

            // Limpiar formulario
            setForm({
                name: '',
                materials: '',
                description: '',
                price: '',
                stock: '',
                status: 'AVAILABLE'
            });
            setSelectedTechniques([]);
            setSelectedCategories([]);
            setFileData({ fileName: '', base64Image: '' });

            // Llamar a onSuccess si existe
            if (onSuccess) {
                setTimeout(() => {
                    onSuccess();
                }, 1500);
            } else {
                setTimeout(() => navigate('/myworks'), 2000);
            }
        } catch (err) {
            const msg = err.message || '';
            if (msg.includes('Data Not Found') || msg.includes('INTERNAL_SERVER_ERROR') || msg.includes('400')) {
                setError('Ocurrió un error al crear su obra, vuelva a intentarlo');
            } else {
                setError(err.message || 'Error al autenticar');
            }
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => {
        if (onCancel) {
            onCancel();
        } else {
            navigate(-1);
        }
    };

    return (
        <div className={`relative max-w-2xl mx-auto p-6 ${embedded ? '' : ''}`}>
            <h2 className="text-2xl font-bold mb-6 text-center">Agregar nueva obra</h2>

            {error && <p className="text-red-500 mb-4 text-center">{error}</p>}
            {success && (
                <div className="flex items-center justify-center mb-4 text-green-600">
                    <img src={toastIcon} alt="" className="w-5 h-5 mr-2" /> {success}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6 bg-white p-6 rounded-lg shadow text-black">
                {/* Nombre */}
                <div>
                    <label className="block text-gray-700 font-medium mb-2">Nombre de la obra</label>
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        placeholder="Ingresa el nombre de tu obra"
                    />
                </div>

                {/* Técnicas (Checkboxes) */}
                <div>
                    <label className="block text-gray-700 font-medium mb-3">Técnicas *</label>
                    <div className="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto p-3 border border-gray-200 rounded-lg bg-gray-50">
                        {TECHNIQUE_OPTIONS.map(technique => (
                            <label key={technique} className="flex items-center space-x-3 cursor-pointer p-2 hover:bg-white rounded transition">
                                <input
                                    type="checkbox"
                                    checked={selectedTechniques.includes(technique)}
                                    onChange={() => handleTechniqueToggle(technique)}
                                    className="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
                                />
                                <span className="text-gray-700">{technique}</span>
                            </label>
                        ))}
                    </div>
                    <p className="text-xs text-gray-500 mt-2">
                        {selectedTechniques.length} seleccionada(s): {selectedTechniques.join(', ') || 'Ninguna'}
                    </p>
                </div>

                {/* Categorías (Checkboxes) */}
                <div>
                    <label className="block text-gray-700 font-medium mb-3">Categorías *</label>
                    <div className="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto p-3 border border-gray-200 rounded-lg bg-gray-50">
                        {CATEGORY_OPTIONS.map(category => (
                            <label key={category} className="flex items-center space-x-3 cursor-pointer p-2 hover:bg-white rounded transition">
                                <input
                                    type="checkbox"
                                    checked={selectedCategories.includes(category)}
                                    onChange={() => handleCategoryToggle(category)}
                                    className="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
                                />
                                <span className="text-gray-700">{category}</span>
                            </label>
                        ))}
                    </div>
                    <p className="text-xs text-gray-500 mt-2">
                        {selectedCategories.length} seleccionada(s): {selectedCategories.join(', ') || 'Ninguna'}
                    </p>
                </div>

                {/* Materiales */}
                <div>
                    <label className="block text-gray-700 font-medium mb-2">Materiales</label>
                    <input
                        name="materials"
                        value={form.materials}
                        onChange={handleChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        placeholder="Lista los materiales utilizados..."
                    />
                </div>

                {/* Descripción */}
                <div>
                    <label className="block text-gray-700 font-medium mb-2">Descripción</label>
                    <textarea
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        rows={3}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        placeholder="Describe tu obra de arte..."
                    />
                </div>

                {/* Precio y Stock */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-gray-700 font-medium mb-2">Precio (Bs.) *</label>
                        <input
                            name="price"
                            type="number"
                            step="0.01"
                            min="0"
                            value={form.price}
                            onChange={handleChange}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                            placeholder="0.00"
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700 font-medium mb-2">Stock *</label>
                        <input
                            name="stock"
                            type="number"
                            min="0"
                            value={form.stock}
                            onChange={handleChange}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                            placeholder="0"
                        />
                    </div>
                </div>

                {/* Estado */}
                <div>
                    <label className="block text-gray-700 font-medium mb-2">Estado</label>
                    <select
                        name="status"
                        value={form.status}
                        onChange={handleChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    >
                        <option value="AVAILABLE">Disponible</option>
                        <option value="UNAVAILABLE">No disponible</option>
                    </select>
                </div>

                {/* Imagen */}
                <div>
                    <label className="block text-gray-700 font-medium mb-2">Imagen</label>
                    <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center hover:border-gray-400 transition-colors">
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
                                    {fileData.fileName ? `Seleccionado: ${fileData.fileName}` : 'Haz clic para subir una imagen'}
                                </span>
                                <span className="text-xs text-gray-500 mt-1">
                                    PNG, JPG, JPEG hasta 10MB
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

                {/* Botones */}
                <div className="flex gap-3 pt-4">
                    {onCancel && (
                        <button
                            type="button"
                            onClick={handleCancel}
                            disabled={loading}
                            className="flex-1 py-3 px-4 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors disabled:opacity-50 font-medium"
                        >
                            Cancelar
                        </button>
                    )}
                    <button
                        type="submit"
                        disabled={loading}
                        className={`flex-1 py-3 px-4 rounded-lg text-white font-medium transition-colors ${
                            loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-black hover:bg-gray-800'
                        }`}
                    >
                        {loading ? 'Creando obra...' : 'Crear Obra'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default AddArt;