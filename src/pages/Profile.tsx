import { motion } from 'framer-motion'
import { useAuthStore } from '../store/authStore'
import { Navigate } from 'react-router-dom'

export default function Profile() {
  const user = useAuthStore((state) => state.user)

  if (!user) {
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
      </motion.div>
    </div>
  )
}
