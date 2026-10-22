import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);
  const navigate = useNavigate();

  // read user from localStorage if logged in
  const storedUser = localStorage.getItem('user');
  const user = storedUser ? JSON.parse(storedUser) : null;
  const role = user ? user.role : null;

  function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  }

  return (
    <nav style={{ backgroundColor: 'var(--surface)', borderBottom: '1px solid var(--border)', boxShadow: '0 1px 3px rgba(0,0,0,0.03)' }}>
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', height: '64px' }}>
        <Link to="/" style={{ fontWeight: 700, fontSize: '1.15rem', color: 'var(--navy-900)', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span>🎓</span> Achievement Portal
        </Link>

        {/* Mobile menu toggle button */}
        <button
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="Toggle navigation"
          style={{ display: 'none', background: 'none', border: '1px solid var(--border)', borderRadius: '6px', padding: '6px 10px', fontSize: '1.1rem' }}
          className="mobile-toggle"
        >
          ☰
        </button>

        {/* Desktop Navigation Links */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <Link to="/" style={{ fontWeight: 500 }}>Home</Link>

          {!user && (
            <>
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
              <Link to="/search" style={{ fontWeight: 500 }}>Search</Link>
              <Link to="/analytics" style={{ fontWeight: 500 }}>Analytics</Link>
            </>
          )}

          {user && (
            <>
              <Link to="/leaderboard" style={{ fontWeight: 500 }}>Leaderboard</Link>
              <Link to="/notifications" style={{ display: 'flex', alignItems: 'center', gap: '4px', fontWeight: 500 }}>
                Notifications
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
