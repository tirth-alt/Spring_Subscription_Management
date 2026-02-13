// ===== Documents Module =====
const Documents = {
    // Current entity context (optional)
    currentEntityType: 'USER',
    currentEntityId: 'current',

    // Initialize documents module
    init: function () {
        // Document upload handler
        const uploadForm = document.getElementById('uploadDocumentForm');
        if (uploadForm) {
            uploadForm.addEventListener('submit', (e) => this.handleUpload(e));
        }
    },

    // Load documents
    loadDocuments: async function () {
        const container = document.getElementById('documentsList');
        if (!container) return;

        container.innerHTML = '<tr><td colspan="5" class="loading">Loading documents...</td></tr>';

        const result = await API.getUserDocuments();

        if (result.success && result.data.data && result.data.data.length > 0) {
            container.innerHTML = result.data.data.map(doc => `
                <tr>
                    <td>
                        <div class="file-info">
                            <span class="file-icon">📄</span>
                            <span>${doc.fileName}</span>
                        </div>
                    </td>
                    <td>${doc.entityType}</td>
                    <td>${UI.formatDate(doc.uploadedAt)}</td>
                    <td>${this.formatFileSize(doc.fileSize)}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-outline btn-sm" onclick="Documents.downloadDocument('${doc.id}', '${doc.fileName}')">Download</button>
                            <button class="btn btn-danger btn-sm" onclick="Documents.deleteDocument('${doc.id}')">Delete</button>
                        </div>
                    </td>
                </tr>
            `).join('');
        } else {
            container.innerHTML = '<tr><td colspan="5" class="no-data">No documents found. Upload one below!</td></tr>';
        }
    },

    // Handle file upload
    handleUpload: async function (e) {
        e.preventDefault();

        const fileInput = document.getElementById('documentFile');
        const entityTypeInput = document.getElementById('documentType');
        const submitBtn = document.getElementById('uploadBtn');

        if (!fileInput.files || fileInput.files.length === 0) {
            UI.showToast('Please select a file');
            return;
        }

        const file = fileInput.files[0];
        const entityType = entityTypeInput ? entityTypeInput.value : 'USER_UPLOAD';

        // Show loading state
        if (submitBtn) {
            submitBtn.textContent = 'Uploading...';
            submitBtn.disabled = true;
        }

        try {
            // Using 'self' as entityId for user uploads
            const result = await API.uploadDocument(file, entityType, 'self');

            if (result.success) {
                UI.showToast('File uploaded successfully');
                document.getElementById('uploadDocumentForm').reset();
                this.loadDocuments();
            } else {
                UI.showToast(result.error || 'Upload failed');
            }
        } catch (error) {
            console.error('Upload error:', error);
            UI.showToast('An error occurred during upload');
        } finally {
            if (submitBtn) {
                submitBtn.textContent = 'Upload';
                submitBtn.disabled = false;
            }
        }
    },

    // Download document
    downloadDocument: async function (id, fileName) {
        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${API_BASE_URL}/api/documents/download/${id}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                a.download = fileName;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
            } else {
                UI.showToast('Failed to download file');
            }
        } catch (error) {
            console.error('Download error:', error);
            UI.showToast('Error downloading file');
        }
    },

    // Delete document
    deleteDocument: async function (id) {
        if (!confirm('Are you sure you want to delete this document?')) return;

        const result = await API.deleteDocument(id);

        if (result.success) {
            UI.showToast('Document deleted successfully');
            this.loadDocuments();
        } else {
            UI.showToast(result.data?.message || 'Failed to delete document');
        }
    },

    // Format file size
    formatFileSize: function (bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
};
