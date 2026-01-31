import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './contexts';

// Layout
import Layout from './components/layout/Layout';
import AuthLayout from './components/layout/AuthLayout';

// Auth Pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

// Dashboard Pages
import DashboardPage from './pages/dashboard/DashboardPage';
import PlansPage from './pages/dashboard/PlansPage';
import SubscriptionsPage from './pages/dashboard/SubscriptionsPage';
import InvoicesPage from './pages/dashboard/InvoicesPage';
import PaymentsPage from './pages/dashboard/PaymentsPage';
import UsagePage from './pages/dashboard/UsagePage';
import FilesPage from './pages/dashboard/FilesPage';
import ProfilePage from './pages/dashboard/ProfilePage';

// Admin Pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminPlansPage from './pages/admin/AdminPlansPage';
import AdminInvoicesPage from './pages/admin/AdminInvoicesPage';

// Protected Route Component
const ProtectedRoute: React.FC<{ children: React.ReactNode; adminOnly?: boolean }> = ({
    children,
    adminOnly = false
}) => {
    const { isAuthenticated, isAdmin, isLoading } = useAuth();

    if (isLoading) {
        return (
            <div className="loading-overlay">
                <div className="spinner" />
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (adminOnly && !isAdmin) {
        return <Navigate to="/dashboard" replace />;
    }

    return <>{children}</>;
};

const App: React.FC = () => {
    const { isAuthenticated } = useAuth();

    return (
        <Routes>
            {/* Public Routes */}
            <Route path="/" element={isAuthenticated ? <Navigate to="/dashboard" /> : <Navigate to="/login" />} />

            {/* Auth Routes */}
            <Route element={<AuthLayout />}>
                <Route path="/login" element={isAuthenticated ? <Navigate to="/dashboard" /> : <LoginPage />} />
                <Route path="/register" element={isAuthenticated ? <Navigate to="/dashboard" /> : <RegisterPage />} />
            </Route>

            {/* Protected User Routes */}
            <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/plans" element={<PlansPage />} />
                <Route path="/subscriptions" element={<SubscriptionsPage />} />
                <Route path="/invoices" element={<InvoicesPage />} />
                <Route path="/payments" element={<PaymentsPage />} />
                <Route path="/usage" element={<UsagePage />} />
                <Route path="/files" element={<FilesPage />} />
                <Route path="/profile" element={<ProfilePage />} />
            </Route>

            {/* Protected Admin Routes */}
            <Route element={<ProtectedRoute adminOnly><Layout /></ProtectedRoute>}>
                <Route path="/admin" element={<AdminDashboardPage />} />
                <Route path="/admin/users" element={<AdminUsersPage />} />
                <Route path="/admin/plans" element={<AdminPlansPage />} />
                <Route path="/admin/invoices" element={<AdminInvoicesPage />} />
            </Route>

            {/* Catch all */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
};

export default App;
