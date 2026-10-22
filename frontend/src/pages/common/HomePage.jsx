import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function HomePage() {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const checkHealth = () => {
    setLoading(true);
    setError('');
    api.get('/health')
      .then((res) => {
        setHealth(res.data);
        setLoading(false);
      })
      .catch(() => {
        setError('Could not connect to backend REST API. Ensure Spring Boot is running on port 8080.');
        setLoading(false);
      });
  };

  useEffect(() => {
    checkHealth();
  }, []);

  return (
    <div style={{ padding: '36px 0' }}>
      <div className="container">
        {/* Hero Section */}
        <div style={{
          backgroundColor: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius)',
          padding: '40px',
          textAlign: 'center',
          boxShadow: '0 2px 8px rgba(0,0,0,0.02)',
          marginBottom: '32px'
        }}>
          <div style={{
            display: 'inline-block',
            backgroundColor: 'var(--indigo-100)',
            color: 'var(--indigo-600)',
            padding: '4px 12px',
            borderRadius: '999px',
            fontSize: '0.8rem',
            fontWeight: 600,
            marginBottom: '12px'
          }}>
            National Institute of Technology
          </div>
          <h1 style={{ fontSize: '2.2rem', color: 'var(--navy-900)', marginBottom: '8px' }}>
            Student Achievement Portal
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', maxWidth: '650px', margin: '0 auto 24px' }}>
            A centralized, verified achievement repository for university students, faculty verifiers, and department administrators.
          </p>

          {/* Backend Health Check Connection Status Card */}
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '8px',
            padding: '10px 20px',
            borderRadius: 'var(--radius)',
            backgroundColor: health ? '#f0fdf4' : '#fffbeb',
            border: `1px solid ${health ? '#bbf7d0' : '#fde68a'}`,
            fontSize: '0.95rem'
          }}>
            <span style={{ fontSize: '1.2rem' }}>{health ? '🟢' : '⏳'}</span>
            <strong>Status:</strong>
            {loading && <Spinner message="Testing backend connection..." />}
            {error && (
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: '10px' }}>
                <span style={{ color: 'var(--danger)' }}>{error}</span>
                <button
                  onClick={checkHealth}
                  style={{
                    backgroundColor: 'var(--navy-700)',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '6px',
                    padding: '4px 10px',
                    fontSize: '0.8rem',
                    cursor: 'pointer'
                  }}
                >
                  Retry
                </button>
              </span>
            )}
            {health && (
              <span style={{ color: 'var(--success)', fontWeight: 600 }}>
                {health.message} (Status: {health.status})
              </span>
            )}
          </div>
        </div>

        {/* Informational Cards */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
          <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
            <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>🎓 For Students</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Upload participation certificates, hackathons, papers, internships, and competitive sports achievements with verifiable proof.
            </p>
          </div>
          <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
            <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>✍️ For Faculty</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Review department-routed submissions, verify student credentials, approve verified points, or request corrections.
            </p>
          </div>
          <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '24px' }}>
            <h3 style={{ color: 'var(--navy-900)', marginBottom: '8px' }}>📊 Analytics & Ranks</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Track college-wide achievements, inspect leaderboards, and monitor department performance metrics.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
