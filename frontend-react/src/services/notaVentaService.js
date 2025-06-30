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
                                     // network = "BISA",
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
            // network,
            country
        })
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error creando transacción (${res.status})`);
    }
    return res.json(); // StereumPagaResponseDto
}

async function updateOrderDetailStock({ userId, productId, quantity }) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/notas-venta/order_detail/update_stock`, {
        method: 'PUT',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ userId, productId, quantity })
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al actualizar carrito`);
    }

    return res.json(); // NotaVentaResponseDto
}

async function assignAddressToNotaVenta({ userId, addressId }) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/notas-venta/set_address`, {
        method: 'PUT',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ userId, addressId })
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al asignar dirección`);
    }
    // No devuelve body
}

async function verifyTransaction(userId) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(
        `${API_URL}/notas-venta/verify_transaction/${userId}`,
        {
            headers: { 'Authorization': `Bearer ${token}` }
        }
    );
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al verificar`);
    }
    return res.json(); // { estado: "...", notaVentaId: 123 }
}


// ➜ nuevo método:
async function getNotaVentaById(id) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/notas-venta/${id}`, {
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        }
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} buscando nota ${id}`);
    }
    return res.json();
}

async function getVentasByEstado(estado, page = 0, size = 10) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(
        `${API_URL}/notas-venta/estado/${estado}?page=${page}&size=${size}`,
        { headers: { 'Authorization': `Bearer ${token}` } }
    );
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} cargando órdenes`);
    }
    return res.json(); // { content: NotaVentaResponseDto[], totalPages, ... }
}

export default { addToCart, getCart, createTransaction, updateOrderDetailStock,
    assignAddressToNotaVenta, verifyTransaction, getNotaVentaById, getVentasByEstado };
