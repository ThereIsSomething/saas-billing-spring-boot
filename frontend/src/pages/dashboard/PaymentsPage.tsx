import React, { useEffect, useState } from 'react';
import { paymentsApi } from '../../api';
import { Payment } from '../../types';
import { CreditCard, DollarSign, Calendar, CheckCircle, XCircle, Clock } from 'lucide-react';
import './PaymentsPage.css';

const PaymentsPage: React.FC = () => {
    const [payments, setPayments] = useState<Payment[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchPayments();
    }, [page]);

    const fetchPayments = async () => {
        try {
            const data = await paymentsApi.getMine(page, 10);
            setPayments(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (error) {
            console.error('Error fetching payments:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const getStatusIcon = (status: string) => {
        switch (status) {
            case 'SUCCESS': return <CheckCircle size={16} className="status-success" />;
            case 'FAILED': return <XCircle size={16} className="status-error" />;
            case 'REFUNDED': return <XCircle size={16} className="status-warning" />;
            default: return <Clock size={16} className="status-pending" />;
        }
    };

    const getStatusBadge = (status: string) => {
        const map: Record<string, string> = {
            SUCCESS: 'success',
            FAILED: 'error',
            REFUNDED: 'warning',
            PENDING: 'info',
        };
        return map[status] || 'info';
    };

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    return (
        <div className="payments-page">
            <div className="page-header">
                <h2>Payment History</h2>
                <p className="text-secondary">View your payment transactions</p>
            </div>

            {payments.length > 0 ? (
                <>
                    <div className="payments-list">
                        {payments.map((payment) => (
                            <div key={payment.id} className="payment-card card">
                                <div className="payment-icon">
                                    <CreditCard size={24} />
                                </div>
                                <div className="payment-info">
                                    <div className="payment-header">
                                        <span className="payment-method">
                                            {payment.paymentMethod || 'Card Payment'}
                                        </span>
                                        <span className={`badge badge-${getStatusBadge(payment.status)}`}>
                                            {getStatusIcon(payment.status)}
                                            {payment.status}
                                        </span>
                                    </div>
                                    <div className="payment-details">
                                        <span className="payment-date">
                                            <Calendar size={14} />
                                            {new Date(payment.createdAt).toLocaleString()}
                                        </span>
                                        {payment.transactionId && (
                                            <span className="transaction-id">
                                                ID: {payment.transactionId}
                                            </span>
                                        )}
                                    </div>
                                </div>
                                <div className="payment-amount">
                                    <DollarSign size={18} />
                                    <span>{payment.amount?.toFixed(2)}</span>
                                    <span className="currency">{payment.currency}</span>
                                </div>
                            </div>
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <div className="pagination">
                            <button
                                className="btn btn-secondary btn-sm"
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                            >
                                Previous
                            </button>
                            <span className="page-info">Page {page + 1} of {totalPages}</span>
                            <button
                                className="btn btn-secondary btn-sm"
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page >= totalPages - 1}
                            >
                                Next
                            </button>
                        </div>
                    )}
                </>
            ) : (
                <div className="empty-state card">
                    <CreditCard size={48} className="empty-icon" />
                    <h3>No Payments</h3>
                    <p>You haven't made any payments yet.</p>
                </div>
            )}
        </div>
    );
};

export default PaymentsPage;
