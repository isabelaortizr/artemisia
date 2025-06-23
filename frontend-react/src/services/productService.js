// src/services/productService.js
const API_URL = import.meta.env.VITE_API_URL;

async function getProducts(page = 0, size = 10) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/products?page=${page}&size=${size}`, {
        method: 'GET',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        },
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Request failed with status ${res.status}`);
    }

    const json = await res.json();
    return {
        items:    Array.isArray(json.content) ? json.content : [],
        page:     json.number,
        totalPages: json.totalPages,
        totalElements: json.totalElements
    };
}

export default { getProducts };
