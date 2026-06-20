// src/App.jsx

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

// Pages
import Home          from "./pages/Home"; // Ahora sí lo importas como componente separado
import Login         from "./pages/Login";
import Register      from "./pages/Register";
import Products      from "./pages/Products";
import SellerMenu    from "./pages/SellerMenu";
import AddArt        from "./pages/AddArt";
import Profile       from "./pages/Profile";
import Cart          from "./pages/Cart";
import Checkout      from "./pages/Checkout";
import MyWorks       from "./pages/MyWorks";
import OrderReceipt  from "./pages/OrderReceipt";
import OrderHistory  from "./pages/OrderHistory";
import ProductsLanding from "./components/ProductsLanding";
import Auctions       from "./pages/Auctions";
import AuctionDetail  from "./pages/AuctionDetail";
import MyAuctions     from "./pages/MyAuctions";

const GuestRoute = ({ children }) => {
  return localStorage.getItem('userId') ? <Navigate to="/products" replace /> : children;
};

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/"              element={<GuestRoute><Home /></GuestRoute>} />
        <Route path="/login"         element={<Login />} />
        <Route path="/register"      element={<Register />} />
        <Route path="/products"      element={<Products />} />
        <Route path="/menu"          element={<SellerMenu />} />
        <Route path="/add-art"       element={<AddArt />} />
        <Route path="/profile"       element={<Profile />} />
        <Route path="/cart"          element={<Cart />} />
        <Route path="/checkout"      element={<Checkout />} />
        <Route path="/myworks"       element={<MyWorks />} />
        <Route path="/orderReceipt"  element={<OrderReceipt />} />
        <Route path="/orderHistory"  element={<OrderHistory />} />
        <Route path="/auctions"       element={<Auctions />} />
        <Route path="/auctions/:id"   element={<AuctionDetail />} />
        <Route path="/my-auctions"    element={<MyAuctions />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
