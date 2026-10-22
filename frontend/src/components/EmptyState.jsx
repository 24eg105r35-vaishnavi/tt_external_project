import React from 'react';

export default function EmptyState({ title = 'No records found', description = 'There are no items to display at this time.' }) {
  return (
    <div style={{
      backgroundColor: 'var(--surface)',
      border: '1px solid var(--border)',
      borderRadius: 'var(--radius)',
      padding: '48px 24px',
      textAlign: 'center',
      color: 'var(--text-muted)'
    }}>
      <div style={{ fontSize: '2.5rem', marginBottom: '8px' }}>📂</div>
      <h3 style={{ fontSize: '1.15rem', color: 'var(--navy-900)', marginBottom: '4px' }}>{title}</h3>
      <p style={{ fontSize: '0.9rem' }}>{description}</p>
    </div>
  );
}
