// Main JavaScript file for Beacon

// Auto-dismiss alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert:not(.alert-info)');
    alerts.forEach(alert => {
        setTimeout(() => {
            // Check if bootstrap.Alert is available before using it
            if (typeof bootstrap !== 'undefined' && bootstrap.Alert && alert) {
                try {
                    const bsAlert = new bootstrap.Alert(alert);
                    bsAlert.close();
                } catch (e) {
                    console.error('Error closing alert:', e);
                }
            }
        }, 5000);
    });
});

// Smooth scroll to top
function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

// Show loading spinner
function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-primary"></div></div>';
    }
}

// Hide loading spinner
function hideLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.style.display = 'none';
    }
}

// Format date to readable string
function formatDate(dateString) {
    const date = new Date(dateString);
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

// Show snackbar notification
function showSnackbar(message, type = 'success') {
    const snackbar = document.createElement('div');
    snackbar.className = `alert alert-${type} position-fixed bottom-0 end-0 m-3 snackbar`;
    snackbar.style.zIndex = '9999';
    snackbar.style.minWidth = '300px';
    snackbar.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-circle'} me-2"></i>
            <span>${message}</span>
            <button type="button" class="btn-close ms-auto" onclick="this.parentElement.parentElement.remove()"></button>
        </div>
    `;
    document.body.appendChild(snackbar);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (snackbar && snackbar.parentElement) {
            snackbar.remove();
        }
    }, 5000);
}

// Initialize tooltips
document.addEventListener('DOMContentLoaded', function() {
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        try {
            const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
            tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl);
            });
        } catch (error) {
            console.error('Error initializing tooltips:', error);
        }
    }
});

// Initialize popovers
document.addEventListener('DOMContentLoaded', function() {
    if (typeof bootstrap !== 'undefined' && bootstrap.Popover) {
        try {
            const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
            popoverTriggerList.map(function (popoverTriggerEl) {
                return new bootstrap.Popover(popoverTriggerEl);
            });
        } catch (error) {
            console.error('Error initializing popovers:', error);
        }
    }
});

// Confirm action
function confirmAction(message) {
    return confirm(message);
}

// Copy to clipboard
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showSnackbar('Copied to clipboard!', 'success');
    }).catch(err => {
        console.error('Failed to copy:', err);
        showSnackbar('Failed to copy', 'danger');
    });
}

// Export to CSV
function exportToCSV(data, filename) {
    const csv = data.map(row => row.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
}

// Print page
function printPage() {
    window.print();
}

// Console welcome message
console.log('%cBeacon 🔆', 'font-size: 24px; color: #0d6efd; font-weight: bold;');
console.log('%cStudent Dropout Prevention System', 'font-size: 14px; color: #6c757d;');
console.log('%cBuilt with Flask & Bootstrap', 'font-size: 12px; color: #adb5bd;');
