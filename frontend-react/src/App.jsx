// import Home from "./Home";

// function App() {
//   return <Home />;
// }

// export default App;

import { BrowserRouter, Routes, Route } from "react-router-dom";
import LandingPage from "./LandingPage";
import Login from "./pages/Login";
import Register     from "./pages/Register";      // ← Nueva importación
import Products    from './pages/Products';
import Cart        from "./pages/Cart";
import Checkout from './pages/Checkout';

function App() {
  // return <LandingPage />;
  return (
      <BrowserRouter>
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/products" element={<Products />} />  {/* Catálogo */}
            <Route path="/cart"     element={<Cart />} />
            <Route path="/checkout" element={<Checkout />} />

        </Routes>
      </BrowserRouter>
  );
  // <Route path="/products" element={<Products />} />
  // {/* Rutas protegidas con un <PrivateRoute> si quieres */}
}
export default App;