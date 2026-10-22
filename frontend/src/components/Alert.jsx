import React from 'react';

export default function Alert({ type = 'info', message }) {
  if (!message) return null;

  const styles = {
    info: { bg: '#eef2ff', color: 'var(--info)', border: '#c7d2fe' },
    success: { bg: '#f0fdf4', color: 'var(--success)', border: '#bbf7d0' },
    warning: { bg: '#fffbeb', color: 'var(--warning)', border: '#fde68a' },
    danger: { bg: '#fef2f2', color: 'var(--danger)', border: '#fecaca' }
  };

  const style = styles[type] || styles.info;

  return (
    <div style={{
      backgroundColor: style.bg,
      color: style.color,
      border: `1px solid ${style.border}`,
      padding: '12px 16px',
      borderRadius: 'var(--radius)',
      fontSize: '0.9rem',
      marginBottom: '16px'
    }}>
      {message}
    </div>
  );
}
