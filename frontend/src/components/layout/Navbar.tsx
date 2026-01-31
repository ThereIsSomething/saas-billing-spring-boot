import React from 'react';
import { useLocation } from 'react-router-dom';
import { Bell, Search, Menu } from 'lucide-react';
import { useAuth } from '../../contexts';
import './Navbar.css';

const pageTitles: Record<string, string> = {
    '/dashboard': 'Dashboard',
    '/plans': 'Plans',
    '/subscriptions': 'My Subscriptions',
    '/invoices': 'Invoices',
    '/payments': 'Payments',
    '/usage': 'Usage Metrics',
    '/files': 'Files',
    '/profile': 'Profile Settings',
    '/admin': 'Admin Dashboard',
    '/admin/users': 'User Management',
    '/admin/plans': 'Plan Management',
};

interface NavbarProps {
    onMenuClick?: () => void;
}

const Navbar: React.FC<NavbarProps> = ({ onMenuClick }) => {
    const location = useLocation();
    const { user } = useAuth();

    const pageTitle = pageTitles[location.pathname] || 'Dashboard';

    return (
        <header className="navbar">
            <div className="navbar-left">
                <button className="menu-btn" onClick={onMenuClick}>
                    <Menu size={24} />
                </button>
                <div className="page-info">
                    <h1 className="page-title">{pageTitle}</h1>
                    <p className="page-subtitle">Welcome back, {user?.fullName?.split(' ')[0] || 'User'}!</p>
                </div>
            </div>

            <div className="navbar-right">
                <div className="search-box">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        placeholder="Search..."
                        className="search-input"
                    />
                </div>

                <button className="icon-btn notification-btn">
                    <Bell size={20} />
                    <span className="notification-badge">3</span>
                </button>

                <div className="navbar-avatar">
                    {user?.fullName?.charAt(0).toUpperCase() || 'U'}
                </div>
            </div>
        </header>
    );
};

export default Navbar;
