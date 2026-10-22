import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Spinner from '../../components/Spinner';

export default function SearchPage() {
  const [results, setResults] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);

  // filter options from backend
  const [filterOptions, setFilterOptions] = useState({
    branches: [],
    levels: [],
    years: [],
    categories: []
  });

  // filter inputs
  const [keyword, setKeyword] = useState('');
  const [branch, setBranch] = useState('');
  const [year, setYear] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [level, setLevel] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  useEffect(() => {
    // load filter metadata
    api.get('/search/filters')
      .then((res) => setFilterOptions(res.data))
      .catch((err) => console.warn('Failed loading filter options', err));

    performSearch(0);
  }, []);

  function buildParams(page = currentPage) {
    const params = { page };
    if (keyword.trim()) params.keyword = keyword.trim();
    if (branch) params.branch = branch;
    if (year) params.year = year;
    if (categoryId) params.categoryId = categoryId;
    if (level) params.level = level;
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;
    return params;
  }

  function performSearch(page = 0) {
    setLoading(true);
    setCurrentPage(page);
    api.get('/search', { params: buildParams(page) })
      .then((res) => {
        setResults(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setTotalElements(res.data.totalElements || 0);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Search request failed', err);
        setLoading(false);
      });
  }

  function handleFilterSubmit(e) {
    e.preventDefault();
    performSearch(0);
  }

  function handleReset() {
    setKeyword('');
    setBranch('');
    setYear('');
    setCategoryId('');
    setLevel('');
    setFromDate('');
    setToDate('');
    setCurrentPage(0);

    setLoading(true);
    api.get('/search', { params: { page: 0 } })
      .then((res) => {
        setResults(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setTotalElements(res.data.totalElements || 0);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Reset failed', err);
        setLoading(false);
      });
  }

  async function handleExportCsv() {
    setExporting(true);
    try {
      const res = await api.get('/export/csv', {
        params: buildParams(0),
        responseType: 'blob'
      });

      const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'achievements_export.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('CSV export failed', err);
      alert('Failed to export CSV. Please try again.');
    } finally {
      setExporting(false);
    }
  }

  return (
    <div className="container" style={{ padding: '36px 16px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
            Achievement Repository Search
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Search and filter verified college achievements across academic departments.
          </p>
        </div>

        <button
          onClick={handleExportCsv}
          disabled={exporting}
          style={{
            backgroundColor: 'var(--navy-900)',
            color: '#fff',
            padding: '10px 18px',
            borderRadius: '6px',
            border: 'none',
            fontWeight: 600,
            fontSize: '0.85rem',
            cursor: exporting ? 'not-allowed' : 'pointer',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '6px'
          }}
        >
          📥 {exporting ? 'Generating CSV...' : 'Export to CSV'}
        </button>
      </div>

      {/* Filter Form Card */}
      <div style={{ backgroundColor: 'var(--surface)', padding: '20px 24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', marginBottom: '24px' }}>
        <form onSubmit={handleFilterSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                Keyword (Name / Roll No)
              </label>
              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="e.g. Rahul or 21EG105..."
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                Department / Branch
              </label>
              <select
                value={branch}
                onChange={(e) => setBranch(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none', backgroundColor: '#fff' }}
              >
                <option value="">All Branches</option>
                {filterOptions.branches?.map((b) => (
                  <option key={b} value={b}>{b}</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                Academic Year
              </label>
              <select
                value={year}
                onChange={(e) => setYear(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none', backgroundColor: '#fff' }}
              >
                <option value="">All Years</option>
                {filterOptions.years?.map((y) => (
                  <option key={y} value={y}>Year {y}</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                Category
              </label>
              <select
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none', backgroundColor: '#fff' }}
              >
                <option value="">All Categories</option>
                {filterOptions.categories?.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                Level
              </label>
              <select
                value={level}
                onChange={(e) => setLevel(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none', backgroundColor: '#fff' }}
              >
                <option value="">All Levels</option>
                {filterOptions.levels?.map((l) => (
                  <option key={l} value={l}>{l}</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                From Date
              </label>
              <input
                type="date"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                To Date
              </label>
              <input
                type="date"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.85rem', outline: 'none' }}
              />
            </div>
          </div>

          <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '6px' }}>
            <button
              type="button"
              onClick={handleReset}
              style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.85rem' }}
            >
              Reset
            </button>
            <button
              type="submit"
              style={{ padding: '8px 22px', borderRadius: '6px', border: 'none', backgroundColor: 'var(--navy-900)', color: '#fff', fontSize: '0.85rem', fontWeight: 600 }}
            >
              Search
            </button>
          </div>
        </form>
      </div>

      {/* Results Section */}
      <div style={{ marginBottom: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
          Showing <strong>{totalElements}</strong> verified achievements
        </span>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Searching records...</p>
        </div>
      ) : results.length === 0 ? (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', padding: '48px 24px', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>No achievements match the selected filters.</p>
        </div>
      ) : (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', overflow: 'hidden' }}>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--bg)', borderBottom: '1px solid var(--border)', color: 'var(--navy-900)' }}>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Student</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Honor Title</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Category</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Level</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Event Date</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600, textAlign: 'right' }}>Points</th>
                </tr>
              </thead>
              <tbody>
                {results.map((r) => (
                  <tr key={r.id} style={{ borderBottom: '1px solid var(--border)' }}>
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ fontWeight: 600, color: 'var(--navy-900)' }}>{r.studentName}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{r.rollNumber} • {r.branch} Yr {r.year}</div>
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ fontWeight: 600, color: 'var(--navy-900)' }}>{r.title}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Org: {r.organizer}</div>
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ backgroundColor: 'var(--bg)', padding: '3px 8px', borderRadius: '4px', fontSize: '0.8rem' }}>
                        {r.categoryName}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', fontSize: '0.85rem' }}>{r.level}</td>
                    <td style={{ padding: '14px 16px', fontSize: '0.85rem', color: 'var(--text-muted)' }}>{r.eventDate}</td>
                    <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: 700, color: 'var(--success)' }}>
                      +{r.pointsAwarded}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination Bar */}
          {totalPages > 1 && (
            <div style={{ padding: '14px 20px', borderTop: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <button
                disabled={currentPage === 0}
                onClick={() => performSearch(currentPage - 1)}
                style={{ padding: '6px 14px', borderRadius: '4px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.85rem', cursor: currentPage === 0 ? 'not-allowed' : 'pointer' }}
              >
                ← Previous
              </button>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                disabled={currentPage >= totalPages - 1}
                onClick={() => performSearch(currentPage + 1)}
                style={{ padding: '6px 14px', borderRadius: '4px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.85rem', cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer' }}
              >
                Next →
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
