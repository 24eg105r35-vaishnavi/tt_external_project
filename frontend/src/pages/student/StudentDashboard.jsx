import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';

export default function StudentDashboard() {
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
              Welcome, {user?.fullName || 'Student'}!
            </h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
              Student Portal • {user?.email}
            </p>
          </div>
          <Link
            to="/student/upload"
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
            + Upload Achievement
          </Link>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Verified Points</span>
          <h2 style={{ color: 'var(--navy-900)', fontSize: '1.8rem', margin: '8px 0 0' }}>0</h2>
        </div>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Pending Approvals</span>
          <h2 style={{ color: 'var(--warning)', fontSize: '1.8rem', margin: '8px 0 0' }}>0</h2>
        </div>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Approved Submissions</span>
          <h2 style={{ color: 'var(--success)', fontSize: '1.8rem', margin: '8px 0 0' }}>0</h2>
        </div>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Action Required</span>
          <h2 style={{ color: 'var(--danger)', fontSize: '1.8rem', margin: '8px 0 0' }}>0</h2>
        </div>
      </div>
    </div>
  );
}
