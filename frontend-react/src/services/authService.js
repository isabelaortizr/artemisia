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
    // body === { id_token, username, user_id }

    return {
        token:   body.id_token,
        user:    body.username,
        userId:  body.user_id,
        role:    body.user_role,    // <–– extrae el role
        firstLogin: body.first_login
    };
}

function logout() {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("username");
    localStorage.removeItem("userRole");
}

export default { login, logout };
