// src/pages/Cart.jsx
import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import notaVentaService from "../services/notaVentaService";
// importa tu icono de “volver” (ajusta la ruta)
import backIcon from "../assets/back-icon.png";

const Cart = () => {
    const [cart, setCart]       = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const userId = localStorage.getItem("userId");
        if (!userId) {
            navigate("/login");
            return;
        }

        setLoading(true);
        notaVentaService.getCart(userId)
            .then(data => setCart(data))
            .catch(err => setError(err.message || "Error al cargar el carrito"))
            .finally(() => setLoading(false));
    }, [navigate]);

    if (loading) return <p className="text-center mt-10">Cargando carrito...</p>;
    if (error)   return <p className="text-center mt-10 text-red-500">{error}</p>;

    // Contenedor para icon + título
    const Header = ({ title }) => (
        <div className="flex items-center mb-6">
            <Link to="/products" className="mr-4">
                <img
                    src={backIcon}
                    alt="Volver"
                    className="w-10 h-10 hover:opacity-80 transition"
                />
            </Link>
            <h2 className="text-2xl font-bold">{title}</h2>
        </div>
    );

    // estado vacío
    if (!cart?.detalles?.length) {
        return (
            <div className="max-w-3xl mx-auto p-6">
                <Header title="Tu carrito está vacío" />

                <button
                    onClick={() => navigate("/products")}
                    className="mt-4 bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition"
                >
                    Volver al catálogo
                </button>
            </div>
        );
    }

    // estado con productos
    return (
        <div className="max-w-4xl mx-auto p-6">
            <Header title="Tu Carrito" />

            <ul className="space-y-4">
                {cart.detalles.map(item => (
                    <li
                        key={item.id}
                        className="flex justify-between items-center border p-4 rounded"
                    >
                        <div>
                            <p className="font-medium">{item.productName}</p>
                            <p className="text-sm text-gray-600">
                                Cantidad: {item.quantity}
                            </p>
                        </div>
                        <p className="font-semibold">${item.total.toFixed(2)}</p>
                    </li>
                ))}
            </ul>

            <div className="mt-6 flex justify-end">
                <p className="text-xl font-bold">
                    Total: ${cart.totalGlobal.toFixed(2)}
                </p>
            </div>

            <div className="mt-6 flex justify-end">
                <button
                    onClick={() => navigate('/checkout', { state: { orderId: cart.id } })}
                    className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700 transition">
                    Finalizar compra
                </button>
            </div>
        </div>
    );
};

export default Cart;
