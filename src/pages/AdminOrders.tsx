import { motion } from 'framer-motion'
import { useEffect, useState } from 'react'
import axios from '../api/axios'
import { useAuthStore } from '../store/authStore'
import { Navigate } from 'react-router-dom'

interface Order {
  id: number
  orderNumber: string
  userId: number
  username?: string
  userEmail?: string
  status: string
  totalAmount: number
  shippingAddress: string
  phoneNumber: string
  createdAt: string
}

const statusOptions = [
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'PREPARING', label: 'Đang chuẩn bị' },
  { value: 'READY_FOR_PICKUP', label: 'Sẵn sàng lấy hàng' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]

const statusColors: Record<string, string> = {
  CONFIRMED: 'bg-blue-100 text-blue-800',
  PREPARING: 'bg-purple-100 text-purple-800',
  READY_FOR_PICKUP: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
}

// Helper function to get available status transitions
const getAvailableTransitions = (currentStatus: string): typeof statusOptions => {
  const transitions: Record<string, string[]> = {
    CONFIRMED: ['PREPARING', 'CANCELLED'],
    PREPARING: ['READY_FOR_PICKUP', 'CANCELLED'],
    READY_FOR_PICKUP: ['DELIVERED'],
    DELIVERED: [],
    CANCELLED: [],
  }

  const availableStatuses = transitions[currentStatus] || []
  return statusOptions.filter(opt => 
    opt.value === currentStatus || availableStatuses.includes(opt.value)
  )
}

export default function AdminOrders() {
  const { user } = useAuthStore()
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingOrder, setUpdatingOrder] = useState<number | null>(null)

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      loadOrders()
    }
  }, [user])

  const loadOrders = async () => {
    setLoading(true)
    try {
      const response = await axios.get('/api/admin/orders')
      setOrders(response.data.content || [])
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Không thể tải danh sách đơn hàng')
    } finally {
      setLoading(false)
    }
  }

  const updateOrderStatus = async (orderId: number, orderNumber: string, currentStatus: string, newStatus: string) => {
    // Don't update if same status
    if (currentStatus === newStatus) return

    setUpdatingOrder(orderId)
    try {
      await axios.put(`/api/admin/orders/${orderId}/status`, { 
        status: newStatus,
        reason: `Admin cập nhật trạng thái từ ${currentStatus} sang ${newStatus}`
      })
      await loadOrders()
    } catch (err: any) {
      alert(err?.response?.data?.error || 'Không thể cập nhật trạng thái')
    } finally {
      setUpdatingOrder(null)
    }
  }

  if (!user || user.role !== 'ADMIN') {
    return <Navigate to="/" />
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-4xl font-bold mb-8">Quản lý đơn hàng</h1>

      {error && (
        <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded mb-6">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải đơn hàng...</p>
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-xl text-gray-600">Chưa có đơn hàng nào</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order, index) => (
            <motion.div
              key={order.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
              className="card p-6"
            >
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div>
                  <p className="text-sm text-gray-600">Mã đơn hàng</p>
                  <p className="font-bold">{order.orderNumber}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Khách hàng</p>
                  <p className="font-semibold">{order.username || `User #${order.userId}`}</p>
                  {order.userEmail && (
                    <p className="text-sm text-gray-500">{order.userEmail}</p>
                  )}
                  <p className="text-sm">{order.phoneNumber}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Tổng tiền</p>
                  <p className="font-bold text-primary-600">
                    {order.totalAmount.toLocaleString('vi-VN')} đ
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Ngày đặt</p>
                  <p className="text-sm">
                    {new Date(order.createdAt).toLocaleString('vi-VN')}
                  </p>
                </div>
              </div>

              <div className="mt-4 pt-4 border-t">
                <p className="text-sm text-gray-600 mb-2">Địa chỉ giao hàng</p>
                <p className="text-sm">{order.shippingAddress}</p>
              </div>

              <div className="mt-4 pt-4 border-t">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600 mb-2">Trạng thái</p>
                    <span className={`px-3 py-1 rounded-full text-sm font-semibold ${statusColors[order.status]}`}>
                      {statusOptions.find(s => s.value === order.status)?.label || order.status}
                    </span>
                  </div>
                  
                  {/* Only show dropdown if order can be updated */}
                  {order.status !== 'DELIVERED' && order.status !== 'CANCELLED' && (
                    <div className="flex flex-col items-end">
                      <label className="text-sm text-gray-600 mb-2">Cập nhật trạng thái</label>
                      <select
                        value={order.status}
                        onChange={(e) => updateOrderStatus(order.id, order.orderNumber, order.status, e.target.value)}
                        disabled={updatingOrder === order.id}
                        className="px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {getAvailableTransitions(order.status).map((option) => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </select>
                      {updatingOrder === order.id && (
                        <p className="text-xs text-gray-500 mt-1">Đang cập nhật...</p>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  )
}
