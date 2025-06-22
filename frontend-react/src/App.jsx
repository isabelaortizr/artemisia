// import Home from "./Home";

// function App() {
//   return <Home />;
// }

// export default App;

import { BrowserRouter, Routes, Route } from "react-router-dom";
import LandingPage from "./LandingPage";
import Login from "./pages/Login";
import Products    from './pages/Products';

function App() {
  // return <LandingPage />;
  return (
      <BrowserRouter>
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<Login />} />
            <Route path="/products" element={<Products />} />  {/* Catálogo */}
            {/* <Route path="/products/:id" element={<ProductDetail />} /> */}
        </Routes>
      </BrowserRouter>
  );
  // <Route path="/products" element={<Products />} />
  // {/* Rutas protegidas con un <PrivateRoute> si quieres */}
}
export default App;