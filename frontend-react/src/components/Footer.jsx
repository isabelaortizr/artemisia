import React from 'react';
import { assets } from '../assets/assets';
import { FaInstagram, FaTwitter, FaFacebook } from 'react-icons/fa';

function Footer() {
  return (
      <footer className="bg-black text-white py-10 px-6 md:px-20 lg:px-32">
        <div className="container mx-auto flex flex-col md:flex-row justify-between gap-12">
          {/* Logo + descripción */}
          <div className="flex-1">
            <img src={assets.logo} alt="Logo" className="h-10 mb-4" />
            <p className="text-sm max-w-sm">
              Artemisia bridges art and technology to empower Bolivian artists and connect creativity with the world.
            </p>
          </div>

          {/* Navegación */}
          <div className="flex-1">
            <h3 className="font-semibold mb-4 text-lg">Explore</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="#Header" className="hover:underline">Home</a></li>
              <li><a href="#About" className="hover:underline">About</a></li>
              <li><a href="#Products" className="hover:underline">Products</a></li>
              <li><a href="#Contact" className="hover:underline">Contact</a></li>
            </ul>
          </div>

          {/* Contacto y redes */}
          <div className="flex-1">
            <h3 className="font-semibold mb-4 text-lg">Connect</h3>
            <p className="text-sm mb-4">contact@artemisia.bo</p>
            <div className="flex space-x-4 text-xl">
              <a
                  href="https://instagram.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-amber-300 transition"
              >
                <FaInstagram />
              </a>
              <a
                  href="https://twitter.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-amber-300 transition"
              >
                <FaTwitter />
              </a>
              <a
                  href="https://facebook.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-amber-300 transition"
              >
                <FaFacebook />
              </a>
            </div>
          </div>
        </div>

        <div className="border-t border-white border-opacity-10 mt-10 pt-6 text-center text-xs text-gray-400">
          © {new Date().getFullYear()} Artemisia. All rights reserved.
        </div>
      </footer>
  );
}

export default Footer;