import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function StudentProfilePage() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [visibility, setVisibility] = useState('PUBLIC');
  const [saving, setSaving] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    fetchProfile();
  }, []);

  function fetchProfile() {
    setLoading(true);
    api.get('/student/profile')
      .then((res) => {
        setProfile(res.data);
        setVisibility(res.data.visibility || 'PUBLIC');
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load student profile', err);
        setErrorMessage('Failed to load profile details.');
        setLoading(false);
      });
  }

  function handleSaveVisibility(e) {
    e.preventDefault();
    setSaving(true);
    setSuccessMessage('');
    setErrorMessage('');

    api.put('/student/profile/visibility', { visibility })
      .then((res) => {
        setProfile(res.data);
        setVisibility(res.data.visibility);
        setSuccessMessage('Profile privacy settings updated successfully.');
        setSaving(false);
      })
      .catch((err) => {
        console.error('Failed to update visibility', err);
        setErrorMessage('Failed to update privacy settings.');
        setSaving(false);
      });
  }

  function copyPortfolioUrl() {
    const url = `${window.location.origin}/u/${profile?.profileSlug}`;
    navigator.clipboard.writeText(url);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  }

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '60px 0' }}>
        <Spinner />
        <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading student profile...</p>
      </div>
    );
  }

  const portfolioUrl = `${window.location.origin}/u/${profile?.profileSlug}`;

  return (
    <div className="container" style={{ padding: '36px 16px', maxWidth: '800px' }}>
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '1.8rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
          Student Profile & Privacy Settings
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Manage your verified student portfolio link and public visibility controls.
        </p>
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

      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
        {/* Student Information Box */}
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <h2 style={{ fontSize: '1.1rem', color: 'var(--navy-900)', marginBottom: '16px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
            Academic Credentials
          </h2>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', fontSize: '0.9rem' }}>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Full Name: </span>
              <div style={{ fontWeight: 600, color: 'var(--navy-900)', marginTop: '2px' }}>{profile?.user?.fullName}</div>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Roll Number: </span>
              <div style={{ fontWeight: 600, color: 'var(--navy-900)', marginTop: '2px' }}>{profile?.rollNumber}</div>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Registered Email: </span>
              <div style={{ fontWeight: 600, color: 'var(--navy-900)', marginTop: '2px' }}>{profile?.user?.email}</div>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Branch & Year: </span>
              <div style={{ fontWeight: 600, color: 'var(--navy-900)', marginTop: '2px' }}>{profile?.branch} • Year {profile?.year}</div>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Total Verified Points: </span>
              <div style={{ fontWeight: 700, color: 'var(--success)', fontSize: '1.1rem', marginTop: '2px' }}>
                +{profile?.totalPoints || 0} Pts
              </div>
            </div>
          </div>
        </div>

        {/* Public Portfolio URL Card */}
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <h2 style={{ fontSize: '1.1rem', color: 'var(--navy-900)', marginBottom: '8px' }}>
            Public Portfolio URL
          </h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '14px' }}>
            Share this link on your resume, LinkedIn, or internship applications to showcase verified credentials.
          </p>

          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <input
              type="text"
              readOnly
              value={portfolioUrl}
              style={{
                flex: 1,
                padding: '10px 14px',
                borderRadius: '6px',
                border: '1px solid var(--border)',
                backgroundColor: 'var(--bg)',
                fontSize: '0.9rem',
                outline: 'none',
                color: 'var(--navy-900)',
                fontWeight: 500
              }}
            />
            <button
              onClick={copyPortfolioUrl}
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
              {copied ? '✓ Copied' : 'Copy Link'}
            </button>
            <a
              href={portfolioUrl}
              target="_blank"
              rel="noopener noreferrer"
              style={{
                padding: '9px 14px',
                borderRadius: '6px',
                border: '1px solid var(--border)',
                backgroundColor: '#fff',
                color: 'var(--indigo-600)',
                fontWeight: 600,
                fontSize: '0.85rem'
              }}
            >
              Open ↗
            </a>
          </div>
        </div>

        {/* Visibility Settings Form */}
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <h2 style={{ fontSize: '1.1rem', color: 'var(--navy-900)', marginBottom: '6px' }}>
            Portfolio Privacy & Leaderboard Visibility
          </h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '16px' }}>
            Control who can view your portfolio and whether your points are published on the campus leaderboard.
          </p>

          <form onSubmit={handleSaveVisibility} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <label style={{ display: 'flex', gap: '12px', alignItems: 'flex-start', cursor: 'pointer', padding: '10px', borderRadius: '6px', backgroundColor: visibility === 'PUBLIC' ? 'var(--bg)' : 'transparent' }}>
              <input
                type="radio"
                name="visibility"
                value="PUBLIC"
                checked={visibility === 'PUBLIC'}
                onChange={() => setVisibility('PUBLIC')}
                style={{ marginTop: '3px' }}
              />
              <div>
                <div style={{ fontWeight: 600, fontSize: '0.95rem', color: 'var(--navy-900)' }}>Public (Recommended)</div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  Anyone with your portfolio URL can view your verified honors. Your points are included on the campus leaderboard.
                </div>
              </div>
            </label>

            <label style={{ display: 'flex', gap: '12px', alignItems: 'flex-start', cursor: 'pointer', padding: '10px', borderRadius: '6px', backgroundColor: visibility === 'COLLEGE_ONLY' ? 'var(--bg)' : 'transparent' }}>
              <input
                type="radio"
                name="visibility"
                value="COLLEGE_ONLY"
                checked={visibility === 'COLLEGE_ONLY'}
                onChange={() => setVisibility('COLLEGE_ONLY')}
                style={{ marginTop: '3px' }}
              />
              <div>
                <div style={{ fontWeight: 600, fontSize: '0.95rem', color: 'var(--navy-900)' }}>College Members Only</div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  Only logged-in university students and faculty can view your portfolio. Visible on leaderboard.
                </div>
              </div>
            </label>

            <label style={{ display: 'flex', gap: '12px', alignItems: 'flex-start', cursor: 'pointer', padding: '10px', borderRadius: '6px', backgroundColor: visibility === 'PRIVATE' ? 'var(--bg)' : 'transparent' }}>
              <input
                type="radio"
                name="visibility"
                value="PRIVATE"
                checked={visibility === 'PRIVATE'}
                onChange={() => setVisibility('PRIVATE')}
                style={{ marginTop: '3px' }}
              />
              <div>
                <div style={{ fontWeight: 600, fontSize: '0.95rem', color: 'var(--navy-900)' }}>Private</div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  Hidden from public visitors. Completely excluded from the public campus leaderboard rankings.
                </div>
              </div>
            </label>

            <div style={{ marginTop: '10px', display: 'flex', justifyContent: 'flex-end' }}>
              <button
                type="submit"
                disabled={saving}
                style={{
                  backgroundColor: 'var(--navy-900)',
                  color: '#fff',
                  padding: '10px 22px',
                  borderRadius: '6px',
                  border: 'none',
                  fontWeight: 600,
                  fontSize: '0.9rem',
                  cursor: saving ? 'not-allowed' : 'pointer'
                }}
              >
                {saving ? 'Saving...' : 'Save Privacy Settings'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
