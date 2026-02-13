// ===== Dashboard Module =====
const Dashboard = {
    // Show user dashboard
    show: async function () {
        if (!Auth.isAuthenticated()) {
            UI.showModal('login');
            return;
        }

        UI.closeDropdown();
        UI.hideAllSections();

        const dashboard = document.getElementById('dashboard');
        const footer = document.getElementById('footer');
        if (dashboard) dashboard.style.display = 'block';
        if (footer) footer.style.display = 'block';

        window.scrollTo({ top: 0, behavior: 'smooth' });

        // Set user name
        const user = Auth.getCurrentUser();
        const userName = user.firstName || user.email.split('@')[0];
        const dashboardUserName = document.getElementById('dashboardUserName');
        if (dashboardUserName) dashboardUserName.textContent = userName;

        await this.loadActiveSubscription();
        await this.loadSubscriptionHistory();
        if (typeof Documents !== 'undefined') {
            await Documents.loadDocuments();
        }
    },

    // Load active subscriptions (all of them)
    loadActiveSubscription: async function () {
        const container = document.getElementById('currentSubscription');
        const statPlan = document.getElementById('statPlan');
        const statStatus = document.getElementById('statStatus');
        const statRenewal = document.getElementById('statRenewal');

        // Get all subscriptions and filter for ACTIVE ones
        const result = await API.getSubscriptionHistory();

        if (result.success && result.data.data) {
            const activeSubs = result.data.data.filter(sub => sub.status === 'ACTIVE');

            if (activeSubs.length > 0) {
                // Update stats with first active sub info
                const firstSub = activeSubs[0];
                const firstPlanName = firstSub.plan?.name || 'Active Plan';

                if (statPlan) statPlan.textContent = activeSubs.length > 1 ? `${activeSubs.length} Active Plans` : firstPlanName;
                if (statStatus) statStatus.textContent = 'Active';
                if (statRenewal) statRenewal.textContent = UI.formatDate(firstSub.endDate);

                // Render all active subscriptions
                if (container) {
                    container.innerHTML = activeSubs.map(sub => {
                        const planName = sub.plan?.name || 'Subscription';
                        return `
                        <div class="subscription-item">
                            <h4>${planName}</h4>
                            <span class="status status-active">ACTIVE</span>
                            <p>Started: ${UI.formatDate(sub.startDate)}</p>
                            <p>Ends: ${UI.formatDate(sub.endDate)}</p>
                            <button class="btn btn-danger btn-sm" style="margin-top: 1rem;" 
                                onclick="Dashboard.cancelSubscription('${sub.id}')">Cancel Subscription</button>
                        </div>
                    `}).join('');
                }
            } else {
                if (statPlan) statPlan.textContent = 'No Plan';
                if (statStatus) statStatus.textContent = 'Inactive';
                if (statRenewal) statRenewal.textContent = 'N/A';
                if (container) container.innerHTML = '<p class="no-subscription">No active subscription</p>';
            }
        } else {
            if (statPlan) statPlan.textContent = 'No Plan';
            if (statStatus) statStatus.textContent = 'Inactive';
            if (statRenewal) statRenewal.textContent = 'N/A';
            if (container) container.innerHTML = '<p class="no-subscription">No active subscription</p>';
        }
    },

    // Load subscription history
    loadSubscriptionHistory: async function () {
        const container = document.getElementById('subscriptionHistory');
        const statHistory = document.getElementById('statHistory');

        const result = await API.getSubscriptionHistory();

        if (result.success && result.data.data && result.data.data.length > 0) {
            if (statHistory) statHistory.textContent = result.data.data.length;
            if (container) {
                container.innerHTML = result.data.data.map(sub => {
                    const planName = sub.plan?.name || 'Subscription';
                    const planPrice = sub.plan?.price || 0;
                    return `
                    <div class="subscription-item">
                        <h4>${planName}</h4>
                        <span class="status status-${sub.status.toLowerCase()}">${sub.status}</span>
                        <p>${UI.formatDate(sub.startDate)} - ${UI.formatDate(sub.endDate)}</p>
                        <p style="font-size: 0.9rem; color: var(--text-muted);">Price: ${UI.formatCurrency(planPrice)}</p>
                        ${sub.status === 'PENDING' ?
                            `<div style="display: flex; gap: 0.5rem; margin-top: 0.75rem;">
                                <button class="btn btn-primary btn-sm" 
                                    onclick="Dashboard.payForSubscription('${sub.id}', ${planPrice}, '${planName}')">
                                    Pay Now
                                </button>
                                <button class="btn btn-danger btn-sm" 
                                    onclick="Dashboard.removeSubscription('${sub.id}')">
                                    Remove
                                </button>
                            </div>` : ''
                        }
                    </div>
                `}).join('');
            }
        } else {
            if (statHistory) statHistory.textContent = '0';
            if (container) container.innerHTML = '<p class="no-history">No subscription history</p>';
        }
    },

    // Cancel subscription
    cancelSubscription: async function (id) {
        if (!confirm('Are you sure you want to cancel this subscription?')) {
            return;
        }

        const result = await API.cancelSubscription(id);

        if (result.success) {
            UI.showToast('Subscription cancelled successfully');
            await this.loadActiveSubscription();
            await this.loadSubscriptionHistory();
        } else {
            UI.showToast(result.data?.message || 'Failed to cancel subscription');
        }
    },

    // Pay for a pending subscription
    payForSubscription: async function (subscriptionId, price, planName) {
        await Payment.initiatePayment(subscriptionId, price, planName);
    },

    // Remove a pending subscription
    removeSubscription: async function (id) {
        if (!confirm('Are you sure you want to remove this subscription?')) {
            return;
        }

        const result = await API.cancelSubscription(id);

        if (result.success) {
            UI.showToast('Subscription removed successfully');
            await this.loadActiveSubscription();
            await this.loadSubscriptionHistory();
        } else {
            UI.showToast(result.data?.message || 'Failed to remove subscription');
        }
    }
};

// ===== Profile Module =====
const Profile = {
    // Show profile
    show: async function () {
        if (!Auth.isAuthenticated()) {
            UI.showModal('login');
            return;
        }

        UI.closeDropdown();
        UI.hideAllSections();

        const profile = document.getElementById('profile');
        const footer = document.getElementById('footer');
        if (profile) profile.style.display = 'block';
        if (footer) footer.style.display = 'block';

        window.scrollTo({ top: 0, behavior: 'smooth' });
        await this.load();
    },

    // Load profile data
    load: async function () {
        const result = await API.getProfile();

        if (result.success && result.data.data) {
            const user = result.data.data;

            const profileEmail = document.getElementById('profileEmail');
            const profileFirstName = document.getElementById('profileFirstName');
            const profileLastName = document.getElementById('profileLastName');
            const profileRole = document.getElementById('profileRole');
            const profileInitial = document.getElementById('profileInitial');

            if (profileEmail) profileEmail.value = user.email;
            if (profileFirstName) profileFirstName.value = user.firstName || '';
            if (profileLastName) profileLastName.value = user.lastName || '';
            if (profileRole) profileRole.value = user.role || 'USER';
            if (profileInitial) profileInitial.textContent = (user.firstName || user.email)[0].toUpperCase();

            // Update local storage
            Auth.saveAuth(Auth.getToken(), user);
        }
    },

    // Update profile
    update: async function (event) {
        event.preventDefault();

        const firstName = document.getElementById('profileFirstName').value;
        const lastName = document.getElementById('profileLastName').value;

        const result = await API.updateProfile({ firstName, lastName });

        if (result.success) {
            const user = Auth.getCurrentUser();
            Auth.saveAuth(Auth.getToken(), { ...user, firstName, lastName });

            const userInitial = document.getElementById('userInitial');
            const profileInitial = document.getElementById('profileInitial');
            if (userInitial) userInitial.textContent = firstName[0].toUpperCase();
            if (profileInitial) profileInitial.textContent = firstName[0].toUpperCase();

            UI.showToast('Profile updated successfully');
        } else {
            UI.showToast(result.data?.message || 'Failed to update profile');
        }
    }
};
