// src/pages/Register.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import userService from '../services/userService';
import { assets } from '../assets/assets';
import Navbar from '../components/Navbar';

// Definir las opciones disponibles
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

const Register = () => {
  const [name, setName] = useState('');
  const [mail, setMail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('BUYER');
  const [selectedTechniques, setSelectedTechniques] = useState([]);
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);
  const navigate = useNavigate();

  // Manejar selección de técnicas
  const handleTechniqueChange = (technique) => {
    setSelectedTechniques(prev => {
      if (prev.includes(technique)) {
        return prev.filter(t => t !== technique);
      } else {
        return [...prev, technique];
      }
    });
  };

  // Manejar selección de categorías
  const handleCategoryChange = (category) => {
    setSelectedCategories(prev => {
      if (prev.includes(category)) {
        return prev.filter(c => c !== category);
      } else {
        return [...prev, category];
      }
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsRegistering(true);

    try {
      console.log('Starting registration process...');
      console.log('User preferences:', {
        techniques: selectedTechniques,
        categories: selectedCategories
      });

      // 1. Crear el usuario con las preferencias
      const userData = {
        name,
        mail,
        password,
        role,
        preferences: {
          techniques: selectedTechniques,
          categories: selectedCategories
        }
      };

      await userService.createUser(userData);
      console.log('User created successfully with preferences');

      setSuccess('Account created successfully! Please login to continue.');

      // Redirigir al login después de un tiempo
      setTimeout(() => {
        navigate('/login', {
          state: {
            message: 'Account created successfully! Please login.',
            prefilledEmail: mail
          }
        });
      }, 2000);

    } catch (err) {
      console.error('Registration error:', err);

      if (err.message?.includes('409') || err.message?.includes('already exists')) {
        setError('This email is already registered. Please use a different email or login.');
      } else {
        setError(err.message || 'Something went wrong during registration.');
      }
    } finally {
      setIsRegistering(false);
    }
  };

  return (
      <div className="relative min-h-screen bg-black">
        <Navbar showSignUpButton={false} />

        {/* Contenedor principal con más margen superior */}
        <div className="pt-24 pb-8 px-4"> {/* Aumentado pt-24 para bajar el contenido */}
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
              className="relative z-10 w-full max-w-4xl mx-auto bg-zinc-900 bg-opacity-90 p-8 rounded-2xl shadow-xl border border-white/10 text-white"
          >
            <h2 className="text-3xl font-semibold text-center mb-6">Create an Account</h2>

            {error && (
                <div className="bg-red-500/20 border border-red-500 rounded-lg p-3 mb-4">
                  <p className="text-red-300 text-sm text-center">{error}</p>
                </div>
            )}

            {success && (
                <div className="bg-green-500/20 border border-green-500 rounded-lg p-3 mb-4">
                  <p className="text-green-300 text-sm text-center">{success}</p>
                </div>
            )}

            <div className="grid md:grid-cols-2 gap-8">
              {/* Columna 1: Información básica */}
              <div className="space-y-4">
                <h3 className="text-xl font-semibold mb-4">Basic Information</h3>

                {/* Name */}
                <div>
                  <label htmlFor="name" className="block text-sm mb-2">Username</label>
                  <input
                      id="name"
                      type="text"
                      value={name}
                      onChange={e => setName(e.target.value)}
                      placeholder="Your name"
                      className="w-full px-4 py-2 bg-black border border-white/20 rounded-md focus:outline-none focus:ring-2 focus:ring-white placeholder:text-white/50"
                      required
                      disabled={isRegistering}
                  />
                </div>

                {/* Email */}
                <div>
                  <label htmlFor="mail" className="block text-sm mb-2">Email</label>
                  <input
                      id="mail"
                      type="email"
                      value={mail}
                      onChange={e => setMail(e.target.value)}
                      placeholder="your@email.com"
                      className="w-full px-4 py-2 bg-black border border-white/20 rounded-md focus:outline-none focus:ring-2 focus:ring-white placeholder:text-white/50"
                      required
                      disabled={isRegistering}
                  />
                </div>

                {/* Password */}
                <div>
                  <label htmlFor="password" className="block text-sm mb-2">Password</label>
                  <input
                      id="password"
                      type="password"
                      value={password}
                      onChange={e => setPassword(e.target.value)}
                      placeholder="••••••••"
                      className="w-full px-4 py-2 bg-black border border-white/20 rounded-md focus:outline-none focus:ring-2 focus:ring-white placeholder:text-white/50"
                      required
                      disabled={isRegistering}
                      minLength="6"
                  />
                </div>

                {/* Role */}
                <div>
                  <label htmlFor="role" className="block text-sm mb-2">
                    I want to sign up as...
                  </label>
                  <div className="relative">
                    <select
                        id="role"
                        value={role}
                        onChange={e => setRole(e.target.value)}
                        className="w-full appearance-none bg-black text-white border border-white/20 rounded-md px-4 py-3 pr-10 focus:outline-none focus:ring-2 focus:ring-white"
                        disabled={isRegistering}
                    >
                      <option value="BUYER">Buyer</option>
                      <option value="SELLER">Seller</option>
                    </select>
                  </div>
                </div>
              </div>

              {/* Columna 2: Preferencias de arte */}
              <div className="space-y-6">
                <h3 className="text-xl font-semibold mb-4">Art Preferences</h3>
                <p className="text-sm text-white/70 mb-4">
                  Tell us about your art preferences to help personalize your experience.
                </p>

                {/* Técnicas */}
                <div>
                  <label className="block text-sm font-medium mb-3">
                    Which art techniques interest you?
                  </label>
                  <div className="grid grid-cols-2 gap-2 max-h-40 overflow-y-auto p-3 border border-white/20 rounded-md bg-black/50">
                    {TECHNIQUE_OPTIONS.map(technique => (
                        <label key={technique} className="flex items-center space-x-2 cursor-pointer p-2 hover:bg-white/10 rounded transition">
                          <input
                              type="checkbox"
                              checked={selectedTechniques.includes(technique)}
                              onChange={() => handleTechniqueChange(technique)}
                              className="rounded text-blue-600 focus:ring-blue-500"
                              disabled={isRegistering}
                          />
                          <span className="text-sm text-white">{technique}</span>
                        </label>
                    ))}
                  </div>
                  <p className="text-xs text-white/50 mt-2">
                    Selected: {selectedTechniques.length > 0 ? selectedTechniques.join(', ') : 'None'}
                  </p>
                </div>

                {/* Categorías */}
                <div>
                  <label className="block text-sm font-medium mb-3">
                    Which art categories appeal to you?
                  </label>
                  <div className="grid grid-cols-2 gap-2 max-h-40 overflow-y-auto p-3 border border-white/20 rounded-md bg-black/50">
                    {CATEGORY_OPTIONS.map(category => (
                        <label key={category} className="flex items-center space-x-2 cursor-pointer p-2 hover:bg-white/10 rounded transition">
                          <input
                              type="checkbox"
                              checked={selectedCategories.includes(category)}
                              onChange={() => handleCategoryChange(category)}
                              className="rounded text-blue-600 focus:ring-blue-500"
                              disabled={isRegistering}
                          />
                          <span className="text-sm text-white">{category}</span>
                        </label>
                    ))}
                  </div>
                  <p className="text-xs text-white/50 mt-2">
                    Selected: {selectedCategories.length > 0 ? selectedCategories.join(', ') : 'None'}
                  </p>
                </div>
              </div>
            </div>

            {/* Submit Button */}
            <div className="mt-8 pt-6 border-t border-white/10">
              <button
                  type="submit"
                  disabled={isRegistering}
                  className={`w-full py-3 font-medium rounded-full transition duration-300 ${
                      isRegistering
                          ? 'bg-gray-400 cursor-not-allowed'
                          : 'bg-white text-black hover:scale-105'
                  }`}
              >
                {isRegistering ? 'Creating Account...' : 'Create Account'}
              </button>

              <p className="mt-6 text-center text-white/70 text-sm">
                Already have an account?{' '}
                <Link to="/login" className="text-white underline hover:text-amber-300 transition">
                  Login
                </Link>
              </p>
            </div>
          </form>
        </div>
      </div>
  );
};

export default Register;