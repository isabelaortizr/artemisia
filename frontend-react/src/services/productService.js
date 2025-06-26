// src/services/productService.js
const API_URL = import.meta.env.VITE_API_URL;

async function getProducts(page = 0, size = 10, filters = {}) {
  const token = localStorage.getItem('authToken');

  const params = new URLSearchParams();
  params.append('page', page);
  params.append('size', size);

  for (const key in filters) {
    const value = filters[key];
    if (
      value !== '' &&
      value !== null &&
      value !== undefined &&
      !(typeof value === 'boolean' && value === false)
    ) {
      params.append(key, value);
    }
  }

  const res = await fetch(`${API_URL}/products?${params.toString()}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || `Request failed with status ${res.status}`);
  }

  const json = await res.json();
  return {
    items: Array.isArray(json.content) ? json.content : [],
    page: json.number,
    totalPages: json.totalPages,
    totalElements: json.totalElements
  };
}

export default { getProducts };

