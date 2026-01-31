import api from './client';
import { Payment, PaymentRequest } from '../types';

// Types for mock Razorpay payment flow
export interface PaymentInitiateResponse {
    orderId: string;
    amount: number;
    currency: string;
    planName: string;
    keyId: string;
    requiresPayment: boolean;
    message: string;
}

export interface PaymentVerifyRequest {
    orderId: string;
    paymentId: string;
    signature: string;
}

export interface PaymentOrderResponse {
    id: string;
    orderId: string;
    paymentId: string;
    amount: number;
    currency: string;
    status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED' | 'CANCELLED';
    planId: string;
    planName: string;
    verified: boolean;
    message: string;
    createdAt: string;
}

export const paymentsApi = {
    // Mock Razorpay payment flow
    initiate: async (planId: string): Promise<PaymentInitiateResponse> => {
        const response = await api.post<PaymentInitiateResponse>('/payments/initiate', { planId });
        return response.data;
    },

    verify: async (data: PaymentVerifyRequest): Promise<PaymentOrderResponse> => {
        const response = await api.post<PaymentOrderResponse>('/payments/verify', data);
        return response.data;
    },

    getOrderStatus: async (orderId: string): Promise<PaymentOrderResponse> => {
        const response = await api.get<PaymentOrderResponse>(`/payments/order/${orderId}/status`);
        return response.data;
    },

    // Original payment endpoints
    getMine: async (page = 0, size = 20): Promise<{ content: Payment[]; totalPages: number }> => {
        const response = await api.get('/payments/my', { params: { page, size } });
        return response.data;
    },

    process: async (data: PaymentRequest): Promise<Payment> => {
        const response = await api.post<Payment>('/payments', data);
        return response.data;
    },

    // Admin endpoints
    getAll: async (page = 0, size = 20): Promise<{ content: Payment[]; totalPages: number }> => {
        const response = await api.get('/payments', { params: { page, size } });
        return response.data;
    },

    getById: async (id: string): Promise<Payment> => {
        const response = await api.get<Payment>(`/payments/${id}`);
        return response.data;
    },

    refund: async (id: string, reason?: string): Promise<Payment> => {
        const response = await api.post<Payment>(`/payments/${id}/refund`, null, {
            params: { reason },
        });
        return response.data;
    },
};

