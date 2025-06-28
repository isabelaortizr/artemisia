import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AddArt from './AddArt';
import MyWorks from './MyWorks';
import { assets } from '../assets/assets';

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
    <div className="relative min-h-screen flex flex-col bg-black text-white py-10 px-4 overflow-hidden">
      {/* Fondo con imagen y overlay */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        style={{
          backgroundImage: `url(${assets.register_img})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          zIndex: 0,
        }}
      />

      <div className="relative z-10">
        {/* Botón de perfil con estilo */}
        <div className="flex justify-end max-w-7xl mx-auto mb-6">
          <button
            onClick={() => navigate('/profile')}
            className="bg-black hover:bg-gray-700 text-white px-6 py-2 rounded-xl text-sm font-medium transition"
          >
            My Profile
          </button>
        </div>

        <h1 className="text-4xl font-bold text-center mb-10">Seller Menu</h1>

        {/* Botones */}
        <div className="flex flex-col sm:flex-row justify-center gap-4 max-w-5xl mx-auto mb-10">
          <button
            onClick={() => setActiveSection('add')}
            className={`flex-1 py-3 rounded-xl text-lg font-medium transition ${
              activeSection === 'add' ? 'bg-white text-black' : 'bg-black hover:bg-white hover:text-black'
            }`}
          >
            Add your piece
          </button>
          <button
            onClick={() => setActiveSection('myworks')}
            className={`flex-1 py-3 rounded-xl text-lg font-medium transition ${
              activeSection === 'myworks' ? 'bg-white text-black' : 'bg-black hover:bg-white hover:text-black'
            }`}
          >
            Check my pieces
          </button>
          <button
            onClick={() => setActiveSection('catalog')}
            className={`flex-1 py-3 rounded-xl text-lg font-medium transition ${
              activeSection === 'catalog' ? 'bg-white text-black' : 'bg-black hover:bg-white hover:text-black'
            }`}
          >
            See catalogue
          </button>
        </div>

        {/* Contenido estilo glass igual que Login */}
        <div className="relative z-10 w-full max-w-6xl mx-auto">
          <div className="bg-zinc-900 bg-opacity-90 p-8 rounded-2xl shadow-xl border border-white/10">
            {activeSection === 'add' && <AddArt embedded dark />}
            {activeSection === 'myworks' && <MyWorks embedded dark />}
            {activeSection === 'catalog' && (
              <div className="text-center text-gray-300 text-lg py-10">
                (integrar catalogo¡¡¡¡¡)
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SellerMenu;
