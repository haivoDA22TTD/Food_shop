import { motion } from 'framer-motion'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import { useAuthStore } from '../store/authStore'
import { extractErrorMessage, normalizeAuthPayload } from '../utils/auth'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)

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
      navigate('/')
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

      // Step 1: Start authentication - get challenge
      const startResponse = await axios.post('/api/passkey/authenticate/start', {
        username: username || 'anonymous' // Can be empty for resident keys
      })
      
      const { challenge } = startResponse.data
      
      // Step 2: Get credential from authenticator
      const credential = await navigator.credentials.get({
        publicKey: {
          challenge: Uint8Array.from(atob(challenge), c => c.charCodeAt(0)),
          timeout: 60000,
          userVerification: 'preferred'
        }
      }) as PublicKeyCredential
      
      if (!credential) {
        setError('Không tìm thấy Passkey')
        setLoading(false)
        return
      }
      
      const response = credential.response as AuthenticatorAssertionResponse
      
      // Step 3: Send credential to server for verification
      const finishResponse = await axios.post('/api/passkey/authenticate/finish', {
        credentialId: btoa(String.fromCharCode(...new Uint8Array(credential.rawId))),
        signature: btoa(String.fromCharCode(...new Uint8Array(response.signature))),
        authenticatorData: btoa(String.fromCharCode(...new Uint8Array(response.authenticatorData))),
        clientDataJSON: btoa(String.fromCharCode(...new Uint8Array(response.clientDataJSON)))
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
      navigate('/')
      
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
