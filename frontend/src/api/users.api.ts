import api from './client';
import { User } from '../types';

export const usersApi = {
    getMe: async (): Promise<User> => {
        const response = await api.get<User>('/users/me');
        return response.data;
    },

    updateMe: async (data: Partial<User>): Promise<User> => {
        const response = await api.put<User>('/users/me', data);
        return response.data;
    },

    // Admin endpoints
    getAllUsers: async (page = 0, size = 20): Promise<{ content: User[]; totalPages: number; totalElements: number }> => {
        const response = await api.get('/users', { params: { page, size } });
        return response.data;
    },

    getUserById: async (id: string): Promise<User> => {
        const response = await api.get<User>(`/users/${id}`);
        return response.data;
    },

    updateUser: async (id: string, data: Partial<User>): Promise<User> => {
        const response = await api.patch<User>(`/users/${id}`, data);
        return response.data;
    },

    deleteUser: async (id: string): Promise<void> => {
        await api.delete(`/users/${id}`);
    },

    toggleActive: async (id: string): Promise<User> => {
        const response = await api.post<User>(`/users/${id}/toggle-active`);
        return response.data;
    },
};
