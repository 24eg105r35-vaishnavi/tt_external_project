import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';
import api from '../../api/axios';
import Spinner from '../../components/Spinner';

export default function FacultyDashboard() {
  const { user } = useAuth();
  const [queueCount, setQueueCount] = useState(0);
  const [recentItems, setRecentItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/faculty/queue')
      .then((res) => {
        setQueueCount(res.data.length);
        setRecentItems(res.data.slice(0, 5));
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load faculty queue preview', err);
        setLoading(false);
      });
  }, []);

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
            Review Queue ({loading ? '...' : queueCount}) &rarr;
          </Link>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px', marginBottom: '32px' }}>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Pending Verifications</div>
          <h2 style={{ fontSize: '2rem', color: queueCount > 0 ? 'var(--warning)' : 'var(--navy-900)', margin: '8px 0 12px' }}>
            {loading ? '—' : queueCount}
          </h2>
          <Link to="/faculty/queue" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Open Verification Queue &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>University Repository</div>
          <h2 style={{ fontSize: '1.25rem', color: 'var(--navy-900)', margin: '8px 0 12px' }}>
            Search & CSV Export
          </h2>
          <Link to="/search" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            Search Verified Achievements &rarr;
          </Link>
        </div>

        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Department Insights</div>
          <h2 style={{ fontSize: '1.25rem', color: 'var(--navy-900)', margin: '8px 0 12px' }}>
            Analytics Dashboard
          </h2>
          <Link to="/analytics" style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.85rem' }}>
            View Analytics &rarr;
          </Link>
        </div>
      </div>

      {/* Queue Preview Card */}
      <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h2 style={{ fontSize: '1.15rem', color: 'var(--navy-900)' }}>Submissions Awaiting Verification</h2>
          <Link to="/faculty/queue" style={{ fontSize: '0.85rem', color: 'var(--indigo-600)', fontWeight: 600 }}>
            View All ({queueCount}) →
          </Link>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '30px 0' }}>
            <Spinner />
          </div>
        ) : recentItems.length === 0 ? (
          <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            ✓ Verification queue is clear. No pending items for your department.
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {recentItems.map((item) => (
              <div
                key={item.id}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '12px 16px',
                  backgroundColor: 'var(--bg)',
                  borderRadius: '6px',
                  flexWrap: 'wrap',
                  gap: '8px'
                }}
              >
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.95rem', color: 'var(--navy-900)' }}>
                    {item.title}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {item.studentName} ({item.rollNumber}) • {item.categoryName} • {item.eventDate}
                  </div>
                </div>

                <Link
                  to={`/faculty/review/${item.id}`}
                  style={{
                    backgroundColor: 'var(--navy-900)',
                    color: '#fff',
                    padding: '6px 14px',
                    borderRadius: '6px',
                    fontSize: '0.85rem',
                    fontWeight: 600
                  }}
                >
                  Review
                </Link>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
