import { useState } from 'react';
import Navbar from '../components/Navbar';
import { assets } from '../assets/assets';

export default function OrderHistory() {
  const [expandedId, setExpandedId] = useState(null);

  // Simulación de datos (los reemplazarás con datos reales del backend)
  const orders = [
    {
      id: 'ORD123456',
      date: 'June 29, 2025',
      total: 1300,
      currency: 'BOB',
      items: [
        { name: 'Banana', qty: 1, price: 300 },
        { name: 'Pastel', qty: 1, price: 1000 }
      ]
    },
    {
      id: 'ORD123457',
      date: 'June 25, 2025',
      total: 850,
      currency: 'USDT',
      items: [
        { name: 'Oil Painting', qty: 1, price: 850 }
      ]
    }
  ];

  const toggleExpand = (id) => {
    setExpandedId(prev => (prev === id ? null : id));
  };

  return (
    <div
      className="min-h-screen bg-cover bg-center relative"
      style={{ backgroundImage: `url(${assets.register_img})` }}
    >
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm z-0" />
      <Navbar />

      <div className="relative z-10 max-w-4xl mx-auto px-6 pt-32 text-white">
        <h1 className="text-3xl font-bold mb-8 text-center">My Orders</h1>

        <div className="space-y-6">
          {orders.map(order => (
            <div
              key={order.id}
              className="bg-zinc-900 bg-opacity-90 rounded-xl p-6 shadow-xl border border-white/10"
            >
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                  <p className="text-sm text-gray-400">Order ID</p>
                  <p className="font-medium">{order.id}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-400">Date</p>
                  <p>{order.date}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-400">Total</p>
                  <p className="font-bold text-lg">
                    {order.currency === 'BOB' ? 'Bs.' : '$'} {order.total.toFixed(2)}
                  </p>
                </div>
                <button
                  onClick={() => toggleExpand(order.id)}
                  className="self-start sm:self-center bg-white text-black px-4 py-2 rounded-full text-sm hover:bg-gray-200 transition"
                >
                  {expandedId === order.id ? 'Hide Details' : 'View Details'}
                </button>
              </div>

              {/* Detalles desplegables */}
              {expandedId === order.id && (
                <div className="mt-6 border-t border-white/10 pt-4 space-y-2 text-sm">
                  {order.items.map((item, idx) => (
                    <div key={idx} className="flex justify-between">
                      <span className="text-gray-300">{item.name} ×{item.qty}</span>
                      <span>{order.currency === 'BOB' ? 'Bs.' : '$'} {item.price.toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
