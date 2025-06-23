import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Header from './components/Header';
import About from './components/About';
import Products from './components/Products';
import Footer from './components/d_Footer';

import Login from './pages/Login';   // <- opcional si también quieres login
import Register from './pages/Register';

const Home = () => (
  <div className='w-full overflow-hidden bg-black'>
    <Header />
    <About />
    <Products />
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
      </Routes>
    </Router>
  );
};

export default App;
