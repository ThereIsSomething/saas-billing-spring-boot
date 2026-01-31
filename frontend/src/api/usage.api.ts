import api from './client';
import { UsageRecord, UsageRequest } from '../types';

export const usageApi = {
    getMySummary: async (): Promise<Record<string, number>> => {
        const response = await api.get<Record<string, number>>('/usage/my/summary');
        return response.data;
    },

    record: async (data: UsageRequest): Promise<UsageRecord> => {
        const response = await api.post<UsageRecord>('/usage', data);
        return response.data;
    },

    getBySubscription: async (subscriptionId: string): Promise<UsageRecord[]> => {
        const response = await api.get<UsageRecord[]>(`/usage/subscription/${subscriptionId}`);
        return response.data;
    },
};
