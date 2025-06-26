// src/services/userService.js
const API_URL = import.meta.env.VITE_API_URL;

async function createUser({ name, mail, password, role }) {
    const res = await fetch(`${API_URL}/users`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, mail, password, role }),
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error al crear usuario (${res.status})`);
    }

    return res.json(); // { id, name, mail, role, ... }
}

async function getUserById(id) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/users/${id}`, {
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        }
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al obtener usuario`);
    }
    return res.json(); // { id, name, mail, role }
}

export default { createUser, getUserById };
