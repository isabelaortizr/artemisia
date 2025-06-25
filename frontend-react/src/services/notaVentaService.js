// src/services/notaVentaService.js
const API_URL = import.meta.env.VITE_API_URL;

async function addToCart({ productId, quantity }) {
    const token  = localStorage.getItem('authToken');
    const userId = localStorage.getItem('userId');  // ← lo sacamos de localStorage
    if (!userId) throw new Error('No userId found, please login first');

    const res = await fetch(`${API_URL}/notas-venta/add`, {
        method: 'POST',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            userId:    parseInt(userId, 10),
            productId,
            quantity
        }),
    });

    if (!res.ok) {
        // intenta leer el body de error, si no viene JSON, lanza genérico
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al añadir al carrito`);
    }

    // devuelve la nota de venta actualizada
    return res.json();
}

async function getCart(userId) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/notas-venta/user/${userId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al cargar carrito`);
    }
    return res.json();
}

async function createTransaction({
                                     userId,
                                     currency = "BOB",
                                     chargeReason = "Compra en Artemisia",
                                     network = "BISA",
                                     country = "BO"
                                 }) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/notas-venta/create_transaction`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            user_id:     Number(userId),
            currency,
            charge_reason: chargeReason,
            network,
            country
        })
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error creando transacción (${res.status})`);
    }
    return res.json(); // StereumPagaResponseDto
}

export default { addToCart, getCart, createTransaction };
