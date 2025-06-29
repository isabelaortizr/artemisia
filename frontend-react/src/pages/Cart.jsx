// src/pages/Cart.jsx
import React, { useState, useEffect } from "react";
import { Link, useNavigate }            from "react-router-dom";
import notaVentaService                  from "../services/notaVentaService";
import addressService                    from "../services/addressService";
import backIcon                          from "../assets/back-icon.png";
import Navbar                            from '../components/Navbar';
import { assets }                        from "../assets/assets";

export default function Cart() {
    const navigate = useNavigate();
    const userId   = Number(localStorage.getItem("userId"));

    // estados
    const [cart,            setCart]          = useState(null);
    const [loading,         setLoading]       = useState(true);
    const [error,           setError]         = useState(null);
    const [addresses,       setAddresses]     = useState([]);
    const [addrLoading,     setAddrLoading]   = useState(true);
    const [selectedAddress, setSelectedAddress] = useState("");
    const [currency,        setCurrency]      = useState("BOB");

    // 1) carga inicial del carrito
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

    // 2) carga inicial de direcciones
    const fetchAddresses = async () => {
        setAddrLoading(true);
        try {
            const res  = await addressService.getAddressesByUser(userId);
            const list = Array.isArray(res) ? res
                : Array.isArray(res.content) ? res.content
                    : [];
            setAddresses(list);
            if (list.length > 0) {
                const firstAddr = list[0].address_id ?? list[0].addressId;
                setSelectedAddress(firstAddr);
            }
        } catch (err) {
            setError(err.message || "Error al cargar direcciones");
        } finally {
            setAddrLoading(false);
        }
    };

    // 3) montaje inicial
    useEffect(() => {
        if (!userId) return navigate("/login", { replace: true });
        fetchCart();
        fetchAddresses();
    }, [navigate]);

    // 4) asigno automáticamente la primera dirección al carrito
    useEffect(() => {
        if (!loading && !addrLoading && cart && addresses.length > 0) {
            const defaultAddr = addresses[0].address_id ?? addresses[0].addressId;
            if (Number(cart.buyerAddress) !== Number(defaultAddr)) {
                notaVentaService
                    .assignAddressToNotaVenta({ userId, addressId: defaultAddr })
                    .then(() => fetchCart())
                    .catch(e => setError(e.message || "Error al sincronizar dirección"));
            }
        }
    }, [loading, addrLoading, cart, addresses, userId]);

    // 5) cuando el usuario elige otra dirección
    const handleAddressChange = async e => {
        const newAddr = Number(e.target.value);
        setSelectedAddress(newAddr);
        try {
            await notaVentaService.assignAddressToNotaVenta({ userId, addressId: newAddr });
            await fetchCart();
        } catch (err) {
            setError(err.message || "Error al guardar dirección");
        }
    };

    // 6) disminuir cantidad de un item
    const handleDecrease = async item => {
        setLoading(true);
        try {
            const updated = await notaVentaService.updateOrderDetailStock({
                userId,
                productId: item.productId,
                quantity:  item.quantity - 1
            });
            setCart(updated);
        } catch (err) {
            setError(err.message || "Error al actualizar el carrito");
        } finally {
            setLoading(false);
        }
    };

    // 7) checkout
    const handleCheckout = async () => {
        if (!selectedAddress) {
            setError("Selecciona una dirección de envío");
            return;
        }
        try {
            const tx = await notaVentaService.createTransaction({
                userId,
                currency,
                chargeReason: "Compra en Artemisia",
                country:      "BO"
            });
            navigate("/checkout", {
                state: { transaction: tx, addressId: selectedAddress }
            });
        } catch (err) {
            setError(err.message || "Error al iniciar pago");
        }
    };

    // loaders / errores
    if (loading) return <p className="text-center mt-10 text-white">Cargando carrito…</p>;
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
            <div className="absolute inset-0 bg-black/60 backdrop-blur-sm z-0" />
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

                        {/* Dirección */}
                        <div className="mb-6">
                            <label className="block text-white mb-1">Dirección de envío</label>
                            {addrLoading ? (
                                <p className="text-white">Cargando direcciones…</p>
                            ) : (
                                <select
                                    value={selectedAddress}
                                    onChange={handleAddressChange}
                                    className="w-60 bg-black bg-opacity-30 border border-white/40 rounded p-2 text-white"
                                >
                                    {addresses.map(addr => (
                                        <option
                                            key={addr.address_id ?? addr.addressId}
                                            value={addr.address_id ?? addr.addressId}
                                        >
                                            {addr.street}, {addr.house_number} — {addr.city}, {addr.country}
                                        </option>
                                    ))}
                                </select>
                            )}
                        </div>

                        {/* Moneda */}
                        <div className="mb-6">
                            <label className="block text-white mb-1">Moneda</label>
                            <select
                                value={currency}
                                onChange={e => setCurrency(e.target.value)}
                                className="w-32 bg-black bg-opacity-30 border border-white/40 rounded p-2 text-white"
                            >
                                <option>BOB</option>
                                <option>USDT</option>
                                <option>USDC</option>
                            </select>
                        </div>

                        {/* Items */}
                        <ul className="space-y-6">
                            {cart.detalles.map(item => (
                                <li
                                    key={item.id}
                                    className="flex justify-between items-center border border-white/10 rounded-lg p-4"
                                >
                                    <p className="font-semibold text-lg">{item.productName}</p>
                                    <div className="flex items-center">
                                        <p className="text-gray-300 mr-2 text-sm">
                                            Cantidad: {item.quantity}
                                        </p>
                                        <button
                                            onClick={() => handleDecrease(item)}
                                            disabled={loading || item.quantity <= 0}
                                            className="px-2 py-1 bg-red-600 hover:bg-red-700 text-white rounded transition"
                                        >−</button>
                                    </div>
                                    <p className="font-bold text-lg">${item.total.toFixed(2)}</p>
                                </li>
                            ))}
                        </ul>

                        {/* Total + Checkout */}
                        <div className="mt-8 flex justify-between items-center">
                            <p className="text-2xl font-bold text-white">
                                Total: ${cart.totalGlobal.toFixed(2)}
                            </p>
                            <button
                                onClick={handleCheckout}
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
}
