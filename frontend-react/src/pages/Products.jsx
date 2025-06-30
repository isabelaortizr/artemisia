import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import FilterSidebar from '../components/FilterSideBar';
import productService from '../services/productService';
import notaVentaService from '../services/notaVentaService';
import cartIcon from '../assets/cart_icon.png';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filters, setFilters] = useState({});
  const [toast, setToast] = useState('');
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      navigate("/login");
      return;
    }

    setLoading(true);
    productService
      .getProducts(page, 12, filters)
      .then(({ items, totalPages }) => {
        setProducts(items);
        setTotalPages(totalPages);
      })
      .catch(err => {
        console.error(err);
        setProducts([]);
      })
      .finally(() => setLoading(false));
  }, [page, filters, navigate]);

  const addToCart = async (product) => {
    try {
      await notaVentaService.addToCart({ productId: product.productId, quantity: 1 });
      setToast(`"${product.name}" añadido al carrito`);
      setTimeout(() => setToast(''), 3000);
    } catch (e) {
      setToast("Error al añadir al carrito");
      setTimeout(() => setToast(''), 3000);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-black text-white">
      <Navbar showSignUpButton={false} />
      <FilterSidebar onApply={setFilters} />

      {/* Hero visual */}
      <section
        className="h-[60vh] bg-cover bg-center flex items-center justify-center text-center text-white relative"
        style={{ backgroundImage: "url('/header_img.png')" }}
      >
        <div className="absolute inset-0 bg-opacity-40" />
        <div>
          <h1 className="text-4xl sm:text-5xl md:text-6xl font-bold mb-4">Our Available Pieces</h1>
          <p className="text-md sm:text-lg max-w-2xl mx-auto text-gray-200">
            Explore unique artworks crafted by talented creators — find your next inspiration.
          </p>
        </div>
      </section>

      {toast && (
        <div
          className={`fixed top-4 left-1/2 transform -translate-x-1/2 z-50 px-6 py-3 rounded shadow-xl animate-bounce
            ${toast.toLowerCase().includes('error') ? 'bg-red-600' : 'bg-green-600'} text-white`}
        >
          {toast}
        </div>
      )}

      <main className="flex-1 px-6 sm:px-10 pt-12 pb-12">
        <div className="mb-8 flex items-center justify-between">
          <div className="flex-1" />
          <h2 className="text-3xl sm:text-4xl font-medium text-center flex-1">Call it Art...</h2>
          <div className="flex-1 flex justify-end">
            <Link to="/cart">
              <img src={cartIcon} alt="Carrito" className="w-10 h-10 hover:scale-110 transition" />
            </Link>
          </div>
        </div>

        {loading ? (
          <div className="flex justify-center items-center h-60">
            <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-white border-opacity-40"></div>
          </div>
        ) : products.length === 0 ? (
          <p className="text-gray-400 text-center">No se encontraron productos.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
            {products.map(prod => {
              const expanded = expandedId === prod.productId;

              return (
                <div
                  key={prod.productId}
                  className="bg-white p-4 rounded-2xl shadow-lg hover:shadow-2xl transition duration-300 flex flex-col text-black group"
                >
                  {/* Imagen con hover */}
                  <div className="overflow-hidden rounded-xl">
                    <img
                      src={
                        prod.image
                          ? `data:image/jpeg;base64,${prod.image}`
                          : 'https://via.placeholder.com/400x400'
                      }
                      alt={prod.name}
                      className="w-full h-64 object-cover transform group-hover:scale-105 transition duration-300"
                    />
                  </div>

                  {/* Info básica */}
                  <div className="mt-4 flex-grow">
                    <h3 className="text-lg font-semibold">{prod.name}</h3>
                    <p className="text-sm text-gray-600">{prod.technique}</p>
                  </div>

                  {/* Precio + botón */}
                  <div className="mt-4 flex justify-between items-center">
                    <span className="text-md font-bold">
                      {new Intl.NumberFormat('es-BO', {
                        style: 'currency',
                        currency: 'BOB'
                      })
                        .format(prod.price ?? 0)
                        .replace('Bs', 'Bs.')}
                    </span>
                    <button
                      onClick={() => addToCart(prod)}
                      className="text-sm bg-black text-white px-4 py-2 rounded-lg hover:bg-gray-800 active:scale-95 transition"
                    >
                      Añadir
                    </button>
                  </div>

                  {/* View Details */}
                  <button
                    onClick={() => setExpandedId(expanded ? null : prod.productId)}
                    className="mt-3 text-sm text-black hover:underline text-left"
                  >
                    {expanded ? 'Hide details' : 'View details'}
                  </button>

                  {/* Detalles extra */}
                  {expanded && (
                    <div className="mt-3 bg-gray-100 rounded-lg p-3 text-sm text-black border border-gray-200">
                      <p><strong>Categoría:</strong> {prod.category}</p>
                      <p><strong>Estado:</strong> {prod.status}</p>
                      <p className="mt-2 text-gray-700 italic">{prod.description || 'Sin descripción.'}</p>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}

        {/* Paginación */}
        <div className="mt-10 flex justify-center items-center gap-4 text-gray-300">
          <button
            disabled={page === 0}
            onClick={() => setPage(p => Math.max(p - 1, 0))}
            className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded disabled:opacity-50"
          >
            Anterior
          </button>
          <span className="text-sm">
            Página {page + 1} de {totalPages}
          </span>
          <button
            disabled={page + 1 >= totalPages}
            onClick={() => setPage(p => Math.min(p + 1, totalPages - 1))}
            className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded disabled:opacity-50"
          >
            Siguiente
          </button>
        </div>
      </main>

      <Footer />
    </div>
  );
}
