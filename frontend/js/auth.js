// ===== Authentication Module =====
const Auth = {
    // Get current user from localStorage
    getCurrentUser: function () {
        try {
            const storedUser = localStorage.getItem('currentUser');
            if (storedUser && storedUser !== 'undefined' && storedUser !== 'null') {
                return JSON.parse(storedUser);
            }
        } catch (e) {
            console.error('Error parsing stored user:', e);
            localStorage.removeItem('currentUser');
        }
        return null;
    },

    // Get auth token
    getToken: function () {
        return localStorage.getItem('authToken') || null;
    },

    // Check if user is authenticated
    isAuthenticated: function () {
        return this.getToken() !== null && this.getCurrentUser() !== null;
    },

    // Check if user is admin
    isAdmin: function () {
        const user = this.getCurrentUser();
        if (!user || !user.role) return false;
        // Handle both 'ROLE_ADMIN' and 'ADMIN' formats
        return user.role === 'ROLE_ADMIN' || user.role === 'ADMIN';
    },

    // Save auth data
    saveAuth: function (token, user) {
        localStorage.setItem('authToken', token);
        localStorage.setItem('currentUser', JSON.stringify(user));
    },

    // Clear auth data
    clearAuth: function () {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
    },

    // Login handler
    login: async function (email, password) {
        const result = await API.login(email, password);

        if (result.success && result.data.data) {
            const authData = result.data.data;
            const user = {
                email: authData.email,
                firstName: authData.firstName,
                lastName: authData.lastName,
                role: authData.role
            };
            this.saveAuth(authData.token, user);
            return { success: true, user };
        }

        return {
            success: false,
            message: result.data?.message || 'Login failed. Please try again.'
        };
    },

    // Register handler
    register: async function (userData) {
        const result = await API.register(userData);

        if (result.success && result.data.data) {
            const authData = result.data.data;
            const user = {
                email: authData.email,
                firstName: authData.firstName,
                lastName: authData.lastName,
                role: authData.role
            };
            this.saveAuth(authData.token, user);
            return { success: true, user };
        }

        return {
            success: false,
            message: result.data?.message || 'Registration failed. Please try again.'
        };
    },

    // Logout handler
    logout: function () {
        this.clearAuth();
        UI.showUnauthenticatedState();
        UI.showToast('Logged out successfully');
        UI.goHome();
    }
};
