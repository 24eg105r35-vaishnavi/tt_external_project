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
          Welcome, {user?.fullName} ({user?.email}) • University System Administrator
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>Category Management</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '14px' }}>
            Configure achievement categories, points weighting, verifier departments, and custom JSON field schemas.
          </p>
          <Link to="/admin/categories" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Manage Categories &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>User Management</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '14px' }}>
            Provision faculty accounts, inspect registered students, and toggle user active statuses.
          </p>
          <Link to="/admin/users" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Manage Users &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>System Audit Logs</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '14px' }}>
            Review immutable chronological audit records of logins, verifications, category changes, and user updates.
          </p>
          <Link to="/admin/audit" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            View Audit Trail &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>Repository Search</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '14px' }}>
            Multi-field search across verified student achievements with full CSV data export.
          </p>
          <Link to="/search" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Open Search &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>Institutional Analytics</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '14px' }}>
            Interactive charts showing distribution by category, branch, level, and academic year.
          </p>
          <Link to="/analytics" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            View Analytics &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>Campus Leaderboard</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '14px' }}>
            Rankings of top performing student achievers by verified points.
          </p>
          <Link to="/leaderboard" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            View Leaderboard &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
}
