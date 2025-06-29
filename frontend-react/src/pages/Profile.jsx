// src/pages/Profile.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import userService from '../services/userService';
import addressService from '../services/addressService';
import backIcon from '../assets/back-icon.png';

const Profile = () => {
  const navigate = useNavigate();
  const userId = localStorage.getItem('userId');

  const [user, setUser] = useState(null);
  const [uLoading, setULoad] = useState(true);
  const [uError, setUError] = useState(null);

  const [addresses, setAddresses] = useState([]);
  const [aLoading, setALoading] = useState(true);
  const [aError, setAError] = useState(null);

  const [newAddr, setNewAddr] = useState({
    recipient_name: '',
    recipient_surname: '',
    country: '',
    city: '',
    street: '',
    house_number: '',
    extra: ''
  });

  useEffect(() => {
    if (!userId) {
      navigate('/login', { replace: true });
      return;
    }
    userService
      .getUserById(userId)
      .then(data => setUser(data))
      .catch(err => setUError(err.message))
      .finally(() => setULoad(false));

    fetchAddresses();
  }, [navigate, userId]);

  const fetchAddresses = async () => {
    setALoading(true);
    try {
      const list = await addressService.getAddressesByUser(userId);
      setAddresses(list);
    } catch (err) {
      setAError(err.message);
    } finally {
      setALoading(false);
    }
  };

  const handleAddrChange = e => {
    const { name, value } = e.target;
    setNewAddr(a => ({ ...a, [name]: value }));
  };

  const handleAddrSubmit = async e => {
    e.preventDefault();
    try {
      await addressService.createAddress({
        ...newAddr,
        user_id: Number(userId)
      });
      setNewAddr({
        recipient_name: '',
        recipient_surname: '',
        country: '',
        city: '',
        street: '',
        house_number: '',
        extra: ''
      });
      fetchAddresses();
    } catch (err) {
      setAError(err.message);
    }
  };

  if (uLoading) return <p className="text-center mt-10 text-white">Cargando perfil…</p>;
  if (uError) return <p className="text-center mt-10 text-red-500">Error: {uError}</p>;

  return (
    <div className="relative min-h-screen bg-gradient-to-br from-black via-zinc-900 to-black text-white">
      <Navbar showSignUpButton={false} />

      <div className="p-6 pt-28">
        {/* <Link to="/products" className="absolute top-6 left-6">
          <img src={backIcon} alt="Volver" className="w-8 h-8 hover:opacity-80 transition" />
        </Link> */}

        <div className="max-w-3xl mx-auto mt-12">
          <h2 className="text-3xl font-bold text-center mb-8">Mi Perfil</h2>

          <div className="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-6 space-y-6 mb-12">
            <div>
              <p className="text-gray-300 text-sm">Nombre</p>
              <p className="text-lg font-semibold">{user.name}</p>
            </div>
            <div>
              <p className="text-gray-300 text-sm">Correo</p>
              <p className="text-lg font-semibold">{user.mail}</p>
            </div>
            <div>
              <p className="text-gray-300 text-sm">Rol</p>
              <p className="text-lg font-semibold">{user.role}</p>
            </div>
          </div>

          <div className="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-6 space-y-6 mb-12">
            <h3 className="text-2xl font-semibold mb-4">Mis Direcciones</h3>
            {aLoading ? (
              <p className="text-white">Cargando direcciones…</p>
            ) : aError ? (
              <p className="text-red-500">{aError}</p>
            ) : addresses.length === 0 ? (
              <p className="text-gray-400">No tienes direcciones guardadas.</p>
            ) : (
              <ul className="space-y-4">
                {addresses.map(addr => (
                  <li key={addr.address_id} className="bg-zinc-900 p-4 rounded-xl border border-white/10">
                    <p className="font-semibold">{addr.recipient_name} {addr.recipient_surname}</p>
                    <p>{addr.street}, {addr.house_number}</p>
                    <p>{addr.city} — {addr.country}</p>
                    {addr.extra && <p className="text-sm text-gray-400">{addr.extra}</p>}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-6">
            <h3 className="text-2xl font-semibold mb-4">Agregar Nueva Dirección</h3>
            <form onSubmit={handleAddrSubmit} className="space-y-4">
              {[
                { name: 'recipient_name', label: 'Nombre destinatario' },
                { name: 'recipient_surname', label: 'Apellido destinatario' },
                { name: 'country', label: 'País' },
                { name: 'city', label: 'Ciudad' },
                { name: 'street', label: 'Calle' },
                { name: 'house_number', label: 'Número de casa' },
                { name: 'extra', label: 'Información extra (opc.)' }
              ].map(fld => (
                <div key={fld.name}>
                  <label className="block text-sm text-gray-300 mb-1">{fld.label}</label>
                  {fld.name === 'extra' ? (
                    <textarea
                      name="extra"
                      rows={2}
                      value={newAddr.extra}
                      onChange={handleAddrChange}
                      className="w-full p-2 rounded-lg bg-zinc-800 text-white border border-white/10 focus:outline-none focus:ring"
                    />
                  ) : (
                    <input
                      name={fld.name}
                      value={newAddr[fld.name]}
                      onChange={handleAddrChange}
                      required={fld.name !== 'extra'}
                      className="w-full p-2 rounded-lg bg-zinc-800 text-white border border-white/10 focus:outline-none focus:ring"
                    />
                  )}
                </div>
              ))}

              <button
                type="submit"
                className="w-full py-2 bg-white text-black rounded-xl hover:bg-gray-200 transition font-semibold"
              >
                Guardar Dirección
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
