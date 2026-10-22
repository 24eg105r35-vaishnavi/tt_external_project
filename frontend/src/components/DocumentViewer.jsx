import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import Spinner from './Spinner';

export default function DocumentViewer({ documentId, originalName, fileType }) {
  const [blobUrl, setBlobUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    let url = null;

    if (!documentId) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');

    api.get(`/documents/${documentId}`, { responseType: 'blob' })
      .then((res) => {
        if (!active) return;
        url = URL.createObjectURL(res.data);
        setBlobUrl(url);
        setLoading(false);
      })
      .catch((err) => {
        if (!active) return;
        console.error('Failed to load document', err);
        setError('Unable to load document proof. You may not have permission or the file was not found.');
        setLoading(false);
      });

    return () => {
      active = false;
      if (url) {
        URL.revokeObjectURL(url);
      }
    };
  }, [documentId]);

  if (loading) {
    return (
      <div style={{ padding: '32px', textAlign: 'center', backgroundColor: 'var(--bg)', borderRadius: '8px' }}>
        <Spinner />
        <p style={{ marginTop: '12px', fontSize: '0.9rem', color: 'var(--text-muted)' }}>Loading document proof...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ padding: '16px', backgroundColor: '#fdf2f2', border: '1px solid #f7caca', borderRadius: '8px', color: 'var(--danger)', fontSize: '0.9rem' }}>
        {error}
      </div>
    );
  }

  if (!blobUrl) {
    return <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>No document proof attached.</div>;
  }

  const isPdf = fileType?.toLowerCase().includes('pdf') || originalName?.toLowerCase().endsWith('.pdf');

  return (
    <div style={{ border: '1px solid var(--border)', borderRadius: '8px', overflow: 'hidden', backgroundColor: '#fff' }}>
      <div style={{ padding: '10px 14px', backgroundColor: 'var(--bg)', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
        <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--navy-900)' }}>
          📄 {originalName || 'Document Proof'}
        </span>
        <div style={{ display: 'flex', gap: '14px', alignItems: 'center' }}>
          <a
            href={blobUrl}
            target="_blank"
            rel="noopener noreferrer"
            style={{ fontSize: '0.85rem', fontWeight: 500, color: 'var(--indigo-600)', textDecoration: 'underline' }}
          >
            Open in New Tab ↗
          </a>
          <a
            href={blobUrl}
            download={originalName || 'achievement_document'}
            style={{ fontSize: '0.85rem', fontWeight: 500, color: 'var(--text-muted)', textDecoration: 'underline' }}
          >
            Download File ⬇
          </a>
        </div>
      </div>

      <div style={{ padding: '12px', textAlign: 'center' }}>
        {isPdf ? (
          <object
            data={blobUrl}
            type="application/pdf"
            style={{ width: '100%', height: '520px', border: 'none', borderRadius: '4px' }}
          >
            <iframe
              src={blobUrl}
              title={originalName || 'PDF Viewer'}
              style={{ width: '100%', height: '520px', border: 'none', borderRadius: '4px' }}
            />
          </object>
        ) : (
          <img
            src={blobUrl}
            alt={originalName || 'Certificate Preview'}
            style={{ maxWidth: '100%', maxHeight: '520px', objectFit: 'contain', borderRadius: '4px' }}
          />
        )}
      </div>
    </div>
  );
}
