import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useState, useEffect } from 'react'
import { useAuthStore } from '../store/authStore'
import { useCartStore } from '../store/cartStore'

export default function Navbar() {
  const [isOpen, setIsOpen] = useState(false)
  const { user, logout } = useAuthStore()
  const { totalItems, fetchCart } = useCartStore()

  useEffect(() => {
    if (user) {
      fetchCart()
    }
  }, [user, fetchCart])

  return (
    <nav className="bg-white shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center">
              <motion.span 
                className="text-2xl font-bold text-primary-600"
                whileHover={{ scale: 1.05 }}
              >
                🍔 Food Shop
              </motion.span>
            </Link>
          </div>

          {/* Desktop Menu */}
          <div className="hidden md:flex items-center space-x-8">
            <Link to="/" className="text-gray-700 hover:text-primary-600 transition-colors">
              Trang chủ
            </Link>
            <Link to="/products" className="text-gray-700 hover:text-primary-600 transition-colors">
              Sản phẩm
            </Link>
            <Link to="/cart" className="text-gray-700 hover:text-primary-600 transition-colors relative">
              🛒 Giỏ hàng
              {totalItems > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center font-bold">
                  {totalItems}
                </span>
              )}
            </Link>
            
            {user ? (
              <>
                <Link to="/orders" className="text-gray-700 hover:text-primary-600 transition-colors">
                  Đơn hàng
                </Link>
                {user.role?.toUpperCase() === 'ADMIN' && (
                  <>
                    <Link to="/admin/products" className="text-gray-700 hover:text-primary-600 transition-colors">
                      Quản lý SP
                    </Link>
                    <Link to="/admin/orders" className="text-gray-700 hover:text-primary-600 transition-colors">
                      Quản lý ĐH
                    </Link>
                  </>
                )}
                <Link to="/profile" className="text-gray-700 hover:text-primary-600 transition-colors">
                  Tài khoản
                </Link>
                <button
                  onClick={() => void logout()}
                  className="text-gray-700 hover:text-primary-600 transition-colors"
                >
                  Đăng xuất
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-gray-700 hover:text-primary-600 transition-colors">
                  Đăng nhập
                </Link>
                <Link to="/register" className="btn-primary">
                  Đăng ký
                </Link>
              </>
            )}
          </div>

          {/* Mobile menu button */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setIsOpen(!isOpen)}
              className="text-gray-700 hover:text-primary-600"
            >
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                {isOpen ? (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                ) : (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                )}
              </svg>
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {isOpen && (
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="md:hidden bg-white border-t"
        >
          <div className="px-2 pt-2 pb-3 space-y-1">
            <Link to="/" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
              Trang chủ
            </Link>
            <Link to="/products" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
              Sản phẩm
            </Link>
            <Link to="/cart" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
              🛒 Giỏ hàng {totalItems > 0 && `(${totalItems})`}
            </Link>
            {user ? (
              <>
                <Link to="/orders" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
                  Đơn hàng
                </Link>
                {user.role?.toUpperCase() === 'ADMIN' && (
                  <>
                    <Link to="/admin/products" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
                      Quản lý sản phẩm
                    </Link>
                    <Link to="/admin/orders" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
                      Quản lý đơn hàng
                    </Link>
                  </>
                )}
                <Link to="/profile" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
                  Tài khoản
                </Link>
                <button
                  onClick={() => void logout()}
                  className="block w-full text-left px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
                >
                  Đăng xuất
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
                  Đăng nhập
                </Link>
                <Link to="/register" className="block px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-md">
                  Đăng ký
                </Link>
              </>
            )}
          </div>
        </motion.div>
      )}
    </nav>
  )
}
