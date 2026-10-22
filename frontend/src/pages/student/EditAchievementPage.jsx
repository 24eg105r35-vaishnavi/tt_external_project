import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../../api/axios';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function EditAchievementPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const [categoryName, setCategoryName] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [level, setLevel] = useState('COLLEGE');
  const [eventDate, setEventDate] = useState('');
  const [organizer, setOrganizer] = useState('');
  const [facultyReason, setFacultyReason] = useState('');

  // dynamic fields
  const [dynamicFields, setDynamicFields] = useState({});
  const [schemaFields, setSchemaFields] = useState([]);

  // new document file
  const [file, setFile] = useState(null);
  const [existingFileName, setExistingFileName] = useState('');

  useEffect(() => {
    fetchAchievementAndCategory();
  }, [id]);

  async function fetchAchievementAndCategory() {
    setLoading(true);
    try {
      const res = await api.get(`/student/achievements/${id}`);
      const ach = res.data;

      setCategoryId(ach.categoryId);
      setCategoryName(ach.categoryName);
      setTitle(ach.title || '');
      setDescription(ach.description || '');
      setLevel(ach.level || 'COLLEGE');
      setEventDate(ach.eventDate || '');
      setOrganizer(ach.organizer || '');
      setExistingFileName(ach.documentOriginalName || '');

      // extract latest rejection or send back reason from verifications
      if (ach.verifications && ach.verifications.length > 0) {
        setFacultyReason(ach.verifications[0].reason || '');
      }

      // parse extra data
      let parsedExtra = {};
      if (ach.extraData) {
        try {
          parsedExtra = JSON.parse(ach.extraData);
        } catch (e) {
          console.warn('Failed parsing extraData', e);
        }
      }
      setDynamicFields(parsedExtra);

      // fetch categories to find fieldSchema
      const catRes = await api.get('/student/categories');
      const currentCat = catRes.data.find((c) => String(c.id) === String(ach.categoryId));
      if (currentCat && currentCat.fieldSchema) {
        try {
          const schema = JSON.parse(currentCat.fieldSchema);
          if (Array.isArray(schema)) {
            setSchemaFields(schema);
          }
        } catch (e) {
          console.warn('Invalid category schema', e);
        }
      }

      setLoading(false);
    } catch (err) {
      console.error('Failed to load achievement for edit', err);
      setErrorMessage('Failed to load achievement for editing.');
      setLoading(false);
    }
  }

  function handleDynamicChange(name, value) {
    setDynamicFields((prev) => ({ ...prev, [name]: value }));
  }

  function handleFileChange(e) {
    const selected = e.target.files[0];
    if (!selected) return;

    if (selected.size > 10 * 1024 * 1024) {
      setErrorMessage('File exceeds the 10MB limit. Please upload a smaller document.');
      setFile(null);
      return;
    }

    const ext = selected.name.toLowerCase();
    const validExtensions = ['.pdf', '.jpg', '.jpeg', '.png'];
    const isValid = validExtensions.some((v) => ext.endsWith(v));
    if (!isValid) {
      setErrorMessage('Invalid file format. Only PDF, JPG, and PNG documents are accepted.');
      setFile(null);
      return;
    }

    setErrorMessage('');
    setFile(selected);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});
    setSubmitting(true);

    const formData = new FormData();
    formData.append('categoryId', categoryId);
    formData.append('title', title);
    formData.append('description', description);
    formData.append('level', level);
    formData.append('eventDate', eventDate);
    formData.append('organizer', organizer);
    formData.append('extraData', JSON.stringify(dynamicFields));
    if (file) {
      formData.append('file', file);
    }

    try {
      await api.put(`/student/achievements/${id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      navigate(`/student/achievements/${id}`);
    } catch (err) {
      console.error('Resubmission failed', err);
      if (err.response?.data?.errors) {
        setFieldErrors(err.response.data.errors);
        setErrorMessage(err.response.data.message || 'Please fix the errors below.');
      } else {
        setErrorMessage(err.response?.data?.message || 'Failed to resubmit achievement.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '60px 0' }}>
        <Spinner />
        <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading submission details...</p>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '36px 16px', maxWidth: '800px' }}>
      <div style={{ marginBottom: '20px' }}>
        <Link to={`/student/achievements/${id}`} style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          ← Back to Details
        </Link>
      </div>

      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '1.8rem', color: 'var(--navy-900)', marginBottom: '6px' }}>
          Edit & Resubmit Achievement
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Category: <strong>{categoryName}</strong>
        </p>
      </div>

      {facultyReason && (
        <div style={{ marginBottom: '24px', padding: '16px 20px', backgroundColor: '#fff3e0', border: '1px solid #ffe0b2', borderRadius: '8px' }}>
          <div style={{ fontWeight: 600, color: '#e65100', fontSize: '0.95rem', marginBottom: '4px' }}>
            Faculty Verifier Feedback:
          </div>
          <div style={{ color: '#5d4037', fontSize: '0.9rem' }}>
            "{facultyReason}"
          </div>
        </div>
      )}

      {errorMessage && (
        <div style={{ marginBottom: '20px' }}>
          <Alert type="danger" message={errorMessage} />
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label htmlFor="edit-title" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
              Title / Honor Name *
            </label>
            <input
              id="edit-title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: `1px solid ${fieldErrors.title ? 'var(--danger)' : 'var(--border)'}`, fontSize: '0.95rem', outline: 'none' }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div>
              <label htmlFor="edit-level" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
                Achievement Level *
              </label>
              <select
                id="edit-level"
                value={level}
                onChange={(e) => setLevel(e.target.value)}
                style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.95rem', backgroundColor: '#fff', outline: 'none' }}
                required
              >
                <option value="COLLEGE">College Level</option>
                <option value="STATE">State Level</option>
                <option value="NATIONAL">National Level</option>
                <option value="INTERNATIONAL">International Level</option>
              </select>
            </div>

            <div>
              <label htmlFor="edit-date" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
                Event / Award Date *
              </label>
              <input
                id="edit-date"
                type="date"
                value={eventDate}
                max={new Date().toISOString().split('T')[0]}
                onChange={(e) => setEventDate(e.target.value)}
                required
                style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: `1px solid ${fieldErrors.eventDate ? 'var(--danger)' : 'var(--border)'}`, fontSize: '0.95rem', outline: 'none' }}
              />
            </div>
          </div>

          <div>
            <label htmlFor="edit-organizer" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
              Organizing Body *
            </label>
            <input
              id="edit-organizer"
              type="text"
              value={organizer}
              onChange={(e) => setOrganizer(e.target.value)}
              required
              style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: `1px solid ${fieldErrors.organizer ? 'var(--danger)' : 'var(--border)'}`, fontSize: '0.95rem', outline: 'none' }}
            />
          </div>

          <div>
            <label htmlFor="edit-desc" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
              Description / Summary (Optional)
            </label>
            <textarea
              id="edit-desc"
              rows="3"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.95rem', outline: 'none', resize: 'vertical' }}
            />
          </div>
        </div>

        {/* Dynamic Category Schema Fields */}
        {schemaFields.length > 0 && (
          <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <h2 style={{ fontSize: '1.1rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
              Category Specific Fields
            </h2>
            {schemaFields.map((field) => (
              <div key={field.name}>
                <label style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
                  {field.label || field.name} {field.required ? '*' : ''}
                </label>
                {field.type === 'select' ? (
                  <select
                    value={dynamicFields[field.name] || ''}
                    onChange={(e) => handleDynamicChange(field.name, e.target.value)}
                    required={field.required}
                    style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.95rem', backgroundColor: '#fff', outline: 'none' }}
                  >
                    <option value="">Select option</option>
                    {field.options?.map((opt) => (
                      <option key={opt} value={opt}>{opt}</option>
                    ))}
                  </select>
                ) : (
                  <input
                    type={field.type === 'number' ? 'number' : 'text'}
                    value={dynamicFields[field.name] || ''}
                    onChange={(e) => handleDynamicChange(field.name, e.target.value)}
                    required={field.required}
                    style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.95rem', outline: 'none' }}
                  />
                )}
              </div>
            ))}
          </div>
        )}

        {/* Document Replacement */}
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <label style={{ display: 'block', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '6px' }}>
            Upload Replacement Certificate (Optional)
          </label>
          {existingFileName && (
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '12px' }}>
              Current file: <strong>{existingFileName}</strong>. Leave empty to keep this file, or choose a new file below to replace it.
            </p>
          )}
          <input
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            onChange={handleFileChange}
            style={{ display: 'block', width: '100%', padding: '10px', borderRadius: '6px', border: '1px dashed var(--border)', backgroundColor: 'var(--bg)', cursor: 'pointer' }}
          />
          {file && (
            <div style={{ marginTop: '12px', fontSize: '0.85rem', color: 'var(--success)', fontWeight: 600 }}>
              New file selected: {file.name}
            </div>
          )}
        </div>

        {/* Submit Actions */}
        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '12px' }}>
          <button
            type="button"
            onClick={() => navigate(`/student/achievements/${id}`)}
            style={{ padding: '11px 20px', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: '#fff', color: 'var(--text)', fontSize: '0.95rem' }}
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            style={{
              padding: '11px 28px',
              borderRadius: '6px',
              border: 'none',
              backgroundColor: 'var(--warning)',
              color: '#fff',
              fontSize: '0.95rem',
              fontWeight: 600,
              cursor: submitting ? 'not-allowed' : 'pointer'
            }}
          >
            {submitting ? 'Resubmitting...' : 'Resubmit for Review'}
          </button>
        </div>
      </form>
    </div>
  );
}
