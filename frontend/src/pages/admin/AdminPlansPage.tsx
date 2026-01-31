import React, { useEffect, useState } from 'react';
import { plansApi } from '../../api';
import { Plan } from '../../types';
import { Package, Plus, Edit2, Trash2, Check, X, Star } from 'lucide-react';
import './AdminPlansPage.css';

const AdminPlansPage: React.FC = () => {
    const [plans, setPlans] = useState<Plan[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingPlan, setEditingPlan] = useState<Plan | null>(null);
    const [actionLoading, setActionLoading] = useState<string | null>(null);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        price: 0,
        currency: 'USD',
        billingCycle: 'MONTHLY' as 'MONTHLY' | 'QUARTERLY' | 'YEARLY',
        usageLimit: 0,
        apiCallsLimit: 0,
        storageLimitMb: 0,
        usersLimit: 1,
        trialDays: 0,
        features: [] as string[],
        isFeatured: false,
        active: true,
        sortOrder: 0,
    });
    const [featureInput, setFeatureInput] = useState('');

    useEffect(() => {
        fetchPlans();
    }, []);

    const fetchPlans = async () => {
        try {
            const data = await plansApi.getAll();
            setPlans(data);
        } catch (error) {
            console.error('Error fetching plans:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleEdit = (plan: Plan) => {
        setEditingPlan(plan);
        setFormData({
            name: plan.name,
            description: plan.description || '',
            price: plan.price,
            currency: plan.currency,
            billingCycle: plan.billingCycle,
            usageLimit: plan.usageLimit || 0,
            apiCallsLimit: plan.apiCallsLimit || 0,
            storageLimitMb: plan.storageLimitMb || 0,
            usersLimit: plan.usersLimit || 1,
            trialDays: plan.trialDays,
            features: plan.features || [],
            isFeatured: plan.isFeatured,
            active: plan.active,
            sortOrder: plan.sortOrder,
        });
        setShowModal(true);
    };

    const handleCreate = () => {
        setEditingPlan(null);
        setFormData({
            name: '',
            description: '',
            price: 0,
            currency: 'USD',
            billingCycle: 'MONTHLY',
            usageLimit: 0,
            apiCallsLimit: 0,
            storageLimitMb: 0,
            usersLimit: 1,
            trialDays: 0,
            features: [],
            isFeatured: false,
            active: true,
            sortOrder: 0,
        });
        setShowModal(true);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setActionLoading('save');

        try {
            console.log('Sending plan data:', JSON.stringify(formData, null, 2));
            if (editingPlan) {
                await plansApi.update(editingPlan.id, formData);
            } else {
                await plansApi.create(formData as Omit<Plan, 'id' | 'createdAt'>);
            }
            await fetchPlans();
            setShowModal(false);
        } catch (error) {
            console.error('Error saving plan:', error);
            alert('Failed to save plan');
        } finally {
            setActionLoading(null);
        }
    };

    const handleDelete = async (id: string) => {
        if (!confirm('Are you sure you want to delete this plan?')) return;

        setActionLoading(id);
        try {
            await plansApi.delete(id);
            await fetchPlans();
        } catch (error) {
            console.error('Error deleting plan:', error);
        } finally {
            setActionLoading(null);
        }
    };

    const handleToggleActive = async (id: string) => {
        setActionLoading(id);
        try {
            await plansApi.toggleActive(id);
            await fetchPlans();
        } catch (error) {
            console.error('Error toggling plan:', error);
        } finally {
            setActionLoading(null);
        }
    };

    const addFeature = () => {
        if (featureInput.trim()) {
            setFormData({ ...formData, features: [...formData.features, featureInput.trim()] });
            setFeatureInput('');
        }
    };

    const removeFeature = (index: number) => {
        setFormData({
            ...formData,
            features: formData.features.filter((_, i) => i !== index)
        });
    };

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    return (
        <div className="admin-plans-page">
            <div className="page-header">
                <div>
                    <h2>Plan Management</h2>
                    <p className="text-secondary">Create and manage subscription plans</p>
                </div>
                <button className="btn btn-primary" onClick={handleCreate}>
                    <Plus size={18} />
                    Create Plan
                </button>
            </div>

            <div className="plans-table-container card">
                <table className="table">
                    <thead>
                        <tr>
                            <th>Plan</th>
                            <th>Price</th>
                            <th>Billing</th>
                            <th>Limits</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {plans.map((plan) => (
                            <tr key={plan.id}>
                                <td>
                                    <div className="plan-cell">
                                        <Package size={20} className="plan-icon" />
                                        <div>
                                            <span className="plan-name">
                                                {plan.name}
                                                {plan.isFeatured && <Star size={14} className="featured-star" />}
                                            </span>
                                            <span className="plan-desc">{plan.description}</span>
                                        </div>
                                    </div>
                                </td>
                                <td className="price-cell">
                                    ${plan.price.toFixed(2)}
                                </td>
                                <td>
                                    <span className="badge badge-info">{plan.billingCycle}</span>
                                </td>
                                <td className="limits-cell">
                                    {plan.apiCallsLimit && <span>{plan.apiCallsLimit.toLocaleString()} API</span>}
                                    {plan.storageLimitMb && <span>{plan.storageLimitMb} MB</span>}
                                </td>
                                <td>
                                    <span className={`badge badge-${plan.active ? 'success' : 'error'}`}>
                                        {plan.active ? 'Active' : 'Inactive'}
                                    </span>
                                </td>
                                <td>
                                    <div className="action-buttons">
                                        <button
                                            className="btn btn-ghost btn-icon"
                                            onClick={() => handleEdit(plan)}
                                            title="Edit"
                                        >
                                            <Edit2 size={16} />
                                        </button>
                                        <button
                                            className="btn btn-ghost btn-icon"
                                            onClick={() => handleToggleActive(plan.id)}
                                            disabled={actionLoading === plan.id}
                                            title={plan.active ? 'Deactivate' : 'Activate'}
                                        >
                                            {plan.active ? <X size={16} /> : <Check size={16} />}
                                        </button>
                                        <button
                                            className="btn btn-ghost btn-icon"
                                            onClick={() => handleDelete(plan.id)}
                                            disabled={actionLoading === plan.id}
                                            title="Delete"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal plan-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3 className="modal-title">
                                {editingPlan ? 'Edit Plan' : 'Create New Plan'}
                            </h3>
                            <button className="btn btn-ghost btn-icon" onClick={() => setShowModal(false)}>
                                <X size={20} />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="plan-form">
                            <div className="form-grid">
                                <div className="input-group">
                                    <label className="input-label">Name *</label>
                                    <input
                                        type="text"
                                        className="input"
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        required
                                    />
                                </div>

                                <div className="input-group">
                                    <label className="input-label">Price *</label>
                                    <input
                                        type="number"
                                        className="input"
                                        step="0.01"
                                        min="0"
                                        value={formData.price}
                                        onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })}
                                        required
                                    />
                                </div>

                                <div className="input-group span-2">
                                    <label className="input-label">Description</label>
                                    <input
                                        type="text"
                                        className="input"
                                        value={formData.description}
                                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                    />
                                </div>

                                <div className="input-group">
                                    <label className="input-label">Billing Cycle</label>
                                    <select
                                        className="input"
                                        value={formData.billingCycle}
                                        onChange={(e) => setFormData({ ...formData, billingCycle: e.target.value as 'MONTHLY' | 'QUARTERLY' | 'YEARLY' })}
                                    >
                                        <option value="MONTHLY">Monthly</option>
                                        <option value="QUARTERLY">Quarterly</option>
                                        <option value="YEARLY">Yearly</option>
                                    </select>
                                </div>

                                <div className="input-group">
                                    <label className="input-label">Trial Days</label>
                                    <input
                                        type="number"
                                        className="input"
                                        min="0"
                                        value={formData.trialDays}
                                        onChange={(e) => setFormData({ ...formData, trialDays: parseInt(e.target.value) || 0 })}
                                    />
                                </div>

                                <div className="input-group">
                                    <label className="input-label">API Calls Limit</label>
                                    <input
                                        type="number"
                                        className="input"
                                        min="0"
                                        value={formData.apiCallsLimit}
                                        onChange={(e) => setFormData({ ...formData, apiCallsLimit: parseInt(e.target.value) || 0 })}
                                    />
                                </div>

                                <div className="input-group">
                                    <label className="input-label">Storage (MB)</label>
                                    <input
                                        type="number"
                                        className="input"
                                        min="0"
                                        value={formData.storageLimitMb}
                                        onChange={(e) => setFormData({ ...formData, storageLimitMb: parseInt(e.target.value) || 0 })}
                                    />
                                </div>

                                <div className="input-group">
                                    <label className="input-label">Users Limit</label>
                                    <input
                                        type="number"
                                        className="input"
                                        min="1"
                                        value={formData.usersLimit || 1}
                                        onChange={(e) => setFormData({ ...formData, usersLimit: parseInt(e.target.value) || 1 })}
                                    />
                                </div>
                            </div>

                            <div className="input-group">
                                <label className="input-label">Features</label>
                                <div className="feature-input-row">
                                    <input
                                        type="text"
                                        className="input"
                                        value={featureInput}
                                        onChange={(e) => setFeatureInput(e.target.value)}
                                        placeholder="Add a feature..."
                                        onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addFeature())}
                                    />
                                    <button type="button" className="btn btn-secondary" onClick={addFeature}>
                                        Add
                                    </button>
                                </div>
                                <div className="features-list">
                                    {formData.features.map((feature, index) => (
                                        <span key={index} className="feature-tag">
                                            {feature}
                                            <button type="button" onClick={() => removeFeature(index)}>
                                                <X size={14} />
                                            </button>
                                        </span>
                                    ))}
                                </div>
                            </div>

                            <div className="form-options">
                                <label className="checkbox-wrapper">
                                    <input
                                        type="checkbox"
                                        checked={formData.isFeatured}
                                        onChange={(e) => setFormData({ ...formData, isFeatured: e.target.checked })}
                                    />
                                    <span className="checkbox-label">Featured Plan</span>
                                </label>
                                <label className="checkbox-wrapper">
                                    <input
                                        type="checkbox"
                                        checked={formData.active}
                                        onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                                    />
                                    <span className="checkbox-label">Active</span>
                                </label>
                            </div>

                            <div className="modal-actions">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-primary" disabled={actionLoading === 'save'}>
                                    {actionLoading === 'save' ? (
                                        <>
                                            <div className="spinner" style={{ width: 16, height: 16 }} />
                                            Saving...
                                        </>
                                    ) : editingPlan ? (
                                        'Update Plan'
                                    ) : (
                                        'Create Plan'
                                    )}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminPlansPage;
