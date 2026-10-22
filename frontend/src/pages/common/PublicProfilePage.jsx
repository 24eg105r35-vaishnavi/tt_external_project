import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../../api/axios';
import Spinner from '../../components/Spinner';

export default function PublicProfilePage() {
  const { slug } = useParams();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    setLoading(true);
    setNotFound(false);
    api.get(`/public/profiles/${slug}`)
      .then((res) => {
        setProfile(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load public profile', err);
        setNotFound(true);
        setLoading(false);
      });
  }, [slug]);

  function copyShareLink() {
    navigator.clipboard.writeText(window.location.href);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  }

  function getInitials(name) {
    if (!name) return 'U';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '80px 0' }}>
        <Spinner />
        <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading verified portfolio...</p>
      </div>
    );
  }

  if (notFound || !profile) {
    return (
      <div className="container" style={{ padding: '80px 16px', maxWidth: '600px', textAlign: 'center' }}>
        <div style={{ backgroundColor: 'var(--surface)', padding: '40px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <div style={{ fontSize: '3rem', marginBottom: '12px' }}>🔒</div>
          <h1 style={{ fontSize: '1.4rem', color: 'var(--navy-900)', marginBottom: '8px' }}>
            Profile Not Available
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginBottom: '24px' }}>
            This student achievement portfolio is either private or does not exist.
          </p>
          <Link to="/" style={{ color: 'var(--indigo-600)', fontWeight: 600 }}>
            Return to Portal Home &rarr;
          </Link>
        </div>
      </div>
    );
  }

  const grouped = profile.groupedAchievements || {};
  const categoryNames = Object.keys(grouped);

  return (
    <div className="container" style={{ padding: '40px 16px', maxWidth: '900px' }}>
      {/* Portfolio Certificate-Style Header */}
      <div style={{
        backgroundColor: 'var(--surface)',
        borderRadius: 'var(--radius)',
        border: '1px solid var(--border)',
        padding: '36px',
        marginBottom: '28px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.03)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '20px' }}>
          <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
            {/* Initials Avatar */}
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              backgroundColor: 'var(--navy-900)',
              color: 'var(--gold-500)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '1.5rem',
              fontWeight: 700,
              boxShadow: '0 2px 6px rgba(0,0,0,0.1)'
            }}>
              {getInitials(profile.fullName)}
            </div>

            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                <h1 style={{ fontSize: '1.65rem', color: 'var(--navy-900)', margin: 0 }}>
                  {profile.fullName}
                </h1>
                <span style={{
                  backgroundColor: '#edf7f2',
                  color: 'var(--success)',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  padding: '2px 8px',
                  borderRadius: '999px',
                  border: '1px solid #c6ebd6',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px'
                }}>
                  ✓ Verified Student
                </span>
              </div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', margin: 0 }}>
                {profile.branch} • Year {profile.year} • National Institute of Technology
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '10px' }}>
            <div style={{
              backgroundColor: 'var(--bg)',
              border: '1px solid var(--border)',
              padding: '10px 18px',
              borderRadius: '8px',
              textAlign: 'center'
            }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Total Verified Points
              </div>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--navy-900)' }}>
                {profile.totalPoints}
              </div>
            </div>

            <button
              onClick={copyShareLink}
              style={{
                background: 'none',
                border: '1px solid var(--border)',
                borderRadius: '6px',
                padding: '6px 14px',
                fontSize: '0.85rem',
                color: 'var(--indigo-600)',
                fontWeight: 600,
                cursor: 'pointer'
              }}
            >
              {copied ? '✓ Link Copied!' : '🔗 Share Portfolio'}
            </button>
          </div>
        </div>

        {/* Category Breakdown Badges */}
        {profile.categoryCounts && Object.keys(profile.categoryCounts).length > 0 && (
          <div style={{ marginTop: '24px', paddingTop: '18px', borderTop: '1px solid var(--border)', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            {Object.entries(profile.categoryCounts).map(([cat, count]) => (
              <span
                key={cat}
                style={{
                  backgroundColor: 'var(--bg)',
                  fontSize: '0.8rem',
                  padding: '4px 10px',
                  borderRadius: '6px',
                  color: 'var(--text)',
                  border: '1px solid var(--border)'
                }}
              >
                {cat}: <strong>{count}</strong>
              </span>
            ))}
          </div>
        )}
      </div>

      {/* Grouped Achievements Section */}
      {categoryNames.length === 0 ? (
        <div style={{ backgroundColor: 'var(--surface)', padding: '40px', textAlign: 'center', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <p style={{ color: 'var(--text-muted)' }}>No approved achievements available on this portfolio yet.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
          {categoryNames.map((catName) => (
            <div key={catName}>
              <h2 style={{ fontSize: '1.2rem', color: 'var(--navy-900)', marginBottom: '12px', paddingBottom: '6px', borderBottom: '2px solid var(--indigo-100)' }}>
                {catName}
              </h2>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {grouped[catName].map((item) => (
                  <div
                    key={item.id}
                    style={{
                      backgroundColor: 'var(--surface)',
                      borderRadius: 'var(--radius)',
                      border: '1px solid var(--border)',
                      padding: '18px 22px',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      flexWrap: 'wrap',
                      gap: '14px'
                    }}
                  >
                    <div style={{ flex: 1, minWidth: '240px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                        <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--indigo-600)', backgroundColor: 'var(--indigo-100)', padding: '2px 8px', borderRadius: '4px' }}>
                          {item.level}
                        </span>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          Event Date: {item.eventDate}
                        </span>
                      </div>
                      <h3 style={{ fontSize: '1.05rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
                        {item.title}
                      </h3>
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                        Organized by <strong>{item.organizer}</strong>
                      </div>

                      {/* Dynamic Extra Attributes */}
                      {item.extraFields && Object.keys(item.extraFields).length > 0 && (
                        <div style={{ marginTop: '8px', display: 'flex', gap: '12px', flexWrap: 'wrap', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {Object.entries(item.extraFields).map(([k, v]) => (
                            <span key={k} style={{ backgroundColor: 'var(--bg)', padding: '2px 8px', borderRadius: '4px' }}>
                              {k}: <strong>{String(v)}</strong>
                            </span>
                          ))}
                        </div>
                      )}
                    </div>

                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--success)' }}>
                        +{item.pointsAwarded} Pts
                      </div>
                      {item.verifiedAt && (
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                          Verified {new Date(item.verifiedAt).toLocaleDateString()}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
