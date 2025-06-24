// src/pages/Register.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import userService from '../services/userService';
import addressService from '../services/addressService.js';

const Register = () => {
    const [name, setName]         = useState('');
    const [mail, setMail]         = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole]         = useState('BUYER');
    const [direction, setDirection] = useState(''); // ← NUEVO campo
    const [error, setError]       = useState('');
    const [success, setSuccess]   = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            // 1) Creamos el usuario
            const user = await userService.createUser({ name, mail, password, role });
            // user === { id, name, mail, role }

            // 2) Creamos la dirección asociada
            await addressService.createAddress({
                direction,
                userId: user.id
            });

            setSuccess('Usuario y dirección creados correctamente. Ya puedes iniciar sesión.');
            setTimeout(() => navigate('/login'), 1200);

        } catch (err) {
            console.error(err);
            setError(err.message);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <form
                onSubmit={handleSubmit}
                className="bg-white p-8 rounded-lg shadow-md w-full max-w-sm"
            >
                <h2 className="text-2xl text-black font-bold mb-6 text-center">Regístrate</h2>

                {error   && <p className="text-red-500 text-sm mb-4">{error}</p>}
                {success && <p className="text-green-600 text-sm mb-4">{success}</p>}

                {/* Nombre */}
                <div className="mb-4">
                    <label htmlFor="name" className="block text-gray-700 mb-2">Nombre</label>
                    <input
                        id="name" type="text" value={name}
                        onChange={e => setName(e.target.value)}
                        placeholder="Tu nombre"
                        className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-gray-400 text-black"
                        required
                    />
                </div>

                {/* Correo */}
                <div className="mb-4">
                    <label htmlFor="mail" className="block text-gray-700 mb-2">Correo</label>
                    <input
                        id="mail" type="email" value={mail}
                        onChange={e => setMail(e.target.value)}
                        placeholder="tu@correo.com"
                        className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-gray-400 text-black"
                        required
                    />
                </div>

                {/* Contraseña */}
                <div className="mb-4">
                    <label htmlFor="password" className="block text-gray-700 mb-2">Contraseña</label>
                    <input
                        id="password" type="password" value={password}
                        onChange={e => setPassword(e.target.value)}
                        placeholder="••••••••"
                        className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-gray-400 text-black"
                        required
                    />
                </div>

                {/* Rol */}
                <div className="mb-4">
                    <label htmlFor="role" className="block text-gray-700 mb-2">Rol</label>
                    <select
                        id="role" value={role}
                        onChange={e => setRole(e.target.value)}
                        className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 text-black"
                    >
                        <option value="BUYER">Comprador</option>
                        <option value="SELLER">Vendedor</option>
                    </select>
                </div>

                {/* Dirección */}
                <div className="mb-6">
                    <label htmlFor="direction" className="block text-gray-700 mb-2">Dirección</label>
                    <input
                        id="direction" type="text" value={direction}
                        onChange={e => setDirection(e.target.value)}
                        placeholder="Tu dirección"
                        className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-gray-400 text-black"
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="w-full py-2 bg-green-600 text-white font-medium rounded hover:bg-green-700 transition"
                >
                    Crear cuenta
                </button>

                <p className="mt-4 text-center text-gray-600">
                    ¿Ya tienes cuenta?{' '}
                    <Link to="/login" className="text-indigo-600 hover:underline">
                        Inicia sesión
                    </Link>
                </p>
            </form>
        </div>
    );
};

export default Register;
