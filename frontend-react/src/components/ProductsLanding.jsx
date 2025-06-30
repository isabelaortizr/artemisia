// frontend-react/src/components/ProductsLanding.jsx
import React, { useEffect, useState } from 'react';
import { assets, projectsData } from '../assets/assets';


function ProductsLanding() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [cardsToShow, setCardsToShow] = useState(1);
  const [showFilters, setShowFilters] = useState(false);


  useEffect(() => {
    const updateCardsToShow = () => {
      if (window.innerWidth >= 1024) {
        setCardsToShow(projectsData.length); // Mostrar todos
      } else {
        setCardsToShow(1); // Carrusel en mobile
      }
    };
    updateCardsToShow();
    window.addEventListener('resize', updateCardsToShow);
    return () => window.removeEventListener('resize', updateCardsToShow);
  }, []);

  const nextProduct = () => {
    setCurrentIndex((prevIndex) => (prevIndex + 1) % projectsData.length);
  };

  const prevProduct = () => {
    setCurrentIndex((prevIndex) =>
      prevIndex === 0 ? projectsData.length - 1 : prevIndex - 1
    );
  };

  return (
    <div
      className="container mx-auto pt-20 px-6 md:px-20 lg:px-32 my-20 w-full overflow-hidden"
      id="ProductsLanding"
    >
      <h1 className="text-2xl sm:text-4xl font-bold text-white mb-2 text-center">
        Artemisia's{' '}
        <span className="underline underline-offset-4 decoration-1 font-light">
          Best Solds
        </span>
      </h1>
      <p className="text-center text-white mb-8 max-w-80 mx-auto">
        Sharing Art, Finding Treasures
      </p>

      {/* slider buttons */}
      <div className="flex justify-end items-center mb-8">
        <button
          onClick={prevProduct}
          className="p-3 bg-white rounded mr-2 hover:scale-105 transition"
          aria-label="Previous Product"
        >
          <img src={assets.left_arrow} alt="Previous" />
        </button>
        <button
          onClick={nextProduct}
          className="p-3 bg-white rounded hover:scale-105 transition"
          aria-label="Next Product"
        >
          <img src={assets.right_arrow} alt="Next" />
        </button>
      </div>

      {/* products slider container */}
      <div className="overflow-hidden">
        <div
          className="flex gap-8 transition-transform duration-500 ease-in-out"
          style={{
            transform: `translateX(-${
              (currentIndex * 100) / cardsToShow
            }%)`,
            width: `${projectsData.length * (100 / cardsToShow)}%`,
          }}
        >
          {projectsData.map((project, index) => (
            <div
              key={index}
              className="relative flex-shrink-0 w-full sm:w-1/2 md:w-1/3 lg:w-1/4 px-2" style={{ perspective: '1000px' }}
                          >
              <img
                src={project.image}
                alt={project.title}
                className="w-full h-[300px] sm:h-[220px] md:h-[240px] lg:h-[260px] xl:h-[280px] object-cover mb-14 rounded-xl"
              />

              <div className="absolute left-0 right-0 bottom-5 flex justify-center">
                <div className="rounded bg-black w-3/4 px-4 py-2 shadow-md">
                  <h2 className="text-xl font-semibold text-amber-50">
                    {project.title}
                  </h2>
                  <p className="text-amber-50 text-sm">
                    {project.price} <span> | </span> {project.artist}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default ProductsLanding;
