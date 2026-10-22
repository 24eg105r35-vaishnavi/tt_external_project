import React from 'react';

// placeholder inline SVG crest representing university achievement
function CrestIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" style={{ marginRight: '8px', color: 'var(--gold-500)' }}>
      <path d="M12 2L3 7v6c0 5.5 3.8 10.7 9 12 5.2-1.3 9-6.5 9-12V7l-9-5z" />
      <path d="M12 8v8" />
      <path d="M8 12h8" />
    </svg>
  );
}

export default function TopStrip() {
  return (
    <div style={{ backgroundColor: 'var(--navy-900)', color: '#fff', fontSize: '0.85rem', padding: '6px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <CrestIcon />
          <span style={{ fontWeight: 600, letterSpacing: '0.02em' }}>Anurag University</span>
          <span style={{ margin: '0 8px', opacity: 0.5 }}>|</span>
          <span style={{ opacity: 0.85 }}>Student Achievement & Verification System</span>
        </div>
        <div style={{ display: 'none' }}>Portal</div>
      </div>
    </div>
  );
}
