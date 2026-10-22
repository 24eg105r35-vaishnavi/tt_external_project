import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import api from '../../api/axios';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function FacultyQueuePage() {
  const location = useLocation();
  const [queue, setQueue] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // filter states
  const [selectedBranch, setSelectedBranch] = useState('');
  const [selectedYear, setSelectedYear] = useState('');
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  const [successMessage, setSuccessMessage] = useState(location.state?.successMessage || '');

  const branches = ['CSE', 'ECE', 'EEE', 'MECH', 'CIVIL', 'IT', 'AI&DS'];
  const years = [1, 2, 3, 4];

  useEffect(() => {
    // load active categories for filter dropdown
    api.get('/student/categories')
      .then((res) => setCategories(res.data))
      .catch((err) => console.warn('Could not load categories for queue filter', err));
  }, []);

  useEffect(() => {
    fetchQueue();
  }, [selectedBranch, selectedYear, selectedCategoryId]);

  function fetchQueue() {
    setLoading(true);
    const params = {};
    if (selectedBranch) params.branch = selectedBranch;
    if (selectedYear) params.year = selectedYear;
    if (selectedCategoryId) params.categoryId = selectedCategoryId;

    api.get('/faculty/queue', { params })
      .then((res) => {
        setQueue(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load faculty verification queue', err);
        setLoading(false);
      });
  }

  return (
    <div className="container" style={{ padding: '36px 16px' }}>
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '6px' }}>
          Department Verification Queue
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Review and verify student submitted certificates, hackathon honors, and extracurricular achievements.
        </p>
      </div>

      {successMessage && (
        <div style={{ marginBottom: '20px' }}>
          <Alert type="success" message={successMessage} onClose={() => setSuccessMessage('')} />
        </div>
      )}

      {/* Filter Bar */}
      <div style={{
        backgroundColor: 'var(--surface)',
        borderRadius: 'var(--radius)',
        border: '1px solid var(--border)',
        padding: '16px 20px',
        marginBottom: '20px',
        display: 'flex',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '14px'
      }}>
        <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)' }}>
          Filter Queue:
        </span>

        <select
          value={selectedBranch}
          onChange={(e) => setSelectedBranch(e.target.value)}
          style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none' }}
        >
          <option value="">All Branches</option>
          {branches.map((b) => (
            <option key={b} value={b}>{b}</option>
          ))}
        </select>

        <select
          value={selectedYear}
          onChange={(e) => setSelectedYear(e.target.value)}
          style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none' }}
        >
          <option value="">All Academic Years</option>
          {years.map((y) => (
            <option key={y} value={y}>Year {y}</option>
          ))}
        </select>

        <select
          value={selectedCategoryId}
          onChange={(e) => setSelectedCategoryId(e.target.value)}
          style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none' }}
        >
          <option value="">All Categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>

        {(selectedBranch || selectedYear || selectedCategoryId) && (
          <button
            onClick={() => { setSelectedBranch(''); setSelectedYear(''); setSelectedCategoryId(''); }}
            style={{ background: 'none', border: 'none', color: 'var(--danger)', fontSize: '0.85rem', cursor: 'pointer', textDecoration: 'underline' }}
          >
            Clear Filters
          </button>
        )}

        <div style={{ marginLeft: 'auto', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          <strong>{queue.length}</strong> items awaiting verification
        </div>
      </div>

      {/* Queue List Table */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading queue items...</p>
        </div>
      ) : queue.length === 0 ? (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', padding: '48px 24px', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>
            No pending submissions matching the selected filters.
          </p>
        </div>
      ) : (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', overflow: 'hidden' }}>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--bg)', borderBottom: '1px solid var(--border)', color: 'var(--navy-900)' }}>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Student</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Achievement Title</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Category</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Level</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Event Date</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600, textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {queue.map((item) => (
                  <tr key={item.id} style={{ borderBottom: '1px solid var(--border)' }}>
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ fontWeight: 600, color: 'var(--navy-900)' }}>{item.studentName || 'Student'}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        {item.rollNumber} • {item.branch} Yr {item.year}
                      </div>
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ fontWeight: 600, color: 'var(--navy-900)' }}>{item.title}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Org: {item.organizer}</div>
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ backgroundColor: 'var(--bg)', padding: '4px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 500 }}>
                        {item.categoryName}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', fontSize: '0.85rem' }}>
                      {item.level}
                    </td>
                    <td style={{ padding: '14px 16px', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                      {item.eventDate}
                    </td>
                    <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                      <Link
                        to={`/faculty/review/${item.id}`}
                        style={{
                          display: 'inline-block',
                          backgroundColor: 'var(--navy-900)',
                          color: '#fff',
                          padding: '7px 14px',
                          borderRadius: '6px',
                          fontWeight: 600,
                          fontSize: '0.85rem'
                        }}
                      >
                        Review Claim
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
