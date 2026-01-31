import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import './Layout.css';

const Layout: React.FC = () => {
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    return (
        <div className="layout">
            <Sidebar />
            <div className="layout-main">
                <Navbar onMenuClick={toggleSidebar} />
                <main className="layout-content">
                    <Outlet />
                </main>
            </div>
            {sidebarOpen && (
                <div className="sidebar-overlay" onClick={toggleSidebar} />
            )}
        </div>
    );
};

export default Layout;
