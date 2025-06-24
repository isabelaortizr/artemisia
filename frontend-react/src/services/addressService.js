// src/services/addressService.js
const API_URL = import.meta.env.VITE_API_URL;

async function createAddress({ direction, userId }) {
    const token = localStorage.getItem('authToken'); // si tu API requiere auth
    const res = await fetch(`${API_URL}/addresses`, {
        method: 'POST',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ direction, userId }),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error al crear dirección (${res.status})`);
    }
    return res.json(); // AddressResponseDto
}

export default { createAddress };
