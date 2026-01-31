import React, { useEffect, useState } from 'react';
import { invoicesApi } from '../../api';
import { Invoice } from '../../types';
import { getErrorMessage } from '../../utils/errorHandler';
import { FileText, Check, X, RefreshCw, DollarSign, AlertCircle, Plus } from 'lucide-react';
import './AdminInvoicesPage.css';

const AdminInvoicesPage: React.FC = () => {
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [actionLoading, setActionLoading] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [subscriptionId, setSubscriptionId] = useState('');
    const [isGenerating, setIsGenerating] = useState(false);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const fetchInvoices = async (page = 0) => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await invoicesApi.getAll(page, 10);
            setInvoices(data.content);
            setTotalPages(data.totalPages);
            setCurrentPage(page);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchInvoices();
    }, []);

    const handleGenerateInvoice = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!subscriptionId.trim()) {
            setError('Please enter a subscription ID');
            return;
        }

        setIsGenerating(true);
        setError(null);
        setSuccessMessage(null);

        try {
            const invoice = await invoicesApi.generate(subscriptionId.trim());
            setSuccessMessage(`Invoice ${invoice.invoiceNumber} generated successfully!`);
            setSubscriptionId('');
            await fetchInvoices(0);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsGenerating(false);
        }
    };

    const handleMarkAsPaid = async (id: string) => {
        setActionLoading(id);
        try {
            await invoicesApi.markAsPaid(id);
            await fetchInvoices(currentPage);
        } catch (err) {
            alert(getErrorMessage(err));
        } finally {
            setActionLoading(null);
        }
    };

    const handleCancel = async (id: string) => {
        if (!confirm('Are you sure you want to cancel this invoice?')) return;
        setActionLoading(id);
        try {
            await invoicesApi.cancel(id);
            await fetchInvoices(currentPage);
        } catch (err) {
            alert(getErrorMessage(err));
        } finally {
            setActionLoading(null);
        }
    };

    const getStatusBadge = (status: string) => {
        const statusClasses: Record<string, string> = {
            PENDING: 'badge-warning',
            PAID: 'badge-success',
            CANCELLED: 'badge-error',
            OVERDUE: 'badge-error',
        };
        return <span className={`badge ${statusClasses[status] || 'badge-default'}`}>{status}</span>;
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        });
    };

    const formatCurrency = (amount: number, currency = 'USD') => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency,
        }).format(amount);
    };

    return (
        <div className="admin-invoices-page">
            <div className="page-header">
                <div className="header-content">
                    <FileText size={28} />
                    <div>
                        <h1>Invoice Management</h1>
                        <p>View and manage all invoices</p>
                    </div>
                </div>
                <button className="btn btn-secondary" onClick={() => fetchInvoices(currentPage)}>
                    <RefreshCw size={18} />
                    <span>Refresh</span>
                </button>
            </div>

            {/* Generate Invoice Form */}
            <div className="generate-invoice-section">
                <h3>Generate Invoice</h3>
                <form onSubmit={handleGenerateInvoice} className="generate-form">
                    <input
                        type="text"
                        placeholder="Enter Subscription ID"
                        value={subscriptionId}
                        onChange={(e) => setSubscriptionId(e.target.value)}
                        className="input"
                        disabled={isGenerating}
                    />
                    <button type="submit" className="btn btn-primary" disabled={isGenerating}>
                        {isGenerating ? (
                            <>
                                <div className="spinner spinner-sm" />
                                <span>Generating...</span>
                            </>
                        ) : (
                            <>
                                <Plus size={18} />
                                <span>Generate Invoice</span>
                            </>
                        )}
                    </button>
                </form>
                <p className="generate-hint">
                    Tip: Copy a Subscription ID from the browser console or database to generate an invoice.
                </p>
            </div>

            {error && (
                <div className="alert alert-error">
                    <AlertCircle size={18} />
                    <span>{error}</span>
                </div>
            )}

            {successMessage && (
                <div className="alert alert-success">
                    <Check size={18} />
                    <span>{successMessage}</span>
                </div>
            )}

            {isLoading ? (
                <div className="loading-container">
                    <div className="spinner" />
                    <span>Loading invoices...</span>
                </div>
            ) : invoices.length === 0 ? (
                <div className="empty-state">
                    <FileText size={48} />
                    <h3>No Invoices Found</h3>
                    <p>Generate an invoice using a subscription ID above, or invoices will be created automatically when users subscribe to paid plans.</p>
                </div>
            ) : (
                <>
                    <div className="invoices-table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Invoice #</th>
                                    <th>User</th>
                                    <th>Description</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                    <th>Invoice Date</th>
                                    <th>Due Date</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {invoices.map((invoice) => (
                                    <tr key={invoice.id}>
                                        <td className="invoice-number">{invoice.invoiceNumber}</td>
                                        <td>{invoice.userId}</td>
                                        <td>{invoice.description || 'Subscription Payment'}</td>
                                        <td className="amount">
                                            <DollarSign size={14} />
                                            {formatCurrency(invoice.totalAmount, invoice.currency)}
                                        </td>
                                        <td>{getStatusBadge(invoice.status)}</td>
                                        <td>{formatDate(invoice.invoiceDate)}</td>
                                        <td>{formatDate(invoice.dueDate)}</td>
                                        <td className="actions">
                                            {invoice.status === 'PENDING' && (
                                                <>
                                                    <button
                                                        className="btn btn-sm btn-success"
                                                        onClick={() => handleMarkAsPaid(invoice.id)}
                                                        disabled={actionLoading === invoice.id}
                                                        title="Mark as Paid"
                                                    >
                                                        {actionLoading === invoice.id ? (
                                                            <div className="spinner spinner-sm" />
                                                        ) : (
                                                            <Check size={14} />
                                                        )}
                                                    </button>
                                                    <button
                                                        className="btn btn-sm btn-error"
                                                        onClick={() => handleCancel(invoice.id)}
                                                        disabled={actionLoading === invoice.id}
                                                        title="Cancel Invoice"
                                                    >
                                                        <X size={14} />
                                                    </button>
                                                </>
                                            )}
                                            {invoice.status === 'PAID' && (
                                                <span className="text-success">Paid</span>
                                            )}
                                            {invoice.status === 'CANCELLED' && (
                                                <span className="text-muted">Cancelled</span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {totalPages > 1 && (
                        <div className="pagination">
                            <button
                                className="btn btn-secondary btn-sm"
                                onClick={() => fetchInvoices(currentPage - 1)}
                                disabled={currentPage === 0}
                            >
                                Previous
                            </button>
                            <span className="page-info">
                                Page {currentPage + 1} of {totalPages}
                            </span>
                            <button
                                className="btn btn-secondary btn-sm"
                                onClick={() => fetchInvoices(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                            >
                                Next
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default AdminInvoicesPage;
