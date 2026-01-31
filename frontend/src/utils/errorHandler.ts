import { AxiosError } from 'axios';

/**
 * Backend error response structure
 */
export interface BackendError {
    timestamp?: string;
    status?: number;
    error?: string;
    code?: string;
    message?: string;
    path?: string;
    validationErrors?: Array<{
        field: string;
        message: string;
        rejectedValue?: unknown;
    }>;
}

/**
 * Extracts a user-friendly error message from an Axios error.
 * Handles various backend error response formats.
 */
export function getErrorMessage(error: unknown): string {
    if (!error) {
        return 'An unexpected error occurred';
    }

    // Handle Axios errors
    if (isAxiosError(error)) {
        const axiosError = error as AxiosError<BackendError>;

        // Get error data from response
        const data = axiosError.response?.data;

        if (data) {
            // Handle validation errors
            if (data.validationErrors && data.validationErrors.length > 0) {
                const messages = data.validationErrors.map(
                    (ve) => `${ve.field}: ${ve.message}`
                );
                return messages.join(', ');
            }

            // Handle standard error message
            if (data.message) {
                return data.message;
            }

            // Handle error field
            if (data.error) {
                return data.error;
            }
        }

        // Handle HTTP status codes with friendly messages
        const status = axiosError.response?.status;
        switch (status) {
            case 400:
                return 'Invalid request. Please check your input.';
            case 401:
                return 'Authentication failed. Please check your credentials.';
            case 403:
                return 'You do not have permission to perform this action.';
            case 404:
                return 'The requested resource was not found.';
            case 409:
                return 'This resource already exists.';
            case 422:
                return 'Invalid data provided. Please check your input.';
            case 429:
                return 'Too many requests. Please try again later.';
            case 500:
                return 'Server error. Please try again later.';
            case 502:
            case 503:
            case 504:
                return 'Service temporarily unavailable. Please try again later.';
            default:
                if (axiosError.message) {
                    return axiosError.message;
                }
        }
    }

    // Handle standard Error objects
    if (error instanceof Error) {
        return error.message;
    }

    // Handle string errors
    if (typeof error === 'string') {
        return error;
    }

    return 'An unexpected error occurred';
}

/**
 * Check if an error is an Axios error
 */
function isAxiosError(error: unknown): error is AxiosError {
    return (error as AxiosError)?.isAxiosError === true;
}

/**
 * Common error messages for specific scenarios
 */
export const ErrorMessages = {
    EMAIL_EXISTS: 'An account with this email already exists.',
    INVALID_CREDENTIALS: 'Invalid email or password.',
    ACCOUNT_DISABLED: 'Your account has been disabled.',
    SESSION_EXPIRED: 'Your session has expired. Please login again.',
    NETWORK_ERROR: 'Unable to connect to the server. Please check your internet connection.',
    PAYMENT_REQUIRED: 'Payment is required to complete this action.',
    SUBSCRIPTION_EXISTS: 'You already have an active subscription.',
};
