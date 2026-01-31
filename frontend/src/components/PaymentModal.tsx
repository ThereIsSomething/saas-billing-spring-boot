import React, { useState } from 'react';
import { CreditCard, Lock, X, Check, AlertCircle } from 'lucide-react';
import { paymentsApi, PaymentInitiateResponse } from '../api/payments.api';
import './PaymentModal.css';

interface PaymentModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: (paymentOrderId: string) => void;
    paymentDetails: PaymentInitiateResponse;
}

const PaymentModal: React.FC<PaymentModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
    paymentDetails,
}) => {
    const [cardNumber, setCardNumber] = useState('');
    const [expiry, setExpiry] = useState('');
    const [cvv, setCvv] = useState('');
    const [cardName, setCardName] = useState('');
    const [isProcessing, setIsProcessing] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [paymentSuccess, setPaymentSuccess] = useState(false);

    const formatCardNumber = (value: string) => {
        const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
        const matches = v.match(/\d{4,16}/g);
        const match = (matches && matches[0]) || '';
        const parts = [];
        for (let i = 0; i < match.length; i += 4) {
            parts.push(match.substring(i, i + 4));
        }
        return parts.length ? parts.join(' ') : v;
    };

    const formatExpiry = (value: string) => {
        const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
        if (v.length >= 2) {
            return v.substring(0, 2) + '/' + v.substring(2, 4);
        }
        return v;
    };

    const validateCard = () => {
        const cardNumClean = cardNumber.replace(/\s/g, '');
        if (cardNumClean.length !== 16) {
            setError('Please enter a valid 16-digit card number');
            return false;
        }
        if (expiry.length !== 5) {
            setError('Please enter a valid expiry date (MM/YY)');
            return false;
        }
        if (cvv.length < 3) {
            setError('Please enter a valid CVV');
            return false;
        }
        if (cardName.trim().length < 2) {
            setError('Please enter the cardholder name');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!validateCard()) return;

        setIsProcessing(true);

        try {
            // Simulate payment processing
            await new Promise(resolve => setTimeout(resolve, 1500));

            // Generate mock payment ID and signature
            const mockPaymentId = 'pay_' + Math.random().toString(36).substring(2, 15);
            const mockSignature = 'mock_sig_' + Math.random().toString(36).substring(2, 15);

            // Verify the payment
            const result = await paymentsApi.verify({
                orderId: paymentDetails.orderId,
                paymentId: mockPaymentId,
                signature: mockSignature,
            });

            if (result.verified) {
                setPaymentSuccess(true);
                // Brief delay to show success animation
                setTimeout(() => {
                    onSuccess(paymentDetails.orderId);
                }, 1500);
            } else {
                setError(result.message || 'Payment verification failed');
            }
        } catch (err: any) {
            setError(err.response?.data?.message || 'Payment failed. Please try again.');
        } finally {
            setIsProcessing(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="payment-modal-overlay" onClick={onClose}>
            <div className="payment-modal" onClick={e => e.stopPropagation()}>
                {paymentSuccess ? (
                    <div className="payment-success">
                        <div className="success-icon">
                            <Check size={48} />
                        </div>
                        <h2>Payment Successful!</h2>
                        <p>Your subscription is being activated...</p>
                    </div>
                ) : (
                    <>
                        <div className="payment-modal-header">
                            <div className="payment-title">
                                <CreditCard size={24} />
                                <h2>Complete Payment</h2>
                            </div>
                            <button className="close-btn" onClick={onClose}>
                                <X size={20} />
                            </button>
                        </div>

                        <div className="payment-summary">
                            <div className="summary-row">
                                <span>Plan</span>
                                <span className="plan-name">{paymentDetails.planName}</span>
                            </div>
                            <div className="summary-row total">
                                <span>Total Amount</span>
                                <span className="amount">
                                    {paymentDetails.currency === 'USD' ? '$' : '₹'}
                                    {paymentDetails.amount.toLocaleString()}
                                </span>
                            </div>
                        </div>

                        <form onSubmit={handleSubmit} className="payment-form">
                            {error && (
                                <div className="payment-error">
                                    <AlertCircle size={16} />
                                    <span>{error}</span>
                                </div>
                            )}

                            <div className="form-group">
                                <label htmlFor="cardNumber">Card Number</label>
                                <input
                                    id="cardNumber"
                                    type="text"
                                    placeholder="1234 5678 9012 3456"
                                    value={cardNumber}
                                    onChange={e => setCardNumber(formatCardNumber(e.target.value))}
                                    maxLength={19}
                                    disabled={isProcessing}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="cardName">Cardholder Name</label>
                                <input
                                    id="cardName"
                                    type="text"
                                    placeholder="John Doe"
                                    value={cardName}
                                    onChange={e => setCardName(e.target.value)}
                                    disabled={isProcessing}
                                />
                            </div>

                            <div className="form-row">
                                <div className="form-group">
                                    <label htmlFor="expiry">Expiry Date</label>
                                    <input
                                        id="expiry"
                                        type="text"
                                        placeholder="MM/YY"
                                        value={expiry}
                                        onChange={e => setExpiry(formatExpiry(e.target.value))}
                                        maxLength={5}
                                        disabled={isProcessing}
                                    />
                                </div>
                                <div className="form-group">
                                    <label htmlFor="cvv">CVV</label>
                                    <input
                                        id="cvv"
                                        type="password"
                                        placeholder="123"
                                        value={cvv}
                                        onChange={e => setCvv(e.target.value.replace(/\D/g, '').substring(0, 4))}
                                        maxLength={4}
                                        disabled={isProcessing}
                                    />
                                </div>
                            </div>

                            <button
                                type="submit"
                                className="pay-button"
                                disabled={isProcessing}
                            >
                                {isProcessing ? (
                                    <>
                                        <div className="spinner" />
                                        <span>Processing...</span>
                                    </>
                                ) : (
                                    <>
                                        <Lock size={18} />
                                        <span>Pay {paymentDetails.currency === 'USD' ? '$' : '₹'}{paymentDetails.amount.toLocaleString()}</span>
                                    </>
                                )}
                            </button>

                            <div className="secure-badge">
                                <Lock size={14} />
                                <span>Secured by Mock Razorpay • Demo Mode</span>
                            </div>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
};

export default PaymentModal;
