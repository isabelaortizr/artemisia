import Header from "./components/Header";
import Navbar from "./components/Navbar";
import ProductsLanding from "./components/ProductsLanding";
import About from "./components/About";
import Footer from "./components/Footer";

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-black flex flex-col">
      <Navbar/>
      <Header/>
      <About/>
      <ProductsLanding/>
      <Footer/>

    </div>
  );
};

export default LandingPage;
