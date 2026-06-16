// src/services/auctionService.js
const API_URL = import.meta.env.VITE_API_URL;

function authHeaders() {
    const token = localStorage.getItem('authToken');
    return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` };
}

async function handleResponse(res) {
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Error ${res.status}`);
    }
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

async function getActiveAuctions(page = 0, size = 12) {
    const res = await fetch(
        `${API_URL}/auctions/active?page=${page}&size=${size}&sortBy=endDate&sortDir=ASC`,
        { headers: authHeaders() }
    );
    return handleResponse(res);
}

async function getAllAuctions(page = 0, size = 12) {
    const res = await fetch(
        `${API_URL}/auctions?page=${page}&size=${size}`,
        { headers: authHeaders() }
    );
    return handleResponse(res);
}

async function getAuctionById(id) {
    const res = await fetch(`${API_URL}/auctions/${id}`, { headers: authHeaders() });
    return handleResponse(res);
}

async function getAuctionBids(id, page = 0, size = 20) {
    const res = await fetch(
        `${API_URL}/auctions/${id}/bids?page=${page}&size=${size}`,
        { headers: authHeaders() }
    );
    return handleResponse(res);
}

async function getMyAuctions(page = 0, size = 12) {
    const res = await fetch(
        `${API_URL}/auctions/mine?page=${page}&size=${size}`,
        { headers: authHeaders() }
    );
    return handleResponse(res);
}

async function getWonAuctions(page = 0, size = 12) {
    const res = await fetch(
        `${API_URL}/auctions/won?page=${page}&size=${size}`,
        { headers: authHeaders() }
    );
    return handleResponse(res);
}

async function createAuction({ productId, startingPrice, startDate, endDate }) {
    const body = { productId, startingPrice, endDate };
    if (startDate) body.startDate = startDate;
    const res = await fetch(`${API_URL}/auctions`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(body),
    });
    return handleResponse(res);
}

async function closeAuction(id) {
    const res = await fetch(`${API_URL}/auctions/${id}/close`, {
        method: 'PUT',
        headers: authHeaders(),
    });
    return handleResponse(res);
}

async function placeBid(id, bidAmount) {
    const res = await fetch(`${API_URL}/auctions/${id}/bids`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ bidAmount }),
    });
    return handleResponse(res);
}

async function confirmPurchase(id, addressId) {
    const res = await fetch(`${API_URL}/auctions/${id}/confirm`, {
        method: 'PUT',
        headers: authHeaders(),
        body: JSON.stringify({ addressId }),
    });
    return handleResponse(res);
}

export default {
    getActiveAuctions, getAllAuctions, getAuctionById, getAuctionBids,
    getMyAuctions, getWonAuctions, createAuction, closeAuction, placeBid, confirmPurchase,
};
