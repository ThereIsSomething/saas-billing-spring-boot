import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ApiError } from '../types';

// Create axios instance
const api = axios.create({
    baseURL: '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor - add auth token
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem('accessToken');
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Auth endpoints that should NOT trigger token refresh/redirect
const AUTH_ENDPOINTS = ['/auth/login', '/auth/register', '/auth/refresh'];

// Response interceptor - handle errors and token refresh
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<ApiError>) => {
        const originalRequest = error.config;
        const requestUrl = originalRequest?.url || '';

        // Skip token refresh logic for auth endpoints
        const isAuthEndpoint = AUTH_ENDPOINTS.some(endpoint => requestUrl.includes(endpoint));

        if (isAuthEndpoint) {
            // For auth endpoints, just pass through the error
            return Promise.reject(error);
        }

        // Handle 401 - try to refresh token (only for non-auth endpoints)
        if (error.response?.status === 401 && originalRequest) {
            const refreshToken = localStorage.getItem('refreshToken');

            if (refreshToken) {
                try {
                    const response = await axios.post('/api/auth/refresh', null, {
                        params: { refreshToken },
                    });

                    const { accessToken, refreshToken: newRefreshToken } = response.data;
                    localStorage.setItem('accessToken', accessToken);
                    localStorage.setItem('refreshToken', newRefreshToken);

                    // Retry original request
                    if (originalRequest.headers) {
                        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                    }
                    return api(originalRequest);
                } catch {
                    // Refresh failed - clear tokens and redirect to login
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('refreshToken');
                    localStorage.removeItem('user');
                    window.location.href = '/login';
                }
            } else {
                // No refresh token - redirect to login
                localStorage.removeItem('accessToken');
                localStorage.removeItem('user');
                window.location.href = '/login';
            }
        }

        return Promise.reject(error);
    }
);

export default api;

