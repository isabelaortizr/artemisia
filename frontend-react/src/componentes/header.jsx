const Header = () => {
  return (
    <header className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-md p-4">
      <div className="max-w-7xl mx-auto flex justify-between items-center">
        <h1 className="text-2xl font-bold">Mi Ecommerce</h1>
        <nav className="space-x-4">
          <a href="#" className="hover:underline">Inicio</a>
          <a href="#" className="hover:underline">Productos</a>
          <a href="#" className="hover:underline">Contacto</a>
        </nav>
      </div>
    </header>
  );
};

export default Header;
