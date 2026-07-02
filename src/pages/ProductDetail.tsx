import { motion, AnimatePresence } from 'framer-motion'
import { useEffect, useState } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import axios from '../api/axios'
import { useCartStore } from '../store/cartStore'
import { useAuthStore } from '../store/authStore'

interface Product {
  id: number
  name: string
  description?: string
  price: number
  image?: string
  stock: number
  category?: string
}

interface Review {
  id: number
  rating: number
  comment: string
  createdAt: string
  user: { id: number; username: string }
}

function StarDisplay({ rating, size = 'text-lg' }: { rating: number; size?: string }) {
  return (
    <span className={`${size} text-yellow-500`}>
      {[1, 2, 3, 4, 5].map(i => (
        <span key={i} className={i <= rating ? '' : 'text-gray-300'}>{i <= rating ? '★' : '☆'}</span>
      ))}
    </span>
  )
}

function StarInput({ value, onChange }: { value: number; onChange: (v: number) => void }) {
  const [hover, setHover] = useState(0)
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map(i => (
        <button
          key={i}
          type="button"
          onMouseEnter={() => setHover(i)}
          onMouseLeave={() => setHover(0)}
          onClick={() => onChange(i)}
          className={`text-2xl transition ${i <= (hover || value) ? 'text-yellow-500' : 'text-gray-300'}`}
        >
          {i <= (hover || value) ? '★' : '☆'}
        </button>
      ))}
    </div>
  )
}

export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const { addToCart } = useCartStore()
  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [addingToCart, setAddingToCart] = useState(false)

  const [reviews, setReviews] = useState<Review[]>([])
  const [loadingReviews, setLoadingReviews] = useState(true)
  const [showReviewForm, setShowReviewForm] = useState(false)
  const [reviewRating, setReviewRating] = useState(0)
  const [reviewComment, setReviewComment] = useState('')
  const [submittingReview, setSubmittingReview] = useState(false)
  const [reviewError, setReviewError] = useState('')
  const [reviewSuccess, setReviewSuccess] = useState(false)
  const [hasDeliveredOrder, setHasDeliveredOrder] = useState(false)
  const [alreadyReviewed, setAlreadyReviewed] = useState(false)

  useEffect(() => {
    const loadProduct = async () => {
      if (!id) { setError('San pham khong hop le.'); setLoading(false); return }
      setLoading(true)
      try {
        const response = await axios.get(`/api/products/${id}`)
        setProduct(response.data || null)
      } catch (err: any) {
        setError(err?.response?.status === 404 ? 'Khong tim thay san pham.' : 'Khong the tai chi tiet san pham.')
      } finally { setLoading(false) }
    }
    void loadProduct()
  }, [id])

  useEffect(() => {
    if (!id) return
    const loadReviews = async () => {
      setLoadingReviews(true)
      try {
        const res = await axios.get(`/api/reviews/product/${id}`)
        setReviews(Array.isArray(res.data) ? res.data : [])
      } catch { /* ignore */ } finally { setLoadingReviews(false) }
    }
    void loadReviews()
  }, [id, reviewSuccess])

  useEffect(() => {
    if (!user || !id) return
    const checkEligibility = async () => {
      try {
        const res = await axios.get('/api/orders')
        const orders = Array.isArray(res.data) ? res.data : res.data?.content || []
        const delivered = orders.some((o: any) =>
          o.status === 'DELIVERED' && o.orderItems?.some((item: any) => String(item.productId) === String(id))
        )
        setHasDeliveredOrder(delivered)
        const reviewed = orders.some((o: any) =>
          o.orderItems?.some((item: any) => String(item.productId) === String(id))
        )
        if (reviewed) {
          const existingReview = reviews.find(r => r.user.id === user.id)
          if (existingReview) setAlreadyReviewed(true)
        }
      } catch { /* ignore */ }
    }
    void checkEligibility()
  }, [user, id, reviews])

  const handleSubmitReview = async () => {
    if (!id || reviewRating === 0 || !reviewComment.trim()) return
    setReviewError('')
    setSubmittingReview(true)
    try {
      const ordersRes = await axios.get('/api/orders')
      const orders = Array.isArray(ordersRes.data) ? ordersRes.data : ordersRes.data?.content || []
      const deliveredOrder = orders.find((o: any) =>
        o.status === 'DELIVERED' && o.orderItems?.some((item: any) => String(item.productId) === String(id))
      )
      if (!deliveredOrder) {
        setReviewError('Ban can co don hang da giao de danh gia san pham nay.')
        setSubmittingReview(false)
        return
      }
      await axios.post('/api/reviews/create', {
        productId: Number(id),
        orderId: deliveredOrder.id,
        rating: reviewRating,
        comment: reviewComment.trim(),
      })
      setReviewSuccess(true)
      setShowReviewForm(false)
      setReviewRating(0)
      setReviewComment('')
      setAlreadyReviewed(true)
    } catch (err: any) {
      setReviewError(err?.response?.data?.error || 'Khong the gui danh gia. Vui long thu lai.')
    } finally { setSubmittingReview(false) }
  }

  const avgRating = reviews.length > 0
    ? (reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length).toFixed(1)
    : '0'

  const handleAddToCart = async () => {
    if (!product) return
    setAddingToCart(true)
    try {
      await addToCart(product.id, quantity, { name: product.name, price: product.price, image: product.image, stock: product.stock })
      setAddingToCart(false)
      alert('Đã thêm vào giỏ hàng!')
      if (!user) {
        const shouldLogin = confirm('Bạn muốn đăng nhập để lưu giỏ hàng?')
        if (shouldLogin) navigate('/login')
      }
    } catch {
      setAddingToCart(false)
      alert(user ? 'Không thể thêm vào giỏ hàng. Vui lòng thử lại.' : 'Đã thêm vào giỏ hàng!')
    }
  }

  if (!id) return <Navigate to="/products" />

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      {loading ? (
        <p className="text-gray-600">Dang tai chi tiet san pham...</p>
      ) : error ? (
        <div className="space-y-3">
          <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-2 rounded">{error}</div>
          <button type="button" className="btn-secondary" onClick={() => navigate('/products')}>Quay lai danh sach</button>
        </div>
      ) : product ? (
        <>
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
            <img
              src={product.image || 'https://via.placeholder.com/600x400?text=No+Image'}
              alt={product.name}
              className="w-full h-96 object-cover rounded-xl"
            />
            <div>
              <h1 className="text-4xl font-bold mb-4">{product.name}</h1>
              <div className="flex items-center gap-3 mb-4">
                <StarDisplay rating={Math.round(Number(avgRating))} />
                <span className="text-gray-600">{avgRating} ({reviews.length} đánh giá)</span>
              </div>
              <p className="text-2xl text-primary-600 mb-4">{product.price.toLocaleString('vi-VN')} đ</p>
              <p className="text-gray-600 mb-3">{product.description || 'Chua co mo ta chi tiet.'}</p>
              <p className="text-gray-700 mb-1">Ton kho: {product.stock}</p>
              <p className="text-gray-700 mb-6">Danh muc: {product.category || 'N/A'}</p>
              <div className="flex items-center gap-4 mb-6">
                <label className="font-semibold">Số lượng:</label>
                <div className="flex items-center border rounded-lg">
                  <button onClick={() => setQuantity(Math.max(1, quantity - 1))} className="px-4 py-2 hover:bg-gray-100">-</button>
                  <span className="px-6 py-2 border-x">{quantity}</span>
                  <button onClick={() => setQuantity(Math.min(product.stock, quantity + 1))} className="px-4 py-2 hover:bg-gray-100">+</button>
                </div>
              </div>
              <button
                onClick={handleAddToCart}
                disabled={addingToCart || product.stock === 0}
                className="btn-primary disabled:opacity-50"
              >
                {addingToCart ? 'Đang thêm...' : product.stock === 0 ? 'Hết hàng' : 'Thêm vào giỏ'}
              </button>
            </div>
          </motion.div>

          {/* Reviews Section */}
          <div className="border-t pt-8">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold">Đánh giá sản phẩm</h2>
              {user && hasDeliveredOrder && !alreadyReviewed && !showReviewForm && (
                <button onClick={() => setShowReviewForm(true)} className="btn-primary text-sm">
                  Viết đánh giá
                </button>
              )}
              {alreadyReviewed && (
                <span className="text-sm text-green-600 font-medium">Bạn đã đánh giá sản phẩm này</span>
              )}
            </div>

            <AnimatePresence>
              {showReviewForm && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }}
                  className="mb-6 overflow-hidden"
                >
                  <div className="bg-gray-50 rounded-xl p-6 border">
                    <h3 className="font-semibold mb-4">Đánh giá của bạn</h3>
                    {reviewSuccess && (
                      <div className="bg-green-100 border border-green-300 text-green-700 px-4 py-2 rounded mb-4">
                        Gửi đánh giá thành công!
                      </div>
                    )}
                    {reviewError && (
                      <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-2 rounded mb-4">{reviewError}</div>
                    )}
                    <div className="mb-4">
                      <label className="block text-sm font-medium mb-2">Số sao *</label>
                      <StarInput value={reviewRating} onChange={setReviewRating} />
                    </div>
                    <div className="mb-4">
                      <label className="block text-sm font-medium mb-2">Nhận xét *</label>
                      <textarea
                        value={reviewComment}
                        onChange={e => setReviewComment(e.target.value)}
                        rows={3}
                        maxLength={500}
                        placeholder="Chia sẻ cảm nhận của bạn..."
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                      />
                      <p className="text-xs text-gray-500 mt-1">{reviewComment.length}/500</p>
                    </div>
                    <div className="flex gap-3">
                      <button
                        onClick={handleSubmitReview}
                        disabled={submittingReview || reviewRating === 0 || !reviewComment.trim()}
                        className="btn-primary disabled:opacity-50"
                      >
                        {submittingReview ? 'Đang gửi...' : 'Gửi đánh giá'}
                      </button>
                      <button onClick={() => { setShowReviewForm(false); setReviewError('') }} className="btn-secondary">
                        Hủy
                      </button>
                    </div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            {loadingReviews ? (
              <p className="text-gray-500">Đang tải đánh giá...</p>
            ) : reviews.length === 0 ? (
              <p className="text-gray-500 text-center py-8">Chưa có đánh giá nào cho sản phẩm này.</p>
            ) : (
              <div className="space-y-4">
                {reviews.map((review, index) => (
                  <motion.div
                    key={review.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.05 }}
                    className="bg-white border rounded-xl p-4"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <div className="w-8 h-8 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center text-sm font-bold">
                          {review.user.username?.charAt(0).toUpperCase()}
                        </div>
                        <span className="font-medium">{review.user.username}</span>
                      </div>
                      <span className="text-xs text-gray-500">{new Date(review.createdAt).toLocaleDateString('vi-VN')}</span>
                    </div>
                    <StarDisplay rating={review.rating} size="text-sm" />
                    <p className="text-gray-700 mt-2">{review.comment}</p>
                  </motion.div>
                ))}
              </div>
            )}
          </div>
        </>
      ) : null}
    </div>
  )
}
