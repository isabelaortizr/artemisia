// src/services/productService.js
const API_URL = import.meta.env.VITE_API_URL;

async function getProducts() {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/products`, {
        method: 'GET',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`  // <— aquí pasamos el JWT
        },
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Request failed with status code ${res.status}`);
    }

    return res.json(); // Devuelve tu array de productos
}

export default { getProducts };
