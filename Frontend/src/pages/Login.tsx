import { motion } from 'framer-motion'
import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import axios from '../api/axios'
import { useAuthStore } from '../store/authStore'
import { useCartStore } from '../store/cartStore'
import { extractErrorMessage, normalizeAuthPayload } from '../utils/auth'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const setAuth = useAuthStore((state) => state.setAuth)
  const syncCartWithServer = useCartStore((state) => state.syncCartWithServer)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setInfo('')
    setLoading(true)

    try {
      const response = await axios.post('/api/auth/login', { username, password })
      const payload = normalizeAuthPayload(response.data, username)
      if (!payload) throw new Error('Dang nhap thanh cong nhung phan hoi khong hop le.')

      setAuth(
        {
          id: payload.userId,
          username: payload.username,
          email: payload.email,
          role: payload.role,
        },
        payload.token
      )
      
      // Sync local cart with server
      await syncCartWithServer()
      
      // Role-based routing
      if (payload.role === 'ADMIN') {
        navigate('/admin/products')
      } else if (payload.role === 'SHIPPER') {
        navigate('/shipper/dashboard')
      } else {
        // Regular users - check for redirect parameter or go home
        const redirect = searchParams.get('redirect') || '/'
        navigate(redirect)
      }
    } catch (err: any) {
      const timeoutMessage =
        err.code === 'ECONNABORTED'
          ? 'Yeu cau dang nhap bi timeout, vui long thu lai.'
          : null
      const networkMessage =
        !err.response && err.message
          ? `Khong the ket noi toi server: ${err.message}`
          : null

      setError(
        timeoutMessage ||
          networkMessage ||
          extractErrorMessage(err, 'Khong the dang nhap. Vui long thu lai.')
      )
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleLogin = () => {
    const apiUrl = import.meta.env.VITE_API_URL || 'https://api-gateway-4tdc.onrender.com'
    window.location.href = `${apiUrl}/oauth2/authorization/google`
  }

  const handlePasskeyLogin = async () => {
    try {
      setError('')
      setInfo('')
      setLoading(true)
      
      // Check browser support
      if (!window.PublicKeyCredential) {
        setError('Trình duyệt không hỗ trợ Passkey')
        setLoading(false)
        return
      }

      // Helper: base64url -> Uint8Array
      const base64UrlToBytes = (b64url: string): Uint8Array => {
        const b64 = b64url.replace(/-/g, '+').replace(/_/g, '/')
        const padded = b64 + '='.repeat((4 - (b64.length % 4)) % 4)
        const binary = atob(padded)
        const bytes = new Uint8Array(binary.length)
        for (let i = 0; i < binary.length; i += 1) bytes[i] = binary.charCodeAt(i)
        return bytes
      }

      // Helper: ArrayBuffer -> base64url (NOT standard base64)
      const toBase64Url = (buffer: ArrayBuffer): string => {
        const bytes = new Uint8Array(buffer)
        let binary = ''
        for (let i = 0; i < bytes.length; i += 1) binary += String.fromCharCode(bytes[i])
        const b64 = btoa(binary)
        // Convert base64 to base64url
        return b64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '')
      }

      // Step 1: Start authentication - get challenge options
      const startResponse = await axios.post('/api/auth/passkey/login/options', {
        email: username || '' // Backend uses email as identifier
      })
      
      console.log('=== PASSKEY LOGIN DEBUG ===')
      console.log('1. Raw response:', startResponse)
      console.log('2. Response data type:', typeof startResponse.data)
      console.log('3. Response data:', startResponse.data)
      
      // Backend returns full credential request options as JSON string
      let options = startResponse.data
      if (typeof options === 'string') {
        console.log('4. Parsing JSON string...')
        options = JSON.parse(options)
      }
      
      console.log('5. Parsed options:', options)
      console.log('6. Options keys:', Object.keys(options))
      console.log('7. Has publicKey?', 'publicKey' in options)
      
      // Step 2: Get credential from authenticator
      // The backend returns the format from toCredentialsGetJson()
      // which has the structure: { publicKey: { challenge, rpId, allowCredentials, ... } }
      const publicKeyData = options.publicKey || options
      
      console.log('8. publicKeyData:', publicKeyData)
      console.log('9. publicKeyData keys:', Object.keys(publicKeyData))
      console.log('10. challenge:', publicKeyData.challenge)
      console.log('11. rpId:', publicKeyData.rpId)
      console.log('12. allowCredentials:', publicKeyData.allowCredentials)
      
      if (!publicKeyData.challenge) {
        console.error('ERROR: No challenge found in response:', publicKeyData)
        throw new Error('Server response thiếu challenge')
      }
      
      const publicKeyOptions: PublicKeyCredentialRequestOptions = {
        ...publicKeyData,
        challenge: base64UrlToBytes(publicKeyData.challenge),
        allowCredentials: publicKeyData.allowCredentials?.map((c: any) => ({
          ...c,
          id: base64UrlToBytes(c.id)
        }))
      }
      
      const credential = await navigator.credentials.get({
        publicKey: publicKeyOptions
      }) as PublicKeyCredential
      
      if (!credential) {
        setError('Không tìm thấy Passkey')
        setLoading(false)
        return
      }
      
      const response = credential.response as AuthenticatorAssertionResponse
      
      // Build JSON-serialized assertion for backend (matches Yubico's parseAssertionResponseJson format)
      const assertionJSON = {
        id: credential.id,
        rawId: toBase64Url(credential.rawId),
        type: credential.type,
        response: {
          authenticatorData: toBase64Url(response.authenticatorData),
          clientDataJSON: toBase64Url(response.clientDataJSON),
          signature: toBase64Url(response.signature),
          userHandle: response.userHandle ? toBase64Url(response.userHandle) : null
        },
        clientExtensionResults: credential.getClientExtensionResults() || {}
      }
      
      // Step 3: Send credential to server for verification
      const finishResponse = await axios.post('/api/auth/passkey/login/verify', {
        assertion: JSON.stringify(assertionJSON)
      })
      
      const payload = normalizeAuthPayload(finishResponse.data, username)
      if (!payload) {
        throw new Error('Dang nhap Passkey thanh cong nhung phan hoi khong hop le.')
      }

      setAuth(
        {
          id: payload.userId,
          username: payload.username,
          email: payload.email,
          role: payload.role,
        },
        payload.token
      )
      
      // Role-based routing
      if (payload.role === 'ADMIN') {
        navigate('/admin/products')
      } else if (payload.role === 'SHIPPER') {
        navigate('/shipper/dashboard')
      } else {
        navigate('/')
      }
      
    } catch (err: any) {
      console.error('Passkey login error:', err)
      setError(extractErrorMessage(err, 'Dang nhap Passkey that bai'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="max-w-md w-full space-y-8"
      >
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Đăng nhập tài khoản
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}
          {info && (
            <div className="bg-blue-100 border border-blue-400 text-blue-700 px-4 py-3 rounded">
              {info}
            </div>
          )}
          <div className="rounded-md shadow-sm space-y-4">
            <div>
              <label htmlFor="username" className="sr-only">Tên đăng nhập</label>
              <input
                id="username"
                name="username"
                type="text"
                required
                className="input-field"
                placeholder="Tên đăng nhập"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="password" className="sr-only">Mật khẩu</label>
              <input
                id="password"
                name="password"
                type="password"
                required
                className="input-field"
                placeholder="Mật khẩu"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={loading}
              className="w-full btn-primary disabled:opacity-50"
            >
              {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
            </button>
          </div>

          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-gray-50 text-gray-500">Hoặc đăng nhập với</span>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3">
            <button
              type="button"
              onClick={handleGoogleLogin}
              className="w-full flex items-center justify-center gap-2 bg-white border border-gray-300 rounded-lg py-2 px-4 hover:bg-gray-50 transition-colors"
            >
              <svg className="w-5 h-5" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Đăng nhập với Google
            </button>

            <button
              type="button"
              onClick={handlePasskeyLogin}
              className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-lg py-2 px-4 hover:from-purple-700 hover:to-blue-700 transition-all"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
              </svg>
              Đăng nhập với Passkey
            </button>
          </div>

          <div className="text-center">
            <Link to="/register" className="text-primary-600 hover:text-primary-700">
              Chưa có tài khoản? Đăng ký ngay
            </Link>
          </div>
        </form>
      </motion.div>
    </div>
  )
}
