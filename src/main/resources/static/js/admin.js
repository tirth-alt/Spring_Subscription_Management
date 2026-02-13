// ===== Admin Dashboard Module =====
const Admin = {
    // Current state
    currentTab: 'overview',
    stats: {
        totalUsers: 0,
        totalPlans: 0,
        activeSubscriptions: 0,
        monthlyRevenue: 0
    },

    // Show admin dashboard
    show: async function () {
        if (!Auth.isAdmin()) {
            UI.showToast('Access denied. Admin privileges required.');
            return;
        }

        UI.closeDropdown();
        UI.hideAllSections();

        const adminDashboard = document.getElementById('adminDashboard');
        const footer = document.getElementById('footer');
        if (adminDashboard) adminDashboard.style.display = 'block';
        if (footer) footer.style.display = 'block';

        window.scrollTo({ top: 0, behavior: 'smooth' });

        await this.loadStats();
        this.switchTab('overview');
    },

    // Load admin statistics
    loadStats: async function () {
        try {
            // Load users
            const usersResult = await API.getAllUsers();
            if (usersResult.success && usersResult.data.data) {
                this.stats.totalUsers = usersResult.data.data.length;
            }

            // Load plans
            const plansResult = await API.getAllPlans();
            if (plansResult.success && plansResult.data.data) {
                this.stats.totalPlans = plansResult.data.data.filter(p => p.active).length;
            }

            // Load subscription stats
            const subsResult = await API.getSubscriptionStats();
            if (subsResult.success && subsResult.data.data) {
                this.stats.activeSubscriptions = subsResult.data.data.activeSubscriptions || 0;
            }

            // Load revenue stats
            const revenueResult = await API.getRevenueStats();
            if (revenueResult.success && revenueResult.data.data) {
                this.stats.totalRevenue = revenueResult.data.data.totalRevenue || 0;
                this.stats.monthlyRevenue = revenueResult.data.data.monthlyRevenue || 0;
                this.stats.totalPayments = revenueResult.data.data.totalPayments || 0;
                this.stats.monthlyPayments = revenueResult.data.data.monthlyPayments || 0;
            }

            this.updateStatsDisplay();
        } catch (error) {
            console.error('Error loading admin stats:', error);
        }
    },

    // Update stats display
    updateStatsDisplay: function () {
        const elements = {
            adminTotalUsers: this.stats.totalUsers,
            adminTotalPlans: this.stats.totalPlans,
            adminActiveSubscriptions: this.stats.activeSubscriptions,
            adminRevenue: UI.formatCurrency(this.stats.monthlyRevenue),
            adminTotalRevenue: UI.formatCurrency(this.stats.totalRevenue),
            adminMonthlyPayments: this.stats.monthlyPayments,
            adminTotalPayments: this.stats.totalPayments
        };

        Object.entries(elements).forEach(([id, value]) => {
            const el = document.getElementById(id);
            if (el) el.textContent = value;
        });
    },

    // Switch admin tab
    switchTab: function (tabName) {
        this.currentTab = tabName;

        // Update tab buttons
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tabName);
        });

        // Update tab content
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.toggle('active', content.id === `${tabName}Tab`);
        });

        // Load content
        switch (tabName) {
            case 'overview':
                this.loadOverview();
                break;
            case 'users':
                this.loadUsers();
                break;
            case 'plans':
                this.loadPlans();
                break;
            case 'analytics':
                this.loadAnalytics();
                break;
        }
    },

    // Load overview
    loadOverview: async function () {
        // Overview is shown by default with stats
        this.renderOverviewCards();
    },

    // Render overview cards
    renderOverviewCards: function () {
        const container = document.getElementById('overviewContent');
        if (!container) return;

        container.innerHTML = `
            <div class="overview-grid">
                <div class="overview-card highlight-card">
                    <div class="overview-icon">+</div>
                    <div class="overview-info">
                        <h3>Add New Plan</h3>
                        <p class="text-muted">Create a new subscription plan for your users</p>
                        <button class="btn btn-primary" onclick="Admin.showCreatePlanModal()">
                            + Create Plan
                        </button>
                    </div>
                </div>
                <div class="overview-card">
                    <div class="overview-icon">◆</div>
                    <div class="overview-info">
                        <h3>Quick Actions</h3>
                        <div class="quick-actions-list">
                            <button class="btn btn-outline btn-sm" onclick="Admin.switchTab('plans')">
                                Manage Plans
                            </button>
                            <button class="btn btn-outline btn-sm" onclick="Admin.switchTab('users')">
                                View Users
                            </button>
                            <button class="btn btn-outline btn-sm" onclick="Admin.switchTab('analytics')">
                                Analytics
                            </button>
                        </div>
                    </div>
                </div>
                <div class="overview-card full-width">
                    <div class="overview-icon">◈</div>
                    <div class="overview-info">
                        <h3>Platform Summary</h3>
                        <div class="summary-grid">
                            <div class="summary-item">
                                <span class="summary-value">${this.stats.totalUsers}</span>
                                <span class="summary-label">Users</span>
                            </div>
                            <div class="summary-item">
                                <span class="summary-value">${this.stats.totalPlans}</span>
                                <span class="summary-label">Active Plans</span>
                            </div>
                            <div class="summary-item">
                                <span class="summary-value">${this.stats.activeSubscriptions}</span>
                                <span class="summary-label">Subscriptions</span>
                            </div>
                            <div class="summary-item">
                                <span class="summary-value">${UI.formatCurrency(this.stats.monthlyRevenue)}</span>
                                <span class="summary-label">Monthly Revenue</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    },

    // Load users table
    loadUsers: async function () {
        const tbody = document.getElementById('usersTableBody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="5" class="loading">Loading users...</td></tr>';

        const result = await API.getAllUsers();

        if (result.success && result.data.data) {
            const users = result.data.data;

            if (users.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="loading">No users found</td></tr>';
                return;
            }

            tbody.innerHTML = users.map(user => `
                <tr>
                    <td>
                        <div class="user-cell">
                            <div class="user-avatar-small">${(user.firstName || user.email)[0].toUpperCase()}</div>
                            <span>${user.firstName || ''} ${user.lastName || ''}</span>
                        </div>
                    </td>
                    <td>${user.email}</td>
                    <td>
                        <span class="badge ${user.role === 'ROLE_ADMIN' ? 'badge-primary' : 'badge-secondary'}">
                            ${user.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}
                        </span>
                    </td>
                    <td>${UI.formatDate(user.createdAt)}</td>
                    <td>
                        <button class="btn btn-outline btn-xs" onclick="Admin.viewUser('${user.id}')">View</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="5" class="loading">Failed to load users</td></tr>';
        }
    },

    // View user details
    viewUser: function (userId) {
        UI.showToast('User details view coming soon');
    },

    // Load plans table
    loadPlans: async function () {
        const tbody = document.getElementById('plansTableBody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="6" class="loading">Loading plans...</td></tr>';

        const result = await API.getAllPlans();

        if (result.success && result.data.data) {
            const plans = result.data.data;

            if (plans.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="loading">No plans found</td></tr>';
                return;
            }

            tbody.innerHTML = plans.map(plan => `
                <tr>
                    <td><strong>${plan.name}</strong></td>
                    <td>${plan.description || '-'}</td>
                    <td>${UI.formatCurrency(plan.price)}</td>
                    <td>${plan.billingCycle || 'MONTHLY'}</td>
                    <td>
                        <span class="badge ${plan.active ? 'badge-success' : 'badge-danger'}">
                            ${plan.active ? 'Active' : 'Inactive'}
                        </span>
                    </td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-outline btn-xs" onclick="Admin.editPlan('${plan.id}')">Edit</button>
                            <button class="btn btn-danger btn-xs" onclick="Admin.deletePlan('${plan.id}')">Delete</button>
                        </div>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="loading">Failed to load plans</td></tr>';
        }
    },

    // Show create plan modal
    showCreatePlanModal: function () {
        const form = document.getElementById('createPlanForm');
        if (form) form.reset();

        const errorEl = document.getElementById('createPlanError');
        if (errorEl) errorEl.textContent = '';

        // Set modal title for create mode
        const modalTitle = document.querySelector('#createPlanModal h2');
        if (modalTitle) modalTitle.textContent = 'Create New Plan';

        // Clear edit mode data
        const form2 = document.getElementById('createPlanForm');
        if (form2) form2.dataset.editId = '';

        UI.showModal('createPlan');
    },

    // Create plan
    createPlan: async function (event) {
        event.preventDefault();

        const name = document.getElementById('planName').value;
        const description = document.getElementById('planDescription').value;
        const price = parseFloat(document.getElementById('planPrice').value);
        const billingCycle = document.getElementById('planBillingCycle').value;
        const featuresInput = document.getElementById('planFeatures').value;
        const features = featuresInput ? featuresInput.split(',').map(f => f.trim()) : [];

        // Convert billingCycle to durationInDays for backend
        const durationMap = { 'MONTHLY': 30, 'QUARTERLY': 90, 'YEARLY': 365, 'WEEKLY': 7 };
        const durationInDays = durationMap[billingCycle] || 30;

        const planData = { name, description, price, durationInDays, features, active: true };

        // Check if editing
        const form = document.getElementById('createPlanForm');
        const editId = form?.dataset.editId;

        let result;
        if (editId) {
            result = await API.updatePlan(editId, planData);
        } else {
            result = await API.createPlan(planData);
        }

        if (result.success) {
            UI.hideModal('createPlan');
            UI.showToast(editId ? 'Plan updated successfully!' : 'Plan created successfully!');
            document.getElementById('createPlanForm').reset();
            this.loadPlans();
            this.loadStats();
            Plans.load(); // Refresh public plans
        } else {
            const errorEl = document.getElementById('createPlanError');
            if (errorEl) errorEl.textContent = result.data?.message || 'Failed to save plan';
        }
    },

    // Edit plan
    editPlan: async function (planId) {
        const result = await API.getPlanById(planId);

        if (result.success && result.data.data) {
            const plan = result.data.data;

            document.getElementById('planName').value = plan.name;
            document.getElementById('planDescription').value = plan.description || '';
            document.getElementById('planPrice').value = plan.price;
            document.getElementById('planBillingCycle').value = plan.billingCycle || 'MONTHLY';
            document.getElementById('planFeatures').value = (plan.features || []).join(', ');

            // Set edit mode
            const form = document.getElementById('createPlanForm');
            if (form) form.dataset.editId = planId;

            const modalTitle = document.querySelector('#createPlanModal h2');
            if (modalTitle) modalTitle.textContent = 'Edit Plan';

            UI.showModal('createPlan');
        } else {
            UI.showToast('Failed to load plan details');
        }
    },

    // Delete plan
    deletePlan: async function (planId) {
        if (!confirm('Are you sure you want to delete this plan?')) {
            return;
        }

        const result = await API.deletePlan(planId);

        if (result.success) {
            UI.showToast('Plan deleted successfully');
            this.loadPlans();
            this.loadStats();
            Plans.load();
        } else {
            UI.showToast(result.data?.message || 'Failed to delete plan');
        }
    },

    // Load analytics
    loadAnalytics: function () {
        const container = document.getElementById('analyticsContent');
        if (!container) return;

        container.innerHTML = `
            <div class="analytics-grid">
                <div class="analytics-card">
                    <h3>User Growth</h3>
                    <div class="chart-placeholder">
                        <div class="chart-bar" style="height: 40%"></div>
                        <div class="chart-bar" style="height: 55%"></div>
                        <div class="chart-bar" style="height: 45%"></div>
                        <div class="chart-bar" style="height: 70%"></div>
                        <div class="chart-bar" style="height: 85%"></div>
                        <div class="chart-bar" style="height: 65%"></div>
                        <div class="chart-bar active" style="height: 100%"></div>
                    </div>
                    <div class="chart-labels">
                        <span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span><span>Sun</span>
                    </div>
                    <p class="analytics-summary">
                        <strong>${this.stats.totalUsers}</strong> total users
                    </p>
                </div>
                <div class="analytics-card">
                    <h3>Revenue Overview</h3>
                    <div class="revenue-display">
                        <span class="revenue-amount">${UI.formatCurrency(this.stats.monthlyRevenue)}</span>
                        <span class="revenue-label">Estimated Monthly</span>
                    </div>
                    <div class="revenue-breakdown">
                        <div class="breakdown-item">
                            <span>Active Plans</span>
                            <strong>${this.stats.totalPlans}</strong>
                        </div>
                        <div class="breakdown-item">
                            <span>Subscriptions</span>
                            <strong>${this.stats.activeSubscriptions}</strong>
                        </div>
                    </div>
                </div>
                <div class="analytics-card full-width">
                    <h3>Plan Performance</h3>
                    <div class="plan-performance">
                        <p class="text-muted">Detailed plan analytics will be available when subscription data is collected.</p>
                    </div>
                </div>
            </div>
        `;
    }
};
