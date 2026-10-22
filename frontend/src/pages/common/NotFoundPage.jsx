import React from 'react';
import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="container" style={{ padding: '80px 16px', maxWidth: '600px', textAlign: 'center' }}>
      <div style={{ backgroundColor: 'var(--surface)', padding: '48px 32px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
        <div style={{ fontSize: '3rem', fontWeight: 800, color: 'var(--navy-900)', marginBottom: '8px' }}>
          404
        </div>
        <h1 style={{ fontSize: '1.4rem', color: 'var(--navy-900)', marginBottom: '8px' }}>
          Page Not Found
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginBottom: '24px' }}>
          The requested page does not exist or may have been relocated.
        </p>
        <Link
          to="/"
          style={{
            backgroundColor: 'var(--navy-900)',
            color: '#fff',
            padding: '10px 22px',
            borderRadius: '6px',
            fontWeight: 600,
            fontSize: '0.9rem',
            textDecoration: 'none'
          }}
        >
          Return to Portal Home
        </Link>
      </div>
    </div>
  );
}
