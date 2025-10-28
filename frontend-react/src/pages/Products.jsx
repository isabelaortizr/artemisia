import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import FilterSidebar from '../components/FilterSideBar';
import productService from '../services/productService';
import notaVentaService from '../services/notaVentaService';
import cartIcon from '../assets/cart_icon.png';

// Función para formatear el texto de enums
const formatText = (text) => {
    if (!text) return '';
    return text.toLowerCase().replace(/_/g, ' ')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
};

// Componente para mostrar productos (reutilizable)
const ProductCard = ({ product, isExpanded, onToggleExpand, onAddToCart }) => {
    const handleToggle = async () => {
        // Si el usuario está expandiendo (viendo detalles), trackear la vista
        if (!isExpanded) {
            try {
                await productService.trackProductView(product.productId);
                console.log(`Tracked view for product: ${product.name}`);
            } catch (error) {
                console.error('Error tracking product view:', error);
                // No mostramos error al usuario para no interrumpir la experiencia
            }
        }

        onToggleExpand();
    };

    return (
        <div className="bg-white p-4 rounded-2xl shadow-lg hover:shadow-2xl transition duration-300 flex flex-col text-black group">
            {/* Imagen con hover */}
            <div className="overflow-hidden rounded-xl">
                <img
                    src={
                        product.image
                            ? `data:image/jpeg;base64,${product.image}`
                            : 'https://via.placeholder.com/400x400'
                    }
                    alt={product.name}
                    className="w-full h-64 object-cover transform group-hover:scale-105 transition duration-300"
                />
            </div>

            {/* Info básica */}
            <div className="mt-4 flex-grow">
                <h3 className="text-lg font-semibold">{product.name}</h3>
                {/* Mostrar primera técnica como principal */}
                <p className="text-sm text-gray-600">
                    {product.techniques && product.techniques.length > 0
                        ? formatText(product.techniques[0])
                        : 'No technique specified'
                    }
                    {product.techniques && product.techniques.length > 1 &&
                        ` +${product.techniques.length - 1} more`
                    }
                </p>
            </div>

            {/* Precio + botón */}
            <div className="mt-4 flex justify-between items-center">
                <span className="text-md font-bold">
                    {new Intl.NumberFormat('es-BO', {
                        style: 'currency',
                        currency: 'BOB'
                    })
                        .format(product.price ?? 0)
                        .replace('Bs', 'Bs.')}
                </span>
                <button
                    onClick={() => onAddToCart(product)}
                    className="text-sm bg-black text-white px-4 py-2 rounded-lg hover:bg-gray-800 active:scale-95 transition"
                >
                    Add to Cart
                </button>
            </div>

            {/* View Details */}
            <button
                onClick={handleToggle}
                className="mt-3 text-sm text-black hover:underline text-left"
            >
                {isExpanded ? 'Hide details' : 'View details'}
            </button>

            {/* Detalles extra */}
            {isExpanded && (
                <div className="mt-3 bg-gray-100 rounded-lg p-3 text-sm text-black border border-gray-200">
                    <p><strong>Techniques:</strong> {product.techniques ? product.techniques.map(formatText).join(', ') : 'N/A'}</p>
                    <p><strong>Categories:</strong> {product.categories ? product.categories.map(formatText).join(', ') : 'N/A'}</p>
                    <p><strong>Status:</strong> {product.status}</p>
                    <p className="mt-2 text-gray-700 italic">{product.description || 'No description.'}</p>
                </div>
            )}
        </div>
    );
};

// El resto del código de Products.jsx se mantiene igual...
export default function Products() {
    const [products, setProducts] = useState([]);
    const [recommendedProducts, setRecommendedProducts] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [filters, setFilters] = useState({});
    const [toast, setToast] = useState('');
    const [loading, setLoading] = useState(true);
    const [recommendedLoading, setRecommendedLoading] = useState(true);
    const [expandedId, setExpandedId] = useState(null);
    const [expandedRecommendedId, setExpandedRecommendedId] = useState(null);
    const [showFilters, setShowFilters] = useState(false);

    const navigate = useNavigate();

    // Cargar productos recomendados usando la API real
    useEffect(() => {
        const loadRecommendedProducts = async () => {
            try {
                setRecommendedLoading(true);
                // Usar la API real de recomendaciones
                const recommendations = await productService.getRecommendedProducts(10);
                setRecommendedProducts(recommendations);
            } catch (error) {
                console.error('Error loading recommended products:', error);
                // Si hay error, mostrar mensaje amigable
                setRecommendedProducts([]);
            } finally {
                setRecommendedLoading(false);
            }
        };

        loadRecommendedProducts();
    }, []);

    // Cargar todos los productos
    useEffect(() => {
        const userId = localStorage.getItem("userId");
        if (!userId) {
            navigate("/login");
            return;
        }

        setLoading(true);
        productService
            .getAvailableProducts(page, 12)
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
            setToast(`"${product.name}" added to cart`);
            setTimeout(() => setToast(''), 3000);
        } catch (e) {
            setToast("Error : Could not add to card");
            setTimeout(() => setToast(''), 3000);
        }
    };

    const toggleProductExpand = (productId) => {
        setExpandedId(expandedId === productId ? null : productId);
    };

    const toggleRecommendedExpand = (productId) => {
        setExpandedRecommendedId(expandedRecommendedId === productId ? null : productId);
    };

    return (
        <div className="min-h-screen flex flex-col bg-black text-white">
            <Navbar showSignUpButton={false} />

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
                <div className="mb-8">
                    <div className="flex items-center justify-between flex-wrap gap-4">
                        <span
                            onClick={() => setShowFilters(true)}
                            className="text-sm text-white font-bold cursor-pointer hover:underline transition"
                        >
                            {/* Filtered Search */}
                        </span>

                        <h2 className="text-3xl sm:text-4xl font-medium text-center flex-1">Call it Art...</h2>
                        <Link to="/cart" className="flex justify-end w-10">
                            <img src={cartIcon} alt="Carrito" className="w-10 h-10 hover:scale-110 transition" />
                        </Link>
                    </div>
                </div>

                {/* Sección de Productos Recomendados */}
                <section className="mb-16">
                    <div className="flex items-center justify-between mb-8">
                        <h2 className="text-2xl sm:text-3xl font-bold text-white">
                                Recommended For You
                        </h2>
                        <div className="w-24 h-1 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full"></div>
                    </div>

                    {recommendedLoading ? (
                        <div className="flex justify-center items-center h-40">
                            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-400"></div>
                            <p className="ml-4 text-yellow-400">Loading personalized recommendations...</p>
                        </div>
                    ) : recommendedProducts.length === 0 ? (
                        <div className="text-center py-8">
                            <p className="text-gray-400 text-lg mb-2">No personalized recommendations yet</p>
                            <p className="text-gray-500 text-sm">Start browsing and adding items to your cart to get personalized suggestions!</p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6">
                            {recommendedProducts.map(product => (
                                <ProductCard
                                    key={product.productId}
                                    product={product}
                                    isExpanded={expandedRecommendedId === product.productId}
                                    onToggleExpand={() => toggleRecommendedExpand(product.productId)}
                                    onAddToCart={addToCart}
                                />
                            ))}
                        </div>
                    )}
                </section>

                {/* Línea divisoria */}
                <div className="border-t border-gray-700 mb-12"></div>

                {/* Sección de Todos los Productos */}
                <section>
                    <h2 className="text-2xl sm:text-3xl font-bold text-white mb-8 text-center">
                        All Available Pieces
                    </h2>

                    {loading ? (
                        <div className="flex justify-center items-center h-60">
                            <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-white border-opacity-40"></div>
                        </div>
                    ) : products.length === 0 ? (
                        <p className="text-gray-400 text-center">No products found.</p>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                            {products.map(product => (
                                <ProductCard
                                    key={product.productId}
                                    product={product}
                                    isExpanded={expandedId === product.productId}
                                    onToggleExpand={() => toggleProductExpand(product.productId)}
                                    onAddToCart={addToCart}
                                />
                            ))}
                        </div>
                    )}

                    {/* Paginación */}
                    {totalPages > 1 && (
                        <div className="mt-10 flex justify-center items-center gap-4 text-gray-300">
                            <button
                                disabled={page === 0}
                                onClick={() => setPage(p => Math.max(p - 1, 0))}
                                className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded disabled:opacity-50 transition"
                            >
                                Previous
                            </button>
                            <span className="text-sm">
                                Page {page + 1} of {totalPages}
                            </span>
                            <button
                                disabled={page + 1 >= totalPages}
                                onClick={() => setPage(p => Math.min(p + 1, totalPages - 1))}
                                className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded disabled:opacity-50 transition"
                            >
                                Next
                            </button>
                        </div>
                    )}
                </section>
            </main>

            {/* Sidebar de filtros deslizante */}
            <FilterSidebar
                visible={showFilters}
                onClose={() => setShowFilters(false)}
                onApply={(newFilters) => {
                    setFilters(newFilters);
                    setShowFilters(false);
                }}
            />

            <Footer />
        </div>
    );
}