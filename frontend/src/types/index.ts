// User types
export interface User {
    id: string;
    email: string;
    fullName: string;
    phone?: string;
    company?: string;
    role: 'USER' | 'ADMIN';
    active: boolean;
    emailVerified: boolean;
    profileImageUrl?: string;
    createdAt: string;
    lastLoginAt?: string;
}

// Auth types
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: User;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    fullName: string;
    phone?: string;
    company?: string;
}

// Plan types
export interface Plan {
    id: string;
    name: string;
    description?: string;
    price: number;
    currency: string;
    billingCycle: 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
    usageLimit?: number;
    apiCallsLimit?: number;
    storageLimitMb?: number;
    usersLimit?: number;
    active: boolean;
    isFeatured: boolean;
    trialDays: number;
    features: string[];
    sortOrder: number;
    createdAt: string;
}

// Subscription types
export interface Subscription {
    id: string;
    userId: string;
    plan: Plan;
    status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED' | 'TRIAL' | 'PENDING';
    startDate: string;
    endDate?: string;
    nextBillingDate?: string;
    autoRenew: boolean;
    cancelledAt?: string;
    cancellationReason?: string;
    createdAt: string;
}

export interface SubscriptionRequest {
    planId: string;
    paymentOrderId?: string;
}

// Invoice types
export interface Invoice {
    id: string;
    invoiceNumber: string;
    userId: string;
    subscriptionId?: string;
    amount: number;
    taxAmount: number;
    totalAmount: number;
    currency: string;
    status: 'PENDING' | 'PAID' | 'CANCELLED' | 'OVERDUE';
    invoiceDate: string;
    dueDate: string;
    paidAt?: string;
    description?: string;
    createdAt: string;
}

// Payment types
export interface Payment {
    id: string;
    userId: string;
    invoiceId?: string;
    amount: number;
    currency: string;
    status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
    paymentMethod?: string;
    transactionId?: string;
    ipAddress?: string;
    refundedAt?: string;
    refundReason?: string;
    createdAt: string;
}

export interface PaymentRequest {
    invoiceId: string;
    amount: number;
    paymentMethod?: string;
    currency?: string;
}

// Usage types
export interface UsageRecord {
    id: string;
    userId: string;
    subscriptionId: string;
    metricName: string;
    quantity: number;
    recordedAt: string;
}

export interface UsageRequest {
    subscriptionId: string;
    metricName: string;
    quantity: number;
}

// File types
export interface UploadedFile {
    id: string;
    userId: string;
    originalFileName: string;
    storedFileName: string;
    contentType: string;
    fileSize: number;
    filePath: string;
    createdAt: string;
}

// Analytics types
export interface MonthlyRevenue {
    year: number;
    month: number;
    revenue: number;
    invoiceCount: number;
    currency: string;
}

export interface SubscriptionStats {
    total: number;
    active: number;
    cancelled: number;
    expired: number;
    trial: number;
    churnRate: number;
}

export interface PlanPopularity {
    planId: string;
    planName: string;
    subscriptionCount: number;
    percentage: number;
}

export interface DashboardSummary {
    monthlyRecurringRevenue: number;
    activeSubscriptions: number;
    churnRate: number;
    averageRevenuePerUser: number;
    subscriptionsByStatus: Record<string, number>;
    topPlans: PlanPopularity[];
}

// API Response types
export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}

export interface ApiError {
    status: number;
    message: string;
    timestamp: string;
    path?: string;
}
