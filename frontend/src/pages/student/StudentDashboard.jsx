import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axios';
import StatusBadge from '../../components/StatusBadge';
import Spinner from '../../components/Spinner';

export default function StudentDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalPoints: 0,
    pendingCount: 0,
    approvedCount: 0,
    needsActionCount: 0,
    recentAchievements: []
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/student/dashboard/summary')
      .then((res) => {
        setStats(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load dashboard summary', err);
        setLoading(false);
      });
  }, []);

  return (
    <div style={{ maxWidth: '1000px', margin: '36px auto', padding: '0 16px' }}>
      {/* Welcome Banner */}
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

      {/* Metric Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', marginBottom: '32px' }}>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Verified Points</span>
          <h2 style={{ color: 'var(--navy-900)', fontSize: '1.8rem', margin: '8px 0 0' }}>
            {loading ? '—' : stats.totalPoints}
          </h2>
        </div>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Pending Approvals</span>
          <h2 style={{ color: 'var(--warning)', fontSize: '1.8rem', margin: '8px 0 0' }}>
            {loading ? '—' : stats.pendingCount}
          </h2>
        </div>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Approved Submissions</span>
          <h2 style={{ color: 'var(--success)', fontSize: '1.8rem', margin: '8px 0 0' }}>
            {loading ? '—' : stats.approvedCount}
          </h2>
        </div>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Action Required</span>
          <h2 style={{ color: 'var(--danger)', fontSize: '1.8rem', margin: '8px 0 0' }}>
            {loading ? '—' : stats.needsActionCount}
          </h2>
        </div>
      </div>

      {/* Recent Submissions Card */}
      <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h2 style={{ fontSize: '1.15rem', color: 'var(--navy-900)' }}>Recent Submissions</h2>
          <Link to="/student/achievements" style={{ fontSize: '0.85rem', color: 'var(--indigo-600)', fontWeight: 600 }}>
            View All ({stats.recentAchievements?.length || 0}) →
          </Link>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '30px 0' }}>
            <Spinner />
          </div>
        ) : !stats.recentAchievements || stats.recentAchievements.length === 0 ? (
          <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            No recent submissions. Get started by uploading your first certificate!
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {stats.recentAchievements.map((item) => (
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
                    {item.categoryName} • {item.eventDate}
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                  <StatusBadge status={item.status} />
                  <Link
                    to={`/student/achievements/${item.id}`}
                    style={{ fontSize: '0.85rem', color: 'var(--indigo-600)', fontWeight: 600 }}
                  >
                    View Details
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
