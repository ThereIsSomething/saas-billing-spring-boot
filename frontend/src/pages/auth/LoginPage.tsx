import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts';
import { getErrorMessage } from '../../utils/errorHandler';
import { Mail, Lock, Eye, EyeOff, AlertCircle } from 'lucide-react';
import './AuthPages.css';

const LoginPage: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            await login({ email, password });
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
                <h2 className="auth-title">Welcome back</h2>
                <p className="auth-description">
                    Enter your credentials to access your account
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
                            placeholder="••••••••"
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
                </div>

                <div className="auth-options">
                    <label className="checkbox-wrapper">
                        <input type="checkbox" />
                        <span className="checkbox-label">Remember me</span>
                    </label>
                    <Link to="/forgot-password" className="forgot-link">
                        Forgot password?
                    </Link>
                </div>

                <button type="submit" className="btn btn-primary btn-lg w-full" disabled={isLoading}>
                    {isLoading ? (
                        <>
                            <div className="spinner" style={{ width: 18, height: 18 }} />
                            <span>Signing in...</span>
                        </>
                    ) : (
                        'Sign In'
                    )}
                </button>
            </form>

            <div className="auth-footer">
                <p>
                    Don't have an account?{' '}
                    <Link to="/register" className="auth-link">Create account</Link>
                </p>
            </div>
        </div>
    );
};

export default LoginPage;
