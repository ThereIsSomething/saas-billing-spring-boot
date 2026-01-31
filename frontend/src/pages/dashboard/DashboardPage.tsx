import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { subscriptionsApi, invoicesApi, usageApi } from '../../api';
import { Subscription, Invoice } from '../../types';
import {
    TrendingUp,
    CreditCard,
    FileText,
    BarChart3,
    ArrowRight,
    Package,
    Calendar,
    DollarSign
} from 'lucide-react';
import './DashboardPage.css';

const DashboardPage: React.FC = () => {
    const [subscription, setSubscription] = useState<Subscription | null>(null);
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [usage, setUsage] = useState<Record<string, number>>({});
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [subData, invData, usageData] = await Promise.all([
                    subscriptionsApi.getActive().catch(() => null),
                    invoicesApi.getMine(0, 5).catch(() => ({ content: [] })),
                    usageApi.getMySummary().catch(() => ({})),
                ]);

                setSubscription(subData);
                setInvoices(invData.content || []);
                setUsage(usageData);
            } catch (error) {
                console.error('Error fetching dashboard data:', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, []);

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    const stats = [
        {
            label: 'Current Plan',
            value: subscription?.plan?.name || 'No Plan',
            icon: <Package size={24} />,
            color: 'primary',
            link: '/subscriptions',
        },
        {
            label: 'Next Billing',
            value: subscription?.nextBillingDate
                ? new Date(subscription.nextBillingDate).toLocaleDateString()
                : 'N/A',
            icon: <Calendar size={24} />,
            color: 'info',
            link: '/invoices',
        },
        {
            label: 'Pending Invoices',
            value: invoices.filter(i => i.status === 'PENDING').length.toString(),
            icon: <FileText size={24} />,
            color: 'warning',
            link: '/invoices',
        },
        {
            label: 'API Calls Used',
            value: (usage['api_calls'] || 0).toLocaleString(),
            icon: <BarChart3 size={24} />,
            color: 'success',
            link: '/usage',
        },
    ];

    return (
        <div className="dashboard-page">
            {/* Stats Grid */}
            <div className="stats-grid">
                {stats.map((stat, index) => (
                    <Link to={stat.link} key={index} className={`stat-card stat-${stat.color}`}>
                        <div className="stat-icon">{stat.icon}</div>
                        <div className="stat-content">
                            <span className="stat-label">{stat.label}</span>
                            <span className="stat-value">{stat.value}</span>
                        </div>
                        <ArrowRight size={16} className="stat-arrow" />
                    </Link>
                ))}
            </div>

            {/* Main Content */}
            <div className="dashboard-grid">
                {/* Current Subscription */}
                <div className="card dashboard-card">
                    <div className="card-header">
                        <h3 className="card-title">Current Subscription</h3>
                        <Link to="/plans" className="btn btn-secondary btn-sm">
                            Upgrade Plan
                        </Link>
                    </div>
                    {subscription ? (
                        <div className="subscription-info">
                            <div className="plan-badge">
                                <TrendingUp size={20} />
                                <span>{subscription.plan?.name}</span>
                            </div>
                            <div className="subscription-details">
                                <div className="detail-item">
                                    <span className="detail-label">Status</span>
                                    <span className={`badge badge-${subscription.status === 'ACTIVE' ? 'success' : 'warning'}`}>
                                        {subscription.status}
                                    </span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Price</span>
                                    <span className="detail-value">
                                        ${subscription.plan?.price?.toFixed(2) || '0.00'}/{subscription.plan?.billingCycle?.toLowerCase()}
                                    </span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Next Billing</span>
                                    <span className="detail-value">
                                        {subscription.nextBillingDate
                                            ? new Date(subscription.nextBillingDate).toLocaleDateString()
                                            : 'N/A'}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="empty-state">
                            <Package size={48} className="empty-icon" />
                            <p>No active subscription</p>
                            <Link to="/plans" className="btn btn-primary">
                                Browse Plans
                            </Link>
                        </div>
                    )}
                </div>

                {/* Recent Invoices */}
                <div className="card dashboard-card">
                    <div className="card-header">
                        <h3 className="card-title">Recent Invoices</h3>
                        <Link to="/invoices" className="btn btn-ghost btn-sm">
                            View All
                        </Link>
                    </div>
                    {invoices.length > 0 ? (
                        <div className="invoice-list">
                            {invoices.slice(0, 4).map((invoice) => (
                                <div key={invoice.id} className="invoice-item">
                                    <div className="invoice-info">
                                        <span className="invoice-number">{invoice.invoiceNumber}</span>
                                        <span className="invoice-date">
                                            {new Date(invoice.invoiceDate).toLocaleDateString()}
                                        </span>
                                    </div>
                                    <div className="invoice-right">
                                        <span className="invoice-amount">
                                            <DollarSign size={14} />
                                            {invoice.totalAmount?.toFixed(2)}
                                        </span>
                                        <span className={`badge badge-${invoice.status === 'PAID' ? 'success' :
                                                invoice.status === 'PENDING' ? 'warning' : 'error'
                                            }`}>
                                            {invoice.status}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">
                            <FileText size={48} className="empty-icon" />
                            <p>No invoices yet</p>
                        </div>
                    )}
                </div>

                {/* Quick Actions */}
                <div className="card dashboard-card quick-actions-card">
                    <div className="card-header">
                        <h3 className="card-title">Quick Actions</h3>
                    </div>
                    <div className="quick-actions">
                        <Link to="/plans" className="quick-action">
                            <div className="action-icon">
                                <Package size={20} />
                            </div>
                            <span>Browse Plans</span>
                        </Link>
                        <Link to="/payments" className="quick-action">
                            <div className="action-icon">
                                <CreditCard size={20} />
                            </div>
                            <span>Make Payment</span>
                        </Link>
                        <Link to="/usage" className="quick-action">
                            <div className="action-icon">
                                <BarChart3 size={20} />
                            </div>
                            <span>View Usage</span>
                        </Link>
                        <Link to="/files" className="quick-action">
                            <div className="action-icon">
                                <FileText size={20} />
                            </div>
                            <span>Manage Files</span>
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;
