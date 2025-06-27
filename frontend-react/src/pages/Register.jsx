import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import userService from '../services/userService';
import addressService from '../services/addressService.js';
import { assets } from '../assets/assets';
import Navbar from '../components/Navbar';

const Register = () => {
    const [name, setName] = useState('');
    const [mail, setMail] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('BUYER');
    const [direction, setDirection] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            const user = await userService.createUser({ name, mail, password, role });
            await addressService.createAddress({
                direction,
                userId: user.id,
            });
            setSuccess('User and address created successfully. You can now log in.');
            setTimeout(() => navigate('/login'), 1200);
        } catch (err) {
            console.error(err);
            setError(err.message || 'Something went wrong.');
        }
    };

    return (
        <div className="relative min-h-screen flex items-center justify-center bg-black px-4">
            <Navbar showSignUpButton={false} />
            <div
                className="absolute inset-0 bg-black/60 backdrop-blur-sm"
                style={{
                    backgroundImage: `url(${assets.register_img})`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    filter: 'brightness(0.5)',
                    zIndex: 0,
                }}
            />

            <form
                onSubmit={handleSubmit}
                className="relative z-10 w-full max-w-md bg-zinc-900 bg-opacity-90 p-8 rounded-2xl shadow-xl border border-white/10 text-white"
            >
                <h2 className="text-3xl font-semibold text-center mb-6">Create an Account</h2>

                {error && <p className="text-red-500 text-sm mb-4 text-center">{error}</p>}
                {success && <p className="text-green-500 text-sm mb-4 text-center">{success}</p>}

                {/* Name */}
                <div className="mb-4">
                    <label htmlFor="name" className="block text-sm mb-2">Username</label>
                    <input
                        id="name"
                        type="text"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        placeholder="Your name"
                        className="w-full px-4 py-2 bg-black border border-white/20 rounded-md focus:outline-none focus:ring-2 focus:ring-white placeholder:text-white/50"
                        required
                    />
                </div>

                {/* Email */}
                <div className="mb-4">
                    <label htmlFor="mail" className="block text-sm mb-2">Email</label>
                    <input
                        id="mail"
                        type="email"
                        value={mail}
                        onChange={e => setMail(e.target.value)}
                        placeholder="your@email.com"
                        className="w-full px-4 py-2 bg-black border border-white/20 rounded-md focus:outline-none focus:ring-2 focus:ring-white placeholder:text-white/50"
                        required
                    />
                </div>

                {/* Password */}
                <div className="mb-4">
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

                {/* Address */}
                {/* <div className="mb-4">
          <label htmlFor="direction" className="block text-sm mb-2">Address</label>
          <input
            id="direction"
            type="text"
            value={direction}
            onChange={e => setDirection(e.target.value)}
            placeholder="Your address"
            className="w-full px-4 py-2 bg-black border border-white/20 rounded-md focus:outline-none focus:ring-2 focus:ring-white placeholder:text-white/50"
            required
          />
        </div> */}

                {/* Role */}
                <div className="mb-6">
                    <label htmlFor="role" className="block text-sm mb-2">
                        I want to sign up as...
                    </label>
                    <div className="relative">
                        <select
                            id="role"
                            value={role}
                            onChange={e => setRole(e.target.value)}
                            className="w-full appearance-none bg-black text-white border border-white/20 rounded-md px-4 py-3 pr-10 focus:outline-none focus:ring-2 focus:ring-white"
                        >
                            <option value="BUYER">Buyer</option>
                            <option value="SELLER">Seller</option>
                        </select>
                        <div className="pointer-events-none absolute inset-y-0 right-3 flex items-center text-white select-none">
                            ▼
                        </div>
                    </div>
                </div>

                {/* Submit */}
                <button
                    type="submit"
                    className="w-full py-3 bg-white text-black font-medium rounded-full hover:scale-105 transition duration-300"
                >
                    Create Account
                </button>

                <p className="mt-6 text-center text-white/70 text-sm">
                    Already have an account?{' '}
                    <Link to="/login" className="text-white underline hover:text-amber-300 transition">
                        Login
                    </Link>
                </p>
            </form>
        </div>
    );
};

export default Register;