// ===== Plans Module =====
const Plans = {
    // Load and render plans
    load: async function () {
        const container = document.getElementById('plansContainer');
        if (!container) return;

        container.innerHTML = '<div class="loading">Loading plans...</div>';

        const result = await API.getPlans();

        if (result.success && result.data.data) {
            this.render(result.data.data);
        } else {
            this.renderSamplePlans();
        }
    },

    // Render plans
    render: function (plans) {
        const container = document.getElementById('plansContainer');
        if (!container) return;

        if (plans.length === 0) {
            container.innerHTML = '<p class="loading">No plans available at the moment.</p>';
            return;
        }

        container.innerHTML = plans.map((plan, index) => `
            <div class="plan-card ${index === 1 ? 'featured' : ''}">
                <h3 class="plan-name">${plan.name}</h3>
                <p class="plan-description">${plan.description || 'Perfect for getting started'}</p>
                <div class="plan-price">
                    ${UI.formatCurrency(plan.price)}<span>/${plan.billingCycle?.toLowerCase() || 'month'}</span>
                </div>
                <ul class="plan-features">
                    ${(plan.features || []).map(feature => `<li>${feature}</li>`).join('')}
                </ul>
                <button class="btn ${index === 1 ? 'btn-primary' : 'btn-outline'}" 
                        onclick="Plans.subscribe('${plan.id}')">
                    Choose Plan
                </button>
            </div>
        `).join('');
    },

    // Render sample plans (fallback)
    renderSamplePlans: function () {
        const container = document.getElementById('plansContainer');
        if (!container) return;

        const samplePlans = [
            {
                id: 'basic',
                name: 'Basic',
                description: 'Perfect for individuals',
                price: 299,
                billingCycle: 'month',
                features: ['5 Projects', 'Basic Support', '1GB Storage']
            },
            {
                id: 'pro',
                name: 'Professional',
                description: 'Best for growing teams',
                price: 799,
                billingCycle: 'month',
                features: ['Unlimited Projects', 'Priority Support', '10GB Storage', 'Advanced Analytics']
            },
            {
                id: 'enterprise',
                name: 'Enterprise',
                description: 'For large organizations',
                price: 1999,
                billingCycle: 'month',
                features: ['Everything in Pro', 'Dedicated Support', 'Unlimited Storage', 'Custom Integrations']
            }
        ];

        container.innerHTML = samplePlans.map((plan, index) => `
            <div class="plan-card ${index === 1 ? 'featured' : ''}">
                <h3 class="plan-name">${plan.name}</h3>
                <p class="plan-description">${plan.description}</p>
                <div class="plan-price">
                    ${UI.formatCurrency(plan.price)}<span>/${plan.billingCycle}</span>
                </div>
                <ul class="plan-features">
                    ${plan.features.map(feature => `<li>${feature}</li>`).join('')}
                </ul>
                <button class="btn ${index === 1 ? 'btn-primary' : 'btn-outline'}" 
                        onclick="Plans.subscribe('${plan.id}')">
                    Choose Plan
                </button>
            </div>
        `).join('');
    },

    // Subscribe to a plan - adds to history, user pays from dashboard
    subscribe: async function (planId) {
        if (!Auth.isAuthenticated()) {
            UI.showModal('login');
            UI.showToast('Please login to subscribe');
            return;
        }

        UI.showToast('Adding plan to your subscriptions...');

        // Create subscription in backend (status will be PENDING)
        const result = await API.subscribe(planId);

        if (!result.success) {
            UI.showToast(result.data?.message || 'Failed to add subscription');
            return;
        }

        UI.showToast('Plan added! Go to My Subscriptions to complete payment.');

        // Redirect to dashboard to pay
        setTimeout(() => Dashboard.show(), 1000);
    }
};
