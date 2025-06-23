import React from 'react';
import { assets } from '../assets/assets';

function About() {
  return (
    <div
      className="flex flex-col items-center justify-center container mx-auto px-6 py-14 md:px-20 lg:px-32 w-full overflow-hidden"
      id="About"
    >
      <h1 className="text-2xl sm:text-4xl font-bold mb-2 text-white">
        About{' '}
        <span className="underline underline-offset-4 decoration-1 font-light">
          Our Store
        </span>
      </h1>
      <p className="text-white max-w-80 text-center mb-8">
        Passionate About Art, Dedicated to Finding It
      </p>

      <div className="flex flex-col md:flex-row items-center md:items-start gap-20">
        <img
          src={assets.brand_img}
          alt="Brand"
          className="w-full sm:w-1/2 max-w-lg hover:scale-105 transition duration-300 rounded-lg shadow-md"
        />

        <div className="flex flex-col items-center md:items-start mt-10 text-white">
          <div className="grid grid-cols-2 gap-6 md:gap-10 w-full 2xl:pr-28">
            <div>
              <p className="text-4xl font-semibold">1k+</p>
              <p>Masterpieces Available</p>
            </div>
            <div>
              <p className="text-4xl font-semibold">50+</p>
              <p>Artists Discovered</p>
            </div>
            <div>
              <p className="text-4xl font-semibold">3</p>
              <p>Young Art & Code Lovers</p>
            </div>
            <div>
              <p className="text-4xl font-semibold">1st</p>
              <p>Bolivian-Based Digital Art Commerce</p>
            </div>
          </div>

          <p className="my-10 max-w-lg text-center md:text-left">
            Artemisia was born as a university project created by three Systems Engineering students who shared a passion for art and technology.
            Their mission: to support and promote local Bolivian artists by giving them a space to showcase their work and reach new audiences.
            What started as an academic initiative quickly evolved into a creative platform that bridges culture, community, and digital innovation.
          </p>

          <button className="bg-white text-black px-8 py-2 rounded transition duration-300 hover:bg-black hover:text-white hover:scale-105">
            Learn More
          </button>
        </div>
      </div>
    </div>
  );
}

export default About;
