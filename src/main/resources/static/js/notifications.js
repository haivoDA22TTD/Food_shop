// Toast Notification System
class ToastNotification {
    constructor() {
        this.container = this.createContainer();
    }
    
    createContainer() {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        return container;
    }
    
    show(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        const icons = {
            success: '✓',
            error: '✕',
            warning: '⚠',
            info: 'ℹ'
        };
        
        const titles = {
            success: 'Thành công',
            error: 'Lỗi',
            warning: 'Cảnh báo',
            info: 'Thông báo'
        };
        
        toast.innerHTML = `
            <div class="toast-icon">${icons[type]}</div>
            <div class="toast-content">
                <div class="toast-title">${titles[type]}</div>
                <div class="toast-message">${message}</div>
            </div>
            <button class="toast-close" onclick="this.parentElement.remove()">×</button>
        `;
        
        this.container.appendChild(toast);
        
        // Auto remove after duration
        setTimeout(() => {
            toast.classList.add('removing');
            setTimeout(() => toast.remove(), 300);
        }, duration);
        
        return toast;
    }
    
    success(message, duration) {
        return this.show(message, 'success', duration);
    }
    
    error(message, duration) {
        return this.show(message, 'error', duration);
    }
    
    warning(message, duration) {
        return this.show(message, 'warning', duration);
    }
    
    info(message, duration) {
        return this.show(message, 'info', duration);
    }
}

// Loading Overlay
class LoadingOverlay {
    constructor() {
        this.overlay = null;
    }
    
    show(message = 'Đang xử lý...') {
        if (this.overlay) return;
        
        this.overlay = document.createElement('div');
        this.overlay.className = 'loading-overlay';
        this.overlay.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner"></div>
                <div class="loading-text">${message}</div>
            </div>
        `;
        
        document.body.appendChild(this.overlay);
        document.body.style.overflow = 'hidden';
    }
    
    hide() {
        if (this.overlay) {
            this.overlay.remove();
            this.overlay = null;
            document.body.style.overflow = '';
        }
    }
}

// Success Modal
function showSuccessModal(title, message, callback) {
    const modal = document.createElement('div');
    modal.className = 'loading-overlay';
    modal.innerHTML = `
        <div class="loading-spinner" style="padding: 3rem;">
            <div class="success-checkmark">
                <div class="check-icon">
                    <span class="icon-line line-tip"></span>
                    <span class="icon-line line-long"></span>
                    <div class="icon-circle"></div>
                    <div class="icon-fix"></div>
                </div>
            </div>
            <h2 style="margin: 1.5rem 0 0.5rem; color: #1f2937; font-size: 1.5rem;">${title}</h2>
            <p style="color: #6b7280; margin-bottom: 1.5rem;">${message}</p>
            <button onclick="this.closest('.loading-overlay').remove(); document.body.style.overflow = '';" 
                    style="padding: 0.75rem 2rem; background: linear-gradient(135deg, #0ea5e9, #06b6d4); color: white; border: none; border-radius: 8px; font-weight: 600; cursor: pointer;">
                Đóng
            </button>
        </div>
    `;
    
    document.body.appendChild(modal);
    document.body.style.overflow = 'hidden';
    
    if (callback) {
        setTimeout(() => {
            modal.remove();
            document.body.style.overflow = '';
            callback();
        }, 3000);
    }
}

// Initialize global instances
const toast = new ToastNotification();
const loading = new LoadingOverlay();

// Make them globally available
window.toast = toast;
window.loading = loading;
window.showSuccessModal = showSuccessModal;
