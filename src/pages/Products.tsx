import { motion, AnimatePresence } from 'framer-motion'
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

  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [sortBy, setSortBy] = useState('')
  const [categories, setCategories] = useState<string[]>([])
  const [showFilters, setShowFilters] = useState(false)
  const [isDesktop, setIsDesktop] = useState(() => window.innerWidth >= 1024)

  useEffect(() => {
    const onResize = () => setIsDesktop(window.innerWidth >= 1024)
    window.addEventListener('resize', onResize)
    return () => window.removeEventListener('resize', onResize)
  }, [])

  const filtered = products.filter(p => {
    if (search && !p.name.toLowerCase().includes(search.toLowerCase()) && !p.description?.toLowerCase().includes(search.toLowerCase())) return false
    if (category && p.category !== category) return false
    if (minPrice && p.price < Number(minPrice)) return false
    if (maxPrice && p.price > Number(maxPrice)) return false
    return true
  })

  const sorted = [...filtered].sort((a, b) => {
    if (sortBy === 'price-asc') return a.price - b.price
    if (sortBy === 'price-desc') return b.price - a.price
    if (sortBy === 'name') return a.name.localeCompare(b.name)
    return 0
  })

  useEffect(() => {
    const loadProducts = async () => {
      setLoading(true)
      try {
        const response = await axios.get('/api/products')
        const data = Array.isArray(response.data) ? response.data : []
        setProducts(data)
        const cats = [...new Set(data.map((p: Product) => p.category).filter(Boolean))] as string[]
        setCategories(cats)
      } catch (err: any) {
        setError(err?.response?.data?.error || 'Khong the tai danh sach san pham.')
      } finally {
        setLoading(false)
      }
    }
    void loadProducts()
  }, [])

  const clearFilters = () => {
    setSearch('')
    setCategory('')
    setMinPrice('')
    setMaxPrice('')
    setSortBy('')
  }

  const hasFilters = search || category || minPrice || maxPrice || sortBy

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-4xl font-bold">Sản phẩm của chúng tôi</h1>
        <button
          onClick={() => setShowFilters(!showFilters)}
          className="lg:hidden px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50"
        >
          {showFilters ? 'Ẩn bộ lọc' : 'Bộ lọc'}
        </button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-2 rounded mb-4">{error}</div>
      )}

      <div className="flex flex-col lg:flex-row gap-8">
        <div className="flex-1 min-w-0">
          {loading ? (
            <p className="text-gray-600">Dang tai san pham...</p>
          ) : sorted.length === 0 ? (
            <div className="text-center py-16">
              <p className="text-gray-500 text-lg mb-4">Khong tim thay san pham phu hop.</p>
              {hasFilters && (
                <button onClick={clearFilters} className="text-primary-600 hover:text-primary-800 font-medium">
                  Xoa bo loc
                </button>
              )}
            </div>
          ) : (
            <>
              <p className="text-sm text-gray-500 mb-4">{sorted.length} sản phẩm</p>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {sorted.map((product, index) => (
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
                    {product.category && (
                      <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded-full mb-2">
                        {product.category}
                      </span>
                    )}
                    <h3 className="text-xl font-bold mb-2">{product.name}</h3>
                    <p className="text-gray-600 text-sm mb-3 line-clamp-2">{product.description || 'Chua co mo ta'}</p>
                    <p className="text-gray-900 font-semibold mb-4">{product.price.toLocaleString('vi-VN')} đ</p>
                    <Link to={`/products/${product.id}`} className="w-full inline-block text-center btn-primary">
                      Xem chi tiet
                    </Link>
                  </motion.div>
                ))}
              </div>
            </>
          )}
        </div>

        <AnimatePresence>
          {(showFilters || isDesktop) && (
            <motion.aside
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              className="w-full lg:w-72 shrink-0"
            >
              <div className="lg:sticky lg:top-24 bg-white border border-gray-200 rounded-xl p-6">
                <h2 className="text-lg font-bold mb-4">Bộ lọc</h2>
                <div className="space-y-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Tìm kiếm</label>
                    <input
                      type="text"
                      placeholder="Nhập tên sản phẩm..."
                      value={search}
                      onChange={e => setSearch(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                  </div>

                  {categories.length > 0 && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Danh mục</label>
                      <select
                        value={category}
                        onChange={e => setCategory(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                      >
                        <option value="">Tất cả</option>
                        {categories.map(c => (
                          <option key={c} value={c}>{c}</option>
                        ))}
                      </select>
                    </div>
                  )}

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Khoảng giá</label>
                    <div className="flex gap-2 items-center">
                      <input
                        type="text"
                        inputMode="numeric"
                        placeholder="Từ"
                        value={minPrice}
                        onChange={e => setMinPrice(e.target.value.replace(/\D/g, ''))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                      />
                      <span className="text-gray-500">-</span>
                      <input
                        type="text"
                        inputMode="numeric"
                        placeholder="Đến"
                        value={maxPrice}
                        onChange={e => setMaxPrice(e.target.value.replace(/\D/g, ''))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Sắp xếp</label>
                    <select
                      value={sortBy}
                      onChange={e => setSortBy(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    >
                      <option value="">Mặc định</option>
                      <option value="price-asc">Giá: Thấp đến Cao</option>
                      <option value="price-desc">Giá: Cao đến Thấp</option>
                      <option value="name">Tên A-Z</option>
                    </select>
                  </div>

                  {hasFilters && (
                    <button
                      onClick={clearFilters}
                      className="w-full py-2 text-sm text-red-600 hover:text-red-800 border border-red-300 rounded-lg hover:bg-red-50 transition"
                    >
                      Xóa bộ lọc
                    </button>
                  )}
                </div>
              </div>
            </motion.aside>
          )}
        </AnimatePresence>
      </div>
    </div>
  )
}
