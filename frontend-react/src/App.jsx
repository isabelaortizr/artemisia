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
import SellerMenu  from './pages/SellerMenu';
import AddArt      from './pages/AddArt.jsx';
import Profile  from './pages/Profile';
import Cart        from "./pages/Cart";
import Checkout   from "./pages/Checkout";
import MyWorks from './pages/MyWorks';

function App() {
  // return <LandingPage />;
  return (
      <BrowserRouter>
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/products" element={<Products />} />
            <Route path="/menu"      element={<SellerMenu />} />
            <Route path="/add-art" element={<AddArt />} />
            <Route path="/profile"  element={<Profile />} />
            <Route path="/cart"     element={<Cart />} />
            <Route path="/checkout" element={<Checkout />} />
            <Route path="/myworks" element={<MyWorks />} />

        </Routes>
      </BrowserRouter>
  );
  // <Route path="/products" element={<Products />} />
  // {/* Rutas protegidas con un <PrivateRoute> si quieres */}
}
export default App;