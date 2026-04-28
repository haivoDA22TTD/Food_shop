import { motion } from 'framer-motion'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from '../api/axios'

interface Product {
  id: number
  name: string
  description?: string
  price: number
  image?: string
  stock: number
  category?: string
}

export default function Products() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const loadProducts = async () => {
      setLoading(true)
      try {
        const response = await axios.get('/api/products')
        setProducts(Array.isArray(response.data) ? response.data : [])
      } catch (err: any) {
        setError(err?.response?.data?.error || 'Khong the tai danh sach san pham.')
      } finally {
        setLoading(false)
      }
    }

    void loadProducts()
  }, [])

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-4xl font-bold mb-8">Sản phẩm của chúng tôi</h1>

      {error && (
        <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-2 rounded mb-4">
          {error}
        </div>
      )}

      {loading ? (
        <p className="text-gray-600">Dang tai san pham...</p>
      ) : products.length === 0 ? (
        <p className="text-gray-600">Chua co san pham nao.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {products.map((product, index) => (
            <motion.div
              key={product.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
              className="card p-6"
            >
              <img
                src={product.image || 'https://via.placeholder.com/300x200?text=No+Image'}
                alt={product.name}
                className="w-full h-40 object-cover rounded-lg mb-4"
              />
              <h3 className="text-xl font-bold mb-2">{product.name}</h3>
              <p className="text-gray-600 text-sm mb-3 line-clamp-2">{product.description || 'Chua co mo ta'}</p>
              <p className="text-gray-900 font-semibold mb-4">{product.price.toLocaleString('vi-VN')} đ</p>
              <Link to={`/products/${product.id}`} className="w-full inline-block text-center btn-primary">
                Xem chi tiet
              </Link>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  )
}
