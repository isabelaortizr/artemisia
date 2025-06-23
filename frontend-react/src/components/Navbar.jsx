import React, { useEffect, useState } from 'react';
import { assets } from '../assets/assets';

function Navbar() {
  const [showMobileMenu, setShowMobileMenu] = useState(false);

  useEffect(() => {
    if (showMobileMenu) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'auto';
    }
    return () => {
      document.body.style.overflow = 'auto';
    };
  }, [showMobileMenu]);

  const closeMenu = () => setShowMobileMenu(false);

  return (
    <div className="absolute top-0 left-0 w-full z-20">
      <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
        <img src={assets.logo} alt="Logo" className="h-10" />

        <ul className="hidden md:flex gap-8 text-sm font-medium text-white">
          <li><a href="#Header" className="hover:text-gray-400">Home</a></li>
          <li><a href="#About" className="hover:text-gray-400">About</a></li>
          <li><a href="#Products" className="hover:text-gray-400">Products</a></li>
          <li><a href="#Profile" className="hover:text-gray-400">My Profile</a></li>
        </ul>

        <button className="hidden md:block bg-white px-8 py-2 rounded-full">
          Sign Up
        </button>

        <img
          src={assets.menu_icon}
          onClick={() => setShowMobileMenu(true)}
          className="md:hidden w-7 cursor-pointer"
          alt="menu"
        />
      </div>


      {/* Menú móvil deslizante */}
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
          <a href="#Header" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">Home</a>
          <a href="#About" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">About</a>
          <a href="#Products" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">Products</a>
          <a href="#Profile" onClick={closeMenu} className="px-4 py-2 rounded-full inline-block">My Profile</a>
        </ul>
      </div>
    </div>
  );
}

export default Navbar;
