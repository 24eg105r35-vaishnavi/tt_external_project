import React from 'react';

export default function Footer() {
  return (
    <footer style={{ marginTop: 'auto', backgroundColor: 'var(--surface)', borderTop: '1px solid var(--border)', padding: '24px 0', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
      <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          © {new Date().getFullYear()} Student Achievement Portal
        </div>
        <div style={{ display: 'flex', gap: '16px' }}>
          <span>Privacy & Security</span>
          <span>•</span>
          <span>Verified Portfolios</span>
        </div>
      </div>
    </footer>
  );
}
