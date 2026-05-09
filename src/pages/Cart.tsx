import { motion } from 'framer-motion'
import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCartStore } from '../store/cartStore'
import { useAuthStore } from '../store/authStore'

export default function Cart() {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const { items, totalItems, totalAmount, loading, error, fetchCart, updateQuantity, removeFromCart } = useCartStore()

  useEffect(() => {
    if (user) {
      fetchCart()
    }
    // If not logged in, cart will use local storage
  }, [user, fetchCart])

  const handleUpdateQuantity = async (productId: number, newQuantity: number) => {
    if (newQuantity < 1) return
    try {
      await updateQuantity(productId, newQuantity)
    } catch (error) {
      console.error('Failed to update quantity:', error)
    }
  }

  const handleRemove = async (productId: number) => {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return
    try {
      await removeFromCart(productId)
    } catch (error) {
      console.error('Failed to remove item:', error)
    }
  }

  const handleCheckout = () => {
    if (!user) {
      const shouldLogin = confirm('Bạn cần đăng nhập để thanh toán. Đăng nhập ngay?')
      if (shouldLogin) {
        navigate('/login?redirect=/checkout')
      }
      return
    }
    navigate('/checkout')
  }

  if (!user) {
    // Allow viewing cart without login
    // return (
    //   <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    //     <div className="text-center">
    //       <h2 className="text-2xl font-bold mb-4">Vui lòng đăng nhập</h2>
    //       <p className="text-gray-600 mb-6">Bạn cần đăng nhập để xem giỏ hàng</p>
    //       <Link to="/login" className="btn-primary">
    //         Đăng nhập
    //       </Link>
    //     </div>
    //   </div>
    // )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-4xl font-bold mb-8">Giỏ hàng của bạn</h1>

      {error && (
        <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded mb-6">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải giỏ hàng...</p>
        </div>
      ) : items.length === 0 ? (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="text-center py-12"
        >
          <div className="text-6xl mb-4">🛒</div>
          <p className="text-xl text-gray-600 mb-6">Giỏ hàng trống</p>
          <Link to="/products" className="btn-primary">
            Tiếp tục mua sắm
          </Link>
        </motion.div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2 space-y-4">
            {items.map((item, index) => (
              <motion.div
                key={item.productId}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
                className="card p-4 flex gap-4"
              >
                <img
                  src={item.productImage || 'https://via.placeholder.com/150'}
                  alt={item.productName}
                  className="w-24 h-24 object-cover rounded-lg"
                />
                <div className="flex-1">
                  <h3 className="font-bold text-lg mb-1">{item.productName}</h3>
                  <p className="text-primary-600 font-semibold mb-2">
                    {item.productPrice.toLocaleString('vi-VN')} đ
                  </p>
                  {!item.inStock && (
                    <p className="text-red-600 text-sm mb-2">Hết hàng</p>
                  )}
                  <div className="flex items-center gap-3">
                    <div className="flex items-center border rounded-lg">
                      <button
                        onClick={() => handleUpdateQuantity(item.productId, item.quantity - 1)}
                        disabled={item.quantity <= 1 || loading}
                        className="px-3 py-1 hover:bg-gray-100 disabled:opacity-50"
                      >
                        -
                      </button>
                      <span className="px-4 py-1 border-x">{item.quantity}</span>
                      <button
                        onClick={() => handleUpdateQuantity(item.productId, item.quantity + 1)}
                        disabled={item.quantity >= item.availableStock || loading}
                        className="px-3 py-1 hover:bg-gray-100 disabled:opacity-50"
                      >
                        +
                      </button>
                    </div>
                    <button
                      onClick={() => handleRemove(item.productId)}
                      disabled={loading}
                      className="text-red-600 hover:text-red-700 text-sm"
                    >
                      Xóa
                    </button>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-bold text-lg">
                    {item.subtotal.toLocaleString('vi-VN')} đ
                  </p>
                </div>
              </motion.div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="card p-6 sticky top-4">
              <h2 className="text-2xl font-bold mb-4">Tổng đơn hàng</h2>
              <div className="space-y-3 mb-6">
                <div className="flex justify-between">
                  <span className="text-gray-600">Số lượng:</span>
                  <span className="font-semibold">{totalItems} sản phẩm</span>
                </div>
                <div className="flex justify-between text-lg font-bold border-t pt-3">
                  <span>Tổng cộng:</span>
                  <span className="text-primary-600">
                    {totalAmount.toLocaleString('vi-VN')} đ
                  </span>
                </div>
              </div>
              <button
                onClick={handleCheckout}
                disabled={items.some(item => !item.inStock)}
                className="w-full btn-primary disabled:opacity-50"
              >
                Thanh toán
              </button>
              <Link
                to="/products"
                className="block text-center mt-3 text-primary-600 hover:text-primary-700"
              >
                Tiếp tục mua sắm
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
