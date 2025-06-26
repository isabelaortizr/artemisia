// src/pages/Profile.jsx
import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import userService from '../services/userService';
import backIcon    from '../assets/back-icon.png'; // tu icono de volver

const Profile = () => {
    const [user, setUser]       = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const id = localStorage.getItem('userId');
        if (!id) {
            navigate('/login');
            return;
        }

        userService.getUserById(id)
            .then(data => setUser(data))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [navigate]);

    if (loading) return <p className="text-center mt-10">Cargando perfil…</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">Error: {error}</p>;

    return (
        <div className="relative max-w-md mx-auto p-6">
            {/* Botón volver */}
            <Link to="/products" className="absolute top-6 left-6">
                <img
                    src={backIcon}
                    alt="Volver"
                    className="w-8 h-8 hover:opacity-80 transition"
                />
            </Link>

            <h2 className="text-2xl font-bold mb-6 text-center">Mi Perfil</h2>

            {/* Card con datos del usuario */}
            {/* Le añadimos text-gray-900 para que el texto sea oscuro sobre el fondo blanco */}
            <div className="space-y-4 bg-white p-6 rounded-lg shadow text-gray-900">
                {/* Nombre */}
                <div>
                    <p className="text-gray-600">Nombre</p>
                    <p className="font-medium">{user.name}</p>
                </div>
                {/* Correo */}
                <div>
                    <p className="text-gray-600">Correo</p>
                    <p className="font-medium">{user.mail}</p>
                </div>
                {/* Rol */}
                <div>
                    <p className="text-gray-600">Rol</p>
                    <p className="font-medium">{user.role}</p>
                </div>
            </div>
        </div>
    );
};

export default Profile;
