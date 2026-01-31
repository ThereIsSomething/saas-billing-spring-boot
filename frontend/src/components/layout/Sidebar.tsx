import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts';
import {
    Home,
    CreditCard,
    FileText,
    BarChart3,
    Upload,
    User,
    Settings,
    Users,
    Package,
    LogOut,
    ChevronRight,
    Layers,
} from 'lucide-react';
import './Sidebar.css';

interface NavItem {
    label: string;
    path: string;
    icon: React.ReactNode;
}

const userNavItems: NavItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: <Home size={20} /> },
    { label: 'Plans', path: '/plans', icon: <Package size={20} /> },
    { label: 'Subscriptions', path: '/subscriptions', icon: <Layers size={20} /> },
    { label: 'Invoices', path: '/invoices', icon: <FileText size={20} /> },
    { label: 'Payments', path: '/payments', icon: <CreditCard size={20} /> },
    { label: 'Usage', path: '/usage', icon: <BarChart3 size={20} /> },
    { label: 'Files', path: '/files', icon: <Upload size={20} /> },
    { label: 'Profile', path: '/profile', icon: <User size={20} /> },
];

const adminNavItems: NavItem[] = [
    { label: 'Admin Dashboard', path: '/admin', icon: <Settings size={20} /> },
    { label: 'Manage Users', path: '/admin/users', icon: <Users size={20} /> },
    { label: 'Manage Plans', path: '/admin/plans', icon: <Package size={20} /> },
    { label: 'Manage Invoices', path: '/admin/invoices', icon: <FileText size={20} /> },
];

const Sidebar: React.FC = () => {
    const { user, logout, isAdmin } = useAuth();
    const location = useLocation();

    const isActive = (path: string) => location.pathname === path;

    return (
        <aside className="sidebar">
            <div className="sidebar-header">
                <div className="sidebar-logo">
                    <div className="logo-icon">
                        <CreditCard size={24} />
                    </div>
                    <span className="logo-text">SaaSBilling</span>
                </div>
            </div>

            <nav className="sidebar-nav">
                <div className="nav-section">
                    <span className="nav-section-title">Main Menu</span>
                    <ul className="nav-list">
                        {userNavItems.map((item) => (
                            <li key={item.path}>
                                <Link
                                    to={item.path}
                                    className={`nav-item ${isActive(item.path) ? 'active' : ''}`}
                                >
                                    <span className="nav-icon">{item.icon}</span>
                                    <span className="nav-label">{item.label}</span>
                                    {isActive(item.path) && <ChevronRight size={16} className="nav-indicator" />}
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>

                {isAdmin && (
                    <div className="nav-section">
                        <span className="nav-section-title">Administration</span>
                        <ul className="nav-list">
                            {adminNavItems.map((item) => (
                                <li key={item.path}>
                                    <Link
                                        to={item.path}
                                        className={`nav-item ${isActive(item.path) ? 'active' : ''}`}
                                    >
                                        <span className="nav-icon">{item.icon}</span>
                                        <span className="nav-label">{item.label}</span>
                                        {isActive(item.path) && <ChevronRight size={16} className="nav-indicator" />}
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </nav>

            <div className="sidebar-footer">
                <div className="user-info">
                    <div className="user-avatar">
                        {user?.fullName?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <div className="user-details">
                        <span className="user-name">{user?.fullName || 'User'}</span>
                        <span className="user-role">{user?.role || 'User'}</span>
                    </div>
                </div>
                <button className="logout-btn" onClick={logout} title="Logout">
                    <LogOut size={20} />
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
