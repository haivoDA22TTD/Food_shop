import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import axios from '../api/axios'

interface User {
  id: number
  username: string
  email: string
  role: string
}

interface AuthState {
  user: User | null
  token: string | null
  setAuth: (user: User, token: string) => void
  logout: () => Promise<void>
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      setAuth: (user, token) => set({ user, token }),
      logout: async () => {
        try {
          if (get().token) {
            await axios.post('/api/auth/logout')
          }
        } catch (error) {
          console.error('Logout request failed:', error)
        } finally {
          set({ user: null, token: null })
        }
      },
    }),
    {
      name: 'auth-storage',
    }
  )
)
