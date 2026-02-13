// ===== Payment Module with Razorpay Integration =====

// Razorpay Key ID (test mode)
const RAZORPAY_KEY_ID = 'rzp_test_S6HYZKHoOUV6bP';

const Payment = {
    // Initiate payment for a subscription
    initiatePayment: async function (subscriptionId, amount, planName) {
        try {
            // Step 1: Create order in backend
            const orderResult = await API.createPaymentOrder(subscriptionId, amount, 'INR');

            if (!orderResult.success) {
                UI.showToast(orderResult.data?.message || 'Failed to create payment order');
                return false;
            }

            const orderData = orderResult.data.data;

            // Step 2: Open Razorpay checkout
            const options = {
                key: RAZORPAY_KEY_ID,
                amount: amount * 100, // Amount in paise
                currency: 'INR',
                name: 'SubsPlat',
                description: `Subscription: ${planName}`,
                order_id: orderData.razorpayOrderId,
                handler: async function (response) {
                    // Step 3: Verify payment on backend
                    await Payment.verifyPayment(response);
                },
                prefill: {
                    email: Auth.getCurrentUser()?.email || '',
                    contact: ''
                },
                theme: {
                    color: '#000000'
                },
                modal: {
                    ondismiss: function () {
                        UI.showToast('Payment cancelled');
                    }
                }
            };

            const rzp = new Razorpay(options);
            rzp.on('payment.failed', function (response) {
                UI.showToast('Payment failed: ' + response.error.description);
                console.error('Payment failed:', response.error);
            });

            rzp.open();
            return true;

        } catch (error) {
            console.error('Payment initiation error:', error);
            UI.showToast('Error initiating payment. Please try again.');
            return false;
        }
    },

    // Verify payment after Razorpay checkout
    verifyPayment: async function (razorpayResponse) {
        try {
            UI.showToast('Verifying payment...');

            const result = await API.verifyPayment(
                razorpayResponse.razorpay_order_id,
                razorpayResponse.razorpay_payment_id,
                razorpayResponse.razorpay_signature
            );

            if (result.success) {
                UI.showToast('Payment successful! Subscription activated.');
                // Redirect to dashboard
                setTimeout(() => Dashboard.show(), 1500);
            } else {
                UI.showToast(result.data?.message || 'Payment verification failed');
            }

        } catch (error) {
            console.error('Payment verification error:', error);
            UI.showToast('Error verifying payment. Please contact support.');
        }
    }
};
