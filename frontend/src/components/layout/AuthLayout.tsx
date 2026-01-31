import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { CreditCard } from 'lucide-react';
import './AuthLayout.css';

const AuthLayout: React.FC = () => {
    return (
        <div className="auth-layout">
            <div className="auth-left">
                <div className="auth-branding">
                    <Link to="/" className="auth-logo">
                        <div className="auth-logo-icon">
                            <CreditCard size={32} />
                        </div>
                        <span className="auth-logo-text">SaaSBilling</span>
                    </Link>
                    <h1 className="auth-headline">
                        Manage your subscriptions with ease
                    </h1>
                    <p className="auth-subheadline">
                        Simple, powerful billing platform for modern businesses.
                        Track invoices, manage subscriptions, and analyze your revenue.
                    </p>
                    <div className="auth-features">
                        <div className="auth-feature">
                            <span className="feature-check">✓</span>
                            <span>Automated billing & invoicing</span>
                        </div>
                        <div className="auth-feature">
                            <span className="feature-check">✓</span>
                            <span>Usage-based pricing support</span>
                        </div>
                        <div className="auth-feature">
                            <span className="feature-check">✓</span>
                            <span>Real-time analytics dashboard</span>
                        </div>
                    </div>
                </div>
            </div>
            <div className="auth-right">
                <div className="auth-form-container">
                    <Outlet />
                </div>
            </div>
        </div>
    );
};

export default AuthLayout;
