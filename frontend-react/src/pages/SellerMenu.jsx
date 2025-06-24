// src/pages/SellerMenu.jsx
import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import profileIcon from '../assets/profile-icon.png'; // tu icono de perfil

const SellerMenu = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const role = localStorage.getItem('userRole');
        if (role !== 'SELLER') {
            navigate('/products', { replace: true });
        }
    }, [navigate]);

    return (
        <div className="relative max-w-md mx-auto p-6 space-y-6">
            {/* Botón de perfil */}
            <Link to="/profile" className="absolute top-6 right-6">
                <img
                    src={profileIcon}
                    alt="Perfil"
                    className="w-8 h-8 hover:opacity-80 transition"
                />
            </Link>

            <h1 className="text-3xl font-bold text-center mb-4">Menú de Vendedor</h1>
            <div className="flex flex-col gap-4">
                <Link
                    to="/add-art"
                    className="block text-center bg-indigo-600 text-white py-3 rounded hover:bg-indigo-700 transition">
                    Agregar Arte
                </Link>
                <Link
                    to="/seller/catalog"
                    className="block text-center bg-indigo-600 text-white py-3 rounded hover:bg-indigo-700 transition">
                    Ver Catálogo de Arte
                </Link>
                <Link
                    to="/my-works"
                    className="block text-center bg-indigo-600 text-white py-3 rounded hover:bg-indigo-700 transition">
                    Ver Mis Obras
                </Link>
            </div>
        </div>
    );
};

export default SellerMenu;
