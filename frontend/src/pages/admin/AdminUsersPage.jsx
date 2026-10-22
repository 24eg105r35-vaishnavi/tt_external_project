import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const [departments, setDepartments] = useState([]);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // modal state
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState('');

  // faculty form state
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [department, setDepartment] = useState('CSE');
  const [designation, setDesignation] = useState('Assistant Professor');
  const [password, setPassword] = useState('');

  useEffect(() => {
    fetchUsers(0);
    api.get('/admin/users/departments')
      .then((res) => setDepartments(res.data))
      .catch((err) => console.warn('Could not load departments', err));
  }, []);

  function fetchUsers(page = currentPage, search = keyword) {
    setLoading(true);
    setCurrentPage(page);
    const params = { page };
    if (search.trim()) params.keyword = search.trim();

    api.get('/admin/users', { params })
      .then((res) => {
        setUsers(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load users', err);
        setLoading(false);
      });
  }

  function handleSearchSubmit(e) {
    e.preventDefault();
    fetchUsers(0, keyword);
  }

  function handleToggleUser(userId) {
    api.patch(`/admin/users/${userId}/toggle-status`)
      .then(() => {
        setUsers((prev) =>
          prev.map((u) => (u.id === userId ? { ...u, enabled: !u.enabled } : u))
        );
        setSuccessMessage('User account status updated.');
      })
      .catch((err) => {
        console.error('Failed to toggle user status', err);
        setErrorMessage(err.response?.data?.message || 'Failed to toggle user status.');
      });
  }

  function openFacultyModal() {
    setFullName('');
    setEmail('');
    setDepartment('CSE');
    setDesignation('Assistant Professor');
    setPassword('faculty123');
    setModalError('');
    setShowModal(true);
  }

  async function handleCreateFaculty(e) {
    e.preventDefault();
    setSubmitting(true);
    setModalError('');

    try {
      await api.post('/admin/users/faculty', {
        fullName,
        email,
        department,
        designation,
        password
      });

      setShowModal(false);
      setSuccessMessage(`Faculty user '${fullName}' onboarded successfully.`);
      fetchUsers(0, keyword);
    } catch (err) {
      console.error('Failed to create faculty user', err);
      setModalError(err.response?.data?.message || 'Failed to onboard faculty user.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="container" style={{ padding: '36px 16px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
            User Account Management
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            View system accounts, manage active privileges, and onboard faculty verifiers.
          </p>
        </div>

        <button
          onClick={openFacultyModal}
          style={{
            backgroundColor: 'var(--navy-900)',
            color: '#fff',
            padding: '10px 18px',
            borderRadius: '6px',
            border: 'none',
            fontWeight: 600,
            fontSize: '0.85rem',
            cursor: 'pointer'
          }}
        >
          + Onboard Faculty Verifier
        </button>
      </div>

      {successMessage && (
        <div style={{ marginBottom: '20px' }}>
          <Alert type="success" message={successMessage} onClose={() => setSuccessMessage('')} />
        </div>
      )}

      {errorMessage && (
        <div style={{ marginBottom: '20px' }}>
          <Alert type="danger" message={errorMessage} onClose={() => setErrorMessage('')} />
        </div>
      )}

      {/* Search Bar */}
      <div style={{ backgroundColor: 'var(--surface)', padding: '14px 20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', marginBottom: '20px' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '10px' }}>
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Search users by name or email..."
            style={{ flex: 1, padding: '8px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none' }}
          />
          <button
            type="submit"
            style={{ backgroundColor: 'var(--navy-900)', color: '#fff', padding: '8px 18px', borderRadius: '6px', border: 'none', fontWeight: 600, fontSize: '0.85rem' }}
          >
            Search
          </button>
          {keyword && (
            <button
              type="button"
              onClick={() => { setKeyword(''); fetchUsers(0, ''); }}
              style={{ backgroundColor: '#fff', border: '1px solid var(--border)', padding: '8px 14px', borderRadius: '6px', fontSize: '0.85rem' }}
            >
              Clear
            </button>
          )}
        </form>
      </div>

      {/* Users Table */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading accounts...</p>
        </div>
      ) : (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ backgroundColor: 'var(--bg)', borderBottom: '1px solid var(--border)', color: 'var(--navy-900)' }}>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Full Name</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Email Address</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>System Role</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Account Status</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Registered</th>
                <th style={{ padding: '12px 16px', fontWeight: 600, textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '14px 16px', fontWeight: 600, color: 'var(--navy-900)' }}>
                    {u.fullName}
                  </td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>
                    {u.email}
                  </td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{
                      padding: '3px 8px',
                      borderRadius: '4px',
                      fontSize: '0.78rem',
                      fontWeight: 600,
                      backgroundColor: u.role === 'ADMIN' ? '#fdf2f2' : u.role === 'FACULTY' ? '#e8ebf7' : '#edf7f2',
                      color: u.role === 'ADMIN' ? 'var(--danger)' : u.role === 'FACULTY' ? 'var(--indigo-600)' : 'var(--success)'
                    }}>
                      {u.role}
                    </span>
                  </td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{
                      padding: '3px 8px',
                      borderRadius: '999px',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      backgroundColor: u.enabled ? '#edf7f2' : '#fdf2f2',
                      color: u.enabled ? 'var(--success)' : 'var(--danger)',
                      border: `1px solid ${u.enabled ? '#c6ebd6' : '#f7caca'}`
                    }}>
                      {u.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                  <td style={{ padding: '14px 16px', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                  </td>
                  <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                    <button
                      onClick={() => handleToggleUser(u.id)}
                      style={{
                        padding: '5px 12px',
                        borderRadius: '4px',
                        border: '1px solid var(--border)',
                        backgroundColor: '#fff',
                        fontSize: '0.8rem',
                        cursor: 'pointer',
                        color: u.enabled ? 'var(--danger)' : 'var(--success)'
                      }}
                    >
                      {u.enabled ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div style={{ padding: '14px 20px', borderTop: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <button
                disabled={currentPage === 0}
                onClick={() => fetchUsers(currentPage - 1)}
                style={{ padding: '6px 14px', borderRadius: '4px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.85rem' }}
              >
                ← Previous
              </button>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                disabled={currentPage >= totalPages - 1}
                onClick={() => fetchUsers(currentPage + 1)}
                style={{ padding: '6px 14px', borderRadius: '4px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.85rem' }}
              >
                Next →
              </button>
            </div>
          )}
        </div>
      )}

      {/* Onboard Faculty Modal */}
      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.4)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '16px'
        }}>
          <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', maxWidth: '520px', width: '100%', padding: '28px' }}>
            <h2 style={{ fontSize: '1.3rem', color: 'var(--navy-900)', marginBottom: '16px' }}>
              Onboard Faculty Verifier
            </h2>

            {modalError && (
              <div style={{ marginBottom: '16px' }}>
                <Alert type="danger" message={modalError} />
              </div>
            )}

            <form onSubmit={handleCreateFaculty} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                  Faculty Full Name *
                </label>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="e.g. Dr. Rajesh Kumar"
                  required
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                  College Email *
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="e.g. rajesh@college.edu"
                  required
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                    Department *
                  </label>
                  <select
                    value={department}
                    onChange={(e) => setDepartment(e.target.value)}
                    style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none', backgroundColor: '#fff' }}
                  >
                    {departments.map((d) => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                    Designation *
                  </label>
                  <input
                    type="text"
                    value={designation}
                    onChange={(e) => setDesignation(e.target.value)}
                    placeholder="Assistant Professor"
                    required
                    style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none' }}
                  />
                </div>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                  Temporary Password *
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '8px' }}>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.85rem' }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  style={{
                    backgroundColor: 'var(--navy-900)',
                    color: '#fff',
                    padding: '8px 20px',
                    borderRadius: '6px',
                    border: 'none',
                    fontWeight: 600,
                    fontSize: '0.85rem',
                    cursor: submitting ? 'not-allowed' : 'pointer'
                  }}
                >
                  {submitting ? 'Onboarding...' : 'Onboard Faculty'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
