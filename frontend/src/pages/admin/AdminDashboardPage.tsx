import React, { useEffect, useState } from 'react';
import { analyticsApi } from '../../api';
import { DashboardSummary, MonthlyRevenue } from '../../types';
import {
    DollarSign,
    Users,
    TrendingDown,
    TrendingUp,
    Package
} from 'lucide-react';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    BarChart,
    Bar,
    PieChart,
    Pie,
    Cell
} from 'recharts';
import './AdminDashboardPage.css';

const COLORS = ['#6366f1', '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b'];

const AdminDashboardPage: React.FC = () => {
    const [dashboard, setDashboard] = useState<DashboardSummary | null>(null);
    const [revenueData, setRevenueData] = useState<MonthlyRevenue[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [dashData, revData] = await Promise.all([
                    analyticsApi.getDashboard(),
                    analyticsApi.getMonthlyRevenue(6),
                ]);
                setDashboard(dashData);
                setRevenueData(revData);
            } catch (error) {
                console.error('Error fetching analytics:', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, []);

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    const stats = [
        {
            label: 'Monthly Recurring Revenue',
            value: `$${(dashboard?.monthlyRecurringRevenue || 0).toLocaleString()}`,
            icon: <DollarSign size={24} />,
            color: 'success',
            change: '+12.5%',
            changeType: 'up',
        },
        {
            label: 'Active Subscriptions',
            value: dashboard?.activeSubscriptions?.toString() || '0',
            icon: <Users size={24} />,
            color: 'primary',
            change: '+5.2%',
            changeType: 'up',
        },
        {
            label: 'Churn Rate',
            value: `${(dashboard?.churnRate || 0).toFixed(1)}%`,
            icon: <TrendingDown size={24} />,
            color: 'warning',
            change: '-2.1%',
            changeType: 'down',
        },
        {
            label: 'Avg. Revenue Per User',
            value: `$${(dashboard?.averageRevenuePerUser || 0).toFixed(2)}`,
            icon: <TrendingUp size={24} />,
            color: 'info',
            change: '+8.3%',
            changeType: 'up',
        },
    ];

    const chartData = revenueData.map(r => ({
        month: `${r.year}-${String(r.month).padStart(2, '0')}`,
        revenue: r.revenue,
        invoices: r.invoiceCount,
    }));

    const pieData = dashboard?.topPlans?.map(p => ({
        name: p.planName,
        value: p.subscriptionCount,
    })) || [];

    return (
        <div className="admin-dashboard">
            <div className="page-header">
                <h2>Admin Dashboard</h2>
                <p className="text-secondary">Platform analytics and insights</p>
            </div>

            {/* Stats Grid */}
            <div className="admin-stats-grid">
                {stats.map((stat, index) => (
                    <div key={index} className={`admin-stat-card stat-${stat.color}`}>
                        <div className="stat-icon">{stat.icon}</div>
                        <div className="stat-content">
                            <span className="stat-label">{stat.label}</span>
                            <span className="stat-value">{stat.value}</span>
                            <span className={`stat-change ${stat.changeType}`}>
                                {stat.changeType === 'up' ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                                {stat.change}
                            </span>
                        </div>
                    </div>
                ))}
            </div>

            {/* Charts */}
            <div className="charts-grid">
                <div className="card chart-card">
                    <h3 className="card-title">Revenue Trend</h3>
                    <div className="chart-container">
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#2a2a4a" />
                                <XAxis dataKey="month" stroke="#6b6b8a" />
                                <YAxis stroke="#6b6b8a" />
                                <Tooltip
                                    contentStyle={{
                                        background: '#1a1a3e',
                                        border: '1px solid #3a3a5a',
                                        borderRadius: '8px'
                                    }}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="revenue"
                                    stroke="#6366f1"
                                    strokeWidth={3}
                                    dot={{ fill: '#6366f1' }}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="card chart-card">
                    <h3 className="card-title">Invoices by Month</h3>
                    <div className="chart-container">
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#2a2a4a" />
                                <XAxis dataKey="month" stroke="#6b6b8a" />
                                <YAxis stroke="#6b6b8a" />
                                <Tooltip
                                    contentStyle={{
                                        background: '#1a1a3e',
                                        border: '1px solid #3a3a5a',
                                        borderRadius: '8px'
                                    }}
                                />
                                <Bar dataKey="invoices" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="card chart-card plan-distribution">
                    <h3 className="card-title">Plan Distribution</h3>
                    <div className="chart-container pie-container">
                        {pieData.length > 0 ? (
                            <>
                                <ResponsiveContainer width="100%" height={250}>
                                    <PieChart>
                                        <Pie
                                            data={pieData}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={60}
                                            outerRadius={100}
                                            paddingAngle={5}
                                            dataKey="value"
                                        >
                                            {pieData.map((_, index) => (
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                            ))}
                                        </Pie>
                                        <Tooltip />
                                    </PieChart>
                                </ResponsiveContainer>
                                <div className="pie-legend">
                                    {pieData.map((item, index) => (
                                        <div key={index} className="legend-item">
                                            <span
                                                className="legend-color"
                                                style={{ background: COLORS[index % COLORS.length] }}
                                            />
                                            <span className="legend-label">{item.name}</span>
                                            <span className="legend-value">{item.value}</span>
                                        </div>
                                    ))}
                                </div>
                            </>
                        ) : (
                            <div className="empty-state">
                                <Package size={48} className="empty-icon" />
                                <p>No plan data available</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboardPage;
