// src/services/addressService.js
const API_URL = import.meta.env.VITE_API_URL;

async function getAddressesByUser(userId, page = 0, size = 10) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(
        `${API_URL}/addresses/user/${userId}?page=${page}&size=${size}`,
        { headers: { 'Authorization': `Bearer ${token}` } }
    );
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al cargar direcciones`);
    }
    const json = await res.json();
    // devolvemos el array de direcciones
    return json.content;
}

async function createAddress(addressDto) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/addresses`, {
        method: 'POST',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(addressDto),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al crear dirección`);
    }
    return res.json(); // AddressResponseDto
}

export default { getAddressesByUser, createAddress };
