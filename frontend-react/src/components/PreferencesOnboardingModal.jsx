// src/components/PreferencesOnboardingModal.jsx
import React, { useState, useEffect } from 'react';
import productService from '../services/productService';
import ImageModal from './ImageModal';

const PreferencesOnboardingModal = ({ isOpen, onComplete }) => {
    const [sampleArtworks, setSampleArtworks] = useState([]);
    const [selectedArtworks, setSelectedArtworks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [selectedImage, setSelectedImage] = useState(null);

    useEffect(() => {
        if (isOpen) {
            loadSampleArtworks();
        }
    }, [isOpen]);

    const loadSampleArtworks = async () => {
        try {
            setLoading(true);
            const response = await productService.getAvailableProducts(0, 12);
            setSampleArtworks(response.items || []);
        } catch (error) {
            console.error('Error loading sample artworks:', error);
            setError('Error loading artworks. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleArtworkSelect = async (artwork) => {
        // Track the view cuando el usuario selecciona una obra
        try {
            await productService.trackProductView(artwork.productId);
        } catch (error) {
            console.error('Error tracking view:', error);
        }

        setSelectedArtworks(prev => {
            if (prev.find(a => a.productId === artwork.productId)) {
                return prev.filter(a => a.productId !== artwork.productId);
            } else {
                return [...prev, artwork];
            }
        });
    };

    const handleImageClick = (artwork, e) => {
        e.stopPropagation();
        setSelectedImage(artwork);
    };

    const handleSubmit = async () => {
        if (selectedArtworks.length === 0) {
            setError('Please select at least one artwork that you like');
            return;
        }

        setSaving(true);
        setError('');

        try {
            // Extraer solo los IDs de los productos seleccionados
            const productIds = selectedArtworks.map(artwork => artwork.productId);

            console.log('Enviando preferencias de primer login:', productIds);

            // Enviar la lista de IDs al backend
            await productService.trackFirstLoginPreferences(productIds);

            console.log('Preferencias guardadas exitosamente');
            onComplete();

        } catch (error) {
            console.error('Error saving preferences:', error);
            setError('Error saving your preferences. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    const handleSkip = () => {
        // Enviar lista vacía para indicar que no hay preferencias
        try {
            productService.trackFirstLoginPreferences([]);
        } catch (error) {
            console.error('Error skipping preferences:', error);
        }
        onComplete();
    };

    if (!isOpen) return null;

    return (
        <>
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-90 backdrop-blur-sm p-4">
                <div className="bg-white rounded-2xl p-6 max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                    <div className="text-center mb-6">
                        <h2 className="text-3xl font-bold text-gray-800 mb-3">
                            Help Us Understand Your Taste
                        </h2>
                        <p className="text-gray-600 text-lg">
                            Select the artworks that catch your eye. This helps us recommend pieces you'll love.
                        </p>
                        <div className="mt-4 bg-blue-50 border border-blue-200 rounded-lg p-3">
                            <p className="text-blue-700 text-sm">
                                <strong>Tip:</strong> Click on any image to view it in full size
                            </p>
                        </div>
                    </div>

                    {error && (
                        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
                            <p className="text-red-700 text-sm text-center">{error}</p>
                        </div>
                    )}

                    {loading ? (
                        <div className="flex justify-center items-center h-40">
                            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-600"></div>
                            <p className="ml-4 text-blue-600">Loading artworks...</p>
                        </div>
                    ) : sampleArtworks.length === 0 ? (
                        <div className="text-center py-8">
                            <p className="text-gray-500">No artworks available at the moment.</p>
                            <button
                                onClick={handleSkip}
                                className="mt-4 px-6 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
                            >
                                Continue Anyway
                            </button>
                        </div>
                    ) : (
                        <>
                            {/* Grid de obras */}
                            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mb-6">
                                {sampleArtworks.map(artwork => {
                                    const isSelected = selectedArtworks.find(a => a.productId === artwork.productId);
                                    const imageSrc = artwork.image
                                        ? `data:image/jpeg;base64,${artwork.image}`
                                        : 'https://via.placeholder.com/300x300';

                                    return (
                                        <div
                                            key={artwork.productId}
                                            className={`relative cursor-pointer group transition-all duration-300 ${
                                                isSelected
                                                    ? 'ring-2 ring-blue-500 scale-105'
                                                    : 'hover:scale-105 hover:ring-2 hover:ring-gray-300'
                                            }`}
                                            onClick={() => handleArtworkSelect(artwork)}
                                        >
                                            {/* Imagen */}
                                            <div className="aspect-square overflow-hidden rounded-lg bg-gray-100">
                                                <img
                                                    src={imageSrc}
                                                    alt={artwork.name}
                                                    className="w-full h-full object-cover group-hover:opacity-90 transition-opacity cursor-zoom-in"
                                                    onClick={(e) => handleImageClick(artwork, e)}
                                                />
                                            </div>

                                            {/* Check flotante en esquina */}
                                            {isSelected && (
                                                <div className="absolute top-2 right-2">
                                                    <div className="bg-blue-500 text-white rounded-full p-2 shadow-lg border-2 border-white">
                                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                                        </svg>
                                                    </div>
                                                </div>
                                            )}

                                            {/* Info de la obra */}
                                            <div className="mt-2 text-center">
                                                <p className="text-sm font-medium text-gray-800 truncate">
                                                    {artwork.name}
                                                </p>
                                                <p className="text-xs text-gray-500">
                                                    {artwork.techniques && artwork.techniques[0] ? artwork.techniques[0] : 'Artwork'}
                                                </p>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>

                            {/* Contador y botones */}
                            <div className="border-t border-gray-200 pt-6">
                                <div className="flex flex-col sm:flex-row justify-between items-center gap-4">
                                    <div className="text-center sm:text-left">
                                        <p className="text-gray-700 font-medium">
                                            Selected: <span className="text-blue-600">{selectedArtworks.length}</span> artwork(s)
                                        </p>
                                        <p className="text-sm text-gray-500">
                                            {selectedArtworks.length >= 3
                                                ? 'Great selection! Ready to continue.'
                                                : 'Select at least 3 artworks for better recommendations'
                                            }
                                        </p>
                                    </div>

                                    <div className="flex gap-3">
                                        <button
                                            onClick={handleSkip}
                                            disabled={saving}
                                            className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 font-medium"
                                        >
                                            Skip for Now
                                        </button>
                                        <button
                                            onClick={handleSubmit}
                                            disabled={saving || selectedArtworks.length === 0}
                                            className={`px-6 py-3 rounded-lg text-white font-medium transition-colors ${
                                                saving || selectedArtworks.length === 0
                                                    ? 'bg-gray-400 cursor-not-allowed'
                                                    : 'bg-blue-600 hover:bg-blue-700'
                                            }`}
                                        >
                                            {saving ? 'Saving...' : `Continue (${selectedArtworks.length})`}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>

            {/* Modal para imagen en grande */}
            {selectedImage && (
                <ImageModal
                    imageSrc={selectedImage.image ? `data:image/jpeg;base64,${selectedImage.image}` : 'https://via.placeholder.com/600x600'}
                    alt={selectedImage.name}
                    onClose={() => setSelectedImage(null)}
                />
            )}
        </>
    );
};

export default PreferencesOnboardingModal;