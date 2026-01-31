import api from './client';
import { Invoice } from '../types';

export const invoicesApi = {
    getMine: async (): Promise<Invoice[]> => {
        const response = await api.get<Invoice[]>('/invoices/my');
        return response.data;
    },

    getById: async (id: string): Promise<Invoice> => {
        const response = await api.get<Invoice>(`/invoices/${id}`);
        return response.data;
    },

    // Admin endpoints
    getAll: async (page = 0, size = 20): Promise<{ content: Invoice[]; totalPages: number }> => {
        const response = await api.get('/invoices', { params: { page, size } });
        return response.data;
    },

    markAsPaid: async (id: string): Promise<Invoice> => {
        const response = await api.post<Invoice>(`/invoices/${id}/mark-paid`);
        return response.data;
    },

    cancel: async (id: string): Promise<Invoice> => {
        const response = await api.post<Invoice>(`/invoices/${id}/cancel`);
        return response.data;
    },

    generate: async (subscriptionId: string): Promise<Invoice> => {
        const response = await api.post<Invoice>('/invoices/generate', null, {
            params: { subscriptionId },
        });
        return response.data;
    },
};

