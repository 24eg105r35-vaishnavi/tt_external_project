import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';

export default function FacultyDashboard() {
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <h1 style={{ color: 'var(--navy-900)', fontSize: '1.8rem', marginBottom: '4px' }}>
              Faculty Portal: {user?.fullName}
            </h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
              Department Reviewer • {user?.email}
            </p>
          </div>
          <Link
            to="/faculty/queue"
            style={{
              backgroundColor: 'var(--navy-900)',
              color: '#ffffff',
              padding: '10px 18px',
              borderRadius: '6px',
              textDecoration: 'none',
              fontWeight: 600,
              fontSize: '0.9rem'
            }}
          >
            Go to Verification Queue &rarr;
          </Link>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>Pending Verifications</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Review student achievement submissions routed to your department.
          </p>
          <Link to="/faculty/queue" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Open Queue &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>College Search</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Filter verified achievements and export reports to CSV.
          </p>
          <Link to="/search" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Search &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
}
