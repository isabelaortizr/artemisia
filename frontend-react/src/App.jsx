// import Home from "./Home";

// function App() {
//   return <Home />;
// }

// export default App;

import {BrowserRouter, Routes, Route, Router} from "react-router-dom";
import Header from './components/Header';
import About from './components/About';
import Footer from './components/Footer';
import ProductsLanding from './components/ProductsLanding';
import Login from "./pages/Login";
import Register     from "./pages/Register";
import Products    from './pages/Products';
import SellerMenu  from './pages/SellerMenu';
import AddArt      from './pages/AddArt.jsx';
import Profile  from './pages/Profile';
import Cart        from "./pages/Cart";
import Checkout   from "./pages/Checkout";
import MyWorks from './pages/MyWorks';

const Home = () => (
    <div className='w-full overflow-hidden bg-black'>
        <Header />
        <About />
        <ProductsLanding/>
        <Footer />
    </div>
);

const App = () => {
    return (
      <BrowserRouter>
        <Routes>
            <Route path="/" element={<Home />} />
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