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
  autoConfirmed?: boolean
  cancellationReason?: string
  shipperId?: number
  shipperName?: string
  shipperPhone?: string
  assignedAt?: string
  pickedUpAt?: string
  deliveredAt?: string
  deliveryNotes?: string
  createdAt: string
}

interface Shipper {
  id: number
  name: string
  phone: string
  status: string
  rating: number
  totalDeliveries: number
  successRate: number
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
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalOrders, setTotalOrders] = useState(0)
  
  // Shipper assignment states
  const [showAssignModal, setShowAssignModal] = useState(false)
  const [selectedOrderForAssign, setSelectedOrderForAssign] = useState<Order | null>(null)
  const [availableShippers, setAvailableShippers] = useState<Shipper[]>([])
  const [loadingShippers, setLoadingShippers] = useState(false)
  const [assignNotes, setAssignNotes] = useState('')

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      loadOrders()
    }
  }, [user, currentPage])

  const loadOrders = async () => {
    setLoading(true)
    try {
      const response = await axios.get('/api/admin/orders', {
        params: {
          page: currentPage,
          size: 20,
          sort: 'createdAt,desc' // Sort by newest first
        }
      })
      setOrders(response.data.content || [])
      setTotalPages(response.data.totalPages || 0)
      setTotalOrders(response.data.totalElements || 0)
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Không thể tải danh sách đơn hàng')
    } finally {
      setLoading(false)
    }
  }

  const updateOrderStatus = async (orderId: number, currentStatus: string, newStatus: string) => {
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

  const openAssignModal = async (order: Order) => {
    setSelectedOrderForAssign(order)
    setShowAssignModal(true)
    setLoadingShippers(true)
    try {
      const response = await axios.get('/api/admin/shippers/available')
      setAvailableShippers(response.data)
    } catch (err: any) {
      alert('Không thể tải danh sách shipper')
    } finally {
      setLoadingShippers(false)
    }
  }

  const handleAssignShipper = async (shipperId: number) => {
    if (!selectedOrderForAssign) return
    setUpdatingOrder(selectedOrderForAssign.id)
    try {
      await axios.put(`/api/admin/orders/${selectedOrderForAssign.id}/assign-shipper`, {
        shipperId,
        notes: assignNotes || undefined
      })
      alert('Đã phân công shipper thành công!')
      setShowAssignModal(false)
      setSelectedOrderForAssign(null)
      setAssignNotes('')
      await loadOrders()
    } catch (err: any) {
      alert(err?.response?.data?.error || 'Không thể phân công shipper')
    } finally {
      setUpdatingOrder(null)
    }
  }

  const handleUnassignShipper = async (orderId: number) => {
    if (!confirm('Bạn có chắc muốn hủy phân công shipper?')) return
    setUpdatingOrder(orderId)
    try {
      await axios.put(`/api/admin/orders/${orderId}/unassign-shipper`)
      alert('Đã hủy phân công shipper!')
      await loadOrders()
    } catch (err: any) {
      alert(err?.response?.data?.error || 'Không thể hủy phân công')
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
                  {order.autoConfirmed && (
                    <p className="text-green-600 text-xs mt-1 flex items-center gap-1">
                      <span>🤖</span> Tự động xác nhận
                    </p>
                  )}
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

              {/* Shipper Information */}
              <div className="mt-4 pt-4 border-t">
                <p className="text-sm text-gray-600 mb-2">Shipper</p>
                {order.shipperId ? (
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-semibold text-blue-900">{order.shipperName || 'N/A'}</p>
                        <p className="text-sm text-blue-700">{order.shipperPhone || 'N/A'}</p>
                        {order.assignedAt && (
                          <p className="text-xs text-blue-600 mt-1">
                            Phân công: {new Date(order.assignedAt).toLocaleString('vi-VN')}
                          </p>
                        )}
                        {order.pickedUpAt && (
                          <p className="text-xs text-green-600 mt-1">
                            ✓ Đã lấy hàng: {new Date(order.pickedUpAt).toLocaleString('vi-VN')}
                          </p>
                        )}
                        {order.deliveredAt && (
                          <p className="text-xs text-green-600 mt-1">
                            ✓ Đã giao: {new Date(order.deliveredAt).toLocaleString('vi-VN')}
                          </p>
                        )}
                        {order.deliveryNotes && (
                          <p className="text-xs text-gray-600 mt-1">
                            Ghi chú: {order.deliveryNotes}
                          </p>
                        )}
                      </div>
                      {order.status !== 'DELIVERED' && order.status !== 'CANCELLED' && (
                        <button
                          onClick={() => handleUnassignShipper(order.id)}
                          disabled={updatingOrder === order.id}
                          className="px-3 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200 disabled:opacity-50"
                        >
                          Hủy phân công
                        </button>
                      )}
                    </div>
                  </div>
                ) : (
                  <div>
                    {(order.status === 'CONFIRMED' || order.status === 'PREPARING' || order.status === 'READY_FOR_PICKUP') ? (
                      <button
                        onClick={() => openAssignModal(order)}
                        disabled={updatingOrder === order.id}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                      >
                        🚚 Phân công shipper
                      </button>
                    ) : (
                      <p className="text-sm text-gray-500">Chưa phân công shipper</p>
                    )}
                  </div>
                )}
              </div>

              {order.cancellationReason && (
                <div className="mt-4 pt-4 border-t">
                  <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                    <p className="text-sm text-gray-600 mb-1">Lý do hủy</p>
                    <p className="text-sm text-red-800">{order.cancellationReason}</p>
                  </div>
                </div>
              )}

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
                        onChange={(e) => updateOrderStatus(order.id, order.status, e.target.value)}
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

      {/* Pagination */}
      {!loading && orders.length > 0 && totalPages > 0 && (
        <div className="mt-8 flex flex-col items-center gap-4">
          <p className="text-sm text-gray-600">
            Trang {currentPage + 1} / {totalPages} • Tổng {totalOrders} đơn hàng
          </p>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setCurrentPage(0)}
              disabled={currentPage === 0}
              className="px-3 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              « Đầu
            </button>
            <button
              onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
              disabled={currentPage === 0}
              className="px-3 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              ←
            </button>
            {(() => {
              const pages = []
              const start = Math.max(0, currentPage - 2)
              const end = Math.min(totalPages - 1, currentPage + 2)
              if (start > 0) {
                pages.push(<span key="start-dots" className="px-2 text-gray-400">...</span>)
              }
              for (let i = start; i <= end; i++) {
                pages.push(
                  <button
                    key={i}
                    onClick={() => setCurrentPage(i)}
                    className={`px-3 py-2 border rounded-lg text-sm ${
                      i === currentPage
                        ? 'bg-primary-600 text-white border-primary-600'
                        : 'hover:bg-gray-50'
                    }`}
                  >
                    {i + 1}
                  </button>
                )
              }
              if (end < totalPages - 1) {
                pages.push(<span key="end-dots" className="px-2 text-gray-400">...</span>)
              }
              return pages
            })()}
            <button
              onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
              disabled={currentPage >= totalPages - 1}
              className="px-3 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              →
            </button>
            <button
              onClick={() => setCurrentPage(totalPages - 1)}
              disabled={currentPage >= totalPages - 1}
              className="px-3 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              Cuối »
            </button>
          </div>
        </div>
      )}

      {/* Assign Shipper Modal */}
      {showAssignModal && selectedOrderForAssign && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full p-6 max-h-[80vh] overflow-y-auto">
            <h2 className="text-2xl font-bold mb-4">Phân Công Shipper</h2>
            <p className="text-gray-600 mb-4">
              Đơn hàng: <span className="font-bold">{selectedOrderForAssign.orderNumber}</span>
            </p>

            {loadingShippers ? (
              <div className="text-center py-8">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                <p className="mt-2 text-gray-600">Đang tải danh sách shipper...</p>
              </div>
            ) : availableShippers.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-600">Không có shipper nào sẵn sàng</p>
              </div>
            ) : (
              <>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Ghi chú (optional)
                  </label>
                  <textarea
                    value={assignNotes}
                    onChange={(e) => setAssignNotes(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    rows={2}
                    placeholder="Ví dụ: Giao trước 5pm, gọi trước khi đến..."
                  />
                </div>

                <div className="space-y-3">
                  <p className="font-medium text-gray-700">Chọn shipper:</p>
                  {availableShippers.map((shipper) => (
                    <div
                      key={shipper.id}
                      className="border border-gray-200 rounded-lg p-4 hover:border-blue-500 hover:bg-blue-50 cursor-pointer transition-all"
                      onClick={() => handleAssignShipper(shipper.id)}
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="font-semibold text-gray-900">{shipper.name}</p>
                          <p className="text-sm text-gray-600">{shipper.phone}</p>
                          <div className="flex items-center gap-4 mt-2">
                            <span className="text-xs text-gray-500">
                              {shipper.totalDeliveries} đơn
                            </span>
                            <span className="text-xs text-gray-500">
                              {shipper.successRate.toFixed(1)}% thành công
                            </span>
                            <span className="text-xs text-yellow-600">
                              ⭐ {shipper.rating.toFixed(1)}
                            </span>
                          </div>
                        </div>
                        <div className="text-right">
                          <span className="px-3 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded-full">
                            {shipper.status}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}

            <div className="mt-6 flex gap-3">
              <button
                onClick={() => {
                  setShowAssignModal(false)
                  setSelectedOrderForAssign(null)
                  setAssignNotes('')
                }}
                className="flex-1 bg-gray-200 text-gray-800 py-2 rounded-lg hover:bg-gray-300 transition-colors"
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}