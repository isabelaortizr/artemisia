import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Header from './components/Header';
import About from './components/About';
import Login from './pages/Login';   // <- opcional si también quieres login
import Register from './pages/Register';
import Footer from './components/Footer';
import ProductsLanding from './components/ProductsLanding';
import Products from './pages/Products';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';


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
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/products" element={<Products />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/checkout" element={<Checkout />} />


      </Routes>
    </Router>
  );
};

export default App;
