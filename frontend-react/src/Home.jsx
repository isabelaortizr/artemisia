import Header from "./componentes/header";

const Home = () => {
  return (
    <>
      <Header />
      <main className="max-w-7xl mx-auto p-6">
        <h2 className="text-3xl font-semibold mb-4">Bienvenido a Mi Ecommerce</h2>
        <p className="text-gray-700">
          Explora nuestros productos y encuentra lo que necesitas. Esta es una pantalla de prueba para desarrollo.
        </p>
      </main>
    </>
  );
};

export default Home;
