import { useState } from "react";

const CATEGORY_OPTIONS = ['Pintura', 'Escultura', 'Ilustración'];
const TECHNIQUE_OPTIONS = ['Óleo', 'Acrílico', 'Tinta', 'Spray'];
const MATERIALS_OPTIONS = ['Lienzo', 'Madera', 'Papel'];

export default function FilterSidebar({ onApply }) {
  const [isOpen, setIsOpen] = useState(false);
  const [filters, setFilters] = useState({
    category: '',
    technique: '',
    materials: '',
    minPrice: '',
    maxPrice: '',
    inStock: false
  });

  const handleChange = e => {
    const { name, value, type, checked } = e.target;
    setFilters(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const applyFilters = () => {
    onApply(filters);
    setIsOpen(false);
  };

  return (
    <>
      {/* Icono discreto para abrir cuando está cerrado */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="fixed top-24 left-2 z-40 bg-white text-black border px-2 py-1 rounded shadow hover:bg-gray-100 transition"
        >
          Filtered Search
        </button>
      )}

      {/* Sidebar visible */}
      {isOpen && (
        <aside className="fixed top-24 left-0 z-30 w-72 bg-white text-black h-full shadow-2xl p-6 overflow-y-auto transition-all">
          {/* Botón de cerrar */}
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-2xl font-semibold">Filtros</h3>
            <button
              onClick={() => setIsOpen(false)}
              className="text-gray-500 hover:text-gray-800 text-xl"
              title="Cerrar filtros"
            >
              ✕
            </button>
          </div>

          {/* Categoría */}
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Categoría</label>
            <select
              name="category"
              value={filters.category}
              onChange={handleChange}
              className="w-full p-2 border rounded text-sm"
            >
              <option value="">Todas</option>
              {CATEGORY_OPTIONS.map(opt => (
                <option key={opt} value={opt}>{opt}</option>
              ))}
            </select>
          </div>

          {/* Técnica */}
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Técnica</label>
            <select
              name="technique"
              value={filters.technique}
              onChange={handleChange}
              className="w-full p-2 border rounded text-sm"
            >
              <option value="">Todas</option>
              {TECHNIQUE_OPTIONS.map(opt => (
                <option key={opt} value={opt}>{opt}</option>
              ))}
            </select>
          </div>

          {/* Materiales */}
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Materiales</label>
            <select
              name="materials"
              value={filters.materials}
              onChange={handleChange}
              className="w-full p-2 border rounded text-sm"
            >
              <option value="">Todos</option>
              {MATERIALS_OPTIONS.map(opt => (
                <option key={opt} value={opt}>{opt}</option>
              ))}
            </select>
          </div>

          {/* Precio */}
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Rango de precio</label>
            <div className="flex gap-2">
              <input
                name="minPrice"
                type="number"
                placeholder="Min"
                value={filters.minPrice}
                onChange={handleChange}
                className="w-1/2 p-2 border rounded text-sm"
              />
              <input
                name="maxPrice"
                type="number"
                placeholder="Max"
                value={filters.maxPrice}
                onChange={handleChange}
                className="w-1/2 p-2 border rounded text-sm"
              />
            </div>
          </div>

          {/* Stock */}
          <div className="mb-6">
            <label className="flex items-center gap-2 text-sm">
              <input
                name="inStock"
                type="checkbox"
                checked={filters.inStock}
                onChange={handleChange}
              />
              Solo productos con stock
            </label>
          </div>

          <button
            onClick={applyFilters}
            className="w-full bg-black text-white py-2 rounded-lg hover:bg-gray-900 transition"
          >
            Aplicar filtros
          </button>
        </aside>
      )}
    </>
  );
}
