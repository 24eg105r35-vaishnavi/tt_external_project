import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // modal state
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState('');

  // form state
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [points, setPoints] = useState(10);
  const [verifierDepartment, setVerifierDepartment] = useState('ALL');
  const [fieldSchema, setFieldSchema] = useState('');

  useEffect(() => {
    fetchCategories();
    api.get('/admin/categories/departments')
      .then((res) => setDepartments(res.data))
      .catch((err) => console.warn('Could not load departments', err));
  }, []);

  function fetchCategories() {
    setLoading(true);
    api.get('/admin/categories')
      .then((res) => {
        setCategories(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load categories', err);
        setLoading(false);
      });
  }

  function handleToggleStatus(id) {
    api.patch(`/admin/categories/${id}/toggle`)
      .then(() => {
        setCategories((prev) =>
          prev.map((c) => (c.id === id ? { ...c, active: !c.active } : c))
        );
        setSuccessMessage('Category active status updated.');
      })
      .catch((err) => {
        console.error('Failed to toggle category', err);
        setErrorMessage('Failed to toggle category status.');
      });
  }

  function openCreateModal() {
    setName('');
    setDescription('');
    setPoints(10);
    setVerifierDepartment('ALL');
    setFieldSchema('');
    setModalError('');
    setShowModal(true);
  }

  function applyPreset(presetType) {
    if (presetType === 'competition') {
      setFieldSchema(JSON.stringify([
        { name: 'rank', label: 'Rank / Standing', type: 'select', options: ['1st Place', '2nd Place', '3rd Place', 'Consolation', 'Participant'], required: true },
        { name: 'teamSize', label: 'Team Size', type: 'number', required: false }
      ], null, 2));
    } else if (presetType === 'publication') {
      setFieldSchema(JSON.stringify([
        { name: 'paperTitle', label: 'Paper Title', type: 'text', required: true },
        { name: 'journalConference', label: 'Journal or Conference Name', type: 'text', required: true },
        { name: 'issnIsbn', label: 'ISSN / ISBN / DOI', type: 'text', required: false }
      ], null, 2));
    } else if (presetType === 'sports') {
      setFieldSchema(JSON.stringify([
        { name: 'sportName', label: 'Sport / Discipline', type: 'text', required: true },
        { name: 'medal', label: 'Medal Won', type: 'select', options: ['Gold', 'Silver', 'Bronze', 'Participation'], required: true }
      ], null, 2));
    }
  }

  async function handleCreateCategory(e) {
    e.preventDefault();
    setSubmitting(true);
    setModalError('');

    try {
      await api.post('/admin/categories', {
        name,
        description,
        points: parseInt(points, 10),
        verifierDepartment,
        fieldSchema: fieldSchema ? fieldSchema.trim() : null,
        active: true
      });

      setShowModal(false);
      setSuccessMessage(`Category '${name}' created successfully.`);
      fetchCategories();
    } catch (err) {
      console.error('Failed to create category', err);
      setModalError(err.response?.data?.message || 'Failed to create category. Please ensure field schema is valid JSON.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="container" style={{ padding: '36px 16px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
            Category Management
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Define achievement criteria, point allocations, and department verification routing.
          </p>
        </div>

        <button
          onClick={openCreateModal}
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
          + Add New Category
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

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading categories...</p>
        </div>
      ) : (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ backgroundColor: 'var(--bg)', borderBottom: '1px solid var(--border)', color: 'var(--navy-900)' }}>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Category Name</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Description</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Points</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Verifier Dept</th>
                <th style={{ padding: '12px 16px', fontWeight: 600 }}>Status</th>
                <th style={{ padding: '12px 16px', fontWeight: 600, textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '14px 16px', fontWeight: 600, color: 'var(--navy-900)' }}>
                    {c.name}
                  </td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)', fontSize: '0.85rem', maxWidth: '280px' }}>
                    {c.description || '—'}
                  </td>
                  <td style={{ padding: '14px 16px', fontWeight: 700, color: 'var(--success)' }}>
                    +{c.points} Pts
                  </td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{ backgroundColor: 'var(--bg)', padding: '3px 8px', borderRadius: '4px', fontSize: '0.8rem' }}>
                      {c.verifierDepartment || 'ALL'}
                    </span>
                  </td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{
                      padding: '3px 8px',
                      borderRadius: '999px',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      backgroundColor: c.active ? '#edf7f2' : '#f4f5f7',
                      color: c.active ? 'var(--success)' : 'var(--text-muted)',
                      border: `1px solid ${c.active ? '#c6ebd6' : '#dfe1e6'}`
                    }}>
                      {c.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                    <button
                      onClick={() => handleToggleStatus(c.id)}
                      style={{
                        padding: '5px 12px',
                        borderRadius: '4px',
                        border: '1px solid var(--border)',
                        backgroundColor: '#fff',
                        fontSize: '0.8rem',
                        cursor: 'pointer',
                        color: c.active ? 'var(--danger)' : 'var(--success)'
                      }}
                    >
                      {c.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Create Modal */}
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
          <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', maxWidth: '580px', width: '100%', padding: '28px', maxHeight: '90vh', overflowY: 'auto' }}>
            <h2 style={{ fontSize: '1.3rem', color: 'var(--navy-900)', marginBottom: '16px' }}>
              Add New Achievement Category
            </h2>

            {modalError && (
              <div style={{ marginBottom: '16px' }}>
                <Alert type="danger" message={modalError} />
              </div>
            )}

            <form onSubmit={handleCreateCategory} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                  Category Name *
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Technical Paper Publication"
                  required
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                  Description (Optional)
                </label>
                <textarea
                  rows="2"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Brief description of eligibility or scope..."
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                    Points Awarded *
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="500"
                    value={points}
                    onChange={(e) => setPoints(e.target.value)}
                    required
                    style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none' }}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '4px' }}>
                    Verifier Department *
                  </label>
                  <select
                    value={verifierDepartment}
                    onChange={(e) => setVerifierDepartment(e.target.value)}
                    style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.9rem', outline: 'none', backgroundColor: '#fff' }}
                  >
                    <option value="ALL">ALL (Any Faculty)</option>
                    {departments.filter((d) => d !== 'ALL').map((d) => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                  <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)' }}>
                    Field Schema JSON (Optional)
                  </label>
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <button
                      type="button"
                      onClick={() => applyPreset('competition')}
                      style={{ fontSize: '0.75rem', padding: '2px 8px', borderRadius: '4px', border: '1px solid var(--border)', background: '#fff', cursor: 'pointer' }}
                    >
                      + Competition Preset
                    </button>
                    <button
                      type="button"
                      onClick={() => applyPreset('publication')}
                      style={{ fontSize: '0.75rem', padding: '2px 8px', borderRadius: '4px', border: '1px solid var(--border)', background: '#fff', cursor: 'pointer' }}
                    >
                      + Paper Preset
                    </button>
                    <button
                      type="button"
                      onClick={() => applyPreset('sports')}
                      style={{ fontSize: '0.75rem', padding: '2px 8px', borderRadius: '4px', border: '1px solid var(--border)', background: '#fff', cursor: 'pointer' }}
                    >
                      + Sports Preset
                    </button>
                  </div>
                </div>
                <textarea
                  rows="4"
                  value={fieldSchema}
                  onChange={(e) => setFieldSchema(e.target.value)}
                  placeholder='e.g. [{"name":"rank","label":"Rank","type":"text","required":true}]'
                  style={{ width: '100%', fontFamily: 'monospace', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.8rem', outline: 'none' }}
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
                  {submitting ? 'Creating...' : 'Create Category'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
