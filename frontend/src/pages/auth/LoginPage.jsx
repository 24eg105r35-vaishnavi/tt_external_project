import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const registeredMessage = new URLSearchParams(location.search).get('registered');

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    setFieldErrors({});
    setGeneralError('');

    api.post('/auth/login', { email, password })
      .then((res) => {
        setLoading(false);
        const { token, role, fullName } = res.data;
        login(token, res.data);

        // redirect based on role
        const cleanRole = role.replace('ROLE_', '');
        if (cleanRole === 'STUDENT') {
          navigate('/student/dashboard');
        } else if (cleanRole === 'FACULTY') {
          navigate('/faculty/queue');
        } else if (cleanRole === 'ADMIN') {
          navigate('/admin/dashboard');
        } else {
          navigate('/');
        }
      })
      .catch((err) => {
        setLoading(false);
        if (err.response && err.response.data) {
          if (err.response.data.errors) {
            setFieldErrors(err.response.data.errors);
          }
          if (err.response.data.message) {
            setGeneralError(err.response.data.message);
          }
        } else {
          setGeneralError('Could not reach the server. Please check your connection.');
        }
      });
  };

  return (
    <div style={{ maxWidth: '440px', margin: '48px auto', padding: '0 16px' }}>
      <div style={{
        backgroundColor: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius)',
        padding: '36px 32px',
        boxShadow: '0 4px 16px rgba(0, 0, 0, 0.04)'
      }}>
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <h2 style={{ color: 'var(--navy-900)', fontSize: '1.6rem', marginBottom: '8px' }}>
            Portal Login
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Enter your credentials to access your portal account
          </p>
        </div>

        {registeredMessage && (
          <Alert type="success" message="Registration successful! Please log in with your credentials." />
        )}

        {generalError && (
          <Alert type="danger" message={generalError} />
        )}

        <form onSubmit={handleSubmit}>
          {/* Email input */}
          <div style={{ marginBottom: '18px' }}>
            <label
              htmlFor="login-email"
              style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text)', marginBottom: '6px' }}
            >
              College Email
            </label>
            <input
              id="login-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. student@college.edu"
              required
              style={{
                width: '100%',
                padding: '10px 14px',
                borderRadius: '6px',
                border: `1px solid ${fieldErrors.email ? 'var(--danger)' : 'var(--border)'}`,
                fontSize: '0.95rem',
                outline: 'none',
                boxSizing: 'border-box'
              }}
            />
            {fieldErrors.email && (
              <span style={{ display: 'block', color: 'var(--danger)', fontSize: '0.8rem', marginTop: '4px' }}>
                {fieldErrors.email}
              </span>
            )}
          </div>

          {/* Password input with show/hide */}
          <div style={{ marginBottom: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
              <label
                htmlFor="login-password"
                style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text)' }}
              >
                Password
              </label>
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--indigo-600)',
                  fontSize: '0.8rem',
                  cursor: 'pointer',
                  padding: 0
                }}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>
            <input
              id="login-password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
              style={{
                width: '100%',
                padding: '10px 14px',
                borderRadius: '6px',
                border: `1px solid ${fieldErrors.password ? 'var(--danger)' : 'var(--border)'}`,
                fontSize: '0.95rem',
                outline: 'none',
                boxSizing: 'border-box'
              }}
            />
            {fieldErrors.password && (
              <span style={{ display: 'block', color: 'var(--danger)', fontSize: '0.8rem', marginTop: '4px' }}>
                {fieldErrors.password}
              </span>
            )}
          </div>

          {/* Submit button */}
          <button
            type="submit"
            disabled={loading}
            style={{
              width: '100%',
              backgroundColor: 'var(--navy-900)',
              color: '#ffffff',
              border: 'none',
              borderRadius: '6px',
              padding: '12px',
              fontSize: '0.95rem',
              fontWeight: 600,
              cursor: loading ? 'not-allowed' : 'pointer',
              transition: 'var(--transition)'
            }}
          >
            {loading ? 'Logging in...' : 'Log In'}
          </button>
        </form>

        <div style={{ marginTop: '20px', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ color: 'var(--indigo-600)', fontWeight: 600, textDecoration: 'none' }}>
            Register as Student
          </Link>
        </div>
      </div>
    </div>
  );
}
