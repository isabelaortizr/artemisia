// // src/services/paymentService.js
// const API_URL = import.meta.env.VITE_API_URL;
//
// async function getPaymentQr(orderId) {
//     const token = localStorage.getItem('authToken');
//     const res = await fetch(`${API_URL}/payment/qr/${orderId}`, {
//         method: 'GET',
//         headers: {
//             'Content-Type': 'application/json',
//             'Authorization': `Bearer ${token}`,
//         },
//     });
//
//     if (!res.ok) {
//         const err = await res.json().catch(() => ({}));
//         throw new Error(err.message || `Error al generar QR: ${res.status}`);
//     }
//
//     // Espero que el back devuelva { qrUrl: "https://..." }
//     const body = await res.json();
//     return body.qrUrl;
// }
//
// export default { getPaymentQr };
