// Passkey (WebAuthn) functionality

const PasskeyManager = {
    // Check if WebAuthn is supported
    isSupported() {
        return window.PublicKeyCredential !== undefined && 
               navigator.credentials !== undefined;
    },

    // Base64URL encoding/decoding helpers
    base64urlToBuffer(base64url) {
        const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
        const padLen = (4 - (base64.length % 4)) % 4;
        const padded = base64 + '='.repeat(padLen);
        const binary = atob(padded);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes.buffer;
    },

    bufferToBase64url(buffer) {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.length; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        const base64 = btoa(binary);
        return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    },

    // Register a new passkey
    async register(nickname = 'My Passkey') {
        if (!this.isSupported()) {
            throw new Error('Passkey không được hỗ trợ trên trình duyệt này');
        }

        try {
            // Get token from localStorage or cookie
            const token = localStorage.getItem('token') || this.getCookie('auth_token');
            if (!token) {
                throw new Error('Vui lòng đăng nhập trước');
            }

            const optionsResponse = await fetch('/api/passkey/register/options', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                credentials: 'include' // Send cookies
            });

            if (!optionsResponse.ok) {
                const error = await optionsResponse.json();
                throw new Error(error.error || 'Không thể tạo passkey');
            }

            const options = await optionsResponse.json();

            // Convert options to proper format
            const publicKeyOptions = {
                challenge: this.base64urlToBuffer(options.challenge),
                rp: options.rp,
                user: {
                    id: this.base64urlToBuffer(options.user.id),
                    name: options.user.name,
                    displayName: options.user.displayName
                },
                pubKeyCredParams: options.pubKeyCredParams,
                timeout: options.timeout,
                attestation: options.attestation,
                authenticatorSelection: options.authenticatorSelection
            };

            // Create credential
            const credential = await navigator.credentials.create({
                publicKey: publicKeyOptions
            });

            if (!credential) {
                throw new Error('Không thể tạo passkey');
            }

            // Prepare credential data for server
            const credentialData = {
                id: credential.id,
                rawId: this.bufferToBase64url(credential.rawId),
                type: credential.type,
                response: {
                    clientDataJSON: this.bufferToBase64url(credential.response.clientDataJSON),
                    attestationObject: this.bufferToBase64url(credential.response.attestationObject)
                }
            };

            // Send to server for verification
            const verifyResponse = await fetch('/api/passkey/register/verify', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                credentials: 'include', // Send cookies
                body: JSON.stringify({
                    credential: credentialData,
                    nickname: nickname
                })
            });

            if (!verifyResponse.ok) {
                const error = await verifyResponse.json();
                throw new Error(error.error || 'Không thể xác thực passkey');
            }

            const result = await verifyResponse.json();
            return result;

        } catch (error) {
            console.error('Passkey registration error:', error);
            throw error;
        }
    },

    // Login with passkey
    async login(username = null) {
        if (!this.isSupported()) {
            throw new Error('Passkey không được hỗ trợ trên trình duyệt này');
        }

        try {
            // Get authentication options from server
            const optionsResponse = await fetch('/api/passkey/login/options', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username: username })
            });

            if (!optionsResponse.ok) {
                const error = await optionsResponse.json();
                throw new Error(error.error || 'Không thể đăng nhập với passkey');
            }

            const options = await optionsResponse.json();

            // Convert options to proper format
            const publicKeyOptions = {
                challenge: this.base64urlToBuffer(options.challenge),
                timeout: options.timeout,
                rpId: options.rpId,
                userVerification: options.userVerification
            };

            if (options.allowCredentials && options.allowCredentials.length > 0) {
                publicKeyOptions.allowCredentials = options.allowCredentials.map(cred => ({
                    type: cred.type,
                    id: this.base64urlToBuffer(cred.id)
                }));
            }

            // Get credential
            const credential = await navigator.credentials.get({
                publicKey: publicKeyOptions
            });

            if (!credential) {
                throw new Error('Không thể xác thực passkey');
            }

            // Prepare credential data for server
            const credentialData = {
                id: credential.id,
                rawId: this.bufferToBase64url(credential.rawId),
                type: credential.type,
                response: {
                    clientDataJSON: this.bufferToBase64url(credential.response.clientDataJSON),
                    authenticatorData: this.bufferToBase64url(credential.response.authenticatorData),
                    signature: this.bufferToBase64url(credential.response.signature),
                    userHandle: credential.response.userHandle ? 
                        this.bufferToBase64url(credential.response.userHandle) : null
                }
            };

            // Send to server for verification
            const verifyResponse = await fetch('/api/passkey/login/verify', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    credential: credentialData
                })
            });

            if (!verifyResponse.ok) {
                const error = await verifyResponse.json();
                throw new Error(error.error || 'Không thể xác thực passkey');
            }

            const result = await verifyResponse.json();
            return result;

        } catch (error) {
            console.error('Passkey login error:', error);
            throw error;
        }
    },

    // Get list of user's passkeys
    async list() {
        const token = localStorage.getItem('token') || this.getCookie('auth_token');
        if (!token) {
            throw new Error('Vui lòng đăng nhập trước');
        }

        const response = await fetch('/api/passkey/list', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            credentials: 'include' // Send cookies
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Không thể lấy danh sách passkey');
        }

        return await response.json();
    },

    // Delete a passkey
    async delete(passkeyId) {
        const token = localStorage.getItem('token') || this.getCookie('auth_token');
        if (!token) {
            throw new Error('Vui lòng đăng nhập trước');
        }

        const response = await fetch(`/api/passkey/${passkeyId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            credentials: 'include' // Send cookies
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Không thể xóa passkey');
        }

        return await response.json();
    },
    
    // Helper to get cookie
    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    }
};

// Export for use in other scripts
window.PasskeyManager = PasskeyManager;
