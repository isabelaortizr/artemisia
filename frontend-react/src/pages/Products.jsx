import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { assets } from '../assets/assets';
import productService from '../services/productService';
import notaVentaService from '../services/notaVentaService';
import cartIcon from '../assets/cart-icon.png';
import logoutIcon from '../assets/logout-icon.png';

const Products = () => {
  const [products, setProducts] = useState([]);
  const [expanded, setExpanded] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [toastMessage, setToastMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      navigate("/login");
      return;
    }

    setLoading(true);
    productService
      .getProducts(page, 9)
      .then(({ items, totalPages }) => {
        setProducts(items);
        setTotalPages(totalPages);
      })
      .catch(err => {
        console.error(err);
        setProducts([]);
      })
      .finally(() => setLoading(false));
  }, [page]);

  const toggleExpand = (id) => {
    setExpanded(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const handleAddToCart = async (prod) => {
    try {
      await notaVentaService.addToCart({
        productId: prod.productId,
        quantity: 1
      });
      setToastMessage(`"${prod.name}" añadido al carrito`);
      setTimeout(() => setToastMessage(''), 3000);
    } catch (err) {
      console.error(err);
      setToastMessage('Error al añadir al carrito');
      setTimeout(() => setToastMessage(''), 3000);
    }
  };

  return (
    <div
      className="min-h-screen bg-cover bg-center relative"
      style={{ backgroundImage: `url(${assets.bg_image})` }}
    >
      {/* Overlay oscuro */}
      <div className="absolute inset-0  bg-opacity-70 z-0" />

      <div className="relative z-10">
        <Navbar showSignUpButton={false} />

        {/* Toast */}
        {toastMessage && (
          <div className="fixed bottom-6 left-1/2 transform -translate-x-1/2 bg-red-600 text-white px-4 py-2 rounded shadow-lg z-50">
            {toastMessage}
          </div>
        )}

        <div className="max-w-7xl mx-auto p-6 pt-28">
          {/* Título y botones */}
          <div className="flex justify-between items-center mb-8 flex-col sm:flex-row gap-4 sm:gap-0">
            <h2 className="text-3xl font-bold text-white">Available Pieces</h2>
            <div className="flex items-center gap-4">
              {/* <Link to="/">
                <img src={logoutIcon} alt="Cerrar sesión" className="w-8 h-8 hover:opacity-80" />
              </Link> */}
              <Link to="/cart">
                <img src={cartIcon} alt="Carrito" className="w-8 h-8 hover:opacity-80" />
              </Link>
            </div>
          </div>

          {/* Productos */}
          {loading ? (
            <p className="text-center text-white mt-10">Cargando productos...</p>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
              {products.map(prod => {
                const isExpanded = expanded.includes(prod.productId);
                return (
                  <div
                    key={prod.productId}
                    className="bg-black/50 backdrop-blur-md border border-black/30 rounded-xl p-5 shadow-lg transition-transform duration-200 hover:scale-105 flex flex-col"
                  >
                    <img
                      src={prod.imageUrl || 'https://via.placeholder.com/300x200'}
                      alt={prod.name}
                      className="w-full h-48 object-cover rounded-lg mb-4"
                    />
                    <h3 className="font-semibold text-xl text-white">{prod.name}</h3>
                    <p className="text-white mt-1 text-lg">${prod.price.toFixed(2)}</p>

                    <button
                      onClick={() => toggleExpand(prod.productId)}
                      className="mt-3 text-white hover:underline text-sm self-start"
                    >
                      {isExpanded ? 'Ver menos ▲' : 'Ver más ▼'}
                    </button>

                    {isExpanded && (
                      <div className="mt-3 text-sm text-gray-300 space-y-1 flex-grow">
                        <p><strong>Técnica:</strong> {prod.technique}</p>
                        {prod.category && <p><strong>Categoría:</strong> {prod.category}</p>}
                        {prod.description && <p><strong>Descripción:</strong> {prod.description}</p>}
                      </div>
                    )}

                    <button
                      onClick={() => handleAddToCart(prod)}
                      className="mt-5 bg-black text-white py-2 rounded-full hover:bg-white hover:text-black transition font-semibold"
                    >
                      Add to Cart
                    </button>
                  </div>
                );
              })}
            </div>
          )}

          {/* Paginación */}
          <div className="flex justify-center items-center space-x-4 mt-12">
            <button
              onClick={() => setPage(p => Math.max(p - 1, 0))}
              disabled={page === 0}
              className="px-4 py-2 bg-gray-200 rounded-full disabled:opacity-50 hover:bg-gray-300 transition"
            >
              Anterior
            </button>
            <span className="text-gray-300">Página {page + 1} de {totalPages}</span>
            <button
              onClick={() => setPage(p => Math.min(p + 1, totalPages - 1))}
              disabled={page + 1 >= totalPages}
              className="px-4 py-2 bg-gray-200 rounded-full disabled:opacity-50 hover:bg-gray-300 transition"
            >
              Siguiente
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Products;
