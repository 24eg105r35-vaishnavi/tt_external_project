import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';

export default function AdminDashboard() {
  const { user } = useAuth();

  return (
    <div style={{ maxWidth: '1000px', margin: '36px auto', padding: '0 16px' }}>
      <div style={{
        backgroundColor: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius)',
        padding: '32px',
        marginBottom: '24px'
      }}>
        <h1 style={{ color: 'var(--navy-900)', fontSize: '1.8rem', marginBottom: '4px' }}>
          Administrator Console
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Welcome, {user?.fullName} ({user?.email})
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>Category Management</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '12px' }}>
            Configure achievement categories, points weighting, verifier departments, and custom JSON field schemas.
          </p>
          <Link to="/admin/categories" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Manage Categories &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>User Management</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '12px' }}>
            Provision faculty accounts, inspect registered students, and toggle user active statuses.
          </p>
          <Link to="/admin/users" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Manage Users &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>System Audit Logs</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '12px' }}>
            Review immutable chronological audit records of logins, verifications, category changes, and user updates.
          </p>
          <Link to="/admin/audit" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            View Audit Trail &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
}
