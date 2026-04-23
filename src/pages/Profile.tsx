import { motion } from 'framer-motion'
import { FormEvent, useEffect, useState } from 'react'
import { useAuthStore } from '../store/authStore'
import { Navigate } from 'react-router-dom'
import axios from '../api/axios'

interface PasskeyItem {
  id: number
  nickname: string
  createdAt?: string
  lastUsedAt?: string
}

const toBase64 = (buffer: ArrayBuffer): string => {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let i = 0; i < bytes.length; i += 1) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}

export default function Profile() {
  const user = useAuthStore((state) => state.user)
  const [passkeys, setPasskeys] = useState<PasskeyItem[]>([])
  const [loadingPasskeys, setLoadingPasskeys] = useState(false)
  const [registeringPasskey, setRegisteringPasskey] = useState(false)
  const [changingPassword, setChangingPassword] = useState(false)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const userId = user?.id

  const loadPasskeys = async (targetUserId: number) => {
    setLoadingPasskeys(true)
    try {
      const response = await axios.get('/api/passkey/list', {
        params: { userId: targetUserId },
      })
      setPasskeys(Array.isArray(response.data) ? response.data : [])
    } catch (err: any) {
      setError(err?.response?.data || 'Khong the tai danh sach Passkey.')
    } finally {
      setLoadingPasskeys(false)
    }
  }

  useEffect(() => {
    if (!userId) {
      setPasskeys([])
      return
    }

    void loadPasskeys(userId)
  }, [userId])

  const handleSetupPasskey = async () => {
    setError('')
    setMessage('')
    setRegisteringPasskey(true)

    try {
      if (!user || !userId) {
        setError('Khong tim thay thong tin nguoi dung.')
        return
      }

      if (!window.PublicKeyCredential) {
        setError('Trinh duyet hien tai khong ho tro Passkey.')
        return
      }

      const startResponse = await axios.post('/api/passkey/register/start', {
        userId: userId,
      })

      const challenge = startResponse.data?.challenge
      if (!challenge) {
        throw new Error('Khong lay duoc challenge cho Passkey.')
      }

      const credential = (await navigator.credentials.create({
        publicKey: {
          challenge: Uint8Array.from(atob(challenge), (c) => c.charCodeAt(0)),
          rp: {
            name: 'Food Shop',
          },
          user: {
            id: Uint8Array.from(String(userId), (c) => c.charCodeAt(0)),
            name: user.username,
            displayName: user.username,
          },
          pubKeyCredParams: [{ type: 'public-key', alg: -7 }],
          timeout: 60000,
          attestation: 'none',
          authenticatorSelection: {
            userVerification: 'preferred',
          },
        },
      })) as PublicKeyCredential | null

      if (!credential) {
        throw new Error('Khong tao duoc Passkey.')
      }

      const response = credential.response as AuthenticatorAttestationResponse

      await axios.post('/api/passkey/register/finish', {
        userId: userId,
        credentialId: toBase64(credential.rawId),
        publicKey: toBase64(response.attestationObject),
        nickname: `Passkey ${new Date().toLocaleString('vi-VN')}`,
      })

      setMessage('Thiet lap Passkey thanh cong.')
      if (userId) {
        await loadPasskeys(userId)
      }
    } catch (err: any) {
      setError(err?.response?.data || err?.message || 'Thiet lap Passkey that bai.')
    } finally {
      setRegisteringPasskey(false)
    }
  }

  const handleDeletePasskey = async (passkeyId: number) => {
    setError('')
    setMessage('')
    try {
      await axios.delete(`/api/passkey/${passkeyId}`, {
        params: { userId: userId },
      })
      setMessage('Da xoa Passkey.')
      if (userId) {
        await loadPasskeys(userId)
      }
    } catch (err: any) {
      setError(err?.response?.data || 'Khong the xoa Passkey.')
    }
  }

  const handleChangePassword = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setMessage('')

    if (!currentPassword || !newPassword || !confirmNewPassword) {
      setError('Vui long nhap day du thong tin doi mat khau.')
      return
    }

    if (newPassword.length < 6) {
      setError('Mat khau moi phai co it nhat 6 ky tu.')
      return
    }

    if (newPassword !== confirmNewPassword) {
      setError('Mat khau moi va xac nhan mat khau khong khop.')
      return
    }

    setChangingPassword(true)
    try {
      await axios.post('/api/users/change-password', {
        currentPassword,
        newPassword,
      })

      setCurrentPassword('')
      setNewPassword('')
      setConfirmNewPassword('')
      setMessage('Doi mat khau thanh cong.')
    } catch (err: any) {
      setError(err?.response?.data || 'Khong the doi mat khau.')
    } finally {
      setChangingPassword(false)
    }
  }

  if (!user || !userId) {
    return <Navigate to="/login" />
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="max-w-2xl mx-auto"
      >
        <h1 className="text-4xl font-bold mb-8">Profile</h1>
        
        <div className="card p-6 space-y-4">
          <div>
            <label className="text-gray-600">Username</label>
            <p className="text-xl font-semibold">{user.username}</p>
          </div>
          <div>
            <label className="text-gray-600">Email</label>
            <p className="text-xl font-semibold">{user.email}</p>
          </div>
          <div>
            <label className="text-gray-600">Role</label>
            <p className="text-xl font-semibold">{user.role}</p>
          </div>
        </div>

        <div className="card p-6 mt-6 space-y-4">
          <h2 className="text-2xl font-bold">Doi mat khau</h2>
          <form onSubmit={handleChangePassword} className="space-y-3">
            <input
              type="password"
              value={currentPassword}
              onChange={(event) => setCurrentPassword(event.target.value)}
              className="input-field"
              placeholder="Mat khau hien tai"
              autoComplete="current-password"
            />
            <input
              type="password"
              value={newPassword}
              onChange={(event) => setNewPassword(event.target.value)}
              className="input-field"
              placeholder="Mat khau moi"
              autoComplete="new-password"
            />
            <input
              type="password"
              value={confirmNewPassword}
              onChange={(event) => setConfirmNewPassword(event.target.value)}
              className="input-field"
              placeholder="Nhap lai mat khau moi"
              autoComplete="new-password"
            />
            <button
              type="submit"
              disabled={changingPassword}
              className="btn-primary disabled:opacity-50"
            >
              {changingPassword ? 'Dang doi mat khau...' : 'Doi mat khau'}
            </button>
          </form>
        </div>

        <div className="card p-6 mt-6 space-y-4">
          <div className="flex items-center justify-between gap-3">
            <h2 className="text-2xl font-bold">Passkey</h2>
            <button
              type="button"
              onClick={handleSetupPasskey}
              disabled={registeringPasskey}
              className="btn-primary disabled:opacity-50"
            >
              {registeringPasskey ? 'Dang thiet lap...' : 'Thiet lap Passkey'}
            </button>
          </div>

          {error && (
            <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-2 rounded">
              {error}
            </div>
          )}

          {message && (
            <div className="bg-green-100 border border-green-300 text-green-700 px-4 py-2 rounded">
              {message}
            </div>
          )}

          {loadingPasskeys ? (
            <p className="text-gray-600">Dang tai danh sach Passkey...</p>
          ) : passkeys.length === 0 ? (
            <p className="text-gray-600">Ban chua co Passkey nao.</p>
          ) : (
            <div className="space-y-2">
              {passkeys.map((passkey) => (
                <div
                  key={passkey.id}
                  className="border rounded-lg p-3 flex items-center justify-between"
                >
                  <div>
                    <p className="font-semibold">{passkey.nickname || `Passkey #${passkey.id}`}</p>
                    <p className="text-sm text-gray-600">
                      Tao luc: {passkey.createdAt ? new Date(passkey.createdAt).toLocaleString('vi-VN') : 'N/A'}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleDeletePasskey(passkey.id)}
                    className="text-red-600 hover:text-red-700"
                  >
                    Xoa
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </motion.div>
    </div>
  )
}
