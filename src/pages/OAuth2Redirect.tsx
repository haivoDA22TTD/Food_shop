import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { normalizeAuthPayload } from '../utils/auth'

export default function OAuth2Redirect() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const rawPayload = {
      token: params.get('token') || '',
      userId: params.get('userId') || '',
      username: params.get('username') || '',
      email: params.get('email') || '',
      role: params.get('role') || '',
    }

    const payload = normalizeAuthPayload(rawPayload)

    if (!payload) {
      navigate('/login', { replace: true })
      return
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

    navigate('/', { replace: true })
  }, [navigate, setAuth])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <p className="text-gray-700">Dang xu ly dang nhap Google...</p>
    </div>
  )
}
