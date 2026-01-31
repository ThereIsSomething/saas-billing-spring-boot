import api from './client';
import { Plan } from '../types';

export const plansApi = {
    getAll: async (): Promise<Plan[]> => {
        const response = await api.get<Plan[]>('/plans');
        return response.data;
    },

    getFeatured: async (): Promise<Plan[]> => {
        const response = await api.get<Plan[]>('/plans/featured');
        return response.data;
    },

    getById: async (id: string): Promise<Plan> => {
        const response = await api.get<Plan>(`/plans/${id}`);
        return response.data;
    },

    // Admin endpoints
    create: async (data: Omit<Plan, 'id' | 'createdAt'>): Promise<Plan> => {
        const response = await api.post<Plan>('/plans', data);
        return response.data;
    },

    update: async (id: string, data: Partial<Plan>): Promise<Plan> => {
        const response = await api.put<Plan>(`/plans/${id}`, data);
        return response.data;
    },

    delete: async (id: string): Promise<void> => {
        await api.delete(`/plans/${id}`);
    },

    toggleActive: async (id: string): Promise<Plan> => {
        const response = await api.patch<Plan>(`/plans/${id}/toggle-active`);
        return response.data;
    },
};
