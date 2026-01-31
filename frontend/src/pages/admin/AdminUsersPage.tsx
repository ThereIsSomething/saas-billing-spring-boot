import React, { useEffect, useState } from 'react';
import { usersApi } from '../../api';
import { User } from '../../types';
import { Users, Shield, CheckCircle, XCircle, MoreVertical, Search } from 'lucide-react';
import './AdminUsersPage.css';

const AdminUsersPage: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [search, setSearch] = useState('');
    const [actionLoading, setActionLoading] = useState<string | null>(null);

    useEffect(() => {
        fetchUsers();
    }, [page]);

    const fetchUsers = async () => {
        setIsLoading(true);
        try {
            const data = await usersApi.getAllUsers(page, 10);
            setUsers(data.content || []);
            setTotalPages(data.totalPages);
        } catch (error) {
            console.error('Error fetching users:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleToggleActive = async (id: string) => {
        setActionLoading(id);
        try {
            await usersApi.toggleActive(id);
            await fetchUsers();
        } catch (error) {
            console.error('Error toggling user status:', error);
        } finally {
            setActionLoading(null);
        }
    };

    const filteredUsers = users.filter(u =>
        u.fullName.toLowerCase().includes(search.toLowerCase()) ||
        u.email.toLowerCase().includes(search.toLowerCase())
    );

    if (isLoading && users.length === 0) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    return (
        <div className="admin-users-page">
            <div className="page-header">
                <div>
                    <h2>User Management</h2>
                    <p className="text-secondary">Manage platform users and their roles</p>
                </div>
            </div>

            <div className="users-toolbar">
                <div className="search-box">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        placeholder="Search users..."
                        className="search-input"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
                <div className="users-count">
                    <Users size={16} />
                    <span>{users.length} users</span>
                </div>
            </div>

            <div className="table-container card">
                <table className="table">
                    <thead>
                        <tr>
                            <th>User</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Verified</th>
                            <th>Joined</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredUsers.map((user) => (
                            <tr key={user.id}>
                                <td>
                                    <div className="user-cell">
                                        <div className="user-avatar">
                                            {user.fullName?.charAt(0).toUpperCase() || 'U'}
                                        </div>
                                        <span className="user-name">{user.fullName}</span>
                                    </div>
                                </td>
                                <td className="email-cell">{user.email}</td>
                                <td>
                                    <span className={`badge badge-${user.role === 'ADMIN' ? 'primary' : 'info'}`}>
                                        <Shield size={12} />
                                        {user.role}
                                    </span>
                                </td>
                                <td>
                                    <span className={`badge badge-${user.active ? 'success' : 'error'}`}>
                                        {user.active ? <CheckCircle size={12} /> : <XCircle size={12} />}
                                        {user.active ? 'Active' : 'Inactive'}
                                    </span>
                                </td>
                                <td>
                                    {user.emailVerified ? (
                                        <CheckCircle size={18} className="verified-icon" />
                                    ) : (
                                        <XCircle size={18} className="unverified-icon" />
                                    )}
                                </td>
                                <td className="date-cell">
                                    {new Date(user.createdAt).toLocaleDateString()}
                                </td>
                                <td>
                                    <div className="action-buttons">
                                        <button
                                            className={`btn btn-sm ${user.active ? 'btn-danger' : 'btn-success'}`}
                                            onClick={() => handleToggleActive(user.id)}
                                            disabled={actionLoading === user.id}
                                        >
                                            {actionLoading === user.id ? (
                                                <div className="spinner" style={{ width: 14, height: 14 }} />
                                            ) : user.active ? (
                                                'Deactivate'
                                            ) : (
                                                'Activate'
                                            )}
                                        </button>
                                        <button className="btn btn-ghost btn-icon">
                                            <MoreVertical size={16} />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {totalPages > 1 && (
                <div className="pagination">
                    <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => setPage(p => Math.max(0, p - 1))}
                        disabled={page === 0}
                    >
                        Previous
                    </button>
                    <span className="page-info">Page {page + 1} of {totalPages}</span>
                    <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                        disabled={page >= totalPages - 1}
                    >
                        Next
                    </button>
                </div>
            )}
        </div>
    );
};

export default AdminUsersPage;
