import { Link } from 'react-router-dom';
import { assets } from '../assets/assets';
import Navbar from '../components/Navbar';

export default function OrderReceipt() {
  return (
    <div
      className="min-h-screen bg-cover bg-center relative"
      style={{ backgroundImage: `url(${assets.register_img})` }}
    >
      {/* Overlay and blur */}
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm z-0" />

      <Navbar showSignUpButton={false} />

      <div className="relative z-10 max-w-3xl mx-auto p-6 pt-32 text-white">
        <div className="bg-zinc-900 bg-opacity-90 p-8 rounded-2xl shadow-xl border border-white/10">
          <h1 className="text-3xl font-bold text-center mb-4">Thank you for your purchase!</h1>
          <p className="text-center text-gray-300 mb-8">We've received your order and it's currently being processed.</p>

          {/* Order info */}
          <div className="mb-6 space-y-3">
            <div className="flex justify-between border-b border-white/10 pb-2">
              <span className="text-gray-400">Order Number:</span>
              <span className="font-semibold">#ORD123456</span>
            </div>
            <div className="flex justify-between border-b border-white/10 pb-2">
              <span className="text-gray-400">Date:</span>
              <span>June 29, 2025</span>
            </div>
            <div className="flex justify-between border-b border-white/10 pb-2">
              <span className="text-gray-400">Payment Method:</span>
              <span>USDT</span>
            </div>
            <div className="flex justify-between border-b border-white/10 pb-2">
              <span className="text-gray-400">Total:</span>
              <span className="font-bold text-lg">Bs. 1300.00</span>
            </div>
          </div>

          {/* Product summary */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-4">Summary</h2>
            <ul className="space-y-2">
              <li className="flex justify-between">
                <span className="text-gray-300">Banana x1</span>
                <span>Bs. 300.00</span>
              </li>
              <li className="flex justify-between">
                <span className="text-gray-300">Pastel x1</span>
                <span>Bs. 1000.00</span>
              </li>
            </ul>
          </div>

          {/* Action buttons */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/products"
              className="w-full sm:w-auto text-center bg-white text-black px-6 py-3 rounded-full hover:bg-gray-200 transition"
            >
              Continue Shopping
            </Link>
            <Link
              to="/orderHistory"
              className="w-full sm:w-auto text-center bg-black border border-white px-6 py-3 rounded-full hover:bg-white hover:text-black transition"
            >
              View My Orders
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
