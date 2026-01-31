import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { plansApi, subscriptionsApi } from '../../api';
import { paymentsApi, PaymentInitiateResponse } from '../../api/payments.api';
import { Plan, Subscription } from '../../types';
import { Check, Star, Zap } from 'lucide-react';
import PaymentModal from '../../components/PaymentModal';
import './PlansPage.css';

const PlansPage: React.FC = () => {
    const [plans, setPlans] = useState<Plan[]>([]);
    const [currentSubscription, setCurrentSubscription] = useState<Subscription | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [subscribingPlanId, setSubscribingPlanId] = useState<string | null>(null);
    const [showPaymentModal, setShowPaymentModal] = useState(false);
    const [paymentDetails, setPaymentDetails] = useState<PaymentInitiateResponse | null>(null);
    const [selectedPlanId, setSelectedPlanId] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [plansData, subData] = await Promise.all([
                    plansApi.getAll(),
                    subscriptionsApi.getActive().catch(() => null),
                ]);
                setPlans(plansData.filter(p => p.active));
                setCurrentSubscription(subData);
            } catch (error) {
                console.error('Error fetching plans:', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, []);

    const handleSubscribe = async (planId: string) => {
        setSubscribingPlanId(planId);
        setSelectedPlanId(planId);

        try {
            // First, initiate payment to check if payment is required
            const paymentResponse = await paymentsApi.initiate(planId);

            if (paymentResponse.requiresPayment) {
                // Show payment modal
                setPaymentDetails(paymentResponse);
                setShowPaymentModal(true);
            } else {
                // No payment required (free trial or free plan)
                await subscriptionsApi.subscribe({ planId });
                navigate('/subscriptions');
            }
        } catch (error: any) {
            console.error('Error subscribing:', error);
            alert(error.response?.data?.message || 'Failed to subscribe. Please try again.');
        } finally {
            setSubscribingPlanId(null);
        }
    };

    const handlePaymentSuccess = async (paymentOrderId: string) => {
        setShowPaymentModal(false);
        setSubscribingPlanId(selectedPlanId);

        try {
            // Create subscription with verified payment
            await subscriptionsApi.subscribe({
                planId: selectedPlanId!,
                paymentOrderId,
            });
            navigate('/subscriptions');
        } catch (error: any) {
            console.error('Error creating subscription after payment:', error);
            alert(error.response?.data?.message || 'Payment successful but failed to create subscription. Please contact support.');
        } finally {
            setSubscribingPlanId(null);
            setSelectedPlanId(null);
        }
    };

    const handlePaymentClose = () => {
        setShowPaymentModal(false);
        setPaymentDetails(null);
        setSelectedPlanId(null);
    };

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    return (
        <div className="plans-page">
            <div className="plans-header">
                <h2 className="plans-title">Choose Your Plan</h2>
                <p className="plans-subtitle">
                    Select the perfect plan for your business needs. Upgrade or downgrade anytime.
                </p>
            </div>

            <div className="plans-grid">
                {plans.map((plan) => {
                    const isCurrentPlan = currentSubscription?.plan?.id === plan.id;
                    const isFeatured = plan.isFeatured;

                    return (
                        <div
                            key={plan.id}
                            className={`plan-card ${isFeatured ? 'featured' : ''} ${isCurrentPlan ? 'current' : ''}`}
                        >
                            {isFeatured && (
                                <div className="plan-badge-featured">
                                    <Star size={14} />
                                    <span>Most Popular</span>
                                </div>
                            )}

                            <div className="plan-header">
                                <h3 className="plan-name">{plan.name}</h3>
                                <p className="plan-description">{plan.description}</p>
                            </div>

                            <div className="plan-pricing">
                                <span className="plan-currency">$</span>
                                <span className="plan-price">{plan.price}</span>
                                <span className="plan-period">/{plan.billingCycle?.toLowerCase()}</span>
                            </div>

                            <ul className="plan-features">
                                {plan.usageLimit && (
                                    <li>
                                        <Check size={16} />
                                        <span>{plan.usageLimit.toLocaleString()} usage units</span>
                                    </li>
                                )}
                                {plan.apiCallsLimit && (
                                    <li>
                                        <Check size={16} />
                                        <span>{plan.apiCallsLimit.toLocaleString()} API calls/month</span>
                                    </li>
                                )}
                                {plan.storageLimitMb && (
                                    <li>
                                        <Check size={16} />
                                        <span>{plan.storageLimitMb} MB storage</span>
                                    </li>
                                )}
                                {plan.usersLimit && (
                                    <li>
                                        <Check size={16} />
                                        <span>Up to {plan.usersLimit} team members</span>
                                    </li>
                                )}
                                {plan.features?.map((feature, index) => (
                                    <li key={index}>
                                        <Check size={16} />
                                        <span>{feature}</span>
                                    </li>
                                ))}
                                {plan.trialDays > 0 && (
                                    <li className="trial-feature">
                                        <Zap size={16} />
                                        <span>{plan.trialDays}-day free trial</span>
                                    </li>
                                )}
                            </ul>

                            <button
                                className={`btn ${isFeatured ? 'btn-primary' : 'btn-secondary'} btn-lg w-full`}
                                onClick={() => handleSubscribe(plan.id)}
                                disabled={isCurrentPlan || subscribingPlanId === plan.id}
                            >
                                {subscribingPlanId === plan.id ? (
                                    <>
                                        <div className="spinner" style={{ width: 18, height: 18 }} />
                                        <span>Processing...</span>
                                    </>
                                ) : isCurrentPlan ? (
                                    'Current Plan'
                                ) : currentSubscription ? (
                                    'Switch to This Plan'
                                ) : plan.trialDays > 0 ? (
                                    'Start Free Trial'
                                ) : (
                                    'Get Started'
                                )}
                            </button>
                        </div>
                    );
                })}
            </div>

            {plans.length === 0 && (
                <div className="empty-state">
                    <p>No plans available at the moment.</p>
                </div>
            )}

            {/* Payment Modal */}
            {paymentDetails && (
                <PaymentModal
                    isOpen={showPaymentModal}
                    onClose={handlePaymentClose}
                    onSuccess={handlePaymentSuccess}
                    paymentDetails={paymentDetails}
                />
            )}
        </div>
    );
};

export default PlansPage;

