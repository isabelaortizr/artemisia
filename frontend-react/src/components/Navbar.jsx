import React, { useEffect, useState } from 'react';
import { assets } from '../assets/assets';
import { Link, useNavigate, useLocation } from 'react-router-dom';

function Navbar({ showSignUpButton = true }) {
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

  const isLanding = location.pathname === '/';

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    const role = localStorage.getItem('userRole');
    setIsLoggedIn(!!userId);
    setUserRole(role);
  }, []);

  useEffect(() => {
    document.body.style.overflow = showMobileMenu ? 'hidden' : 'auto';
    return () => (document.body.style.overflow = 'auto');
  }, [showMobileMenu]);

  const closeMenu = () => setShowMobileMenu(false);

  const handleProductsClick = () => {
    const userId = localStorage.getItem('userId');
    if (!isLanding) {
      navigate('/products');
    } else {
      const el = document.getElementById('ProductsLanding');
      if (el) {
        el.scrollIntoView({ behavior: 'smooth' });
      }
    }
  };

  return (
    <div className="absolute top-0 left-0 w-full z-20">
      <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
        <Link to="/">
          <img src={assets.logo} alt="Logo" className="h-10 cursor-pointer" />
        </Link>

        {/* Desktop menu */}
        <ul className="hidden md:flex gap-8 text-sm font-medium text-white">
          {isLanding && (
            <>
              <li><a href="#Header" className="hover:text-gray-400">Home</a></li>
              <li><a href="#About" className="hover:text-gray-400">About</a></li>
            </>
          )}
          <li>
            <button onClick={handleProductsClick} className="hover:text-gray-400">Products</button>
          </li>

          {isLoggedIn && !isLanding && (
            <li>
              <Link to="/profile" className="hover:text-gray-400">My Profile</Link>
            </li>
          )}

          {isLoggedIn && !isLanding && (
            <li>
              <Link to="/orderHistory" className="hover:text-gray-400">Order History</Link>
            </li>
          )}

          {!isLanding && userRole === 'SELLER' && (
            <li>
              <Link to="/menu" className="hover:text-gray-400">Seller Menu</Link>
            </li>
          )}
        </ul>

        {showSignUpButton && (
          <Link to="/register">
            <button className="hidden md:block bg-white text-black px-8 py-2 rounded-full hover:bg-black hover:text-white transition">
              Sign Up
            </button>
          </Link>
        )}

        <img
          src={assets.menu_icon}
          onClick={() => setShowMobileMenu(true)}
          className="md:hidden w-7 cursor-pointer"
          alt="menu"
        />
      </div>

      {/* Mobile menu */}
      <div
        className={`md:hidden fixed top-0 right-0 h-full w-3/4 max-w-xs bg-white z-50 transform transition-transform duration-300 ${
          showMobileMenu ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        <div className="flex justify-end p-6 cursor-pointer">
          <img
            src={assets.cross_icon}
            onClick={closeMenu}
            className="w-6"
            alt="close"
          />
        </div>

        <ul className="flex flex-col items-center gap-4 mt-5 px-5 text-lg font-medium">
          {isLanding && (
            <>
              <a href="#Header" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">Home</a>
              <a href="#About" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">About</a>
            </>
          )}
          <button
            onClick={() => {
              handleProductsClick();
              closeMenu();
            }}
            className="px-4 py-2 rounded-full inline-block"
          >
            Products
          </button>

          {isLoggedIn && !isLanding && (
            <Link to="/profile" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">
              My Profile
            </Link>
          )}

          {isLoggedIn && !isLanding && (
            <Link to="/orderHistory" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">
              Order History
            </Link>
          )}

          {!isLanding && userRole === 'SELLER' && (
            <Link to="/menu" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">
              Seller Menu
            </Link>
          )}
        </ul>
      </div>
    </div>
  );
}

export default Navbar;
