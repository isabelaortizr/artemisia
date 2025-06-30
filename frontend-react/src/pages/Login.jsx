// src/pages/Login.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import { assets } from '../assets/assets';  // importar imágenes
import Navbar from '../components/Navbar';  // navbar
import Footer from '../components/Footer';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError]       = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const { token, user, userId, role } = await authService.login({ username, password });

            localStorage.setItem('authToken', token);
            localStorage.setItem('username',  user);
            localStorage.setItem('userId',    String(userId));
            localStorage.setItem('userRole',  role);

            if (role === 'SELLER') {
                navigate('/menu');
            } else {
                navigate('/products');
            }
        } catch (err) {
            setError(err.message || 'Error al autenticar');
        }
    };

    return (
        <div className="relative min-h-screen flex items-center justify-center bg-black px-4">
            <Navbar showSignUpButton={false} />

            {/* Fondo con imagen, overlay oscuro y blur */}
            <div
                className="absolute inset-0 bg-black/60 backdrop-blur-sm"
                style={{
                    backgroundImage: `url(${assets.register_img})`,  // misma imagen o cambia aquí si quieres otra
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    filter: 'brightness(0.5)',
                    zIndex: 0,
                }}
            />

            {/* Formulario encima del fondo */}
            <form
                onSubmit={handleSubmit}
                className="relative z-10 w-full max-w-md bg-zinc-900 bg-opacity-90 p-8 rounded-2xl shadow-xl border border-white/10 text-white"
            >
                <h2 className="text-3xl font-semibold text-center mb-6">Log In</h2>

                {error && <p className="text-red-500 text-sm mb-4 text-center">{error}</p>}

                <div className="mb-4">
                    <label htmlFor="username" className="block text-sm mb-2">User</label>
                    <input
                        id="username"
                        type="text"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                        placeholder="yourUser"
                        className="w-full px-4 py-2 bg-black border border-white/20 rounded-md focus:outline-none focus:ring-2 focus:ring-white placeholder:text-white/50"
                        required
                    />
                </div>

                <div className="mb-6">
                    <label htmlFor="password" className="block text-sm mb-2">Password</label>
                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        placeholder="••••••••"
                        className="w-full px-4 py-2 bg-black border border-white/20 rounded-md focus:outline-none focus:ring-2 focus:ring-white placeholder:text-white/50"
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="w-full py-3 bg-white text-black font-medium rounded-full hover:scale-105 transition duration-300"
                >
                    Login
                </button>

                <p className="mt-6 text-center text-white/70 text-sm">
                    Don't have an account yet?{' '}
                    <Link to="/register" className="text-white underline hover:text-amber-300 transition">
                        Sign Up!
                    </Link>
                </p>
            </form>
        </div>
    );
};

export default Login;