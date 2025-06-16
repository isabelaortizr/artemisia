
import { useState } from "react";

const LandingPage = () => {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="min-h-screen bg-white text-gray-900 flex flex-col">
      {/* Navbar */}
      <nav className="bg-white shadow fixed w-full z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16">
          <h1 className="text-xl font-bold text-indigo-600">Artemisia</h1>
          <div className="hidden md:flex gap-8 text-sm font-medium text-gray-600">
            <a href="#productos" className="hover:text-indigo-600">Products</a>
            <a href="#nosotros" className="hover:text-indigo-600">Us</a>
            <a href="#contacto" className="hover:text-indigo-600">Contact</a>
          </div>
          <button
            className="md:hidden text-gray-600"
            onClick={() => setMenuOpen(!menuOpen)}
          >
            ☰
          </button>
        </div>
        {menuOpen && (
          <div className="md:hidden px-4 pb-4 bg-white">
            <a href="#productos" className="block py-2">Products</a>
            <a href="#nosotros" className="block py-2">Us</a>
            <a href="#contacto" className="block py-2">Contact</a>
          </div>
        )}
      </nav>

      {/* Hero */}
      <header className="bg-gray-100 pt-32 pb-20 text-center">
        <div className="max-w-4xl mx-auto px-4">
          <h1 className="text-4xl sm:text-5xl font-extrabold leading-tight tracking-tight">
            <span className="text-indigo-600">Artemisia</span> says ¡hello!
          </h1>
          <p className="mt-4 text-lg text-gray-600 max-w-xl mx-auto">
            Where art never fades — Unique & Legit pieces for each moment.
          </p>
          <div className="mt-6">
            <a
              href="#productos"
              className="inline-block rounded-md bg-indigo-600 px-6 py-3 text-white font-bold hover:bg-indigo-700 transition"
            >
              All pieces...
            </a>
          </div>
        </div>
      </header>

      {/* Productos destacados */}
      <section id="productos" className="py-20 bg-white flex-grow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-semibold mb-12 text-center tracking-tight">
            Our newest drops
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {[1, 2, 3, 4].map((n) => (
              <div
                key={n}
                className="border rounded-xl p-4 shadow-md hover:shadow-xl transition duration-300"
              >
                <img
                  src={`/Users/isabelaortiz/Desktop/artemisia/frontend-react/src/res/test.jpg`}
                  alt={`Producto ${n}`}
                  className="w-full h-64 object-cover rounded-lg"
                />
                <h3 className="mt-4 font-semibold text-lg">Producto {n}</h3>
                <p className="text-gray-500 mt-1">$49.00</p>
                <button className="mt-4 w-full bg-indigo-600 text-black py-2 rounded hover:bg-indigo-700 transition">
                  Comprar
                </button>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-100 py-8 text-center text-sm text-gray-500 mt-10">
        <p className="mb-2">© {new Date().getFullYear()} Artemisia.</p>
        <p>with love, from an art place. Todos los derechos reservados.</p>
      </footer>
    </div>
  );
};

export default LandingPage;

      {/* cambiar las fotosssssss */}
