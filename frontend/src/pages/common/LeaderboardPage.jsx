import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Spinner from '../../components/Spinner';

export default function LeaderboardPage() {
  const [leaderboard, setLeaderboard] = useState([]);
  const [branches, setBranches] = useState([]);
  const [years, setYears] = useState([]);
  const [selectedBranch, setSelectedBranch] = useState('');
  const [selectedYear, setSelectedYear] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLeaderboard();
  }, [selectedBranch, selectedYear]);

  function fetchLeaderboard() {
    setLoading(true);
    const params = {};
    if (selectedBranch) params.branch = selectedBranch;
    if (selectedYear) params.year = selectedYear;

    api.get('/leaderboard', { params })
      .then((res) => {
        setLeaderboard(res.data.leaderboard || []);
        setBranches(res.data.branches || []);
        setYears(res.data.years || []);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load leaderboard', err);
        setLoading(false);
      });
  }

  function getMedalBadge(rank) {
    if (rank === 1) {
      return (
        <span style={{
          backgroundColor: '#fef3c7',
          color: '#92400e',
          border: '1px solid #fde68a',
          fontWeight: 700,
          padding: '3px 9px',
          borderRadius: '999px',
          fontSize: '0.8rem'
        }}>
          🥇 1st
        </span>
      );
    }
    if (rank === 2) {
      return (
        <span style={{
          backgroundColor: '#f3f4f6',
          color: '#374151',
          border: '1px solid #e5e7eb',
          fontWeight: 700,
          padding: '3px 9px',
          borderRadius: '999px',
          fontSize: '0.8rem'
        }}>
          🥈 2nd
        </span>
      );
    }
    if (rank === 3) {
      return (
        <span style={{
          backgroundColor: '#ffedd5',
          color: '#9a3412',
          border: '1px solid #fed7aa',
          fontWeight: 700,
          padding: '3px 9px',
          borderRadius: '999px',
          fontSize: '0.8rem'
        }}>
          🥉 3rd
        </span>
      );
    }
    return (
      <span style={{ color: 'var(--text-muted)', fontWeight: 600, fontSize: '0.9rem', padding: '0 6px' }}>
        #{rank}
      </span>
    );
  }

  return (
    <div className="container" style={{ padding: '36px 16px', maxWidth: '900px' }}>
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
          Campus Achievement Leaderboard
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Top performing student achievers ranked by verified department points.
        </p>
      </div>

      {/* Filter Bar */}
      <div style={{
        backgroundColor: 'var(--surface)',
        borderRadius: 'var(--radius)',
        border: '1px solid var(--border)',
        padding: '14px 20px',
        marginBottom: '20px',
        display: 'flex',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '12px'
      }}>
        <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)' }}>
          Filter Ranking:
        </span>

        <select
          value={selectedBranch}
          onChange={(e) => setSelectedBranch(e.target.value)}
          style={{ padding: '7px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none', backgroundColor: '#fff' }}
        >
          <option value="">All Departments</option>
          {branches.map((b) => (
            <option key={b} value={b}>{b}</option>
          ))}
        </select>

        <select
          value={selectedYear}
          onChange={(e) => setSelectedYear(e.target.value)}
          style={{ padding: '7px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none', backgroundColor: '#fff' }}
        >
          <option value="">All Academic Years</option>
          {years.map((y) => (
            <option key={y} value={y}>Year {y}</option>
          ))}
        </select>

        {(selectedBranch || selectedYear) && (
          <button
            onClick={() => { setSelectedBranch(''); setSelectedYear(''); }}
            style={{ background: 'none', border: 'none', color: 'var(--danger)', fontSize: '0.85rem', cursor: 'pointer', textDecoration: 'underline' }}
          >
            Clear Filters
          </button>
        )}
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Calculating rankings...</p>
        </div>
      ) : leaderboard.length === 0 ? (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', padding: '48px 24px', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-muted)' }}>No student rankings available for this selection.</p>
        </div>
      ) : (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ backgroundColor: 'var(--bg)', borderBottom: '1px solid var(--border)', color: 'var(--navy-900)' }}>
                <th style={{ padding: '12px 16px', fontWeight: 600, width: '90px' }}>Rank</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Achiever Name</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Branch & Year</th>
                <th style={{ padding: '12px 16px', fontWeight: 600, textAlign: 'center' }}>Approved Honors</th>
                <th style={{ padding: '12px 16px', fontWeight: 600, textAlign: 'right' }}>Verified Points</th>
              </tr>
            </thead>
            <tbody>
              {leaderboard.map((row) => (
                <tr
                  key={row.rank}
                  style={{
                    borderBottom: '1px solid var(--border)',
                    backgroundColor: row.rank <= 3 ? '#fafaf7' : '#fff'
                  }}
                >
                  <td style={{ padding: '14px 16px' }}>
                    {getMedalBadge(row.rank)}
                  </td>
                  <td style={{ padding: '14px 16px', fontWeight: 600, color: 'var(--navy-900)' }}>
                    {row.fullName}
                  </td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>
                    {row.branch} • Year {row.year}
                  </td>
                  <td style={{ padding: '14px 16px', textAlign: 'center', color: 'var(--text)' }}>
                    {row.achievementCount}
                  </td>
                  <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: 700, fontSize: '1.05rem', color: 'var(--navy-900)' }}>
                    {row.totalPoints} pts
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
