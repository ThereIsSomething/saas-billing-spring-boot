import api from './client';
import { Subscription, SubscriptionRequest } from '../types';

export const subscriptionsApi = {
    getMine: async (): Promise<Subscription[]> => {
        const response = await api.get<Subscription[]>('/subscriptions/my');
        return response.data;
    },

    getActive: async (): Promise<Subscription | null> => {
        const response = await api.get<Subscription>('/subscriptions/my/active');
        return response.data;
    },

    subscribe: async (data: SubscriptionRequest): Promise<Subscription> => {
        const response = await api.post<Subscription>('/subscriptions', data);
        return response.data;
    },

    cancel: async (id: string, reason?: string): Promise<Subscription> => {
        const response = await api.post<Subscription>(`/subscriptions/${id}/cancel`, null, {
            params: { reason },
        });
        return response.data;
    },

    changePlan: async (id: string, newPlanId: string): Promise<Subscription> => {
        const response = await api.post<Subscription>(`/subscriptions/${id}/change-plan`, null, {
            params: { newPlanId },
        });
        return response.data;
    },

    renew: async (id: string): Promise<Subscription> => {
        const response = await api.post<Subscription>(`/subscriptions/${id}/renew`);
        return response.data;
    },

    toggleAutoRenew: async (id: string, autoRenew: boolean): Promise<Subscription> => {
        const response = await api.post<Subscription>(`/subscriptions/${id}/auto-renew`, null, {
            params: { autoRenew },
        });
        return response.data;
    },

    // Admin endpoints
    getAll: async (page = 0, size = 20): Promise<{ content: Subscription[]; totalPages: number }> => {
        const response = await api.get('/subscriptions', { params: { page, size } });
        return response.data;
    },

    getById: async (id: string): Promise<Subscription> => {
        const response = await api.get<Subscription>(`/subscriptions/${id}`);
        return response.data;
    },
};
