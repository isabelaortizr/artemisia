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

// Función para formatear el texto (solo para mostrar, no para enviar)
const formatText = (text) => {
    if (!text) return '';
    return text;
};

const AddArt = ({ embedded, dark, onSuccess, onCancel }) => {
    const [form, setForm] = useState({
        name: '',
        materials: '',
        description: '',
        price: '',
        stock: '',
        status: 'AVAILABLE'
    });

    // Estados para técnicas y categorías múltiples
    const [techniques, setTechniques] = useState(['']);
    const [categories, setCategories] = useState(['']);

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

    // Manejar cambios en técnicas
    const handleTechniqueChange = (index, value) => {
        const newTechniques = [...techniques];
        newTechniques[index] = value;
        setTechniques(newTechniques);
    };

    // Agregar nuevo dropdown de técnica
    const addTechnique = () => {
        setTechniques([...techniques, '']);
    };

    // Eliminar técnica
    const removeTechnique = (index) => {
        if (techniques.length > 1) {
            const newTechniques = techniques.filter((_, i) => i !== index);
            setTechniques(newTechniques);
        }
    };

    // Manejar cambios en categorías
    const handleCategoryChange = (index, value) => {
        const newCategories = [...categories];
        newCategories[index] = value;
        setCategories(newCategories);
    };

    // Agregar nuevo dropdown de categoría
    const addCategory = () => {
        setCategories([...categories, '']);
    };

    // Eliminar categoría
    const removeCategory = (index) => {
        if (categories.length > 1) {
            const newCategories = categories.filter((_, i) => i !== index);
            setCategories(newCategories);
        }
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

        // Filtrar técnicas y categorías vacías
        const filteredTechniques = techniques.filter(tech => tech !== '');
        const filteredCategories = categories.filter(cat => cat !== '');

        // Validaciones
        if (filteredTechniques.length === 0) {
            setError('Selecciona al menos una técnica.');
            return;
        }

        if (filteredCategories.length === 0) {
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
                techniques: filteredTechniques,
                categories: filteredCategories
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
            setTechniques(['']);
            setCategories(['']);
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
        <div className={`relative max-w-lg mx-auto p-6 ${embedded ? '' : ''}`}>
            <h2 className="text-2xl font-bold mb-6 text-center">Agregar nueva obra</h2>

            {error && <p className="text-red-500 mb-4 text-center">{error}</p>}
            {success && (
                <div className="flex items-center justify-center mb-4 text-green-600">
                    <img src={toastIcon} alt="" className="w-5 h-5 mr-2" /> {success}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded-lg shadow text-black">
                {/* Nombre */}
                <div>
                    <label className="block text-gray-700">Nombre de la obra</label>
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded"
                    />
                </div>

                {/* Técnicas (Múltiples dropdowns) */}
                <div>
                    <label className="block text-gray-700 mb-2">Técnicas *</label>
                    {techniques.map((technique, index) => (
                        <div key={index} className="flex gap-2 mb-2">
                            <select
                                value={technique}
                                onChange={(e) => handleTechniqueChange(index, e.target.value)}
                                required={index === 0}
                                className="flex-1 px-3 py-2 border rounded"
                            >
                                <option value="">Selecciona una técnica</option>
                                {TECHNIQUE_OPTIONS.map(tech => (
                                    <option key={tech} value={tech}>
                                        {tech}
                                    </option>
                                ))}
                            </select>
                            {techniques.length > 1 && (
                                <button
                                    type="button"
                                    onClick={() => removeTechnique(index)}
                                    className="px-3 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                                >
                                    ✕
                                </button>
                            )}
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={addTechnique}
                        className="text-sm text-blue-600 hover:text-blue-800 font-medium"
                    >
                        + Agregar otra técnica
                    </button>
                    <p className="text-xs text-gray-500 mt-1">
                        Seleccionadas: {techniques.filter(t => t !== '').join(', ') || 'Ninguna'}
                    </p>
                </div>

                {/* Categorías (Múltiples dropdowns) */}
                <div>
                    <label className="block text-gray-700 mb-2">Categorías *</label>
                    {categories.map((category, index) => (
                        <div key={index} className="flex gap-2 mb-2">
                            <select
                                value={category}
                                onChange={(e) => handleCategoryChange(index, e.target.value)}
                                required={index === 0}
                                className="flex-1 px-3 py-2 border rounded"
                            >
                                <option value="">Selecciona una categoría</option>
                                {CATEGORY_OPTIONS.map(cat => (
                                    <option key={cat} value={cat}>
                                        {cat}
                                    </option>
                                ))}
                            </select>
                            {categories.length > 1 && (
                                <button
                                    type="button"
                                    onClick={() => removeCategory(index)}
                                    className="px-3 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                                >
                                    ✕
                                </button>
                            )}
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={addCategory}
                        className="text-sm text-blue-600 hover:text-blue-800 font-medium"
                    >
                        + Agregar otra categoría
                    </button>
                    <p className="text-xs text-gray-500 mt-1">
                        Seleccionadas: {categories.filter(c => c !== '').join(', ') || 'Ninguna'}
                    </p>
                </div>

                {/* Materiales */}
                <div>
                    <label className="block text-gray-700">Materiales</label>
                    <input
                        name="materials"
                        value={form.materials}
                        onChange={handleChange}
                        className="w-full mt-1 px-3 py-2 border rounded"
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
                        className="w-full mt-1 px-3 py-2 border rounded"
                    />
                </div>

                {/* Precio y Stock */}
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-gray-700">Precio (Bs.)</label>
                        <input
                            name="price"
                            type="number"
                            step="0.01"
                            min="0"
                            value={form.price}
                            onChange={handleChange}
                            required
                            className="w-full mt-1 px-3 py-2 border rounded"
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700">Stock</label>
                        <input
                            name="stock"
                            type="number"
                            min="0"
                            value={form.stock}
                            onChange={handleChange}
                            required
                            className="w-full mt-1 px-3 py-2 border rounded"
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
                        className="w-full mt-1 px-3 py-2 border rounded"
                    >
                        <option value="AVAILABLE">Disponible</option>
                        <option value="UNAVAILABLE">No disponible</option>
                    </select>
                </div>

                {/* Imagen */}
                <div>
                    <label className="block text-gray-700 mb-1">Imagen</label>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="w-full mt-1 p-4 border-2 border-dashed border-gray-400 rounded-lg cursor-pointer hover:border-gray-600 focus:outline-none focus:border-indigo-500"
                    />
                    {fileData.fileName && (
                        <p className="mt-1 text-sm text-gray-600">
                            Seleccionado: <span className="font-medium">{fileData.fileName}</span>
                        </p>
                    )}
                </div>

                {/* Botones */}
                <div className="flex gap-3">
                    {onCancel && (
                        <button
                            type="button"
                            onClick={handleCancel}
                            disabled={loading}
                            className="flex-1 py-2 font-medium rounded transition bg-gray-300 text-black hover:bg-gray-400"
                        >
                            Cancelar
                        </button>
                    )}
                    <button
                        type="submit"
                        disabled={loading}
                        className={`flex-1 py-2 font-medium rounded transition ${
                            loading
                                ? 'bg-gray-400 cursor-not-allowed'
                                : 'bg-black text-white hover:bg-gray-900'
                        }`}
                    >
                        {loading ? 'Guardando...' : 'Crear Obra'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default AddArt;