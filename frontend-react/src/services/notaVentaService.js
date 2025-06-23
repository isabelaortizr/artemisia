// src/services/notaVentaService.js
const API_URL = import.meta.env.VITE_API_URL;

async function getCart(userId) {
    const token = localStorage.getItem("authToken");
    const res = await fetch(`${API_URL}/notas-venta/user/${userId}`, {
        method: "GET",
        headers: {
            "Content-Type":  "application/json",
            "Authorization": `Bearer ${token}`,
        },
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status}`);
    }
    return res.json(); // devuelve la NotaVentaResponseDto
}

// (Ya tenías esto para añadir al carrito…)
async function addToCart(dto) {
    const token = localStorage.getItem("authToken");
    const res = await fetch(`${API_URL}/notas-venta/add`, {
        method: "POST",
        headers: {
            "Content-Type":  "application/json",
            "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify(dto),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status}`);
    }
    return res.json();
}

export default { getCart, addToCart };
