// src/App.jsx

import { BrowserRouter, Routes, Route } from "react-router-dom";

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

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/"              element={<Home />} />
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
      </Routes>
    </BrowserRouter>
  );
};

export default App;
