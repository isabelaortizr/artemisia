import ProductCard from "./d_ProductCard";

const productos = [
  {
    id: 1,
    image: "/res/test1.jpg",
    title: "Producto 1",
    price: "$49.00",
  },
  {
    id: 2,
    image: "/res/test.jpg",
    title: "Producto 2",
    price: "$49.00",
  },
  {
    id: 3,
    image: "/res/test.jpg",
    title: "Producto 3",
    price: "$49.00",
  },
  {
    id: 4,
    image: "/res/test.jpg",
    title: "Producto 4",
    price: "$49.00",
  },
];

const ProductGrid = () => (
  <section id="productos" className="py-20 bg-orange-500 flex-grow">
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <h2 className="text-3xl font-semibold mb-12 text-center tracking-tight">
        Our newest drops
      </h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 gap-8">
        {productos.map((p) => (
          <ProductCard key={p.id} {...p} />
        ))}
      </div>
    </div>
  </section>
);

export default ProductGrid;
