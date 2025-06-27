// src/pages/AddArt.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import productService from '../services/productService';
import backIcon       from '../assets/back-icon.png';
import toastIcon      from '../assets/toast-icon.png';

const AddArt = () => {
    const [form, setForm] = useState({
        name: '',
        technique: '',
        category: '',
        materials: '',
        description: '',
        price: '',
        stock: '',
        status: 'AVAILABLE',
        image: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError]     = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();
    const sellerId = localStorage.getItem('userId');

    const handleChange = e => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async e => {
        e.preventDefault();
        setError('');
        setSuccess('');
        if (!sellerId) {
            setError('Debes iniciar sesión como vendedor');
            return;
        }
        setLoading(true);
        try {
            await productService.createProduct({
                sellerId:    Number(sellerId),
                name:        form.name,
                technique:   form.technique,
                category:    form.category,
                materials:   form.materials,
                description: form.description,
                price:       parseFloat(form.price),
                stock:       parseInt(form.stock, 10),
                status:      form.status,
                image:       form.image
            });
            setSuccess('¡Obra creada con éxito!');
            setTimeout(() => navigate('/myworks'), 2000);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="relative max-w-lg mx-auto p-6">
            <Link to="/menu" className="absolute top-6 left-6">
                <img src={backIcon} alt="Volver" className="w-8 h-8 hover:opacity-80 transition" />
            </Link>

            <h2 className="text-2xl font-bold mb-6 text-center">Agregar Nueva Obra</h2>

            {error && <p className="text-red-500 mb-4 text-center">{error}</p>}
            {success && (
                <div className="flex items-center justify-center mb-4 text-green-600">
                    <img src={toastIcon} alt="" className="w-5 h-5 mr-2"/> {success}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded-lg shadow">
                {/* Nombre */}
                <div>
                    <label className="block text-gray-700">Nombre de la Obra</label>
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded text-black"
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
                        className="w-full mt-1 px-3 py-2 border rounded text-black"
                    >
                        <option value="">Selecciona una técnica</option>
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
                    <label className="block text-gray-700">Categoría</label>
                    <select
                        name="category"
                        value={form.category}
                        onChange={handleChange}
                        required
                        className="w-full mt-1 px-3 py-2 border rounded text-black"
                    >
                        <option value="">Selecciona una categoría</option>
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
                    <label className="block text-gray-700">Materiales</label>
                    <input
                        name="materials"
                        value={form.materials}
                        onChange={handleChange}
                        className="w-full mt-1 px-3 py-2 border rounded text-black"
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
                        className="w-full mt-1 px-3 py-2 border rounded text-black"
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
                            className="w-full mt-1 px-3 py-2 border rounded text-black"
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
                            className="w-full mt-1 px-3 py-2 border rounded text-black"
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
                        className="w-full mt-1 px-3 py-2 border rounded text-black"
                    >
                        <option value="AVAILABLE">Disponible</option>
                        <option value="UNAVAILABLE">No disponible</option>
                    </select>
                </div>

                {/* Imagen */}
                <div>
                    <label className="block text-gray-700">URL de la Imagen</label>
                    <input
                        name="image"
                        value={form.image}
                        onChange={handleChange}
                        placeholder="https://..."
                        className="w-full mt-1 px-3 py-2 border rounded text-black"
                    />
                </div>

                {/* Botón */}
                <button
                    type="submit"
                    disabled={loading}
                    className={`w-full py-2 font-medium rounded transition ${
                        loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700 text-white'
                    }`}
                >
                    {loading ? 'Guardando...' : 'Crear Obra'}
                </button>
            </form>
        </div>
    );
};

export default AddArt;
