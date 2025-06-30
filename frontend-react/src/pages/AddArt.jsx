// src/pages/AddArt.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import productService          from '../services/productService';
import imageService            from '../services/imageService';
import toastIcon               from '../assets/toast-icon.png';

const AddArt = ({ embedded, dark }) => {
    const [form, setForm] = useState({
        name: '',
        technique: '',
        category: '',
        materials: '',
        description: '',
        price: '',
        stock: '',
        status: 'AVAILABLE'
    });
    const [fileData, setFileData] = useState({ fileName: '', base64Image: '' });
    const [loading, setLoading]   = useState(false);
    const [error, setError]       = useState('');
    const [success, setSuccess]   = useState('');
    const navigate                = useNavigate();
    const sellerId                = Number(localStorage.getItem('userId'));

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
        setError(''); setSuccess('');

        // Validaciones extra por si el navegador no las impide
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
            // 1) Creamos el producto sin imagen
            const newProd = await productService.createProduct({
                sellerId,
                name:        form.name,
                technique:   form.technique,
                category:    form.category,
                materials:   form.materials,
                description: form.description,
                price:       parseFloat(form.price),
                stock:       parseInt(form.stock, 10),
                status:      form.status
            });

            // 2) Si hay imagen, la subimos al endpoint /api/images/upload
            if (fileData.base64Image) {
                await imageService.uploadImage({
                    productId:   newProd.productId,
                    fileName:    fileData.fileName,
                    base64Image: fileData.base64Image
                });
            }

            setSuccess('¡Piece created successfully!');
            // setTimeout(() => navigate('/myworks'), 2000);
        } catch (err) {
            // setError(err.message);
            const msg = err.message || '';
            if (msg.includes('Data Not Found') || msg.includes('INTERNAL_SERVER_ERROR') || msg.includes('400')) {
                setError('Ocurrio un error al crear su obra, vuelva a intentarlo');
            } else {
                setError(err.message || 'Error al autenticar');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={`relative max-w-lg mx-auto p-6 ${embedded ? '' : ''}`}>
            <h2 className="text-2xl font-bold mb-6 text-center">Add new piece</h2>

            {error && <p className="text-red-500 mb-4 text-center">{error}</p>}
            {success && (
                <div className="flex items-center justify-center mb-4 text-green-600">
                    <img src={toastIcon} alt="" className="w-5 h-5 mr-2" /> {success}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded-lg shadow text-black">
                {/* Nombre */}
                <div>
                    <label className="block text-gray-700">Piece Name</label>
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded"
                    />
                </div>

                {/* Técnica */}
                <div>
                    <label className="block text-gray-700">Technique</label>
                    <select
                        name="technique"
                        value={form.technique}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded"
                    >
                        <option value="">Select a technique</option>
                        <option value="Óleo">Óleo</option>
                        <option value="Acrílico">Acrílico</option>
                        <option value="Acuarela">Acuarela</option>
                        <option value="Temple">Temple</option>
                        <option value="Fresco">Fresco</option>
                        <option value="Gouache">Gouache</option>
                        <option value="Tinta">Tinta</option>
                        <option value="Mixta">Mixta</option>
                        <option value="Spray">Spray</option>
                        <option value="Digital">Digital</option>
                    </select>
                </div>

                {/* Categoría */}
                <div>
                    <label className="block text-gray-700">Category</label>
                    <select
                        name="category"
                        value={form.category}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded"
                    >
                        <option value="">Select a category</option>
                        <option value="Realista">Realista</option>
                        <option value="Abstracta">Abstracta</option>
                        <option value="Expresionista">Expresionista</option>
                        <option value="Impresionista">Impresionista</option>
                        <option value="Surrealista">Surrealista</option>
                        <option value="Conceptual">Conceptual</option>
                        <option value="Religiosa">Religiosa</option>
                        <option value="Histórica">Histórica</option>
                        <option value="Decorativa">Decorativa</option>
                        <option value="Contemporánea">Contemporánea</option>
                    </select>
                </div>

                {/* Materiales */}
                <div>
                    <label className="block text-gray-700">Materials</label>
                    <input
                        name="materials"
                        value={form.materials}
                        onChange={handleChange}
                        className="w-full mt-1 px-3 py-2 border rounded"
                    />
                </div>

                {/* Descripción */}
                <div>
                    <label className="block text-gray-700">Description</label>
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
                        <label className="block text-gray-700">Price (Bs.)</label>
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
                    <label className="block text-gray-700">Status</label>
                    <select
                        name="status"
                        value={form.status}
                        onChange={handleChange}
                        className="w-full mt-1 px-3 py-2 border rounded"
                    >
                        <option value="AVAILABLE">Available</option>
                        <option value="UNAVAILABLE">Unavailable</option>
                    </select>
                </div>

                {/* Imagen */}
                <div>
                    <label className="block text-gray-700 mb-1">Picture</label>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="w-full mt-1 p-4 border-2 border-dashed border-gray-400 rounded-lg cursor-pointer
                        hover:border-gray-600 focus:outline-none focus:border-indigo-500"
                    />
                    {fileData.fileName && (
                        <p className="mt-1 text-sm text-gray-600">
                            Selecting: <span className="font-medium">{fileData.fileName}</span>
                        </p>
                    )}
                </div>


                {/* Botón */}
                <button
                    type="submit"
                    disabled={loading}
                    className={`w-full py-2 font-medium rounded transition ${
                        loading
                            ? 'bg-gray-400 cursor-not-allowed'
                            : 'bg-black text-white hover:bg-gray-900'
                    }`}
                >
                    {loading ? 'Saving...' : 'Create Piece'}
                </button>
            </form>
        </div>
    );
};

export default AddArt;
