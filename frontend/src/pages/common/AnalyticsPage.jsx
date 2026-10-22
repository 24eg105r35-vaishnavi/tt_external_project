import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Spinner from '../../components/Spinner';

export default function AnalyticsPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  useEffect(() => {
    fetchAnalytics();
  }, []);

  function fetchAnalytics(from = fromDate, to = toDate) {
    setLoading(true);
    const params = {};
    if (from) params.from = from;
    if (to) params.to = to;

    api.get('/analytics', { params })
      .then((res) => {
        setData(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load analytics', err);
        setLoading(false);
      });
  }

  function handleFilterSubmit(e) {
    e.preventDefault();
    fetchAnalytics(fromDate, toDate);
  }

  function handleReset() {
    setFromDate('');
    setToDate('');
    fetchAnalytics('', '');
  }

  function renderChartSection(title, items, barColor = 'var(--indigo-600)') {
    if (!items || items.length === 0) {
      return (
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <h3 style={{ fontSize: '1.05rem', color: 'var(--navy-900)', marginBottom: '14px' }}>{title}</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No data available for this criteria.</p>
        </div>
      );
    }

    return (
      <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
        <h3 style={{ fontSize: '1.05rem', color: 'var(--navy-900)', marginBottom: '18px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
          {title}
        </h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {items.map((item, idx) => (
            <div key={idx}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '6px' }}>
                <span style={{ fontWeight: 600, color: 'var(--navy-900)' }}>{item.label}</span>
                <span style={{ color: 'var(--text-muted)' }}>
                  <strong>{item.count}</strong> ({item.percentage}%)
                </span>
              </div>
              <div style={{ width: '100%', height: '8px', backgroundColor: 'var(--bg)', borderRadius: '999px', overflow: 'hidden' }}>
                <div
                  style={{
                    width: `${Math.max(item.percentage, 2)}%`,
                    height: '100%',
                    backgroundColor: barColor,
                    borderRadius: '999px',
                    transition: 'width 0.4s ease-in-out'
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '36px 16px' }}>
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
          University Achievement Analytics
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Campus-wide metrics, category distributions, and departmental performance trends.
        </p>
      </div>

      {/* Date Filter Card */}
      <div style={{ backgroundColor: 'var(--surface)', padding: '16px 20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', marginBottom: '24px' }}>
        <form onSubmit={handleFilterSubmit} style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '14px' }}>
          <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)' }}>
            Filter Date Range:
          </span>

          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>From</span>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              style={{ padding: '6px 10px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none' }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>To</span>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              style={{ padding: '6px 10px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none' }}
            />
          </div>

          <button
            type="submit"
            style={{ backgroundColor: 'var(--navy-900)', color: '#fff', padding: '7px 16px', borderRadius: '6px', border: 'none', fontWeight: 600, fontSize: '0.85rem' }}
          >
            Apply Range
          </button>

          {(fromDate || toDate) && (
            <button
              type="button"
              onClick={handleReset}
              style={{ background: 'none', border: 'none', color: 'var(--danger)', fontSize: '0.85rem', cursor: 'pointer', textDecoration: 'underline' }}
            >
              Reset
            </button>
          )}
        </form>
      </div>

      {loading || !data ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Aggregating campus analytics...</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
          {/* Summary Metric Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px' }}>
            <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Total Approved Achievements</span>
              <h2 style={{ fontSize: '2rem', color: 'var(--navy-900)', margin: '8px 0 0' }}>
                {data.summary?.totalApprovedAchievements || 0}
              </h2>
            </div>

            <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Students with Verified Honors</span>
              <h2 style={{ fontSize: '2rem', color: 'var(--indigo-600)', margin: '8px 0 0' }}>
                {data.summary?.totalParticipatingStudents || 0}
              </h2>
            </div>

            <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Total Points Awarded</span>
              <h2 style={{ fontSize: '2rem', color: 'var(--success)', margin: '8px 0 0' }}>
                +{data.summary?.totalPointsAwarded || 0}
              </h2>
            </div>
          </div>

          {/* Visual Distribution Charts */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
            {renderChartSection('Achievements by Category', data.byCategory, 'var(--indigo-600)')}
            {renderChartSection('Achievements by Department / Branch', data.byBranch, 'var(--navy-700)')}
            {renderChartSection('Achievements by Competition Level', data.byLevel, 'var(--gold-500)')}
            {renderChartSection('Achievements by Academic Event Year', data.byYear, 'var(--success)')}
          </div>
        </div>
      )}
    </div>
  );
}
