// upload.js - File drag and drop handling

document.addEventListener('DOMContentLoaded', () => {
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('resume-upload');
    const fileInfo = document.getElementById('file-info');
    const fileName = document.getElementById('file-name');
    const fileSize = document.getElementById('file-size');
    const removeBtn = document.getElementById('remove-file');
    const fileError = document.getElementById('file-error');

    if (!dropZone || !fileInput) return;

    // Prevent default drag behaviors
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, preventDefaults, false);
        document.body.addEventListener(eventName, preventDefaults, false);
    });

    // Highlight drop zone when item is dragged over it
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, highlight, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, unhighlight, false);
    });

    // Handle dropped files
    dropZone.addEventListener('drop', handleDrop, false);

    // Handle file input change
    fileInput.addEventListener('change', function() {
        handleFiles(this.files);
    });

    // Remove file
    removeBtn.addEventListener('click', () => {
        fileInput.value = '';
        fileInfo.style.display = 'none';
        fileError.style.display = 'none';
        dropZone.style.display = 'block';
    });

    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    function highlight(e) {
        dropZone.classList.add('dragover');
    }

    function unhighlight(e) {
        dropZone.classList.remove('dragover');
    }

    function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        handleFiles(files);
    }

    function handleFiles(files) {
        fileError.style.display = 'none';
        
        if (files.length === 0) return;
        
        const file = files[0];
        
        // Basic validation in UI before submission validation
        const validTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain'];
        const maxSize = 10 * 1024 * 1024; // 10MB

        if (!validTypes.includes(file.type) && !file.name.match(/\.(pdf|docx|txt)$/i)) {
            showError("Invalid file type. Please upload a PDF, DOCX, or TXT file.");
            return;
        }

        if (file.size > maxSize) {
            showError("File is too large. Maximum size is 10 MB.");
            return;
        }

        // Update UI
        fileName.textContent = file.name;
        fileSize.textContent = formatBytes(file.size);
        
        // Hide drop zone, show file info
        dropZone.style.display = 'none';
        fileInfo.style.display = 'block';
        
        // Update input file object (for drag and drop)
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        fileInput.files = dataTransfer.files;
    }

    function showError(msg) {
        fileError.textContent = msg;
        fileError.style.display = 'block';
        fileInput.value = '';
    }

    function formatBytes(bytes, decimals = 2) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    }
});
