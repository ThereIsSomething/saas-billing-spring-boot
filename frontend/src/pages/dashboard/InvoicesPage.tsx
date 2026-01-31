import React, { useEffect, useState } from 'react';
import { invoicesApi } from '../../api';
import { Invoice } from '../../types';
import { FileText, Download, DollarSign, Calendar, CheckCircle } from 'lucide-react';
import './InvoicesPage.css';

const InvoicesPage: React.FC = () => {
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetchInvoices();
    }, []);

    const fetchInvoices = async () => {
        try {
            const data = await invoicesApi.getMine();
            setInvoices(data || []);
        } catch (error) {
            console.error('Error fetching invoices:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const getStatusBadge = (status: string) => {
        const statusMap: Record<string, string> = {
            PAID: 'success',
            PENDING: 'warning',
            CANCELLED: 'error',
            OVERDUE: 'error',
        };
        return statusMap[status] || 'info';
    };

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    return (
        <div className="invoices-page">
            <div className="page-header">
                <h2>Invoices</h2>
                <p className="text-secondary">View and manage your billing invoices</p>
            </div>

            {invoices.length > 0 ? (
                <>
                    <div className="table-container card">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Invoice #</th>
                                    <th>Date</th>
                                    <th>Due Date</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {invoices.map((invoice) => (
                                    <tr key={invoice.id}>
                                        <td>
                                            <div className="invoice-number">
                                                <FileText size={16} />
                                                {invoice.invoiceNumber}
                                            </div>
                                        </td>
                                        <td>
                                            <div className="date-cell">
                                                <Calendar size={14} />
                                                {new Date(invoice.invoiceDate).toLocaleDateString()}
                                            </div>
                                        </td>
                                        <td>
                                            <div className="date-cell">
                                                {new Date(invoice.dueDate).toLocaleDateString()}
                                            </div>
                                        </td>
                                        <td>
                                            <div className="amount-cell">
                                                <DollarSign size={14} />
                                                {invoice.totalAmount?.toFixed(2)}
                                                <span className="currency">{invoice.currency}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <span className={`badge badge-${getStatusBadge(invoice.status)}`}>
                                                {invoice.status === 'PAID' && <CheckCircle size={12} />}
                                                {invoice.status}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="action-buttons">
                                                <button className="btn btn-ghost btn-sm" title="Download">
                                                    <Download size={16} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </>
            ) : (
                <div className="empty-state card">
                    <FileText size={48} className="empty-icon" />
                    <h3>No Invoices</h3>
                    <p>You don't have any invoices yet.</p>
                </div>
            )}
        </div>
    );
};

export default InvoicesPage;
