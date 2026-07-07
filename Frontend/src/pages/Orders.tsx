import { motion, AnimatePresence } from 'framer-motion'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from '../api/axios'
import { useAuthStore } from '../store/authStore'

interface OrderItem {
  productId: number
  productName: string
  quantity: number
  productPrice: number
}

interface Order {
  id: number
  orderNumber: string
  status: string
  totalAmount: number
  paymentMethod: string
  paymentNumber?: string
  autoConfirmed?: boolean
  cancellationReason?: string
  createdAt: string
  orderItems: OrderItem[]
}

interface ReviewModal {
  orderId: number
  productId: number
  productName: string
}

const statusColors: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  PREPARING: 'bg-purple-100 text-purple-800',
  READY_FOR_PICKUP: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
}

const statusLabels: Record<string, string> = {
  PENDING: 'Chờ thanh toán',
  CONFIRMED: 'Đã xác nhận',
  PREPARING: 'Đang chuẩn bị',
  READY_FOR_PICKUP: 'Sẵn sàng lấy hàng',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
}

function StarRating({ value, onChange }: { value: number; onChange: (v: number) => void }) {
  const [hovered, setHovered] = useState(0)
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          onClick={() => onChange(star)}
          onMouseEnter={() => setHovered(star)}
          onMouseLeave={() => setHovered(0)}
          className="text-3xl transition-transform hover:scale-110 focus:outline-none"
        >
          <span className={(hovered || value) >= star ? 'text-yellow-400' : 'text-gray-300'}>★</span>
        </button>
      ))}
    </div>
  )
}

function ReviewModal({
  modal,
  onClose,
  onSuccess,
}: {
  modal: ReviewModal
  onClose: () => void
  onSuccess: () => void
}) {
  const [rating, setRating] = useState(0)
  const [comment, setComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (rating === 0) { setError('Vui lòng chọn số sao'); return }
    if (!comment.trim()) { setError('Vui lòng nhập nhận xét'); return }

    setSubmitting(true)
    setError('')
    try {
      await axios.post('/api/reviews/create', {
        productId: modal.productId,
        orderId: modal.orderId,
        rating,
        comment: comment.trim(),
      })
      onSuccess()
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Không thể gửi đánh giá. Vui lòng thử lại.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />
      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95, y: 20 }}
        className="relative bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 z-10"
      >
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 text-2xl leading-none"
        >
          ×
        </button>

        <h2 className="text-xl font-bold mb-1">Đánh giá sản phẩm</h2>
        <p className="text-gray-500 text-sm mb-5">{modal.productName}</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Chất lượng sản phẩm</label>
            <StarRating value={rating} onChange={setRating} />
            {rating > 0 && (
              <p className="text-sm text-gray-500 mt-1">
                {['', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Rất tốt'][rating]}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Nhận xét của bạn</label>
            <textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              rows={4}
              placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..."
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none"
              maxLength={500}
            />
            <p className="text-xs text-gray-400 text-right">{comment.length}/500</p>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-3 py-2">
              {error}
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 font-medium transition-colors"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 disabled:opacity-60 transition-colors"
            >
              {submitting ? 'Đang gửi...' : 'Gửi đánh giá'}
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  )
}

export default function Orders() {
  const { user } = useAuthStore()
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reviewModal, setReviewModal] = useState<ReviewModal | null>(null)
  const [reviewedKeys, setReviewedKeys] = useState<Set<string>>(new Set())
  const [successMsg, setSuccessMsg] = useState('')

  useEffect(() => {
    if (user) loadOrders()
  }, [user])

  const loadOrders = async () => {
    setLoading(true)
    try {
      const response = await axios.get('/api/orders')
      setOrders(response.data.content || [])
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Không thể tải danh sách đơn hàng')
    } finally {
      setLoading(false)
    }
  }

  const openReview = (orderId: number, productId: number, productName: string) => {
    setReviewModal({ orderId, productId, productName })
  }

  const handleReviewSuccess = () => {
    const key = `${reviewModal!.orderId}-${reviewModal!.productId}`
    setReviewedKeys(prev => new Set(prev).add(key))
    setReviewModal(null)
    setSuccessMsg('Đánh giá của bạn đã được gửi thành công!')
    setTimeout(() => setSuccessMsg(''), 3000)
  }

  if (!user) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center">
        <h2 className="text-2xl font-bold mb-4">Vui lòng đăng nhập</h2>
        <p className="text-gray-600 mb-6">Bạn cần đăng nhập để xem đơn hàng</p>
        <Link to="/login" className="btn-primary">Đăng nhập</Link>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-4xl font-bold mb-8">Đơn hàng của bạn</h1>

      {error && (
        <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded mb-6">{error}</div>
      )}

      {successMsg && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0 }}
          className="bg-green-50 border border-green-300 text-green-800 px-4 py-3 rounded mb-6 flex items-center gap-2"
        >
          <span>✅</span> {successMsg}
        </motion.div>
      )}

      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto" />
          <p className="mt-4 text-gray-600">Đang tải đơn hàng...</p>
        </div>
      ) : orders.length === 0 ? (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-center py-12">
          <div className="text-6xl mb-4">📦</div>
          <p className="text-xl text-gray-600 mb-6">Chưa có đơn hàng nào</p>
          <Link to="/products" className="btn-primary">Mua sắm ngay</Link>
        </motion.div>
      ) : (
        <div className="space-y-6">
          {orders.map((order, index) => (
            <motion.div
              key={order.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
              className="card p-6"
            >
              {/* Header */}
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="font-bold text-lg">Đơn hàng #{order.orderNumber}</h3>
                  <p className="text-gray-600 text-sm">{new Date(order.createdAt).toLocaleString('vi-VN')}</p>
                  {order.autoConfirmed && (
                    <p className="text-green-600 text-xs mt-1 flex items-center gap-1">
                      <span>✓</span> Đã tự động xác nhận
                    </p>
                  )}
                </div>
                <span className={`px-3 py-1 rounded-full text-sm font-semibold ${statusColors[order.status] || 'bg-gray-100 text-gray-800'}`}>
                  {statusLabels[order.status] || order.status}
                </span>
              </div>

              {order.cancellationReason && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
                  <p className="text-red-800 text-sm"><strong>Lý do hủy:</strong> {order.cancellationReason}</p>
                </div>
              )}

              {/* Order Items */}
              <div className="border-t pt-4 mb-4 space-y-3">
                {order.orderItems.map((item, idx) => {
                  const reviewKey = `${order.id}-${item.productId}`
                  const reviewed = reviewedKeys.has(reviewKey)
                  const canReview = order.status === 'DELIVERED' && item.productId

                  return (
                    <div key={idx} className="flex justify-between items-center">
                      <div className="flex-1">
                        <span className="text-gray-700">{item.productName} x {item.quantity}</span>
                        {canReview && (
                          <div className="mt-1">
                            {reviewed ? (
                              <span className="text-xs text-green-600 flex items-center gap-1">
                                <span>✓</span> Đã đánh giá
                              </span>
                            ) : (
                              <button
                                onClick={() => openReview(order.id, item.productId, item.productName)}
                                className="text-xs text-primary-600 hover:text-primary-800 font-medium flex items-center gap-1 hover:underline"
                              >
                                <span>★</span> Đánh giá sản phẩm
                              </button>
                            )}
                          </div>
                        )}
                      </div>
                      <span className="font-semibold ml-4">
                        {(item.productPrice * item.quantity).toLocaleString('vi-VN')} đ
                      </span>
                    </div>
                  )
                })}
              </div>

              {/* Payment info */}
              <div className="bg-gray-50 p-3 rounded-lg mb-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">Hình thức thanh toán:</span>
                  <span className="font-medium">
                    {order.paymentMethod === 'COD' && '💵 Thanh toán khi nhận hàng'}
                    {order.paymentMethod === 'BANK_TRANSFER' && '🏧 Chuyển khoản QR'}
                    {order.paymentMethod === 'VNPAY' && '🏦 VNPay'}
                    {order.paymentMethod === 'ZALOPAY' && '💜 ZaloPay'}
                    {order.paymentMethod === 'MOMO' && '📱 MoMo'}
                  </span>
                </div>
                {order.paymentNumber && (
                  <div className="flex justify-between items-center mt-1">
                    <span className="text-sm text-gray-600">Mã thanh toán:</span>
                    <span className="text-sm font-mono">{order.paymentNumber}</span>
                  </div>
                )}
                {['ZALOPAY', 'BANK_TRANSFER', 'VNPAY'].includes(order.paymentMethod) && order.status === 'PENDING' && (
                  <div className="mt-2 p-2 bg-yellow-50 border border-yellow-200 rounded text-sm text-yellow-800">
                    ⏳ Đơn hàng đang chờ thanh toán
                  </div>
                )}
              </div>

              {/* Banner đánh giá cho đơn DELIVERED */}
              {order.status === 'DELIVERED' && order.orderItems.some(i => i.productId) && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-3 mb-4 flex items-center gap-2">
                  <span className="text-green-600">🎉</span>
                  <p className="text-green-800 text-sm flex-1">
                    Đơn hàng đã được giao! Hãy đánh giá sản phẩm để giúp người mua khác nhé.
                  </p>
                </div>
              )}

              <div className="flex justify-between items-center border-t pt-4">
                <span className="font-bold text-lg">Tổng cộng:</span>
                <span className="font-bold text-xl text-primary-600">
                  {order.totalAmount.toLocaleString('vi-VN')} đ
                </span>
              </div>
            </motion.div>
          ))}
        </div>
      )}

      {/* Review Modal */}
      <AnimatePresence>
        {reviewModal && (
          <ReviewModal
            modal={reviewModal}
            onClose={() => setReviewModal(null)}
            onSuccess={handleReviewSuccess}
          />
        )}
      </AnimatePresence>
    </div>
  )
}