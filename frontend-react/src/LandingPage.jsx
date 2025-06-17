import Navbar from "./componentes/Navbar";
import Hero from "./componentes/Hero";
import ProductGrid from "./componentes/ProductGrid";
import Footer from "./componentes/Footer";

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-white text-gray-900 flex flex-col">
      <Navbar />
      <Hero />
      <ProductGrid />
      <Footer />
    </div>
  );
};

export default LandingPage;
