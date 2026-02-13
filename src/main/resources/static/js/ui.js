// ===== UI Utilities Module =====
const UI = {
    // Show toast notification
    showToast: function (message, duration = 3000) {
        const toast = document.getElementById('toast');
        if (toast) {
            toast.textContent = message;
            toast.classList.add('show');
            setTimeout(() => toast.classList.remove('show'), duration);
        }
    },

    // Format date
    formatDate: function (dateString) {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    },

    // Format currency
    formatCurrency: function (amount) {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            minimumFractionDigits: 0
        }).format(amount);
    },

    // Show modal
    showModal: function (type) {
        const modal = document.getElementById(`${type}Modal`);
        if (modal) {
            modal.style.display = 'block';
            document.body.style.overflow = 'hidden';
        }
    },

    // Hide modal
    hideModal: function (type) {
        const modal = document.getElementById(`${type}Modal`);
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    },

    // Switch modal
    switchModal: function (from, to) {
        this.hideModal(from);
        this.showModal(to);
    },

    // Show authenticated state
    showAuthenticatedState: function () {
        const user = Auth.getCurrentUser();
        if (!user) return;

        const authButtons = document.getElementById('authButtons');
        const userMenu = document.getElementById('userMenu');
        const userInitial = document.getElementById('userInitial');
        const getStartedBtn = document.getElementById('getStartedBtn');
        const mySubsBtn = document.getElementById('mySubsBtn');
        const adminLink = document.getElementById('adminLink');

        if (authButtons) authButtons.style.display = 'none';
        if (userMenu) userMenu.style.display = 'block';
        if (userInitial) userInitial.textContent = (user.firstName || user.email)[0].toUpperCase();
        if (getStartedBtn) getStartedBtn.style.display = 'none';
        if (mySubsBtn) mySubsBtn.style.display = 'inline-flex';
        if (adminLink) adminLink.style.display = Auth.isAdmin() ? 'block' : 'none';
    },

    // Show unauthenticated state
    showUnauthenticatedState: function () {
        const authButtons = document.getElementById('authButtons');
        const userMenu = document.getElementById('userMenu');
        const getStartedBtn = document.getElementById('getStartedBtn');
        const mySubsBtn = document.getElementById('mySubsBtn');
        const adminLink = document.getElementById('adminLink');

        if (authButtons) authButtons.style.display = 'flex';
        if (userMenu) userMenu.style.display = 'none';
        if (getStartedBtn) getStartedBtn.style.display = 'inline-flex';
        if (mySubsBtn) mySubsBtn.style.display = 'none';
        if (adminLink) adminLink.style.display = 'none';
    },

    // Navigate to home
    goHome: function () {
        this.hideAllSections();
        const sections = ['home', 'plans', 'features', 'footer'];
        sections.forEach(id => {
            const el = document.getElementById(id);
            if (el) el.style.display = id === 'home' ? 'flex' : 'block';
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    // Hide all sections
    hideAllSections: function () {
        const sections = ['home', 'plans', 'features', 'dashboard', 'adminDashboard', 'profile'];
        sections.forEach(id => {
            const el = document.getElementById(id);
            if (el) el.style.display = 'none';
        });
    },

    // Scroll to plans
    scrollToPlans: function () {
        const plans = document.getElementById('plans');
        if (plans) plans.scrollIntoView({ behavior: 'smooth' });
    },

    // Toggle dropdown
    toggleDropdown: function () {
        const dropdown = document.getElementById('dropdownMenu');
        if (dropdown) dropdown.classList.toggle('show');
    },

    // Close dropdown
    closeDropdown: function () {
        const dropdown = document.getElementById('dropdownMenu');
        if (dropdown) dropdown.classList.remove('show');
    },

    // Setup click outside handler
    setupClickOutside: function () {
        document.addEventListener('click', (e) => {
            const userMenu = document.getElementById('userMenu');
            if (userMenu && !userMenu.contains(e.target)) {
                this.closeDropdown();
            }
        });

        // Close modals on outside click
        window.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal')) {
                e.target.style.display = 'none';
                document.body.style.overflow = 'auto';
            }
        });
    },

    // Initialize UI
    init: function () {
        this.setupClickOutside();

        if (Auth.isAuthenticated()) {
            this.showAuthenticatedState();
        } else {
            this.showUnauthenticatedState();
        }
    }
};
