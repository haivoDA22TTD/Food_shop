// Category Filter
document.addEventListener('DOMContentLoaded', function() {
    const categoryBtns = document.querySelectorAll('.category-btn');
    const productCards = document.querySelectorAll('.product-card');
    
    categoryBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const category = this.getAttribute('data-category');
            
            // Update active button
            categoryBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            
            // Filter products
            productCards.forEach(card => {
                if (category === 'all' || card.getAttribute('data-category') === category) {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    });
    
    // Add to cart functionality
    const addToCartBtns = document.querySelectorAll('.btn-add-cart');
    addToCartBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const productId = this.getAttribute('data-id');
            addToCart(productId);
        });
    });
});

function addToCart(productId) {
    let cart = JSON.parse(localStorage.getItem('cart') || '[]');
    
    const existingItem = cart.find(item => item.id === productId);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({ id: productId, quantity: 1 });
    }
    
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartCount();
    
    // Show toast notification instead of alert
    toast.success('Đã thêm sản phẩm vào giỏ hàng!');
}

function updateCartCount() {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    const cartCountElement = document.querySelector('.cart-count');
    if (cartCountElement) {
        cartCountElement.textContent = totalItems;
    }
}

// Update cart count on page load
updateCartCount();


// Check login status and update UI
function checkLoginStatus() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    
    const loginLink = document.getElementById('loginLink');
    const adminLink = document.getElementById('adminLink');
    
    if (token && username) {
        loginLink.textContent = '👤 ' + username;
        loginLink.href = '#';
        loginLink.onclick = function(e) {
            e.preventDefault();
            if (confirm('Bạn có muốn đăng xuất?')) {
                localStorage.clear();
                window.location.reload();
            }
        };
        
        // Show admin link if user is admin
        if (role && role.includes('ADMIN')) {
            adminLink.style.display = 'inline-block';
        }
    }
}

// Call on page load
checkLoginStatus();
