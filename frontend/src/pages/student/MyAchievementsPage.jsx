import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import api from '../../api/axios';
import StatusBadge from '../../components/StatusBadge';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function MyAchievementsPage() {
  const location = useLocation();
  const [achievements, setAchievements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [successMessage, setSuccessMessage] = useState(location.state?.successMessage || '');

  useEffect(() => {
    fetchAchievements();
  }, []);

  function fetchAchievements() {
    setLoading(true);
    api.get('/student/achievements')
      .then((res) => {
        setAchievements(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load achievements', err);
        setLoading(false);
      });
  }

  const filteredAchievements = achievements.filter((a) => {
    // Filter tab
    if (activeTab !== 'ALL' && a.status !== activeTab) {
      return false;
    }
    // Search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchTitle = a.title?.toLowerCase().includes(q);
      const matchCat = a.categoryName?.toLowerCase().includes(q);
      const matchOrg = a.organizer?.toLowerCase().includes(q);
      return matchTitle || matchCat || matchOrg;
    }
    return true;
  });

  return (
    <div className="container" style={{ padding: '36px 16px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
            My Submissions
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Track the verification progress, points, and feedback for all your uploaded achievements.
          </p>
        </div>
        <Link
          to="/student/upload"
          style={{
            backgroundColor: 'var(--navy-900)',
            color: '#fff',
            padding: '10px 18px',
            borderRadius: '6px',
            fontWeight: 600,
            fontSize: '0.9rem',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '6px'
          }}
        >
          <span>+</span> Upload Achievement
        </Link>
      </div>

      {successMessage && (
        <div style={{ marginBottom: '20px' }}>
          <Alert type="success" message={successMessage} onClose={() => setSuccessMessage('')} />
        </div>
      )}

      {/* Filter Tabs & Search Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '20px', backgroundColor: 'var(--surface)', padding: '12px 16px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          {[
            { id: 'ALL', label: 'All' },
            { id: 'PENDING', label: 'Pending Review' },
            { id: 'APPROVED', label: 'Approved' },
            { id: 'NEEDS_CORRECTION', label: 'Action Required' },
            { id: 'REJECTED', label: 'Rejected' }
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                padding: '6px 14px',
                borderRadius: '6px',
                border: '1px solid',
                borderColor: activeTab === tab.id ? 'var(--navy-900)' : 'transparent',
                backgroundColor: activeTab === tab.id ? 'var(--navy-900)' : 'transparent',
                color: activeTab === tab.id ? '#fff' : 'var(--text)',
                fontSize: '0.85rem',
                fontWeight: activeTab === tab.id ? 600 : 500,
                cursor: 'pointer'
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <input
          type="text"
          placeholder="Search by title or category..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          style={{
            padding: '7px 12px',
            borderRadius: '6px',
            border: '1px solid var(--border)',
            fontSize: '0.85rem',
            width: '260px',
            outline: 'none'
          }}
        />
      </div>

      {/* Content */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading your submissions...</p>
        </div>
      ) : filteredAchievements.length === 0 ? (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', padding: '48px 24px', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem', marginBottom: '16px' }}>
            No achievements found for this filter.
          </p>
          <Link
            to="/student/upload"
            style={{ color: 'var(--indigo-600)', fontWeight: 600, fontSize: '0.95rem' }}
          >
            Upload your first achievement →
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {filteredAchievements.map((item) => (
            <div
              key={item.id}
              style={{
                backgroundColor: 'var(--surface)',
                borderRadius: 'var(--radius)',
                border: '1px solid var(--border)',
                padding: '18px 22px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '16px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.02)'
              }}
            >
              <div style={{ flex: 1, minWidth: '260px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
                  <StatusBadge status={item.status} />
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 500 }}>
                    {item.categoryName} • {item.level} Level
                  </span>
                </div>
                <h3 style={{ fontSize: '1.05rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
                  {item.title}
                </h3>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  Organized by <strong>{item.organizer}</strong> • Event Date: {item.eventDate}
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Points</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 700, color: item.status === 'APPROVED' ? 'var(--success)' : 'var(--text-muted)' }}>
                    {item.status === 'APPROVED' ? `+${item.pointsAwarded}` : '0'}
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '8px' }}>
                  <Link
                    to={`/student/achievements/${item.id}`}
                    style={{
                      padding: '8px 14px',
                      borderRadius: '6px',
                      border: '1px solid var(--border)',
                      backgroundColor: 'var(--bg)',
                      color: 'var(--navy-900)',
                      fontSize: '0.85rem',
                      fontWeight: 600
                    }}
                  >
                    View
                  </Link>

                  {(item.status === 'NEEDS_CORRECTION' || item.status === 'REJECTED') && (
                    <Link
                      to={`/student/achievements/${item.id}/edit`}
                      style={{
                        padding: '8px 14px',
                        borderRadius: '6px',
                        backgroundColor: 'var(--warning)',
                        color: '#fff',
                        fontSize: '0.85rem',
                        fontWeight: 600
                      }}
                    >
                      Fix & Resubmit
                    </Link>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
