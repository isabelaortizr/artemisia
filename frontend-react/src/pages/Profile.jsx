// src/pages/Profile.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, Link }         from 'react-router-dom';
import userService                    from '../services/userService';
import addressService                 from '../services/addressService';
import backIcon                       from '../assets/back-icon.png';

const Profile = () => {
    const navigate = useNavigate();
    const userId   = localStorage.getItem('userId');

    // — User Info —
    const [user,    setUser]    = useState(null);
    const [uLoading, setULoad]  = useState(true);
    const [uError,   setUError] = useState(null);

    // — Addresses —
    const [addresses,   setAddresses]   = useState([]);
    const [aLoading,    setALoading]    = useState(true);
    const [aError,      setAError]      = useState(null);

    // — New Address Form —
    const [newAddr, setNewAddr] = useState({
        recipientName:    '',
        recipientSurname: '',
        country:          '',
        city:             '',
        street:           '',
        houseNumber:      '',
        extra:            ''
    });

    // Fetch user info
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

    // Fetch addresses
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

    // Handle new address form changes
    const handleAddrChange = e => {
        const { name, value } = e.target;
        setNewAddr(a => ({ ...a, [name]: value }));
    };

    // Submit new address
    const handleAddrSubmit = async e => {
        e.preventDefault();
        try {
            await addressService.createAddress({
                ...newAddr,
                user_id: Number(userId)
            });
            // reset form + reload list
            setNewAddr({
                recipientName:    '',
                recipientSurname: '',
                country:          '',
                city:             '',
                street:           '',
                houseNumber:      '',
                extra:            ''
            });
            fetchAddresses();
        } catch (err) {
            setAError(err.message);
        }
    };

    if (uLoading) return <p className="text-center mt-10">Cargando perfil…</p>;
    if (uError)   return <p className="text-center mt-10 text-red-500">Error: {uError}</p>;

    return (
        <div className="relative max-w-md mx-auto p-6">
            {/* Volver */}
            <Link to="/products" className="absolute top-6 left-6">
                <img
                    src={backIcon}
                    alt="Volver"
                    className="w-8 h-8 hover:opacity-80 transition"
                />
            </Link>

            <h2 className="text-2xl font-bold mb-6 text-center">Mi Perfil</h2>

            {/* — User Card — */}
            <div className="space-y-4 bg-white p-6 rounded-lg shadow text-gray-900 mb-8">
                <div>
                    <p className="text-gray-600">Nombre</p>
                    <p className="font-medium">{user.name}</p>
                </div>
                <div>
                    <p className="text-gray-600">Correo</p>
                    <p className="font-medium">{user.mail}</p>
                </div>
                <div>
                    <p className="text-gray-600">Rol</p>
                    <p className="font-medium">{user.role}</p>
                </div>
            </div>

            {/* — Addresses List — */}
            <div className="space-y-4 mb-8">
                <h3 className="text-xl font-semibold">Mis Direcciones</h3>

                {aLoading ? (
                    <p>Cargando direcciones…</p>
                ) : aError ? (
                    <p className="text-red-500">{aError}</p>
                ) : addresses.length === 0 ? (
                    <p className="text-gray-600">No tienes direcciones guardadas.</p>
                ) : (
                    <ul className="space-y-3">
                        {addresses.map(addr => (
                            <li
                                key={addr.address_id}
                                className="bg-white p-4 rounded-lg shadow text-gray-900"
                            >
                                <p>
                                    <strong>
                                        {addr.recipient_name} {addr.recipient_surname}
                                    </strong>
                                </p>
                                <p>
                                    {addr.street}, {addr.house_number}
                                </p>
                                <p>
                                    {addr.city} — {addr.country}
                                </p>
                                {addr.extra && (
                                    <p className="text-sm text-gray-600">{addr.extra}</p>
                                )}
                            </li>
                        ))}
                    </ul>
                )}
            </div>

            {/* — New Address Form — */}
            <div className="bg-white p-6 rounded-lg shadow text-gray-900">
                <h3 className="text-xl font-semibold mb-4">Agregar Nueva Dirección</h3>
                <form onSubmit={handleAddrSubmit} className="space-y-4">
                    {[
                        { name: 'recipientName',    label: 'Nombre destinatario'    },
                        { name: 'recipientSurname', label: 'Apellido destinatario'  },
                        { name: 'country',          label: 'País'                    },
                        { name: 'city',             label: 'Ciudad'                 },
                        { name: 'street',           label: 'Calle'                  },
                        { name: 'houseNumber',      label: 'Número de casa'         },
                        { name: 'extra',            label: 'Información extra (opc.)'}
                    ].map(fld => (
                        <div key={fld.name}>
                            <label className="block text-gray-700 mb-1">{fld.label}</label>
                            {fld.name === 'extra' ? (
                                <textarea
                                    name="extra"
                                    rows={2}
                                    value={newAddr.extra}
                                    onChange={handleAddrChange}
                                    className="w-full p-2 border rounded focus:outline-none"
                                />
                            ) : (
                                <input
                                    name={fld.name}
                                    value={newAddr[fld.name]}
                                    onChange={handleAddrChange}
                                    required={fld.name !== 'extra'}
                                    className="w-full p-2 border rounded focus:outline-none"
                                />
                            )}
                        </div>
                    ))}

                    <button
                        type="submit"
                        className="w-full py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition"
                    >
                        Guardar Dirección
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Profile;
