import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    fetchNotifications();
  }, []);

  function fetchNotifications() {
    setLoading(true);
    api.get('/notifications')
      .then((res) => {
        setNotifications(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load notifications', err);
        setLoading(false);
      });
  }

  function handleMarkAsRead(id) {
    api.patch(`/notifications/${id}/read`)
      .then(() => {
        setNotifications((prev) =>
          prev.map((n) => (n.id === id ? { ...n, read: true } : n))
        );
      })
      .catch((err) => console.error('Failed to mark read', err));
  }

  function handleMarkAllAsRead() {
    api.post('/notifications/read-all')
      .then(() => {
        setNotifications((prev) =>
          prev.map((n) => ({ ...n, read: true }))
        );
        setSuccessMessage('All notifications marked as read.');
      })
      .catch((err) => console.error('Failed to mark all read', err));
  }

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="container" style={{ padding: '36px 16px', maxWidth: '800px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
            Notifications
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Updates regarding your achievement approvals, verification feedback, and points.
          </p>
        </div>

        {unreadCount > 0 && (
          <button
            onClick={handleMarkAllAsRead}
            style={{
              background: 'none',
              border: '1px solid var(--border)',
              borderRadius: '6px',
              padding: '8px 16px',
              fontSize: '0.85rem',
              color: 'var(--indigo-600)',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            ✓ Mark All as Read ({unreadCount})
          </button>
        )}
      </div>

      {successMessage && (
        <div style={{ marginBottom: '20px' }}>
          <Alert type="success" message={successMessage} onClose={() => setSuccessMessage('')} />
        </div>
      )}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner />
          <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading notifications...</p>
        </div>
      ) : notifications.length === 0 ? (
        <div style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius)', border: '1px solid var(--border)', padding: '48px 24px', textAlign: 'center' }}>
          <div style={{ fontSize: '2.5rem', marginBottom: '12px' }}>🔔</div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            You have no notifications right now.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {notifications.map((n) => (
            <div
              key={n.id}
              style={{
                backgroundColor: 'var(--surface)',
                borderRadius: 'var(--radius)',
                border: '1px solid var(--border)',
                borderLeft: n.read ? '1px solid var(--border)' : '4px solid var(--indigo-600)',
                padding: '16px 20px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '12px',
                boxShadow: n.read ? 'none' : '0 1px 3px rgba(0,0,0,0.03)'
              }}
            >
              <div style={{ flex: 1, minWidth: '240px' }}>
                <p style={{ fontSize: '0.95rem', color: 'var(--navy-900)', fontWeight: n.read ? 400 : 600, marginBottom: '4px' }}>
                  {n.message}
                </p>
                <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                  {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                </span>
              </div>

              {!n.read && (
                <button
                  onClick={() => handleMarkAsRead(n.id)}
                  style={{
                    background: 'none',
                    border: '1px solid var(--border)',
                    borderRadius: '4px',
                    padding: '4px 10px',
                    fontSize: '0.8rem',
                    color: 'var(--text-muted)',
                    cursor: 'pointer'
                  }}
                >
                  Mark read
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
