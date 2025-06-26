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


async function createProduct({
                                 sellerId,
                                 name,
                                 technique,
                                 materials,
                                 description,
                                 price,
                                 stock,
                                 status,
                                 image,
                                 category
                             }) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/products`, {
        method: 'POST',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            sellerId,
            name,
            technique,
            materials,
            description,
            price,
            stock,
            status,
            image,
            category
        }),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al crear producto`);
    }
    return res.json(); // ProductResponseDto
}

async function getProductsBySeller(sellerId, page = 0, size = 10,) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(
        `${API_URL}/products/seller/${sellerId}?page=${page}&size=${size}`,
        {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        }
    );
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status} al cargar obras del vendedor`);
    }
    const pageData = await res.json();
    return {
        items: pageData.content,
        totalPages: pageData.totalPages
    };
}

async function updateProduct(id, productDto) {
    const token = localStorage.getItem('authToken');
    const res = await fetch(`${API_URL}/products/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(productDto),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error actualizando producto (${res.status})`);
    }
    return res.json(); // ProductResponseDto
}

export default { getProducts, createProduct, getProductsBySeller, updateProduct };
