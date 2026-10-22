import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import Alert from '../../components/Alert';

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    rollNumber: '',
    branch: 'CSE',
    year: 1,
    phone: '',
    password: '',
    confirmPassword: ''
  });

  const [showPassword, setShowPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'year' ? parseInt(value, 10) : value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    setFieldErrors({});
    setGeneralError('');

    // client check for password match
    if (formData.password !== formData.confirmPassword) {
      setGeneralError('Passwords do not match.');
      setLoading(false);
      return;
    }

    api.post('/auth/register', formData)
      .then(() => {
        setLoading(false);
        navigate('/login?registered=true');
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
          setGeneralError('Registration failed. Please check your network connection.');
        }
      });
  };

  return (
    <div style={{ maxWidth: '600px', margin: '40px auto', padding: '0 16px' }}>
      <div style={{
        backgroundColor: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius)',
        padding: '36px 32px',
        boxShadow: '0 4px 16px rgba(0, 0, 0, 0.04)'
      }}>
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <h2 style={{ color: 'var(--navy-900)', fontSize: '1.6rem', marginBottom: '8px' }}>
            Student Registration
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Create your account using your official college email
          </p>
        </div>

        {generalError && (
          <Alert type="danger" message={generalError} />
        )}

        <form onSubmit={handleSubmit}>
          {/* Full Name */}
          <div style={{ marginBottom: '16px' }}>
            <label htmlFor="reg-name" style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
              Full Name
            </label>
            <input
              id="reg-name"
              type="text"
              name="fullName"
              value={formData.fullName}
              onChange={handleChange}
              placeholder="e.g. John Doe"
              required
              style={{
                width: '100%',
                padding: '9px 12px',
                borderRadius: '6px',
                border: `1px solid ${fieldErrors.fullName ? 'var(--danger)' : 'var(--border)'}`,
                boxSizing: 'border-box'
              }}
            />
            {fieldErrors.fullName && <span style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>{fieldErrors.fullName}</span>}
          </div>

          {/* Email */}
          <div style={{ marginBottom: '16px' }}>
            <label htmlFor="reg-email" style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
              College Email
            </label>
            <input
              id="reg-email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="e.g. yourname@anurag.edu.in"
              required
              style={{
                width: '100%',
                padding: '9px 12px',
                borderRadius: '6px',
                border: `1px solid ${fieldErrors.email ? 'var(--danger)' : 'var(--border)'}`,
                boxSizing: 'border-box'
              }}
            />
            <small style={{ color: 'var(--text-muted)', fontSize: '0.78rem' }}>
              Only official college email addresses are permitted.
            </small>
            {fieldErrors.email && <span style={{ display: 'block', color: 'var(--danger)', fontSize: '0.8rem' }}>{fieldErrors.email}</span>}
          </div>

          {/* Row: Roll Number & Phone */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '16px' }}>
            <div>
              <label htmlFor="reg-roll" style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                Roll Number
              </label>
              <input
                id="reg-roll"
                type="text"
                name="rollNumber"
                value={formData.rollNumber}
                onChange={handleChange}
                placeholder="e.g. 21EG105A01"
                required
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '6px',
                  border: `1px solid ${fieldErrors.rollNumber ? 'var(--danger)' : 'var(--border)'}`,
                  boxSizing: 'border-box'
                }}
              />
              {fieldErrors.rollNumber && <span style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>{fieldErrors.rollNumber}</span>}
            </div>

            <div>
              <label htmlFor="reg-phone" style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                Phone Number
              </label>
              <input
                id="reg-phone"
                type="text"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                placeholder="e.g. 9876543210"
                required
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '6px',
                  border: `1px solid ${fieldErrors.phone ? 'var(--danger)' : 'var(--border)'}`,
                  boxSizing: 'border-box'
                }}
              />
              {fieldErrors.phone && <span style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>{fieldErrors.phone}</span>}
            </div>
          </div>

          {/* Row: Branch & Year */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '16px' }}>
            <div>
              <label htmlFor="reg-branch" style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                Branch / Department
              </label>
              <select
                id="reg-branch"
                name="branch"
                value={formData.branch}
                onChange={handleChange}
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '6px',
                  border: '1px solid var(--border)',
                  backgroundColor: '#fff',
                  boxSizing: 'border-box'
                }}
              >
                <option value="CSE">Computer Science and Engineering (CSE)</option>
                <option value="IT">Information Technology (IT)</option>
                <option value="ECE">Electronics and Communication (ECE)</option>
                <option value="EEE">Electrical and Electronics (EEE)</option>
                <option value="MECH">Mechanical Engineering (MECH)</option>
                <option value="CIVIL">Civil Engineering (CIVIL)</option>
                <option value="AI_DS">Artificial Intelligence & Data Science</option>
              </select>
            </div>

            <div>
              <label htmlFor="reg-year" style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                Current Year
              </label>
              <select
                id="reg-year"
                name="year"
                value={formData.year}
                onChange={handleChange}
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '6px',
                  border: '1px solid var(--border)',
                  backgroundColor: '#fff',
                  boxSizing: 'border-box'
                }}
              >
                <option value={1}>1st Year</option>
                <option value={2}>2nd Year</option>
                <option value={3}>3rd Year</option>
                <option value={4}>4th Year</option>
              </select>
            </div>
          </div>

          {/* Password & Confirm Password */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '24px' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                <label htmlFor="reg-password" style={{ fontSize: '0.85rem', fontWeight: 600 }}>
                  Password
                </label>
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{ background: 'none', border: 'none', color: 'var(--indigo-600)', fontSize: '0.78rem', cursor: 'pointer', padding: 0 }}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
              <input
                id="reg-password"
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Min 8 characters"
                required
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '6px',
                  border: `1px solid ${fieldErrors.password ? 'var(--danger)' : 'var(--border)'}`,
                  boxSizing: 'border-box'
                }}
              />
              {fieldErrors.password && <span style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>{fieldErrors.password}</span>}
            </div>

            <div>
              <label htmlFor="reg-confirm" style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                Confirm Password
              </label>
              <input
                id="reg-confirm"
                type={showPassword ? 'text' : 'password'}
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Re-enter password"
                required
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '6px',
                  border: `1px solid ${fieldErrors.confirmPassword ? 'var(--danger)' : 'var(--border)'}`,
                  boxSizing: 'border-box'
                }}
              />
              {fieldErrors.confirmPassword && <span style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>{fieldErrors.confirmPassword}</span>}
            </div>
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
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            {loading ? 'Submitting registration...' : 'Register'}
          </button>
        </form>

        <div style={{ marginTop: '20px', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--indigo-600)', fontWeight: 600, textDecoration: 'none' }}>
            Log in here
          </Link>
        </div>
      </div>
    </div>
  );
}
