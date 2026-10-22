import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../../api/axios';
import DocumentViewer from '../../components/DocumentViewer';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function VerificationReviewPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [achievement, setAchievement] = useState(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // decision modal / action box state
  const [activeAction, setActiveAction] = useState(null); // 'APPROVE' | 'SEND_BACK' | 'REJECT'
  const [reason, setReason] = useState('');
  const [actionError, setActionError] = useState('');

  useEffect(() => {
    fetchAchievementForReview();
  }, [id]);

  function fetchAchievementForReview() {
    setLoading(true);
    api.get(`/faculty/achievements/${id}`)
      .then((res) => {
        setAchievement(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load achievement for review', err);
        setErrorMessage('Failed to load achievement or you do not have permission for this category.');
        setLoading(false);
      });
  }

  function handleDecision(decisionType) {
    if (decisionType === 'APPROVE') {
      submitDecision('APPROVE', 'Verified and approved by faculty.');
    } else {
      setActiveAction(decisionType);
      setReason('');
      setActionError('');
    }
  }

  async function submitDecision(decision, decisionReason) {
    if ((decision === 'REJECT' || decision === 'SEND_BACK') && (!decisionReason || !decisionReason.trim())) {
      setActionError('Please provide a reason/explanation for the student.');
      return;
    }

    setSubmitting(true);
    setActionError('');

    try {
      await api.post(`/faculty/achievements/${id}/verify`, {
        decision: decision,
        reason: decisionReason ? decisionReason.trim() : null
      });

      const message = decision === 'APPROVE'
        ? `Achievement approved! Points awarded to ${achievement.studentName}.`
        : decision === 'SEND_BACK'
        ? `Achievement sent back to ${achievement.studentName} for correction.`
        : `Achievement marked as rejected.`;

      navigate('/faculty/queue', { state: { successMessage: message } });
    } catch (err) {
      console.error('Failed to record verification decision', err);
      setActionError(err.response?.data?.message || 'Failed to record decision.');
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '60px 0' }}>
        <Spinner />
        <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading achievement for verification...</p>
      </div>
    );
  }

  if (errorMessage || !achievement) {
    return (
      <div className="container" style={{ padding: '40px 16px' }}>
        <div style={{ backgroundColor: '#fdf2f2', border: '1px solid #f7caca', padding: '20px', borderRadius: '8px', color: 'var(--danger)', marginBottom: '16px' }}>
          {errorMessage || 'Achievement not found.'}
        </div>
        <Link to="/faculty/queue" style={{ color: 'var(--indigo-600)', fontWeight: 600 }}>
          ← Back to Verification Queue
        </Link>
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
    <div className="container" style={{ padding: '36px 16px' }}>
      <div style={{ marginBottom: '16px' }}>
        <Link to="/faculty/queue" style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          ← Back to Verification Queue
        </Link>
      </div>

      <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
            Review Achievement Claim
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Verify proof document, authenticate claim details, and record a verification decision.
          </p>
        </div>
        <div style={{ backgroundColor: 'var(--surface)', border: '1px solid var(--border)', padding: '8px 16px', borderRadius: '6px', fontSize: '0.9rem' }}>
          Category: <strong>{achievement.categoryName}</strong>
        </div>
      </div>

      {/* 2-Column Split View */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.1fr 1fr', gap: '24px', marginBottom: '32px' }}>
        {/* Left Column: Claim Details */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Student Profile Info */}
          <div style={{ backgroundColor: 'var(--surface)', padding: '20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
            <h2 style={{ fontSize: '1rem', color: 'var(--navy-900)', marginBottom: '12px', borderBottom: '1px solid var(--border)', paddingBottom: '6px' }}>
              Student Information
            </h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', fontSize: '0.9rem' }}>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Full Name: </span>
                <span style={{ fontWeight: 600 }}>{achievement.studentName}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Roll Number: </span>
                <span style={{ fontWeight: 600 }}>{achievement.rollNumber}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Branch: </span>
                <span style={{ fontWeight: 600 }}>{achievement.branch}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Academic Year: </span>
                <span style={{ fontWeight: 600 }}>Year {achievement.year}</span>
              </div>
            </div>
          </div>

          {/* Achievement Claim Details */}
          <div style={{ backgroundColor: 'var(--surface)', padding: '20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
            <h2 style={{ fontSize: '1rem', color: 'var(--navy-900)', marginBottom: '12px', borderBottom: '1px solid var(--border)', paddingBottom: '6px' }}>
              Claimed Honor / Title
            </h2>
            <h3 style={{ fontSize: '1.2rem', color: 'var(--navy-900)', marginBottom: '10px' }}>
              {achievement.title}
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.9rem' }}>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Organizer: </span>
                <span style={{ fontWeight: 600 }}>{achievement.organizer}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Level: </span>
                <span style={{ fontWeight: 600 }}>{achievement.level}</span>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Event Date: </span>
                <span style={{ fontWeight: 600 }}>{achievement.eventDate}</span>
              </div>
              {achievement.description && (
                <div style={{ marginTop: '8px', paddingTop: '8px', borderTop: '1px dashed var(--border)' }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Description:</div>
                  <div style={{ whiteSpace: 'pre-wrap', color: 'var(--text)' }}>{achievement.description}</div>
                </div>
              )}
            </div>
          </div>

          {/* Extra Metadata Fields */}
          {Object.keys(extraDataMap).length > 0 && (
            <div style={{ backgroundColor: 'var(--surface)', padding: '20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
              <h2 style={{ fontSize: '1rem', color: 'var(--navy-900)', marginBottom: '12px', borderBottom: '1px solid var(--border)', paddingBottom: '6px' }}>
                Category Specific Attributes
              </h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.9rem' }}>
                {Object.entries(extraDataMap).map(([k, v]) => (
                  <div key={k} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0', borderBottom: '1px dashed var(--border)' }}>
                    <span style={{ color: 'var(--text-muted)', textTransform: 'capitalize' }}>{k.replace(/([A-Z])/g, ' $1')}:</span>
                    <span style={{ fontWeight: 600 }}>{String(v)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Document Proof Viewer */}
        <div>
          <div style={{ backgroundColor: 'var(--surface)', padding: '20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
            <h2 style={{ fontSize: '1rem', color: 'var(--navy-900)', marginBottom: '12px', borderBottom: '1px solid var(--border)', paddingBottom: '6px' }}>
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

      {/* Decision Card */}
      <div style={{ backgroundColor: 'var(--surface)', padding: '28px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
        <h2 style={{ fontSize: '1.15rem', color: 'var(--navy-900)', marginBottom: '8px' }}>
          Record Verification Decision
        </h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '20px' }}>
          Approving this claim will award points to the student profile and update department leaderboards.
        </p>

        {actionError && (
          <div style={{ marginBottom: '16px' }}>
            <Alert type="danger" message={actionError} />
          </div>
        )}

        {/* Action Buttons */}
        {!activeAction ? (
          <div style={{ display: 'flex', gap: '14px', flexWrap: 'wrap' }}>
            <button
              onClick={() => handleDecision('APPROVE')}
              disabled={submitting}
              style={{
                backgroundColor: 'var(--success)',
                color: '#fff',
                padding: '12px 24px',
                borderRadius: '6px',
                border: 'none',
                fontWeight: 600,
                fontSize: '0.95rem',
                cursor: 'pointer'
              }}
            >
              ✓ Approve Claim
            </button>

            <button
              onClick={() => handleDecision('SEND_BACK')}
              disabled={submitting}
              style={{
                backgroundColor: 'var(--warning)',
                color: '#fff',
                padding: '12px 24px',
                borderRadius: '6px',
                border: 'none',
                fontWeight: 600,
                fontSize: '0.95rem',
                cursor: 'pointer'
              }}
            >
              ✎ Request Correction
            </button>

            <button
              onClick={() => handleDecision('REJECT')}
              disabled={submitting}
              style={{
                backgroundColor: 'var(--danger)',
                color: '#fff',
                padding: '12px 24px',
                borderRadius: '6px',
                border: 'none',
                fontWeight: 600,
                fontSize: '0.95rem',
                cursor: 'pointer'
              }}
            >
              ✕ Reject Claim
            </button>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', maxWidth: '650px' }}>
            <div style={{ fontWeight: 600, fontSize: '0.95rem', color: activeAction === 'REJECT' ? 'var(--danger)' : 'var(--warning)' }}>
              {activeAction === 'REJECT' ? 'Reason for Rejection' : 'Specific Correction Instructions for Student'} *
            </div>
            <textarea
              rows="3"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder={activeAction === 'REJECT' ? 'e.g. Invalid document or unverified organization...' : 'e.g. Please upload a higher resolution scan showing the certificate serial number...'}
              style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.95rem', outline: 'none' }}
              required
            />
            <div style={{ display: 'flex', gap: '12px' }}>
              <button
                type="button"
                onClick={() => setActiveAction(null)}
                style={{ padding: '9px 18px', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: '#fff', fontSize: '0.9rem' }}
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => submitDecision(activeAction, reason)}
                disabled={submitting}
                style={{
                  padding: '9px 24px',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: activeAction === 'REJECT' ? 'var(--danger)' : 'var(--warning)',
                  color: '#fff',
                  fontWeight: 600,
                  fontSize: '0.9rem',
                  cursor: submitting ? 'not-allowed' : 'pointer'
                }}
              >
                {submitting ? 'Submitting...' : `Confirm ${activeAction === 'REJECT' ? 'Rejection' : 'Correction Request'}`}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
