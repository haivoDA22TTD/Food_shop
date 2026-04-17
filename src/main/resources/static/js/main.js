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


// Logout function with API call
async function logout() {
    const token = localStorage.getItem('token');
    
    if (!token) {
        localStorage.clear();
        window.location.reload();
        return;
    }
    
    try {
        // Call logout API to blacklist token
        const response = await fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            console.log('Logout response:', data);
            if (window.toast) {
                toast.success('Đăng xuất thành công!');
            }
        } else {
            console.error('Logout failed:', response.status);
            if (window.toast) {
                toast.warning('Đã đăng xuất (offline)');
            }
        }
    } catch (error) {
        console.error('Logout error:', error);
        if (window.toast) {
            toast.warning('Đã đăng xuất (offline)');
        }
    } finally {
        // Always clear local storage and reload
        localStorage.clear();
        setTimeout(() => window.location.reload(), 500);
    }
}

// Check login status and update UI
async function checkLoginStatus() {
    try {
        // Call API to check if user is authenticated via cookie
        const response = await fetch('/api/auth/me', {
            credentials: 'include' // Important: send cookies
        });
        
        if (response.ok) {
            const data = await response.json();
            const username = data.username;
            const role = data.role;
            
            // Update UI
            const loginLink = document.getElementById('loginLink');
            const adminLink = document.getElementById('adminLink');
            const profileLink = document.getElementById('profileLink');
            
            if (loginLink) {
                loginLink.textContent = '👤 ' + username;
                loginLink.href = '#';
                loginLink.onclick = function(e) {
                    e.preventDefault();
                    if (confirm('Bạn có muốn đăng xuất?')) {
                        logoutWithCookie();
                    }
                };
            }
            
            // Show profile link for logged in users
            if (profileLink) {
                profileLink.style.display = 'inline-block';
            }
            
            // Show admin link if user is admin
            if (role && role.includes('ADMIN') && adminLink) {
                adminLink.style.display = 'inline-block';
            }
            
            // Store in localStorage for backward compatibility
            localStorage.setItem('username', username);
            localStorage.setItem('role', role);
        } else {
            // Not authenticated, check localStorage for old token
            const token = localStorage.getItem('token');
            const username = localStorage.getItem('username');
            const role = localStorage.getItem('role');
            
            if (token && username) {
                // Old token-based auth
                const loginLink = document.getElementById('loginLink');
                const adminLink = document.getElementById('adminLink');
                const profileLink = document.getElementById('profileLink');
                
                if (loginLink) {
                    loginLink.textContent = '👤 ' + username;
                    loginLink.href = '#';
                    loginLink.onclick = function(e) {
                        e.preventDefault();
                        if (confirm('Bạn có muốn đăng xuất?')) {
                            logout();
                        }
                    };
                }
                
                if (profileLink) {
                    profileLink.style.display = 'inline-block';
                }
                
                if (role && role.includes('ADMIN') && adminLink) {
                    adminLink.style.display = 'inline-block';
                }
            }
        }
    } catch (error) {
        console.error('Error checking login status:', error);
    }
}

// Logout function for cookie-based auth
async function logoutWithCookie() {
    try {
        // Get token from cookie
        const token = getCookie('auth_token');
        
        if (token) {
            // Call logout API to blacklist token
            const response = await fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                credentials: 'include'
            });
            
            if (response.ok) {
                if (window.toast) {
                    toast.success('Đăng xuất thành công!');
                }
            }
        }
    } catch (error) {
        console.error('Logout error:', error);
    } finally {
        // Clear cookie by setting expiry to past
        document.cookie = 'auth_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        localStorage.clear();
        setTimeout(() => window.location.href = '/', 500);
    }
}

// Helper function to get cookie value
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

// Call on page load
checkLoginStatus();
