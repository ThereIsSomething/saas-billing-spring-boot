import api from './client';
import { MonthlyRevenue, SubscriptionStats, PlanPopularity, DashboardSummary } from '../types';

export const analyticsApi = {
    getDashboard: async (): Promise<DashboardSummary> => {
        const response = await api.get<DashboardSummary>('/analytics/dashboard');
        return response.data;
    },

    getMonthlyRevenue: async (months = 12): Promise<MonthlyRevenue[]> => {
        const response = await api.get<MonthlyRevenue[]>('/analytics/monthly-revenue', {
            params: { months },
        });
        return response.data;
    },

    getSubscriptionStats: async (): Promise<SubscriptionStats> => {
        const response = await api.get<SubscriptionStats>('/analytics/subscription-stats');
        return response.data;
    },

    getPlanPopularity: async (): Promise<PlanPopularity[]> => {
        const response = await api.get<PlanPopularity[]>('/analytics/plan-popularity');
        return response.data;
    },
};
