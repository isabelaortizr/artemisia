// src/main.jsx
console.log("🚀 VITE_API_URL =", import.meta.env.VITE_API_URL);

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

console.log("todos los env:", import.meta.env);
console.log("mi API_URL:", import.meta.env.VITE_API_URL);

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <App />
    </StrictMode>,
)