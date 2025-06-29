// src/services/imageService.js
const API_URL = import.meta.env.VITE_API_URL;

async function uploadImage({ productId, fileName, base64Image }) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/images/upload`, {
        method: 'POST',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ productId, fileName, base64Image })
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al subir imagen`);
    }
}
export default { uploadImage };
