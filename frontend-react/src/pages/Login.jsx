// src/pages/Login.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError]       = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // authService.login debe hacer POST a tu endpoint de Spring Boot,
            // devolviendo { token, user } o lanzando un error
            // Aquí destruturamos token y user de la respuesta:
            const { token, user } = await authService.login({ username, password });

            // Guardamos el token para futuras peticiones
            localStorage.setItem('authToken', token);
            // (Opcional) guarda también info del usuario si la necesitas más tarde
            localStorage.setItem('username', user);

            // Redirigimos al catálogo
            navigate('/products');  // o la ruta que definas para el catálogo
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.message ||
                'Error al autenticar'
            );
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
                <form
                    onSubmit={handleSubmit}
                    className="bg-white p-8 rounded-lg shadow-md w-full max-w-sm"
                >
                    <h2 className="text-2xl text-black font-bold mb-6 text-center">Iniciar Sesión</h2>

                    {error && (
                        <p className="text-red-500 text-sm mb-4">{error}</p>
                    )}

                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2" htmlFor="username">
                            Usuario
                        </label>
                        <input
                            id="username"
                            type="text"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-gray-400 text-black"
                            placeholder="Tu usuario"
                            required
                        />
                    </div>


                    <div className="mb-6">
                        <label className="block text-gray-700 mb-2" htmlFor="password">
                            Contraseña
                        </label>
                        <input
                            id="password"
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-gray-400 text-black"
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        className="w-full py-2 bg-indigo-600 text-white font-medium rounded hover:bg-indigo-700 transition"
                    >
                        Entrar
                    </button>
                </form>
        </div>
    );
};

export default Login;