import React from 'react';
import Navbar from './Navbar';
import { motion } from "framer-motion";
import { Link } from 'react-router-dom';

function Header() {
  return (
    <div
      className="min-h-screen mb-4 bg-cover bg-center flex items-center w-full overflow-hidden"
      style={{ backgroundImage: "url('/header_img.png')" }}
      id="Header"
    >
      <Navbar />
      <motion.div
        initial={{ opacity: 0, y: 100 }}
        transition={{ duration: 1.5 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
        className="container text-center mx-auto py-4 px-6 md:px-20 lg:px-42 text-white"
      >
        <h2 className="text-5xl sm:text-6xl md:text-[82px] inline-block max-w-3xl font-semibold pt-20">
          Where art never fades
        </h2>

        {/* Botones corregidos para vista móvil */}
        <div className="flex flex-col sm:flex-row items-center gap-4 sm:gap-6 mt-6 w-full justify-center">
          <a
            href="#ProductsLanding"
            className="w-48 text-center border border-white px-6 py-3 rounded text-white transition duration-300 hover:bg-white hover:text-black hover:scale-105 text-sm sm:text-base"
          >
            Pieces
          </a>
          <Link
            to="/login"
            className="w-48 text-center bg-white px-6 py-3 rounded text-black transition duration-300 hover:bg-black hover:text-white hover:scale-105 text-sm sm:text-base"
          >
            Get Started
          </Link>
        </div>
      </motion.div>
    </div>
  );
}

export default Header;
