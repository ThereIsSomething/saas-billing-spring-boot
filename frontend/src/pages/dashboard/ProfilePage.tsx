import React, { useState } from 'react';
import { useAuth } from '../../contexts';
import { usersApi } from '../../api';
import { User, Mail, Phone, Building2, Shield, Save, AlertCircle, CheckCircle } from 'lucide-react';
import './ProfilePage.css';

const ProfilePage: React.FC = () => {
    const { user, updateUser } = useAuth();
    const [formData, setFormData] = useState({
        fullName: user?.fullName || '',
        phone: user?.phone || '',
        company: user?.company || '',
    });
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setMessage(null);

        try {
            const updated = await usersApi.updateMe(formData);
            updateUser(updated);
            setMessage({ type: 'success', text: 'Profile updated successfully!' });
        } catch (error) {
            console.error('Error updating profile:', error);
            setMessage({ type: 'error', text: 'Failed to update profile. Please try again.' });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="profile-page">
            <div className="page-header">
                <h2>Profile Settings</h2>
                <p className="text-secondary">Manage your account information</p>
            </div>

            <div className="profile-grid">
                {/* Profile Card */}
                <div className="card profile-card">
                    <div className="profile-avatar">
                        {user?.fullName?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <h3 className="profile-name">{user?.fullName}</h3>
                    <p className="profile-email">{user?.email}</p>
                    <div className="profile-badges">
                        <span className={`badge badge-${user?.role === 'ADMIN' ? 'primary' : 'info'}`}>
                            <Shield size={12} />
                            {user?.role}
                        </span>
                        {user?.emailVerified && (
                            <span className="badge badge-success">
                                <CheckCircle size={12} />
                                Verified
                            </span>
                        )}
                    </div>
                </div>

                {/* Edit Form */}
                <div className="card form-card">
                    <h3 className="form-title">Edit Profile</h3>

                    {message && (
                        <div className={`alert alert-${message.type}`}>
                            {message.type === 'success' ? <CheckCircle size={18} /> : <AlertCircle size={18} />}
                            {message.text}
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="input-group">
                            <label className="input-label">Full Name</label>
                            <div className="input-with-icon">
                                <User size={18} className="input-icon" />
                                <input
                                    type="text"
                                    name="fullName"
                                    className="input"
                                    value={formData.fullName}
                                    onChange={handleChange}
                                    placeholder="Your full name"
                                />
                            </div>
                        </div>

                        <div className="input-group">
                            <label className="input-label">Email Address</label>
                            <div className="input-with-icon">
                                <Mail size={18} className="input-icon" />
                                <input
                                    type="email"
                                    className="input"
                                    value={user?.email || ''}
                                    disabled
                                    placeholder="Email cannot be changed"
                                />
                            </div>
                            <span className="input-hint">Email cannot be changed</span>
                        </div>

                        <div className="input-group">
                            <label className="input-label">Phone Number</label>
                            <div className="input-with-icon">
                                <Phone size={18} className="input-icon" />
                                <input
                                    type="tel"
                                    name="phone"
                                    className="input"
                                    value={formData.phone}
                                    onChange={handleChange}
                                    placeholder="+1 (555) 000-0000"
                                />
                            </div>
                        </div>

                        <div className="input-group">
                            <label className="input-label">Company</label>
                            <div className="input-with-icon">
                                <Building2 size={18} className="input-icon" />
                                <input
                                    type="text"
                                    name="company"
                                    className="input"
                                    value={formData.company}
                                    onChange={handleChange}
                                    placeholder="Your company name"
                                />
                            </div>
                        </div>

                        <button type="submit" className="btn btn-primary w-full" disabled={isLoading}>
                            {isLoading ? (
                                <>
                                    <div className="spinner" style={{ width: 16, height: 16 }} />
                                    Saving...
                                </>
                            ) : (
                                <>
                                    <Save size={18} />
                                    Save Changes
                                </>
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default ProfilePage;
