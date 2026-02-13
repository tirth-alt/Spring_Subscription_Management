// ===== API Configuration and Utilities =====
const API_BASE_URL = 'http://localhost:8080';

// Generic API call helper
async function apiCall(endpoint, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // Add auth token if available
    const token = localStorage.getItem('authToken');
    if (token) {
        defaultOptions.headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers,
        },
    };

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
        const data = await response.json();

        // Handle 401 Unauthorized
        if (response.status === 401) {
            Auth.logout();
            UI.showToast('Session expired. Please login again.');
            return { success: false, error: 'unauthorized' };
        }

        return { response, data, success: response.ok && data.success };
    } catch (error) {
        console.error('API Error:', error);
        return { success: false, error: error.message };
    }
}

// API Endpoints
const API = {
    // Auth
    login: (email, password) => apiCall('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
    }),

    register: (userData) => apiCall('/auth/register', {
        method: 'POST',
        body: JSON.stringify(userData),
    }),

    // Plans
    getPlans: () => apiCall('/api/plans'),
    getAllPlans: () => apiCall('/api/plans/all'),
    getPlanById: (id) => apiCall(`/api/plans/${id}`),
    createPlan: (planData) => apiCall('/api/plans', {
        method: 'POST',
        body: JSON.stringify(planData),
    }),
    updatePlan: (id, planData) => apiCall(`/api/plans/${id}`, {
        method: 'PUT',
        body: JSON.stringify(planData),
    }),
    deletePlan: (id) => apiCall(`/api/plans/${id}`, {
        method: 'DELETE',
    }),

    // Subscriptions
    subscribe: (planId) => apiCall('/api/subscriptions', {
        method: 'POST',
        body: JSON.stringify({ planId }),
    }),
    getActiveSubscription: () => apiCall('/api/subscriptions/active'),
    getSubscriptionHistory: (page = 0, size = 10) =>
        apiCall(`/api/subscriptions/history?page=${page}&size=${size}`),
    cancelSubscription: (id) => apiCall(`/api/subscriptions/${id}`, {
        method: 'DELETE',
    }),

    // Users
    getProfile: () => apiCall('/api/users/profile'),
    updateProfile: (userData) => apiCall('/api/users/profile', {
        method: 'PUT',
        body: JSON.stringify(userData),
    }),
    getAllUsers: () => apiCall('/api/users'),
    getUserById: (id) => apiCall(`/api/users/${id}`),

    // Payments
    createPaymentOrder: (subscriptionId, amount, currency = 'INR') => apiCall('/api/payments/create', {
        method: 'POST',
        body: JSON.stringify({ subscriptionId, amount, currency }),
    }),
    verifyPayment: (razorpayOrderId, razorpayPaymentId, razorpaySignature) => apiCall('/api/payments/verify', {
        method: 'POST',
        body: JSON.stringify({ razorpayOrderId, razorpayPaymentId, razorpaySignature }),
    }),
    getPaymentHistory: () => apiCall('/api/payments/history'),

    // Admin Stats
    getSubscriptionStats: () => apiCall('/api/subscriptions/admin/stats'),
    getRevenueStats: () => apiCall('/api/payments/admin/revenue-stats'),

    // Analytics (if available)
    getAnalytics: () => apiCall('/api/analytics'),

    // Documents
    uploadDocument: async (file, entityType, entityId) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('entityType', entityType);
        formData.append('entityId', entityId);

        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${API_BASE_URL}/api/documents/upload`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });
            const data = await response.json();
            return { success: response.ok, data };
        } catch (error) {
            console.error('API Error:', error);
            return { success: false, error: error.message };
        }
    },
    getUserDocuments: () => apiCall('/api/documents'),
    deleteDocument: (id) => apiCall(`/api/documents/${id}`, { method: 'DELETE' }),
};
