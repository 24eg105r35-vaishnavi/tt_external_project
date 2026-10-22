// Student Achievement Portal - Dynamic Upload & Client Validation
document.addEventListener('DOMContentLoaded', () => {
    const categorySelect = document.getElementById('categoryId');
    const extraFieldsContainer = document.getElementById('extraFieldsContainer');
    const fileInput = document.getElementById('proofFile');
    const filePreview = document.getElementById('filePreview');
    const previewName = document.getElementById('previewName');
    const previewImg = document.getElementById('previewImg');
    const clientFileError = document.getElementById('clientFileError');
    const submitBtn = document.getElementById('submitBtn');

    // 1. Dynamic category field builder
    if (categorySelect) {
        categorySelect.addEventListener('change', () => {
            const categoryId = categorySelect.value;
            extraFieldsContainer.innerHTML = '';

            if (!categoryId) {
                return;
            }

            fetch(`/student/achievements/fields?categoryId=${categoryId}`)
                .then(response => response.json())
                .then(fields => {
                    extraFieldsContainer.innerHTML = '';
                    if (!fields || fields.length === 0) {
                        return;
                    }

                    const titleHeader = document.createElement('h3');
                    titleHeader.className = 'form-section-title';
                    titleHeader.textContent = 'Category-Specific Information';
                    extraFieldsContainer.appendChild(titleHeader);

                    fields.forEach(field => {
                        const formGroup = document.createElement('div');
                        formGroup.className = 'form-group';

                        const label = document.createElement('label');
                        label.htmlFor = `extra_${field.name}`;
                        label.innerHTML = field.label + (field.required ? ' <span style="color:var(--danger-color)">*</span>' : '');
                        formGroup.appendChild(label);

                        let inputElement;

                        if (field.type === 'select' && field.options) {
                            inputElement = document.createElement('select');
                            inputElement.className = 'form-control';
                            inputElement.id = `extra_${field.name}`;
                            inputElement.name = field.name;
                            if (field.required) {
                                inputElement.required = true;
                            }

                            const placeholderOption = document.createElement('option');
                            placeholderOption.value = '';
                            placeholderOption.textContent = `-- Select ${field.label} --`;
                            inputElement.appendChild(placeholderOption);

                            field.options.forEach(opt => {
                                const option = document.createElement('option');
                                option.value = opt;
                                option.textContent = opt;
                                inputElement.appendChild(option);
                            });

                        } else if (field.type === 'number') {
                            inputElement = document.createElement('input');
                            inputElement.type = 'number';
                            inputElement.className = 'form-control';
                            inputElement.id = `extra_${field.name}`;
                            inputElement.name = field.name;
                            inputElement.placeholder = `Enter ${field.label}`;
                            if (field.required) {
                                inputElement.required = true;
                            }
                        } else {
                            inputElement = document.createElement('input');
                            inputElement.type = 'text';
                            inputElement.className = 'form-control';
                            inputElement.id = `extra_${field.name}`;
                            inputElement.name = field.name;
                            inputElement.placeholder = `Enter ${field.label}`;
                            if (field.required) {
                                inputElement.required = true;
                            }
                        }

                        formGroup.appendChild(inputElement);
                        extraFieldsContainer.appendChild(formGroup);
                    });
                })
                .catch(err => {
                    console.error('Failed to load category fields:', err);
                });
        });

        // Trigger change on page load if category is already selected (e.g. on validation reload)
        if (categorySelect.value) {
            categorySelect.dispatchEvent(new Event('change'));
        }
    }

    // 2. Client-side file validation and preview
    if (fileInput) {
        fileInput.addEventListener('change', () => {
            clientFileError.textContent = '';
            clientFileError.style.display = 'none';
            filePreview.style.display = 'none';
            previewImg.style.display = 'none';
            previewImg.src = '';

            const file = fileInput.files[0];
            if (!file) {
                return;
            }

            const fileName = file.name;
            const fileSizeMB = (file.size / (1024 * 1024)).toFixed(2);
            const extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
            const allowedExtensions = ['.pdf', '.jpg', '.jpeg', '.png'];

            // Validate extension
            if (!allowedExtensions.includes(extension)) {
                clientFileError.textContent = 'Invalid file type. Please upload a PDF, PNG, or JPG file.';
                clientFileError.style.display = 'block';
                fileInput.value = '';
                return;
            }

            // Validate file size (<= 10MB)
            if (file.size > 10 * 1024 * 1024) {
                clientFileError.textContent = `File size (${fileSizeMB} MB) exceeds maximum allowed limit of 10 MB.`;
                clientFileError.style.display = 'block';
                fileInput.value = '';
                return;
            }

            // Show preview
            filePreview.style.display = 'block';
            previewName.innerHTML = `<strong>Selected:</strong> ${fileName} (${fileSizeMB} MB)`;

            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewImg.src = e.target.result;
                    previewImg.style.display = 'block';
                };
                reader.readAsDataURL(file);
            }
        });
    }
});
