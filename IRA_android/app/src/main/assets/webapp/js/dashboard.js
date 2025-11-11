// Dashboard JavaScript for Counselor Interface

document.addEventListener('DOMContentLoaded', function() {
    // Track which students have been loaded
    const loadedStudents = new Set();

    // Listen for accordion expansion
    const accordionButtons = document.querySelectorAll('.accordion-button');
    accordionButtons.forEach(button => {
        button.addEventListener('click', function() {
            const studentId = this.getAttribute('data-student-id');
            
            // Only load once
            if (!loadedStudents.has(studentId)) {
                loadStudentDetails(studentId);
                loadedStudents.add(studentId);
            }
        });
    });
    
    // Load notifications
    loadNotifications();
    
    // Refresh notifications every 30 seconds
    setInterval(loadNotifications, 30000);
});

function loadStudentDetails(studentId) {
    const loadingEl = document.getElementById(`loading${studentId}`);
    const detailsEl = document.getElementById(`details${studentId}`);
    
    // Show loading
    if (loadingEl) loadingEl.style.display = 'block';
    if (detailsEl) detailsEl.style.display = 'none';
    
    // Fetch student details
    fetch(`/student_details/${studentId}`)
        .then(response => response.json())
        .then(data => {
            // Hide loading
            if (loadingEl) loadingEl.style.display = 'none';
            if (detailsEl) detailsEl.style.display = 'block';
            
            // Render mood chart
            renderMoodChart(studentId, data.moods);
            
            // Render sleep data
            renderSleepData(studentId, data.activities);
            
            // Render attendance data
            renderAttendanceData(studentId, data.attendance);
        })
        .catch(error => {
            console.error('Error loading student details:', error);
            if (loadingEl) {
                loadingEl.innerHTML = '<div class="alert alert-danger">Failed to load data</div>';
            }
        });
}

function renderMoodChart(studentId, moods) {
    const ctx = document.getElementById(`moodChart${studentId}`);
    if (!ctx) return;
    
    // Check if Chart.js is available
    if (typeof Chart === 'undefined') {
        console.error('Chart.js is not loaded');
        ctx.parentElement.innerHTML = '<div class="alert alert-warning">Chart library not loaded</div>';
        return;
    }
    
    // Prepare data
    const labels = moods.map(m => m.date).reverse();
    const data = moods.map(m => m.avg_mood).reverse();
    
    // Create chart
    try {
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Mood Score',
                    data: data,
                    borderColor: '#0dcaf0',
                    backgroundColor: 'rgba(13, 202, 240, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 10,
                        ticks: {
                            stepSize: 2
                        }
                    },
                    x: {
                        display: false
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error creating chart:', error);
        ctx.parentElement.innerHTML = '<div class="alert alert-warning">Failed to create chart</div>';
    }
}

function renderSleepData(studentId, activities) {
    const container = document.getElementById(`sleepData${studentId}`);
    if (!container) return;
    
    if (activities.length === 0) {
        container.innerHTML = '<p class="text-muted small mb-0">No data</p>';
        return;
    }
    
    // Calculate average sleep
    const avgSleep = activities.reduce((sum, a) => sum + a.sleep_hours, 0) / activities.length;
    
    // Create simple visualization
    container.innerHTML = `
        <div class="text-center">
            <h4 class="mb-1">${avgSleep.toFixed(1)}h</h4>
            <p class="small text-muted mb-2">Avg Sleep</p>
            <div class="progress" style="height: 8px;">
                <div class="progress-bar ${avgSleep >= 7 ? 'bg-success' : avgSleep >= 6 ? 'bg-warning' : 'bg-danger'}" 
                     style="width: ${(avgSleep / 10) * 100}%"></div>
            </div>
            <small class="text-muted">${activities.length} days</small>
        </div>
    `;
}

function renderAttendanceData(studentId, attendance) {
    const container = document.getElementById(`attendanceData${studentId}`);
    if (!container) return;
    
    if (attendance.length === 0) {
        container.innerHTML = '<p class="text-muted small mb-0">No data</p>';
        return;
    }
    
    // Get latest attendance
    const latest = attendance[0];
    
    // Create visualization
    container.innerHTML = `
        <div class="text-center">
            <h4 class="mb-1">${latest.attendance_percentage.toFixed(1)}%</h4>
            <p class="small text-muted mb-2">${latest.month}</p>
            <div class="progress" style="height: 8px;">
                <div class="progress-bar ${latest.attendance_percentage >= 85 ? 'bg-success' : latest.attendance_percentage >= 75 ? 'bg-warning' : 'bg-danger'}" 
                     style="width: ${latest.attendance_percentage}%"></div>
            </div>
            <small class="text-muted">${latest.attended_classes}/${latest.total_classes} classes</small>
        </div>
    `;
}

// Search functionality (if needed)
function searchStudents() {
    const input = document.getElementById('studentSearch');
    if (!input) return;
    
    const filter = input.value.toUpperCase();
    const accordion = document.getElementById('studentsAccordion');
    const items = accordion.getElementsByClassName('accordion-item');
    
    for (let i = 0; i < items.length; i++) {
        const button = items[i].getElementsByClassName('accordion-button')[0];
        const txtValue = button.textContent || button.innerText;
        if (txtValue.toUpperCase().indexOf(filter) > -1) {
            items[i].style.display = '';
        } else {
            items[i].style.display = 'none';
        }
    }
}

// Export student list
function exportStudentList() {
    // Get all student data
    const students = [];
    const items = document.querySelectorAll('.accordion-item');
    
    items.forEach(item => {
        const button = item.querySelector('.accordion-button');
        const text = button.textContent.trim();
        students.push(text);
    });
    
    // Create CSV
    const csv = students.join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'student_risk_report.csv';
    a.click();
    window.URL.revokeObjectURL(url);
}

// Notification functions
function loadNotifications() {
    fetch('/notifications')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateNotificationBadge(data.unread_count);
                displayNotifications(data.notifications);
            }
        })
        .catch(error => console.error('Error loading notifications:', error));
}

function updateNotificationBadge(count) {
    const badge = document.getElementById('notificationBadge');
    if (badge) {
        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = 'inline-flex';
        } else {
            badge.style.display = 'none';
        }
    }
}

function displayNotifications(notifications) {
    const list = document.getElementById('notificationList');
    
    if (notifications.length === 0) {
        list.innerHTML = '<p class="text-center text-muted p-3">No notifications</p>';
        return;
    }
    
    list.innerHTML = '';
    notifications.forEach(notification => {
        const item = document.createElement('div');
        item.className = `notification-item ${notification.is_read ? '' : 'unread'}`;
        
        item.innerHTML = `
            <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1">
                    <h6 class="mb-1 small fw-bold">${notification.title}</h6>
                    <p class="mb-1 small text-muted">${notification.message}</p>
                    <small class="text-muted"><i class="bi bi-clock"></i> ${formatDate(notification.created_at)}</small>
                </div>
                ${!notification.is_read ? '<span class="badge bg-danger">New</span>' : ''}
            </div>
        `;
        
        item.addEventListener('click', () => {
            markNotificationRead(notification.id);
            
            // If there's a reference_id, it's a student-related notification
            if (notification.reference_id) {
                const studentId = notification.reference_id;
                toggleNotifications(); // Close notification panel
                
                // Scroll to and open the student accordion
                const studentAccordion = document.getElementById(`student${studentId}`);
                if (studentAccordion) {
                    // Check if Bootstrap is available
                    if (typeof bootstrap !== 'undefined' && bootstrap.Collapse) {
                        try {
                            // Collapse all accordions first
                            document.querySelectorAll('.accordion-collapse').forEach(acc => {
                                if (acc !== studentAccordion) {
                                    const bsCollapse = bootstrap.Collapse.getInstance(acc);
                                    if (bsCollapse) bsCollapse.hide();
                                }
                            });
                            
                            // Open the target accordion
                            const bsCollapse = new bootstrap.Collapse(studentAccordion, { toggle: true });
                        } catch (error) {
                            console.error('Error toggling accordion:', error);
                        }
                    }
                    
                    // Scroll to the accordion with a slight delay
                    setTimeout(() => {
                        const accordionItem = studentAccordion.closest('.accordion-item');
                        if (accordionItem) {
                            accordionItem.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        }
                    }, 400);
                }
            } else if (notification.link) {
                window.location.href = notification.link;
            }
        });
        
        list.appendChild(item);
    });
}

function toggleNotifications() {
    const panel = document.getElementById('notificationPanel');
    const backdrop = document.getElementById('notificationBackdrop');
    
    if (panel.style.display === 'none' || panel.style.display === '') {
        panel.style.display = 'block';
        
        // Create backdrop if it doesn't exist
        if (!backdrop) {
            const newBackdrop = document.createElement('div');
            newBackdrop.id = 'notificationBackdrop';
            newBackdrop.style.cssText = 'position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.3); z-index: 9998; display: block;';
            newBackdrop.onclick = toggleNotifications;
            document.body.appendChild(newBackdrop);
        } else {
            backdrop.style.display = 'block';
        }
    } else {
        panel.style.display = 'none';
        if (backdrop) {
            backdrop.style.display = 'none';
        }
    }
}

function markNotificationRead(notificationId) {
    fetch(`/mark_notification_read/${notificationId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            loadNotifications();
        }
    })
    .catch(error => console.error('Error marking notification as read:', error));
}

function formatDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return 'Just now';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} minutes ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`;
    if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)} days ago`;
    
    return date.toLocaleDateString();
}

// Schedule meeting with student
function scheduleMeetingWithStudent(studentId, studentName) {
    if (!confirm(`Schedule a counseling session with ${studentName}?`)) {
        return;
    }
    
    fetch(`/schedule_meeting_for_student/${studentId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Show success snackbar
            const snackbar = document.createElement('div');
            snackbar.className = 'alert alert-success position-fixed bottom-0 end-0 m-3';
            snackbar.style.zIndex = '9999';
            snackbar.innerHTML = `
                <i class="bi bi-check-circle"></i> ${data.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            `;
            document.body.appendChild(snackbar);
            setTimeout(() => snackbar.remove(), 5000);
            
            // Reload page to update meetings list
            setTimeout(() => location.reload(), 2000);
        } else {
            alert('Error: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to schedule meeting. Please try again.');
    });
}

// Close notification panel when clicking outside
document.addEventListener('click', function(event) {
    const panel = document.getElementById('notificationPanel');
    const button = event.target.closest('[onclick="toggleNotifications()"]');
    
    if (!button && !panel.contains(event.target) && panel.style.display === 'block') {
        panel.style.display = 'none';
        const backdrop = document.getElementById('notificationBackdrop');
        if (backdrop) {
            backdrop.style.display = 'none';
        }
    }
});
