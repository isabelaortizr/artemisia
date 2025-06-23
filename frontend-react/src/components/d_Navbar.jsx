// import { useState } from "react";

// const Navbar = () => {
//   const [menuOpen, setMenuOpen] = useState(false);

//   return (
//     <nav className="bg-white shadow fixed w-full z-10">
//       <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16">
//         <h1 className="text-xl font-bold text-indigo-600">Artemisia</h1>
//         <div className="hidden md:flex gap-8 text-sm font-medium text-gray-600">
//           <a href="#productos" className="hover:text-indigo-600">Products</a>
//           <a href="#nosotros" className="hover:text-indigo-600">Us</a>
//           <a href="#contacto" className="hover:text-indigo-600">Contact</a>
//         </div>
//         <button
//           className="md:hidden text-gray-600"
//           onClick={() => setMenuOpen(!menuOpen)}
//         >
//           ☰
//         </button>
//       </div>
//       {menuOpen && (
//         <div className="md:hidden px-4 pb-4 bg-white">
//           <a href="#productos" className="block py-2">Products</a>
//           <a href="#nosotros" className="block py-2">Us</a>
//           <a href="#contacto" className="block py-2">Contact</a>
//         </div>
//       )}
//     </nav>
//   );
// };

// export default Navbar;
