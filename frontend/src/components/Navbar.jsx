import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const role = user?.role ? user.role.replace('ROLE_', '') : null;

  useEffect(() => {
    if (user) {
      api.get('/notifications/unread-count')
        .then((res) => {
          setUnreadCount(res.data.unreadCount || 0);
        })
        .catch(() => {});
    } else {
      setUnreadCount(0);
    }
  }, [user, location.pathname]);

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <nav style={{ backgroundColor: 'var(--surface)', borderBottom: '1px solid var(--border)', boxShadow: '0 1px 3px rgba(0,0,0,0.03)' }}>
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', height: '64px' }}>
        <Link to="/" style={{ fontWeight: 700, fontSize: '1.15rem', color: 'var(--navy-900)', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span>🎓</span> Achievement Portal
        </Link>

        {/* Desktop Navigation Links */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <Link to="/" style={{ fontWeight: 500 }}>Home</Link>

          {!user && (
            <>
              <Link to="/leaderboard" style={{ fontWeight: 500 }}>Leaderboard</Link>
              <Link to="/analytics" style={{ fontWeight: 500 }}>Analytics</Link>
              <Link to="/login" style={{ fontWeight: 500 }}>Login</Link>
              <Link to="/register" style={{ fontWeight: 500, backgroundColor: 'var(--indigo-600)', color: '#fff', padding: '6px 14px', borderRadius: '6px' }}>Register</Link>
            </>
          )}

          {role === 'STUDENT' && (
            <>
              <Link to="/student/dashboard" style={{ fontWeight: 500 }}>Dashboard</Link>
              <Link to="/student/achievements" style={{ fontWeight: 500 }}>My Uploads</Link>
              <Link to="/student/upload" style={{ fontWeight: 500, color: 'var(--indigo-600)' }}>+ Upload</Link>
              <Link to="/student/profile" style={{ fontWeight: 500 }}>Profile</Link>
              <Link to="/search" style={{ fontWeight: 500 }}>Search</Link>
            </>
          )}

          {role === 'FACULTY' && (
            <>
              <Link to="/faculty/dashboard" style={{ fontWeight: 500 }}>Dashboard</Link>
              <Link to="/faculty/queue" style={{ fontWeight: 500 }}>Queue</Link>
              <Link to="/search" style={{ fontWeight: 500 }}>Search</Link>
              <Link to="/analytics" style={{ fontWeight: 500 }}>Analytics</Link>
            </>
          )}

          {role === 'ADMIN' && (
            <>
              <Link to="/admin/dashboard" style={{ fontWeight: 500 }}>Admin Console</Link>
              <Link to="/admin/categories" style={{ fontWeight: 500 }}>Categories</Link>
              <Link to="/admin/users" style={{ fontWeight: 500 }}>Users</Link>
              <Link to="/admin/audit" style={{ fontWeight: 500 }}>Audit Logs</Link>
              <Link to="/search" style={{ fontWeight: 500 }}>Search</Link>
              <Link to="/analytics" style={{ fontWeight: 500 }}>Analytics</Link>
            </>
          )}

          {user && (
            <>
              <Link to="/leaderboard" style={{ fontWeight: 500 }}>Leaderboard</Link>
              <Link to="/notifications" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 500 }}>
                <span>Notifications</span>
                {unreadCount > 0 && (
                  <span style={{
                    backgroundColor: 'var(--danger)',
                    color: '#fff',
                    fontSize: '0.72rem',
                    fontWeight: 700,
                    padding: '1px 6px',
                    borderRadius: '999px',
                    lineHeight: '1.2'
                  }}>
                    {unreadCount}
                  </span>
                )}
              </Link>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>({user.fullName || user.email})</span>
              <button
                onClick={handleLogout}
                style={{ background: 'none', border: '1px solid var(--border)', color: 'var(--danger)', padding: '5px 12px', borderRadius: '6px', fontSize: '0.85rem' }}
              >
                Logout
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
