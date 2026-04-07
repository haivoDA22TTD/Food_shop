// ============================================
// Chatbot JavaScript
// ============================================

class Chatbot {
    constructor() {
        this.isOpen = false;
        this.messages = [];
        this.init();
    }

    init() {
        this.createChatUI();
        this.attachEventListeners();
        this.loadWelcomeMessage();
    }

    createChatUI() {
        // Create chat button
        const chatButton = document.createElement('button');
        chatButton.className = 'chat-button';
        chatButton.id = 'chatButton';
        chatButton.innerHTML = '🤖';
        document.body.appendChild(chatButton);

        // Create chat window
        const chatWindow = document.createElement('div');
        chatWindow.className = 'chat-window';
        chatWindow.id = 'chatWindow';
        chatWindow.innerHTML = `
            <div class="chat-header">
                <div class="chat-header-info">
                    <div class="chat-avatar">🤖</div>
                    <div class="chat-header-text">
                        <h3>Food Shop AI</h3>
                        <p>Trợ lý ảo của bạn</p>
                    </div>
                </div>
                <button class="chat-close" id="chatClose">×</button>
            </div>
            <div class="chat-messages" id="chatMessages"></div>
            <div class="chat-input-container">
                <div class="chat-input-wrapper">
                    <input 
                        type="text" 
                        class="chat-input" 
                        id="chatInput" 
                        placeholder="Nhập tin nhắn..."
                        autocomplete="off"
                    />
                    <button class="chat-send-btn" id="chatSend">
                        ➤
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(chatWindow);
    }

    attachEventListeners() {
        const chatButton = document.getElementById('chatButton');
        const chatClose = document.getElementById('chatClose');
        const chatSend = document.getElementById('chatSend');
        const chatInput = document.getElementById('chatInput');

        chatButton.addEventListener('click', () => this.toggleChat());
        chatClose.addEventListener('click', () => this.toggleChat());
        chatSend.addEventListener('click', () => this.sendMessage());
        chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
    }

    toggleChat() {
        this.isOpen = !this.isOpen;
        const chatWindow = document.getElementById('chatWindow');
        const chatButton = document.getElementById('chatButton');
        
        if (this.isOpen) {
            chatWindow.classList.add('active');
            chatButton.classList.add('active');
            chatButton.innerHTML = '×';
            document.getElementById('chatInput').focus();
        } else {
            chatWindow.classList.remove('active');
            chatButton.classList.remove('active');
            chatButton.innerHTML = '🤖';
        }
    }

    loadWelcomeMessage() {
        const welcomeMessage = {
            message: "Xin chào! 👋 Tôi là trợ lý AI của Food Shop.\n\nTôi có thể giúp bạn:\n• Tư vấn sản phẩm\n• Tìm món ăn phù hợp\n• Tạo mã giảm giá\n• Hỗ trợ đặt hàng\n\nBạn cần tôi giúp gì?",
            type: "text",
            suggestions: [
                "Có món gì ngon?",
                "Món nào dưới 50k?",
                "Cho tôi mã giảm giá"
            ]
        };
        this.addBotMessage(welcomeMessage);
    }

    async sendMessage() {
        const input = document.getElementById('chatInput');
        const message = input.value.trim();
        
        if (!message) return;

        // Add user message
        this.addUserMessage(message);
        input.value = '';

        // Show typing indicator
        this.showTypingIndicator();

        try {
            // Call API
            const response = await this.callChatAPI(message);
            
            // Remove typing indicator
            this.hideTypingIndicator();
            
            // Add bot response
            this.addBotMessage(response);
            
        } catch (error) {
            console.error('Chat error:', error);
            this.hideTypingIndicator();
            this.addBotMessage({
                message: "Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.",
                type: "error"
            });
        }
    }

    async callChatAPI(message) {
        const token = localStorage.getItem('token');
        
        const response = await fetch('/api/chatbot/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            },
            body: JSON.stringify({ message })
        });

        if (response.status === 429) {
            // Rate limit exceeded
            const data = await response.json();
            throw new Error(data.message || 'Bạn đang gửi tin nhắn quá nhanh. Vui lòng chờ một chút.');
        }

        if (!response.ok) {
            throw new Error('API call failed');
        }

        return await response.json();
    }

    addUserMessage(message) {
        const messagesContainer = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message user';
        messageDiv.innerHTML = `
            <div class="message-content">
                <div class="message-bubble">${this.escapeHtml(message)}</div>
                <div class="message-time">${this.getCurrentTime()}</div>
            </div>
            <div class="message-avatar">👤</div>
        `;
        messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }

    addBotMessage(response) {
        const messagesContainer = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message bot';
        
        let content = `
            <div class="message-avatar">🤖</div>
            <div class="message-content">
                <div class="message-bubble">${this.formatMessage(response.message)}</div>
        `;

        // Add products if available
        if (response.data && response.data.products) {
            content += this.renderProducts(response.data.products);
        }

        // Add voucher if available
        if (response.data && response.data.voucher) {
            content += this.renderVoucher(response.data.voucher);
        }

        // Add suggestions
        if (response.suggestions && response.suggestions.length > 0) {
            content += this.renderSuggestions(response.suggestions);
        }

        content += `
                <div class="message-time">${this.getCurrentTime()}</div>
            </div>
        `;

        messageDiv.innerHTML = content;
        messagesContainer.appendChild(messageDiv);
        
        // Attach suggestion click handlers
        this.attachSuggestionHandlers(messageDiv);
        
        this.scrollToBottom();
    }

    renderProducts(products) {
        let html = '';
        products.forEach(product => {
            const imageUrl = product.image.startsWith('http') 
                ? product.image 
                : `/img/${product.image}`;
            
            html += `
                <div class="chat-product-card">
                    <img src="${imageUrl}" alt="${product.name}" class="chat-product-image" />
                    <div class="chat-product-info">
                        <div class="chat-product-name">${product.name}</div>
                        <div class="chat-product-price">${this.formatPrice(product.price)}</div>
                        <button class="chat-product-btn" onclick="window.location.href='/product/${product.id}'">
                            Xem chi tiết
                        </button>
                    </div>
                </div>
            `;
        });
        return html;
    }

    renderVoucher(voucher) {
        const discountText = voucher.discountType === 'PERCENTAGE' 
            ? `${voucher.discountValue}%` 
            : this.formatPrice(voucher.discountValue);
        
        return `
            <div class="chat-voucher-card">
                <div style="text-align: center; margin-bottom: 8px;">🎁 Mã giảm giá</div>
                <div class="voucher-code">${voucher.code}</div>
                <div class="voucher-info">
                    • Giảm: ${discountText}<br/>
                    • Đơn tối thiểu: ${this.formatPrice(voucher.minOrderValue)}<br/>
                    • Hết hạn: ${this.formatDateTime(voucher.expiresAt)}
                </div>
            </div>
        `;
    }

    renderSuggestions(suggestions) {
        let html = '<div class="chat-suggestions">';
        suggestions.forEach(suggestion => {
            html += `<button class="suggestion-btn" data-suggestion="${this.escapeHtml(suggestion)}">${this.escapeHtml(suggestion)}</button>`;
        });
        html += '</div>';
        return html;
    }

    attachSuggestionHandlers(messageDiv) {
        const suggestions = messageDiv.querySelectorAll('.suggestion-btn');
        suggestions.forEach(btn => {
            btn.addEventListener('click', () => {
                const suggestion = btn.getAttribute('data-suggestion');
                document.getElementById('chatInput').value = suggestion;
                this.sendMessage();
            });
        });
    }

    showTypingIndicator() {
        const messagesContainer = document.getElementById('chatMessages');
        const typingDiv = document.createElement('div');
        typingDiv.className = 'chat-message bot';
        typingDiv.id = 'typingIndicator';
        typingDiv.innerHTML = `
            <div class="message-avatar">🤖</div>
            <div class="message-content">
                <div class="message-bubble">
                    <div class="typing-indicator">
                        <div class="typing-dot"></div>
                        <div class="typing-dot"></div>
                        <div class="typing-dot"></div>
                    </div>
                </div>
            </div>
        `;
        messagesContainer.appendChild(typingDiv);
        this.scrollToBottom();
    }

    hideTypingIndicator() {
        const typingIndicator = document.getElementById('typingIndicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('chatMessages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    formatMessage(message) {
        return message.replace(/\n/g, '<br/>');
    }

    formatPrice(price) {
        return new Intl.NumberFormat('vi-VN').format(price) + 'đ';
    }

    formatDateTime(dateTimeStr) {
        const date = new Date(dateTimeStr);
        return date.toLocaleString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    getCurrentTime() {
        const now = new Date();
        return now.toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize chatbot when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.chatbot = new Chatbot();
});
