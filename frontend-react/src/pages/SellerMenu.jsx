import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AddArt from './AddArt';
import MyWorks from './MyWorks';
import { assets } from '../assets/assets';
import Navbar from '../components/Navbar';

const SellerMenu = () => {
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

                {/* Botones de navegación - Quitamos "Add your piece" */}
                {/* Contenedor estilo glass */}
                <div className="bg-zinc-900 bg-opacity-90 p-8 rounded-2xl shadow-xl border border-white/10">
                    <MyWorks embedded dark />
                </div>
            </div>
        </div>
    );
};

export default SellerMenu;