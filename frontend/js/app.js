// ===== Main Application Entry Point =====

// Initialize application when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

const App = {
    init: function () {
        // Initialize UI
        UI.init();

        // Initialize Documents
        if (typeof Documents !== 'undefined') {
            Documents.init();
        }

        // Load plans
        Plans.load();

        // Setup form handlers
        this.setupFormHandlers();

        // Setup hash-based routing
        this.setupRouter();

        // Handle initial route
        this.handleRoute();

        console.log('Application initialized');
    },

    // Hash-based router
    setupRouter: function () {
        window.addEventListener('hashchange', () => this.handleRoute());
    },

    handleRoute: function () {
        const hash = window.location.hash || '#home';

        switch (hash) {
            case '#dashboard':
                if (Auth.isAuthenticated()) {
                    Dashboard.show();
                } else {
                    window.location.hash = '#home';
                    UI.showModal('login');
                }
                break;
            case '#admin':
                if (Auth.isAuthenticated() && Auth.isAdmin()) {
                    Admin.show();
                } else {
                    window.location.hash = '#home';
                }
                break;
            case '#profile':
                if (Auth.isAuthenticated()) {
                    Profile.show();
                } else {
                    window.location.hash = '#home';
                    UI.showModal('login');
                }
                break;
            case '#plans':
                UI.scrollToPlans();
                break;
            case '#home':
            default:
                UI.goHome();
                break;
        }
    },

    // Navigate to a route
    navigate: function (route) {
        window.location.hash = route;
    },

    setupFormHandlers: function () {
        // Login form
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const email = document.getElementById('loginEmail').value;
                const password = document.getElementById('loginPassword').value;

                const result = await Auth.login(email, password);

                if (result.success) {
                    UI.hideModal('login');
                    UI.showAuthenticatedState();
                    UI.showToast('Login successful!');
                    loginForm.reset();
                    document.getElementById('loginError').textContent = '';
                } else {
                    document.getElementById('loginError').textContent = result.message;
                }
            });
        }

        // Register form
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const userData = {
                    firstName: document.getElementById('registerFirstName').value,
                    lastName: document.getElementById('registerLastName').value,
                    email: document.getElementById('registerEmail').value,
                    password: document.getElementById('registerPassword').value
                };

                const result = await Auth.register(userData);

                if (result.success) {
                    UI.hideModal('register');
                    UI.showAuthenticatedState();
                    UI.showToast('Account created successfully!');
                    registerForm.reset();
                    document.getElementById('registerError').textContent = '';
                } else {
                    document.getElementById('registerError').textContent = result.message;
                }
            });
        }

        // Profile form
        const profileForm = document.getElementById('profileForm');
        if (profileForm) {
            profileForm.addEventListener('submit', (e) => Profile.update(e));
        }

        // Create plan form
        const createPlanForm = document.getElementById('createPlanForm');
        if (createPlanForm) {
            createPlanForm.addEventListener('submit', (e) => Admin.createPlan(e));
        }
    }
};

// Global function exports for onclick handlers
function showModal(type) { UI.showModal(type); }
function hideModal(type) { UI.hideModal(type); }
function switchModal(type) {
    if (type === 'register') {
        UI.switchModal('login', 'register');
    } else {
        UI.switchModal('register', 'login');
    }
}
function logout() {
    Auth.logout();
    App.navigate('#home');
}
function goHome() { App.navigate('#home'); }
function scrollToPlans() { App.navigate('#plans'); }
function toggleUserDropdown() { UI.toggleDropdown(); }
function showDashboard() { App.navigate('#dashboard'); }
function showProfile() { App.navigate('#profile'); }
function showAdminDashboard() { App.navigate('#admin'); }
