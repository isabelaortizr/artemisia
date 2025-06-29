import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AddArt from './AddArt';
import MyWorks from './MyWorks';
import { assets } from '../assets/assets';
import Navbar from '../components/Navbar';

const SellerMenu = () => {
  const [activeSection, setActiveSection] = useState('add');
  const navigate = useNavigate();

  useEffect(() => {
    const role = localStorage.getItem('userRole');
    if (role !== 'SELLER') {
      navigate('/products', { replace: true });
    }
  }, [navigate]);

  return (
    <div className="relative min-h-screen bg-black text-white overflow-hidden">
      {/* Fondo con imagen y overlay */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm z-0"
        style={{
          backgroundImage: `url(${assets.register_img})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
        }}
      />

      {/* Navbar */}
      <Navbar showSignUpButton={false} />

      {/* Contenido */}
      <div className="relative z-10 pt-32 px-4 pb-12 max-w-7xl mx-auto">
        <h1 className="text-4xl font-bold text-center mb-10">Seller Menu</h1>

        {/* Botones de navegación */}
        <div className="flex flex-col sm:flex-row justify-center gap-4 mb-10">
          <button
            onClick={() => setActiveSection('add')}
            className={`flex-1 py-3 rounded-xl text-lg font-medium transition ${
              activeSection === 'add'
                ? 'bg-white text-black'
                : 'bg-black hover:bg-white hover:text-black border border-white/20'
            }`}
          >
            Add your piece
          </button>
          <button
            onClick={() => setActiveSection('myworks')}
            className={`flex-1 py-3 rounded-xl text-lg font-medium transition ${
              activeSection === 'myworks'
                ? 'bg-white text-black'
                : 'bg-black hover:bg-white hover:text-black border border-white/20'
            }`}
          >
            Check my pieces
          </button>
          <button
            onClick={() => setActiveSection('catalog')}
            className={`flex-1 py-3 rounded-xl text-lg font-medium transition ${
              activeSection === 'catalog'
                ? 'bg-white text-black'
                : 'bg-black hover:bg-white hover:text-black border border-white/20'
            }`}
          >
            See catalogue
          </button>
        </div>

        {/* Contenedor estilo glass */}
        <div className="bg-zinc-900 bg-opacity-90 p-8 rounded-2xl shadow-xl border border-white/10">
          {activeSection === 'add' && <AddArt embedded dark />}
          {activeSection === 'myworks' && <MyWorks embedded dark />}
          {activeSection === 'catalog' && (
            <div className="text-center text-gray-300 text-lg py-10">
              (integrar catálogo pronto...)
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SellerMenu;
