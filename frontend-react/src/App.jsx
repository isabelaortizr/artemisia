import React from 'react'
import Header from './components/Header'
import About from './components/About'
import Products from './components/Products'
import Footer from './components/d_Footer'

const App = () => {
  return (
    <div className='w-full overflow-hidden bg-black'>
      <Header/> 
      <About/>
      <Products/>
      <Footer/>
    </div>
  )
}

export default App

// // import Home from "./Home";

// // function App() {
// //   return <Home />;
// // }

// // export default App;

// import LandingPage from "./LandingPage";
// function App() {
//   return <LandingPage />;

// }
// export default LandingPage;
