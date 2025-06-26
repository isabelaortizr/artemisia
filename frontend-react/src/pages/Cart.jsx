// src/pages/Cart.jsx
import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import notaVentaService from "../services/notaVentaService";
import backIcon        from "../assets/back-icon.png";
import Navbar          from '../components/Navbar';
import { assets }      from "../assets/assets";

const Cart = () => {
    const [cart,    setCart]    = useState(null);
    const [loading, setLoading] = useState(true);
    const [error,   setError]   = useState(null);
    const navigate = useNavigate();
    const userId   = localStorage.getItem("userId");

    const fetchCart = async () => {
        setLoading(true);
        try {
            const data = await notaVentaService.getCart(userId);
            setCart(data);
        } catch (err) {
            setError(err.message || "Error al cargar el carrito");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!userId) {
            navigate("/login", { replace: true });
            return;
        }
        fetchCart();
    }, [navigate]);

    const handleDecrease = async (item) => {
        // calculamos la nueva cantidad (0 elimina)
        const newQty = item.quantity - 1;
        setLoading(true);
        try {
            // Llamamos al endpoint PUT /order_detail/update_stock
            const updatedCart = await notaVentaService.updateOrderDetailStock({
                userId,
                productId: item.productId,
                quantity:  newQty
            });
            // refrescamos directamente con la respuesta
            setCart(updatedCart);
        } catch (err) {
            setError(err.message || "Error al actualizar el carrito");
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <p className="text-center mt-10 text-white">Cargando carrito...</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">{error}</p>;

    const Header = ({ title }) => (
        <div className="flex items-center mb-6">
            <Link to="/products" className="mr-4">
                <img src={backIcon} alt="Volver" className="w-10 h-10 hover:opacity-80 transition" />
            </Link>
            <h2 className="text-3xl font-bold text-white">{title}</h2>
        </div>
    );

    return (
        <div
            className="min-h-screen bg-cover bg-center relative"
            style={{ backgroundImage: `url(${assets.register_img})` }}
        >
            {/* Overlay oscuro para contraste y blur */}
            <div className="absolute inset-0 bg-black/60 backdrop-blur-sm z-0"></div>

            <div className="relative z-10 max-w-4xl mx-auto p-6 pt-28">
                <Navbar showSignUpButton={false} />

                {!cart?.detalles?.length ? (
                    <div className="bg-black/50 backdrop-blur-md rounded-xl p-8 border border-white/20 shadow-lg text-center">
                        <Header title="Tu carrito está vacío" />
                        <button
                            onClick={() => navigate("/products")}
                            className="mt-6 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-6 py-3 rounded-full transition"
                        >
                            Volver al catálogo
                        </button>
                    </div>
                ) : (
                    <div className="bg-black/50 backdrop-blur-md rounded-xl p-8 border border-white/20 shadow-lg text-white">
                        <Header title="Tu Carrito" />
                        <ul className="space-y-6">
                            {cart.detalles.map(item => (
                                <li
                                    key={item.id}
                                    className="flex justify-between items-center border border-white/10 rounded-lg p-4"
                                >
                                    <div>
                                        <p className="font-semibold text-lg">{item.productName}</p>
                                    </div>
                                    <div className="flex items-center">
                                        <p className="text-gray-300 mr-2 text-sm">
                                            Cantidad: {item.quantity}
                                        </p>
                                        <button
                                            onClick={() => handleDecrease(item)}
                                            disabled={loading || item.quantity <= 0}
                                            className="px-2 py-1 bg-red-600 hover:bg-red-700 text-white rounded transition"
                                        >
                                            −
                                        </button>
                                    </div>
                                    <p className="font-bold text-lg">${item.total.toFixed(2)}</p>
                                </li>
                            ))}
                        </ul>

                        <div className="mt-8 flex justify-end">
                            <p className="text-2xl font-bold text-white">
                                Total: ${cart.totalGlobal.toFixed(2)}
                            </p>
                        </div>

                        <div className="mt-8 flex justify-end">
                            <button
                                onClick={() => navigate('/checkout')}
                                className="bg-green-600 hover:bg-green-700 text-white font-semibold px-8 py-3 rounded-full transition"
                            >
                                Finalizar compra
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Cart;
