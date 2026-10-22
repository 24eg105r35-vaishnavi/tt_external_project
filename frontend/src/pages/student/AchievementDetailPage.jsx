import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../../api/axios';
import StatusBadge from '../../components/StatusBadge';
import DocumentViewer from '../../components/DocumentViewer';
import Spinner from '../../components/Spinner';

export default function AchievementDetailPage() {
  const { id } = useParams();
  const [achievement, setAchievement] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    api.get(`/student/achievements/${id}`)
      .then((res) => {
        setAchievement(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load achievement', err);
        setError('Failed to load achievement details.');
        setLoading(false);
      });
  }, [id]);

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '60px 0' }}>
        <Spinner />
        <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading achievement details...</p>
      </div>
    );
  }

  if (error || !achievement) {
    return (
      <div className="container" style={{ padding: '40px 16px' }}>
        <div style={{ backgroundColor: '#fdf2f2', border: '1px solid #f7caca', padding: '20px', borderRadius: '8px', color: 'var(--danger)' }}>
          {error || 'Achievement not found.'}
        </div>
        <div style={{ marginTop: '16px' }}>
          <Link to="/student/achievements" style={{ color: 'var(--indigo-600)', fontWeight: 600 }}>
            ← Back to My Submissions
          </Link>
        </div>
      </div>
    );
  }

  let extraDataMap = {};
  if (achievement.extraData) {
    try {
      extraDataMap = JSON.parse(achievement.extraData);
    } catch (e) {
      console.warn('Could not parse extraData', e);
    }
  }

  return (
    <div className="container" style={{ padding: '36px 16px', maxWidth: '960px' }}>
      <div style={{ marginBottom: '20px' }}>
        <Link to="/student/achievements" style={{ color: 'var(--text-muted)', fontSize: '0.9rem', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
          ← Back to My Submissions
        </Link>
      </div>

      {/* Header Card */}
      <div style={{ backgroundColor: 'var(--surface)', padding: '24px 28px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', marginBottom: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <StatusBadge status={achievement.status} />
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>
                {achievement.categoryName} • {achievement.level} Level
              </span>
            </div>
            <h1 style={{ fontSize: '1.6rem', color: 'var(--navy-900)', marginBottom: '6px' }}>
              {achievement.title}
            </h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Organized by <strong>{achievement.organizer}</strong> • Event Date: {achievement.eventDate}
            </p>
          </div>

          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Verified Points</div>
            <div style={{ fontSize: '1.8rem', fontWeight: 700, color: achievement.status === 'APPROVED' ? 'var(--success)' : 'var(--text-muted)' }}>
              {achievement.status === 'APPROVED' ? `+${achievement.pointsAwarded}` : '0'}
            </div>
          </div>
        </div>

        {/* Needs Correction banner */}
        {(achievement.status === 'NEEDS_CORRECTION' || achievement.status === 'REJECTED') && (
          <div style={{ marginTop: '20px', padding: '14px 18px', backgroundColor: '#fff3e0', border: '1px solid #ffe0b2', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
            <div>
              <div style={{ fontWeight: 600, color: '#e65100', fontSize: '0.9rem' }}>
                Action Required by Faculty Verifier
              </div>
              <div style={{ fontSize: '0.85rem', color: '#5d4037', marginTop: '2px' }}>
                Please review the faculty remarks below and click Edit & Resubmit to update details or upload a new certificate.
              </div>
            </div>
            <Link
              to={`/student/achievements/${achievement.id}/edit`}
              style={{
                backgroundColor: 'var(--warning)',
                color: '#fff',
                padding: '8px 16px',
                borderRadius: '6px',
                fontWeight: 600,
                fontSize: '0.85rem'
              }}
            >
              Fix & Resubmit
            </Link>
          </div>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        {/* Left Column: Details & Extra Fields */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div style={{ backgroundColor: 'var(--surface)', padding: '22px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
            <h2 style={{ fontSize: '1.05rem', color: 'var(--navy-900)', marginBottom: '14px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
              Submission Details
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '0.9rem' }}>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Category: </span>
                <span style={{ fontWeight: 600 }}>{achievement.categoryName}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Level: </span>
                <span style={{ fontWeight: 600 }}>{achievement.level}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Organizer: </span>
                <span style={{ fontWeight: 600 }}>{achievement.organizer}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Event Date: </span>
                <span style={{ fontWeight: 600 }}>{achievement.eventDate}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Submitted On: </span>
                <span style={{ fontWeight: 600 }}>{achievement.createdAt ? new Date(achievement.createdAt).toLocaleDateString() : '—'}</span>
              </div>
              {achievement.description && (
                <div style={{ marginTop: '8px', paddingTop: '8px', borderTop: '1px solid var(--border)' }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '4px' }}>Description:</div>
                  <div style={{ whiteSpace: 'pre-wrap', color: 'var(--text)' }}>{achievement.description}</div>
                </div>
              )}
            </div>
          </div>

          {/* Dynamic Extra Data Fields */}
          {Object.keys(extraDataMap).length > 0 && (
            <div style={{ backgroundColor: 'var(--surface)', padding: '22px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
              <h2 style={{ fontSize: '1.05rem', color: 'var(--navy-900)', marginBottom: '14px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
                Category Metadata
              </h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.9rem' }}>
                {Object.entries(extraDataMap).map(([k, v]) => (
                  <div key={k} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px dashed var(--border)' }}>
                    <span style={{ color: 'var(--text-muted)', textTransform: 'capitalize' }}>{k.replace(/([A-Z])/g, ' $1')}:</span>
                    <span style={{ fontWeight: 600 }}>{String(v)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Verification Timeline */}
          <div style={{ backgroundColor: 'var(--surface)', padding: '22px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
            <h2 style={{ fontSize: '1.05rem', color: 'var(--navy-900)', marginBottom: '14px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
              Verification History
            </h2>
            {(!achievement.verifications || achievement.verifications.length === 0) ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Waiting for faculty review in department queue.
              </p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {achievement.verifications.map((v, i) => (
                  <div key={v.id || i} style={{ padding: '10px 14px', backgroundColor: 'var(--bg)', borderRadius: '6px', borderLeft: '3px solid var(--navy-900)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                      <span style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--navy-900)' }}>
                        {v.verifierName || 'Faculty Verifier'} ({v.verifierDepartment || 'Dept'})
                      </span>
                      <StatusBadge status={v.decision} />
                    </div>
                    {v.reason && (
                      <div style={{ fontSize: '0.85rem', color: 'var(--text)', marginTop: '4px' }}>
                        "{v.reason}"
                      </div>
                    )}
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                      {v.decidedAt ? new Date(v.decidedAt).toLocaleString() : ''}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Document Proof */}
        <div>
          <div style={{ backgroundColor: 'var(--surface)', padding: '22px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
            <h2 style={{ fontSize: '1.05rem', color: 'var(--navy-900)', marginBottom: '14px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
              Attached Certificate Proof
            </h2>
            {achievement.documentId ? (
              <DocumentViewer
                documentId={achievement.documentId}
                originalName={achievement.documentOriginalName}
                fileType={achievement.documentFileType}
              />
            ) : (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>No document proof was attached.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
