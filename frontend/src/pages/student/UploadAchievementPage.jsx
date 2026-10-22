import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import Alert from '../../components/Alert';
import Spinner from '../../components/Spinner';

export default function UploadAchievementPage() {
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  const [loadingCategories, setLoadingCategories] = useState(true);

  // standard fields
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [level, setLevel] = useState('COLLEGE');
  const [eventDate, setEventDate] = useState('');
  const [organizer, setOrganizer] = useState('');

  // dynamic fields
  const [dynamicFields, setDynamicFields] = useState({});
  const [schemaFields, setSchemaFields] = useState([]);

  // document file
  const [file, setFile] = useState(null);
  const [filePreview, setFilePreview] = useState(null);

  // status states
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    api.get('/student/categories')
      .then((res) => {
        setCategories(res.data);
        if (res.data.length > 0) {
          handleCategoryChange(res.data[0].id, res.data);
        }
        setLoadingCategories(false);
      })
      .catch((err) => {
        console.error('Failed to load categories', err);
        setErrorMessage('Failed to load achievement categories.');
        setLoadingCategories(false);
      });
  }, []);

  function handleCategoryChange(catId, catList = categories) {
    setSelectedCategoryId(catId);
    const cat = catList.find((c) => String(c.id) === String(catId));
    if (cat && cat.fieldSchema) {
      try {
        const parsed = JSON.parse(cat.fieldSchema);
        if (Array.isArray(parsed)) {
          setSchemaFields(parsed);
          const initial = {};
          parsed.forEach((f) => {
            initial[f.name] = '';
          });
          setDynamicFields(initial);
          return;
        }
      } catch (e) {
        console.warn('Invalid category schema', e);
      }
    }
    setSchemaFields([]);
    setDynamicFields({});
  }

  function handleDynamicChange(name, value) {
    setDynamicFields((prev) => ({ ...prev, [name]: value }));
  }

  function handleFileChange(e) {
    const selected = e.target.files[0];
    if (!selected) return;

    // Validate size (10MB = 10 * 1024 * 1024 bytes)
    if (selected.size > 10 * 1024 * 1024) {
      setErrorMessage('File exceeds the 10MB limit. Please upload a smaller document.');
      setFile(null);
      setFilePreview(null);
      return;
    }

    const ext = selected.name.toLowerCase();
    const validExtensions = ['.pdf', '.jpg', '.jpeg', '.png'];
    const isValid = validExtensions.some((v) => ext.endsWith(v));
    if (!isValid) {
      setErrorMessage('Invalid file format. Only PDF, JPG, and PNG documents are accepted.');
      setFile(null);
      setFilePreview(null);
      return;
    }

    setErrorMessage('');
    setFile(selected);

    if (selected.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = () => setFilePreview(reader.result);
      reader.readAsDataURL(selected);
    } else {
      setFilePreview(null);
    }
  }

  function formatBytes(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});

    if (!selectedCategoryId) {
      setErrorMessage('Please select an achievement category.');
      return;
    }

    if (!file) {
      setErrorMessage('Please upload a proof document or certificate.');
      return;
    }

    setSubmitting(true);

    const formData = new FormData();
    formData.append('categoryId', selectedCategoryId);
    formData.append('title', title);
    formData.append('description', description);
    formData.append('level', level);
    formData.append('eventDate', eventDate);
    formData.append('organizer', organizer);
    formData.append('extraData', JSON.stringify(dynamicFields));
    formData.append('file', file);

    try {
      await api.post('/student/achievements', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      navigate('/student/achievements', { state: { successMessage: 'Achievement uploaded successfully! Pending faculty verification.' } });
    } catch (err) {
      console.error('Submission failed', err);
      if (err.response?.data?.errors) {
        setFieldErrors(err.response.data.errors);
        setErrorMessage(err.response.data.message || 'Please fix the errors below.');
      } else {
        setErrorMessage(err.response?.data?.message || 'Failed to submit achievement. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  const selectedCategory = categories.find((c) => String(c.id) === String(selectedCategoryId));

  if (loadingCategories) {
    return (
      <div className="container" style={{ padding: '60px 0', textAlign: 'center' }}>
        <Spinner />
        <p style={{ marginTop: '12px', color: 'var(--text-muted)' }}>Loading categories...</p>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '40px 16px', maxWidth: '800px' }}>
      <div style={{ marginBottom: '28px' }}>
        <h1 style={{ fontSize: '1.8rem', color: 'var(--navy-900)', marginBottom: '6px' }}>
          Upload New Achievement
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Submit your award, certificate, or extracurricular achievement for department verification.
        </p>
      </div>

      {errorMessage && (
        <div style={{ marginBottom: '20px' }}>
          <Alert type="danger" message={errorMessage} />
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
        {/* Section 1: Category */}
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <label htmlFor="category-select" style={{ display: 'block', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '8px' }}>
            1. Select Category *
          </label>
          <select
            id="category-select"
            value={selectedCategoryId}
            onChange={(e) => handleCategoryChange(e.target.value)}
            style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.95rem', backgroundColor: '#fff', outline: 'none' }}
            required
          >
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name} ({cat.points} Points)
              </option>
            ))}
          </select>
          {selectedCategory && (
            <div style={{ marginTop: '12px', fontSize: '0.85rem', color: 'var(--text-muted)', backgroundColor: 'var(--bg)', padding: '10px 14px', borderRadius: '6px' }}>
              <strong>Points: </strong>
              <span style={{ color: 'var(--success)', fontWeight: 600 }}>+{selectedCategory.points} Points</span>
              {selectedCategory.description && (
                <span style={{ marginLeft: '12px' }}>{selectedCategory.description}</span>
              )}
            </div>
          )}
        </div>

        {/* Section 2: Standard Details */}
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <h2 style={{ fontSize: '1.1rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
            2. Achievement Details
          </h2>

          <div>
            <label htmlFor="ach-title" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
              Title / Honor Name *
            </label>
            <input
              id="ach-title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. First Prize in National Level Web Hackathon 2026"
              required
              style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: `1px solid ${fieldErrors.title ? 'var(--danger)' : 'var(--border)'}`, fontSize: '0.95rem', outline: 'none' }}
            />
            {fieldErrors.title && <div style={{ color: 'var(--danger)', fontSize: '0.8rem', marginTop: '4px' }}>{fieldErrors.title}</div>}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div>
              <label htmlFor="ach-level" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
                Competition / Achievement Level *
              </label>
              <select
                id="ach-level"
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
              <label htmlFor="ach-date" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
                Event / Award Date *
              </label>
              <input
                id="ach-date"
                type="date"
                value={eventDate}
                max={new Date().toISOString().split('T')[0]}
                onChange={(e) => setEventDate(e.target.value)}
                required
                style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: `1px solid ${fieldErrors.eventDate ? 'var(--danger)' : 'var(--border)'}`, fontSize: '0.95rem', outline: 'none' }}
              />
              {fieldErrors.eventDate && <div style={{ color: 'var(--danger)', fontSize: '0.8rem', marginTop: '4px' }}>{fieldErrors.eventDate}</div>}
            </div>
          </div>

          <div>
            <label htmlFor="ach-organizer" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
              Organizing Authority / Institution *
            </label>
            <input
              id="ach-organizer"
              type="text"
              value={organizer}
              onChange={(e) => setOrganizer(e.target.value)}
              placeholder="e.g. IIT Bombay / AICTE / IEEE Hyderabad Section"
              required
              style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: `1px solid ${fieldErrors.organizer ? 'var(--danger)' : 'var(--border)'}`, fontSize: '0.95rem', outline: 'none' }}
            />
            {fieldErrors.organizer && <div style={{ color: 'var(--danger)', fontSize: '0.8rem', marginTop: '4px' }}>{fieldErrors.organizer}</div>}
          </div>

          <div>
            <label htmlFor="ach-desc" style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, marginBottom: '6px', color: 'var(--navy-900)' }}>
              Description / Summary (Optional)
            </label>
            <textarea
              id="ach-desc"
              rows="3"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Brief details regarding the competition, project presented, or rank achieved..."
              style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.95rem', outline: 'none', resize: 'vertical' }}
            />
          </div>
        </div>

        {/* Section 3: Dynamic Category Fields */}
        {schemaFields.length > 0 && (
          <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <h2 style={{ fontSize: '1.1rem', color: 'var(--navy-900)', marginBottom: '4px' }}>
              3. Category Specific Information
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
                    placeholder={field.placeholder || `Enter ${field.label || field.name}`}
                    style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border)', fontSize: '0.95rem', outline: 'none' }}
                  />
                )}
              </div>
            ))}
          </div>
        )}

        {/* Section 4: Document Proof */}
        <div style={{ backgroundColor: 'var(--surface)', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
          <label style={{ display: 'block', fontWeight: 600, color: 'var(--navy-900)', marginBottom: '6px' }}>
            {schemaFields.length > 0 ? '4' : '3'}. Certificate / Proof Document *
          </label>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '14px' }}>
            Upload a clear scan or official certificate (PDF, JPG, or PNG up to 10MB).
          </p>

          <input
            id="file-upload-input"
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            onChange={handleFileChange}
            required
            style={{ display: 'block', width: '100%', padding: '10px', borderRadius: '6px', border: '1px dashed var(--border)', backgroundColor: 'var(--bg)', cursor: 'pointer' }}
          />

          {file && (
            <div style={{ marginTop: '16px', padding: '12px', backgroundColor: 'var(--bg)', borderRadius: '6px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <div style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--navy-900)' }}>{file.name}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{formatBytes(file.size)}</div>
              </div>
              <span style={{ color: 'var(--success)', fontSize: '0.85rem', fontWeight: 600 }}>✓ Ready for upload</span>
            </div>
          )}

          {filePreview && (
            <div style={{ marginTop: '14px', textAlign: 'center' }}>
              <img
                src={filePreview}
                alt="Certificate preview"
                style={{ maxWidth: '100%', maxHeight: '240px', objectFit: 'contain', borderRadius: '6px', border: '1px solid var(--border)' }}
              />
            </div>
          )}
        </div>

        {/* Submit Actions */}
        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '12px' }}>
          <button
            type="button"
            onClick={() => navigate('/student/dashboard')}
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
              backgroundColor: 'var(--navy-900)',
              color: '#fff',
              fontSize: '0.95rem',
              fontWeight: 600,
              cursor: submitting ? 'not-allowed' : 'pointer',
              opacity: submitting ? 0.75 : 1
            }}
          >
            {submitting ? 'Submitting...' : 'Submit for Verification'}
          </button>
        </div>
      </form>
    </div>
  );
}
