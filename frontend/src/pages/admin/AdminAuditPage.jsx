import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Spinner from '../../components/Spinner';

export default function AdminAuditPage() {
  const [logs, setLogs] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLogs(0);
  }, []);

  function fetchLogs(page = 0) {
    setLoading(true);
    setCurrentPage(page);
    api.get('/admin/audit-logs', { params: { page } })
      .then((res) => {
        setLogs(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load audit logs', err);
        setLoading(false);
      });
  }

  function getActionColor(action) {
    if (action.includes('APPROVED')) return 'var(--success)';
    if (action.includes('REJECT') || action.includes('DISABLED')) return 'var(--danger)';
    if (action.includes('CREATED')) return 'var(--indigo-600)';
    if (action.includes('CORRECTION') || action.includes('SEND_BACK')) return 'var(--warning)';
    return 'var(--navy-900)';
  }

  return (
    <div className="container" style={{ padding: '36px 16px' }}>
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
          System Audit Trail
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Immutable activity log of user account modifications, category updates, and verification decisions.
        </p>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading audit trail...</p>
        </div>
      ) : logs.length === 0 ? (
        <div style={{ backgroundColor: 'var(--surface)', padding: '40px', textAlign: 'center', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <p style={{ color: 'var(--text-muted)' }}>No audit events recorded yet.</p>
        </div>
      ) : (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', overflow: 'hidden' }}>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--bg)', borderBottom: '1px solid var(--border)', color: 'var(--navy-900)' }}>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Timestamp</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Action</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Performed By</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Target Entity</th>
                  <th style={{ padding: '12px 16px', fontWeight: 600 }}>Details / Value Changes</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.id} style={{ borderBottom: '1px solid var(--border)' }}>
                    <td style={{ padding: '12px 16px', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                      {log.createdAt ? new Date(log.createdAt).toLocaleString() : '—'}
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <span style={{
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontWeight: 600,
                        fontSize: '0.75rem',
                        backgroundColor: 'var(--bg)',
                        color: getActionColor(log.action || '')
                      }}>
                        {log.action}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px', fontWeight: 600, color: 'var(--navy-900)' }}>
                      {log.performedBy || 'System'}
                    </td>
                    <td style={{ padding: '12px 16px', color: 'var(--text-muted)' }}>
                      {log.entityName} {log.entityId ? `#${log.entityId}` : ''}
                    </td>
                    <td style={{ padding: '12px 16px', maxWidth: '380px', color: 'var(--text)' }}>
                      {log.oldValue && (
                        <div style={{ fontSize: '0.78rem', color: 'var(--danger)', textDecoration: 'line-through' }}>
                          {log.oldValue}
                        </div>
                      )}
                      <div style={{ fontSize: '0.8rem', color: log.oldValue ? 'var(--success)' : 'var(--text)' }}>
                        {log.newValue || '—'}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div style={{ padding: '14px 20px', borderTop: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <button
                disabled={currentPage === 0}
                onClick={() => fetchLogs(currentPage - 1)}
                style={{ padding: '6px 14px', borderRadius: '4px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.85rem' }}
              >
                ← Previous
              </button>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                disabled={currentPage >= totalPages - 1}
                onClick={() => fetchLogs(currentPage + 1)}
                style={{ padding: '6px 14px', borderRadius: '4px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.85rem' }}
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
