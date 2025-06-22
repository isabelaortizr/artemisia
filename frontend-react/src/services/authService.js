// src/services/authService.js
const API_URL = import.meta.env.VITE_API_URL;

async function login({ username, password }) {
    console.log("🔐 Intentando login en:", `${API_URL}/auth/token`);
    console.log("🔐 Payload:", { username, password });

    const res = await fetch(`${API_URL}/auth/token`, {
        method: 'POST',
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
    });

    console.log("🔐 Código HTTP login:", res.status);

    if (!res.ok) {
        const errorBody = await res.json().catch(() => ({}));
        console.error("🔐 Error body:", errorBody);
        throw new Error(errorBody.message || "Error en autenticación");
    }

    const body = await res.json();
    console.log("🔐 Respuesta login:", body);
    // return body;
    return {
        token: body.id_token,
        user: body.username
    };
}

function logout() {
    localStorage.removeItem("authToken");
}

export default { login, logout };
