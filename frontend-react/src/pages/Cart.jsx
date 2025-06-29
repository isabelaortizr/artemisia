// src/pages/Cart.jsx
import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import notaVentaService from "../services/notaVentaService";
import addressService from "../services/addressService";
import backIcon from "../assets/back-icon.png";
import Navbar from '../components/Navbar';
import { assets } from "../assets/assets";

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
    const [checkoutData,    setCheckoutData]  = useState(null);

    // Estados para verificar la transacción
    const [verifyLoading, setVerifyLoading] = useState(false);
    const [verifyResult,  setVerifyResult]  = useState(null);
    const [verifyError,   setVerifyError]   = useState(null);

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
        if (!userId) {
            navigate("/login", { replace: true });
            return;
        }
        fetchCart();
        fetchAddresses();
    }, [navigate]);

    // 4) asignar automáticamente la primera dirección (solo al cargar)
    useEffect(() => {
        if (
            !loading &&
            !addrLoading &&
            cart &&
            addresses.length > 0 &&
            Number(cart.buyerAddress) !== Number(selectedAddress)
        ) {
            // asigno la dirección por defecto sin tocar estadoVenta
            notaVentaService
                .assignAddressToNotaVenta({ userId, addressId: selectedAddress })
                .then(fetchCart)
                .catch(e => setError(e.message || "Error al sincronizar dirección"));
        }
    }, [loading, addrLoading, cart, addresses, selectedAddress, userId]);

    // 5) el usuario elige otra dirección
    const handleAddressChange = async (newAddr) => {
        setSelectedAddress(newAddr);
        setLoading(true);
        try {
            await notaVentaService.assignAddressToNotaVenta({ userId, addressId: newAddr });
            await fetchCart();
        } catch (err) {
            setError(err.message || "Error al guardar dirección");
        } finally {
            setLoading(false);
        }
    };

    // 6) disminuir cantidad de un item
    const handleDecrease = async (item) => {
        setLoading(true);
        try {
            const updated = await notaVentaService.updateOrderDetailStock({
                userId,
                productId: item.productId,
                quantity: item.quantity - 1
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
            setCheckoutData({ transaction: tx, addressId: selectedAddress });
        } catch (err) {
            setError(err.message || "Error al iniciar pago");
        }
    };

    // ——— NUEVO: función para verificar el pago ———
    const handleVerify = async () => {
        setVerifyLoading(true);
        setVerifyError(null);
        try {
            const res = await notaVentaService.verifyTransaction(userId);
            setVerifyResult(res);
        } catch (err) {
            setVerifyError(err.message || "Error al verificar");
        } finally {
            setVerifyLoading(false);
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
    <div className="min-h-screen bg-cover bg-center relative" style={{ backgroundImage: `url(${assets.register_img})` }}>
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
                    <div className="bg-zinc-900 bg-opacity-90 rounded-xl p-8 border border-white/10 shadow-xl text-white">
                        <Header title="Tu Carrito" />

                        {/* Dirección */}
                        <div className="mb-6">
                            <label className="block text-white mb-2">Dirección de envío</label>
                            {addrLoading ? (
                                <p className="text-white">Cargando direcciones…</p>
                            ) : (
                                <div className="flex flex-wrap gap-2">
                                    {addresses.map(addr => {
                                        const id = addr.address_id ?? addr.addressId;
                                        return (
                                            <button
                                                key={id}
                                                onClick={() => handleAddressChange(id)}
                                                className={`px-4 py-2 rounded-xl border text-sm transition ${
                                                    Number(selectedAddress) === Number(id)
                                                        ? "bg-white text-black border-white"
                                                        : "bg-black bg-opacity-40 border-white/20 text-white hover:bg-white/10"
                                                }`}
                                            >
                                                {addr.street}, {addr.house_number} — {addr.city}, {addr.country}
                                            </button>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        {/* Moneda */}
                        <div className="mb-6">
                            <label className="block text-white mb-2">Moneda</label>
                            <div className="flex gap-2">
                                {["BOB", "USDT", "USDC"].map(c => (
                                    <button
                                        key={c}
                                        onClick={() => setCurrency(c)}
                                        className={`px-4 py-2 rounded-xl border text-sm transition ${
                                            currency === c
                                                ? "bg-white text-black border-white"
                                                : "bg-black bg-opacity-40 border-white/20 text-white hover:bg-white/10"
                                        }`}
                                    >
                                        {c}
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Items */}
                        <ul className="space-y-6">
                            {cart.detalles.map(item => (
                                <li
                                    key={item.id}
                                    className="flex justify-between items-center border border-white/10 rounded-lg p-4"
                                >
                                    <p className="font-semibold text-lg w-1/3 truncate">{item.productName}</p>
                                    <div className="w-1/3 flex justify-center items-center gap-2">
                                        <span className="text-sm text-gray-300">Cantidad: {item.quantity}</span>
                                        <button
                                            onClick={() => handleDecrease(item)}
                                            disabled={loading || item.quantity <= 0}
                                            className="px-2 py-1 bg-red-600 hover:bg-red-700 text-white rounded transition"
                                        >−</button>
                                    </div>
                                    <p className="font-bold text-lg w-1/3 text-right">
                                        ${item.total.toFixed(2)}
                                    </p>
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

                        {/* Modal de checkout */}
                        {checkoutData && (
                            <div
                                className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50
                     flex items-center justify-center p-4 overflow-y-auto"
                            >
                                <div
                                    className="bg-white text-black max-w-3xl w-full mx-auto rounded-2xl
                       p-6 relative shadow-xl
                       max-h-[calc(100vh-4rem)] overflow-y-auto"
                                >
                                    {/* Botón Cerrar */}
                                    <button
                                        className="absolute top-4 right-4 text-black text-2xl hover:text-red-600"
                                        onClick={() => {
                                            setCheckoutData(null);
                                            setVerifyResult(null);
                                            setVerifyError(null);
                                        }}
                                    >
                                        ×
                                    </button>

                                    <h2 className="text-2xl font-bold mb-4 text-center">Finaliza tu compra</h2>

                                    <div className="flex justify-center mb-4">
                                        <iframe
                                            src={checkoutData.transaction.payment_link}
                                            title="Stereum Pay"
                                            allow="clipboard-read; clipboard-write"
                                            allowFullScreen
                                            loading="lazy"
                                            className="w-full h-[500px] border rounded-lg shadow"
                                        />
                                    </div>
                                    <p className="text-center mb-6 text-sm">
                                        Si no carga,{' '}
                                        <a
                                            href={checkoutData.transaction.payment_link}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="text-indigo-600 underline"
                                        >
                                            haz clic aquí
                                        </a>.
                                    </p>

                                    {/* ——— Botón Verificar pago ——— */}
                                    <div className="text-center mb-6">
                                        <button
                                            onClick={handleVerify}
                                            disabled={verifyLoading}
                                            className="px-6 py-2 bg-indigo-600 hover:bg-indigo-700
                           text-white rounded-full transition disabled:opacity-50"
                                        >
                                            {verifyLoading ? "Verificando…" : "Verificar pago"}
                                        </button>

                                        {verifyResult && (
                                            <div className="mt-4 space-y-1">
                                                <p>Estado: <strong>{verifyResult.estado}</strong></p>
                                                <p>NotaVenta ID: <strong>{verifyResult.notaVentaId}</strong></p>
                                            </div>
                                        )}

                                        {verifyError && (
                                            <p className="mt-4 text-red-500">Error: {verifyError}</p>
                                        )}
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
