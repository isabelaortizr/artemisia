const Hero = () => (
    <header className="bg-yellow-300 pt-32 pb-20 text-center">
      <div className="max-w-4xl mx-auto px-4">
        <h1 className="text-4xl sm:text-5xl font-extrabold leading-tight tracking-tight">
          <span className="text-orange-500">Artemisia</span> says ¡hello!
        </h1>
        <p className="mt-4 text-lg text-gray-600 max-w-xl mx-auto">
          Where art never fades — Unique & Legit pieces for each moment.
        </p>
        <div className="mt-6">
          <a
            href="#productos"
            className="inline-block rounded-md bg-orange-500 px-6 py-3 text-white font-bold hover:bg-orange-500 transition"
          >
            All pieces...
          </a>
        </div>
      </div>
    </header>
  );
  
  export default Hero;
  