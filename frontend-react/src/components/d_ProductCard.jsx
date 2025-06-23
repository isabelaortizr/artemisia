const ProductCard = ({ image, title, price }) => (
    <div className="rounded-xl p-6 shadow-md hover:shadow-xl transition duration-300">
      <img
        src={"/res/test1.jpg"}  
        alt={title}
        className="w-full h-72 object-cover rounded-lg"
      />
        <h3 className="mt-4 text-xl font-semibold">{title}</h3>
    <p className="text-gray-500 mt-1 text-base">{price}</p>
      <button className="mt-4 w-full bg-indigo-600 text-black py-2 rounded hover:bg-indigo-700 transition">
        Comprar
      </button>
    </div>
  );
  
  export default ProductCard;
  