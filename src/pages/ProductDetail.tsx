import { motion } from 'framer-motion'
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

  useEffect(() => {
    const loadProduct = async () => {
      if (!id) {
        setError('San pham khong hop le.')
        setLoading(false)
        return
      }

      setLoading(true)
      try {
        const response = await axios.get(`/api/products/${id}`)
        setProduct(response.data || null)
      } catch (err: any) {
        if (err?.response?.status === 404) {
          setError('Khong tim thay san pham.')
        } else {
          setError(err?.response?.data?.error || 'Khong the tai chi tiet san pham.')
        }
      } finally {
        setLoading(false)
      }
    }

    void loadProduct()
  }, [id])

  const handleAddToCart = async () => {
    if (!product) return

    setAddingToCart(true)
    try {
      await addToCart(product.id, quantity, {
        name: product.name,
        price: product.price,
        image: product.image,
        stock: product.stock,
      })
      alert('Đã thêm vào giỏ hàng!')
      
      // If user is not logged in, suggest login
      if (!user) {
        const shouldLogin = confirm('Bạn muốn đăng nhập để lưu giỏ hàng?')
        if (shouldLogin) {
          navigate('/login')
        }
      }
    } catch (error: any) {
      console.error('Add to cart error:', error)
      // Don't show error for guest users - cart is saved locally
      if (!user) {
        alert('Đã thêm vào giỏ hàng!')
      } else {
        alert('Không thể thêm vào giỏ hàng. Vui lòng thử lại.')
      }
    } finally {
      setAddingToCart(false)
    }
  }

  if (!id) {
    return <Navigate to="/products" />
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      {loading ? (
        <p className="text-gray-600">Dang tai chi tiet san pham...</p>
      ) : error ? (
        <div className="space-y-3">
          <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-2 rounded">{error}</div>
          <button type="button" className="btn-secondary" onClick={() => navigate('/products')}>
            Quay lai danh sach
          </button>
        </div>
      ) : product ? (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="grid grid-cols-1 md:grid-cols-2 gap-8"
        >
          <img
            src={product.image || 'https://via.placeholder.com/600x400?text=No+Image'}
            alt={product.name}
            className="w-full h-96 object-cover rounded-xl"
          />
          <div>
            <h1 className="text-4xl font-bold mb-4">{product.name}</h1>
            <p className="text-2xl text-primary-600 mb-4">{product.price.toLocaleString('vi-VN')} đ</p>
            <p className="text-gray-600 mb-3">{product.description || 'Chua co mo ta chi tiet.'}</p>
            <p className="text-gray-700 mb-1">Ton kho: {product.stock}</p>
            <p className="text-gray-700 mb-6">Danh muc: {product.category || 'N/A'}</p>
            
            <div className="flex items-center gap-4 mb-6">
              <label className="font-semibold">Số lượng:</label>
              <div className="flex items-center border rounded-lg">
                <button
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className="px-4 py-2 hover:bg-gray-100"
                >
                  -
                </button>
                <span className="px-6 py-2 border-x">{quantity}</span>
                <button
                  onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                  className="px-4 py-2 hover:bg-gray-100"
                >
                  +
                </button>
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
      ) : null}
    </div>
  )
}
