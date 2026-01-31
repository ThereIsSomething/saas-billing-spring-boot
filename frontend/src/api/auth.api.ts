import api from './client';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types';

export const authApi = {
    register: async (data: RegisterRequest): Promise<AuthResponse> => {
        const response = await api.post<AuthResponse>('/auth/register', data);
        return response.data;
    },

    login: async (data: LoginRequest): Promise<AuthResponse> => {
        const response = await api.post<AuthResponse>('/auth/login', data);
        return response.data;
    },

    refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
        const response = await api.post<AuthResponse>('/auth/refresh', null, {
            params: { refreshToken },
        });
        return response.data;
    },
};
