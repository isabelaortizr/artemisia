const ProductCard = ({ image, title, price }) => (
    <div className="border rounded-xl p-4 shadow-md hover:shadow-xl transition duration-300">
      <img
        src={image}
        alt={title}
        className="w-full h-64 object-cover rounded-lg"
      />
      <h3 className="mt-4 font-semibold text-lg">{title}</h3>
      <p className="text-gray-500 mt-1">{price}</p>
      <button className="mt-4 w-full bg-indigo-600 text-black py-2 rounded hover:bg-indigo-700 transition">
        Comprar
      </button>
    </div>
  );
  
  export default ProductCard;
  