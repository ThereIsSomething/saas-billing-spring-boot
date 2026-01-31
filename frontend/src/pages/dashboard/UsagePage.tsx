import React, { useEffect, useState } from 'react';
import { usageApi } from '../../api';
import { BarChart3, Activity, Database, Cloud } from 'lucide-react';
import './UsagePage.css';

const UsagePage: React.FC = () => {
    const [usage, setUsage] = useState<Record<string, number>>({});
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetchUsage();
    }, []);

    const fetchUsage = async () => {
        try {
            const data = await usageApi.getMySummary();
            setUsage(data);
        } catch (error) {
            console.error('Error fetching usage:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const getMetricIcon = (metric: string) => {
        if (metric.includes('api')) return <Activity size={24} />;
        if (metric.includes('storage')) return <Database size={24} />;
        if (metric.includes('bandwidth')) return <Cloud size={24} />;
        return <BarChart3 size={24} />;
    };

    const formatMetricName = (metric: string) => {
        return metric
            .replace(/_/g, ' ')
            .replace(/\b\w/g, (l) => l.toUpperCase());
    };

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    const metrics = Object.entries(usage);

    return (
        <div className="usage-page">
            <div className="page-header">
                <h2>Usage Metrics</h2>
                <p className="text-secondary">Monitor your resource consumption</p>
            </div>

            {metrics.length > 0 ? (
                <div className="usage-grid">
                    {metrics.map(([metric, value]) => (
                        <div key={metric} className="usage-card card">
                            <div className="usage-icon">
                                {getMetricIcon(metric)}
                            </div>
                            <div className="usage-info">
                                <span className="usage-label">{formatMetricName(metric)}</span>
                                <span className="usage-value">{value.toLocaleString()}</span>
                            </div>
                            <div className="usage-bar">
                                <div
                                    className="usage-bar-fill"
                                    style={{ width: `${Math.min((value / 10000) * 100, 100)}%` }}
                                />
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="empty-state card">
                    <BarChart3 size={48} className="empty-icon" />
                    <h3>No Usage Data</h3>
                    <p>Usage metrics will appear here once you start using the platform.</p>
                </div>
            )}
        </div>
    );
};

export default UsagePage;
