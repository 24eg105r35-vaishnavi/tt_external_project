import React from 'react';

export default function StatusBadge({ status }) {
  let bg = '#f4f5f7';
  let color = '#5e6c84';
  let border = '#dfe1e6';
  let label = status || 'Unknown';

  switch (status?.toUpperCase()) {
    case 'APPROVED':
      bg = '#edf7f2';
      color = '#2f7d5b';
      border = '#c6ebd6';
      label = 'Approved';
      break;
    case 'PENDING':
      bg = '#fef9ee';
      color = '#b7791f';
      border = '#f9e7b9';
      label = 'Pending Review';
      break;
    case 'NEEDS_CORRECTION':
      bg = '#fff3e0';
      color = '#e65100';
      border = '#ffe0b2';
      label = 'Needs Correction';
      break;
    case 'REJECTED':
      bg = '#fdf2f2';
      color = '#b04a4a';
      border = '#f7caca';
      label = 'Rejected';
      break;
    default:
      break;
  }

  return (
    <span
      style={{
        display: 'inline-block',
        padding: '3px 10px',
        borderRadius: '999px',
        fontSize: '0.78rem',
        fontWeight: 600,
        backgroundColor: bg,
        color: color,
        border: `1px solid ${border}`,
        letterSpacing: '0.01em',
        whiteSpace: 'nowrap'
      }}
    >
      {label}
    </span>
  );
}
