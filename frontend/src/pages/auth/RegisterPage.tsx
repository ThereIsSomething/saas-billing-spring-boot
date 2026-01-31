import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts';
import { getErrorMessage } from '../../utils/errorHandler';
import { Mail, Lock, User, Eye, EyeOff, AlertCircle, Check } from 'lucide-react';
import './AuthPages.css';

const RegisterPage: React.FC = () => {
    const [fullName, setFullName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const { register } = useAuth();
    const navigate = useNavigate();

    const passwordRequirements = [
        { label: 'At least 8 characters', met: password.length >= 8 },
        { label: 'Contains a number', met: /\d/.test(password) },
        { label: 'Contains uppercase letter', met: /[A-Z]/.test(password) },
        { label: 'Contains special character', met: /[!@#$%^&*]/.test(password) },
    ];

    const allRequirementsMet = passwordRequirements.every((req) => req.met);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (password !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        if (!allRequirementsMet) {
            setError('Please meet all password requirements');
            return;
        }

        setIsLoading(true);

        try {
            await register({ email, password, fullName });
            navigate('/dashboard');
        } catch (err: unknown) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-header">
                <h2 className="auth-title">Create an account</h2>
                <p className="auth-description">
                    Start your 14-day free trial, no credit card required
                </p>
            </div>

            {error && (
                <div className="alert alert-error">
                    <AlertCircle size={18} />
                    <span>{error}</span>
                </div>
            )}

            <form onSubmit={handleSubmit} className="auth-form">
                <div className="input-group">
                    <label className="input-label">Full Name</label>
                    <div className="input-with-icon">
                        <User size={18} className="input-icon" />
                        <input
                            type="text"
                            className="input"
                            placeholder="John Smith"
                            value={fullName}
                            onChange={(e) => setFullName(e.target.value)}
                            required
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
                            placeholder="name@company.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                </div>

                <div className="input-group">
                    <label className="input-label">Password</label>
                    <div className="input-with-icon">
                        <Lock size={18} className="input-icon" />
                        <input
                            type={showPassword ? 'text' : 'password'}
                            className="input"
                            placeholder="Create a strong password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() => setShowPassword(!showPassword)}
                        >
                            {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                        </button>
                    </div>
                    {password && (
                        <div className="password-requirements">
                            {passwordRequirements.map((req, index) => (
                                <div key={index} className={`requirement ${req.met ? 'met' : ''}`}>
                                    <Check size={14} />
                                    <span>{req.label}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div className="input-group">
                    <label className="input-label">Confirm Password</label>
                    <div className="input-with-icon">
                        <Lock size={18} className="input-icon" />
                        <input
                            type={showPassword ? 'text' : 'password'}
                            className="input"
                            placeholder="Confirm your password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                        />
                    </div>
                </div>

                <label className="checkbox-wrapper mt-md">
                    <input type="checkbox" required />
                    <span className="checkbox-label">
                        I agree to the <Link to="/terms">Terms of Service</Link> and{' '}
                        <Link to="/privacy">Privacy Policy</Link>
                    </span>
                </label>

                <button type="submit" className="btn btn-primary btn-lg w-full" disabled={isLoading}>
                    {isLoading ? (
                        <>
                            <div className="spinner" style={{ width: 18, height: 18 }} />
                            <span>Creating account...</span>
                        </>
                    ) : (
                        'Create Account'
                    )}
                </button>
            </form>

            <div className="auth-footer">
                <p>
                    Already have an account?{' '}
                    <Link to="/login" className="auth-link">Sign in</Link>
                </p>
            </div>
        </div>
    );
};

export default RegisterPage;
