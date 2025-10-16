import numpy as np
from services.vector_builder import product_to_vector, update_user_accum, DIM


def test_product_to_vector_basic():
    product = {
        'categories': ['Realista', 'Abstracta'],
        'techniques': ['Óleo', 'Acrílico'],
        'price': 150.0
    }
    v = product_to_vector(product)
    assert v.shape[0] == DIM
    # categories: Realista and Abstracta should increment first two indices accordingly
    # depending on config ordering, we check non-zero sum
    assert v.sum() > 0


def test_update_user_accum():
    prod1 = {'categories': ['Realista'], 'techniques': ['Óleo'], 'price': 100}
    prod2 = {'categories': ['Abstracta'], 'techniques': ['Acrílico'], 'price': 200}
    v1 = product_to_vector(prod1)
    v2 = product_to_vector(prod2)

    accum, ws, norm = update_user_accum(None, 0.0, v1, 1.0)
    assert ws == 1.0
    assert accum.shape == v1.shape

    accum2, ws2, norm2 = update_user_accum(accum, ws, v2, 2.0)
    assert ws2 == 3.0
    assert accum2.shape == v1.shape
    # normalized vector should be unit length or zero
    norm_val = np.linalg.norm(norm2)
    assert abs(norm_val - 1.0) < 1e-6 or norm_val == 0.0
