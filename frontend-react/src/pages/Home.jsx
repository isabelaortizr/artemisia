// src/pages/Home.jsx
import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Header from '../components/Header';
import About from '../components/About';
import ProductsLanding from '../components/ProductsLanding';
import Footer from '../components/Footer';

export default function Home() {
  const location = useLocation();

  useEffect(() => {
    if (location.state?.scrollTo === 'ProductsLanding') {
      const el = document.getElementById('ProductsLanding');
      if (el) {
        setTimeout(() => {
          el.scrollIntoView({ behavior: 'smooth' });
        }, 100);
      }
    }
  }, [location]);

  return (
    <div className='w-full overflow-hidden bg-black'>
      <Header />
      <About />
      <ProductsLanding />
      <Footer />
    </div>
  );
}
