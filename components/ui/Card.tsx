import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  hover?: boolean;
  elevated?: boolean;
}

export const Card: React.FC<CardProps> = ({ children, className = '', hover = true, elevated = false }) => {
  const baseClass = 'bg-bg-surface border border-border rounded-jpm-lg';
  const shadowClass = elevated ? 'shadow-elevated' : 'shadow-card';
  const hoverClass = hover ? 'hover:shadow-card-hover transition-all duration-200' : '';

  return (
    <div className={baseClass + ' ' + shadowClass + ' ' + hoverClass + ' ' + className}>
      {children}
    </div>
  );
};

interface StatCardProps {
  label: string;
  value: string | number;
  icon?: React.ReactNode;
  trend?: { value: number; positive: boolean };
  className?: string;
}

export const StatCard: React.FC<StatCardProps> = ({ label, value, icon, trend, className = '' }) => {
  return (
    <Card className={'p-6 ' + className}>
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm text-text-muted font-medium">{label}</p>
          <p className="text-3xl font-light text-text-primary mt-2 tracking-tight" style={{ letterSpacing: '-0.02em' }}>
            {value}
          </p>
          {trend && (
            <div className="flex items-center gap-1 mt-2">
              <span className={'text-xs font-medium ' + (trend.positive ? 'text-status-success' : 'text-status-error')}>
                {trend.positive ? '+' : '-'}{Math.abs(trend.value)}%
              </span>
              <span className="text-xs text-text-muted">vs last month</span>
            </div>
          )}
        </div>
        {icon && (
          <div className="p-3 bg-jpm-red-subtle rounded-jpm text-jpm-red">
            {icon}
          </div>
        )}
      </div>
    </Card>
  );
};

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'success' | 'warning' | 'error' | 'neutral';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ children, variant = 'neutral', className = '' }) => {
  const variants: Record<string, string> = {
    success: 'bg-status-success-bg text-status-success border border-status-success-border',
    warning: 'bg-warning/10 text-warning border border-warning/30',
    error: 'bg-jpm-red-subtle text-jpm-red border border-jpm-red/30',
    neutral: 'bg-bg-elevated text-text-secondary border border-border',
  };

  return (
    <span className={'inline-flex items-center px-2.5 py-0.5 rounded-pill text-xs font-medium ' + variants[variant] + ' ' + className}>
      {children}
    </span>
  );
};

interface SectionHeaderProps {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
  className?: string;
}

export const SectionHeader: React.FC<SectionHeaderProps> = ({ title, subtitle, action, className = '' }) => {
  return (
    <div className={'flex items-center justify-between mb-6 ' + className}>
      <div>
        <h2 className="text-xl font-light text-text-primary tracking-tight" style={{ letterSpacing: '-0.02em' }}>
          {title}
        </h2>
        {subtitle && <p className="text-sm text-text-muted mt-1">{subtitle}</p>}
      </div>
      {action && <div>{action}</div>}
    </div>
  );
};
