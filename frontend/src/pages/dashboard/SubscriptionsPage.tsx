import React, { useEffect, useState } from 'react';
import { subscriptionsApi } from '../../api';
import { Subscription } from '../../types';
import { Package, Calendar, RefreshCw, XCircle, Check, AlertCircle } from 'lucide-react';
import './SubscriptionsPage.css';

const SubscriptionsPage: React.FC = () => {
    const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState<string | null>(null);

    useEffect(() => {
        fetchSubscriptions();
    }, []);

    const fetchSubscriptions = async () => {
        try {
            const data = await subscriptionsApi.getMine();
            setSubscriptions(data);
        } catch (error) {
            console.error('Error fetching subscriptions:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleCancel = async (id: string) => {
        if (!confirm('Are you sure you want to cancel this subscription?')) return;

        setActionLoading(id);
        try {
            await subscriptionsApi.cancel(id, 'User requested cancellation');
            await fetchSubscriptions();
        } catch (error) {
            console.error('Error cancelling subscription:', error);
        } finally {
            setActionLoading(null);
        }
    };

    const handleToggleAutoRenew = async (id: string, currentValue: boolean) => {
        setActionLoading(id);
        try {
            await subscriptionsApi.toggleAutoRenew(id, !currentValue);
            await fetchSubscriptions();
        } catch (error) {
            console.error('Error toggling auto-renew:', error);
        } finally {
            setActionLoading(null);
        }
    };

    const getStatusBadge = (status: string) => {
        const statusMap: Record<string, string> = {
            ACTIVE: 'success',
            CANCELLED: 'error',
            EXPIRED: 'warning',
            TRIAL: 'info',
            PENDING: 'warning',
        };
        return statusMap[status] || 'info';
    };

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    return (
        <div className="subscriptions-page">
            <div className="page-header">
                <h2>My Subscriptions</h2>
                <p className="text-secondary">Manage your active and past subscriptions</p>
            </div>

            {subscriptions.length > 0 ? (
                <div className="subscriptions-list">
                    {subscriptions.map((sub) => (
                        <div key={sub.id} className={`subscription-card ${sub.status === 'ACTIVE' ? 'active' : ''}`}>
                            <div className="sub-header">
                                <div className="sub-plan">
                                    <Package size={24} />
                                    <div>
                                        <h3>{sub.plan?.name || 'Unknown Plan'}</h3>
                                        <span className={`badge badge-${getStatusBadge(sub.status)}`}>
                                            {sub.status}
                                        </span>
                                    </div>
                                </div>
                                <div className="sub-price">
                                    <span className="price">${sub.plan?.price?.toFixed(2) || '0.00'}</span>
                                    <span className="period">/{sub.plan?.billingCycle?.toLowerCase()}</span>
                                </div>
                            </div>

                            <div className="sub-details">
                                <div className="detail">
                                    <Calendar size={16} />
                                    <span>Started: {new Date(sub.startDate).toLocaleDateString()}</span>
                                </div>
                                {sub.nextBillingDate && (
                                    <div className="detail">
                                        <RefreshCw size={16} />
                                        <span>Next billing: {new Date(sub.nextBillingDate).toLocaleDateString()}</span>
                                    </div>
                                )}
                                {sub.endDate && (
                                    <div className="detail">
                                        <AlertCircle size={16} />
                                        <span>Ends: {new Date(sub.endDate).toLocaleDateString()}</span>
                                    </div>
                                )}
                            </div>

                            {sub.status === 'ACTIVE' && (
                                <div className="sub-actions">
                                    <label className="toggle-wrapper">
                                        <input
                                            type="checkbox"
                                            checked={sub.autoRenew}
                                            onChange={() => handleToggleAutoRenew(sub.id, sub.autoRenew)}
                                            disabled={actionLoading === sub.id}
                                        />
                                        <span className="toggle-label">
                                            {sub.autoRenew ? <Check size={14} /> : null}
                                            Auto-renew
                                        </span>
                                    </label>
                                    <button
                                        className="btn btn-danger btn-sm"
                                        onClick={() => handleCancel(sub.id)}
                                        disabled={actionLoading === sub.id}
                                    >
                                        {actionLoading === sub.id ? (
                                            <div className="spinner" style={{ width: 14, height: 14 }} />
                                        ) : (
                                            <>
                                                <XCircle size={14} />
                                                Cancel
                                            </>
                                        )}
                                    </button>
                                </div>
                            )}

                            {sub.cancellationReason && (
                                <div className="cancellation-reason">
                                    <AlertCircle size={14} />
                                    <span>Cancelled: {sub.cancellationReason}</span>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            ) : (
                <div className="empty-state card">
                    <Package size={48} className="empty-icon" />
                    <h3>No Subscriptions</h3>
                    <p>You don't have any subscriptions yet.</p>
                    <a href="/plans" className="btn btn-primary">Browse Plans</a>
                </div>
            )}
        </div>
    );
};

export default SubscriptionsPage;
