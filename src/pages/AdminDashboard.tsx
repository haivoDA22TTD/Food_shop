import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import axios from '../api/axios'
import { useAuthStore } from '../store/authStore'
import { Navigate } from 'react-router-dom'

interface TopProduct {
  productId: number
  productName: string
  totalSold: number
  totalRevenue: number
}

interface DailyRevenue {
  date: string
  orders: number
  revenue: number
}

interface DashboardData {
  totalOrders: number
  totalRevenue: number
  averageOrderValue: number
  totalCustomers: number
  totalShippers: number
  ordersByStatus: Record<string, number>
  topProducts: TopProduct[]
  revenueByDay: DailyRevenue[]
}

const statusLabels: Record<string, string> = {
  PENDING: 'Chờ xử lý',
  CONFIRMED: 'Đã xác nhận',
  PREPARING: 'Đang chuẩn bị',
  READY_FOR_PICKUP: 'Sẵn sàng lấy hàng',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
}

const statusColors: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  PREPARING: 'bg-purple-100 text-purple-800',
  READY_FOR_PICKUP: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
}

export default function AdminDashboard() {
  const { user } = useAuthStore()
  const [data, setData] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      loadDashboard()
    }
  }, [user])

  const loadDashboard = async () => {
    setLoading(true)
    try {
      const res = await axios.get('/api/admin/dashboard')
      setData(res.data)
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Không thể tải dữ liệu')
    } finally {
      setLoading(false)
    }
  }

  if (!user || user.role !== 'ADMIN') {
    return <Navigate to="/" />
  }

  const maxRevenue = data?.revenueByDay
    ? Math.max(...data.revenueByDay.map(d => Number(d.revenue)), 1)
    : 1

  const maxSold = data?.topProducts
    ? Math.max(...data.topProducts.map(p => p.totalSold), 1)
    : 1

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-4xl font-bold">Tổng quan</h1>
        <button
          onClick={loadDashboard}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2"
        >
          🔄 Làm mới
        </button>
      </div>

      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      ) : error ? (
        <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded">{error}</div>
      ) : data ? (
        <div className="space-y-8">
          {/* Stat Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="card p-6">
              <p className="text-sm text-gray-600">Tổng đơn hàng</p>
              <p className="text-3xl font-bold text-blue-600">{data.totalOrders}</p>
            </motion.div>
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }} className="card p-6">
              <p className="text-sm text-gray-600">Tổng doanh thu</p>
              <p className="text-3xl font-bold text-green-600">{Number(data.totalRevenue).toLocaleString('vi-VN')}đ</p>
            </motion.div>
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="card p-6">
              <p className="text-sm text-gray-600">Giá trị TB/đơn</p>
              <p className="text-3xl font-bold text-purple-600">{Number(data.averageOrderValue).toLocaleString('vi-VN')}đ</p>
            </motion.div>
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }} className="card p-6">
              <p className="text-sm text-gray-600">Shipper hoạt động</p>
              <p className="text-3xl font-bold text-orange-600">{data.totalShippers}</p>
            </motion.div>
          </div>

          {/* Orders by Status */}
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.4 }} className="card p-6">
            <h2 className="text-xl font-bold mb-4">Đơn hàng theo trạng thái</h2>
            <div className="space-y-3">
              {Object.entries(data.ordersByStatus).map(([status, count]) => (
                <div key={status} className="flex items-center gap-3">
                  <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusColors[status] || 'bg-gray-100'}`}>
                    {statusLabels[status] || status}
                  </span>
                  <div className="flex-1 bg-gray-200 rounded-full h-4 overflow-hidden">
                    <div
                      className="h-full bg-blue-500 rounded-full transition-all"
                      style={{ width: `${(count / Math.max(data.totalOrders, 1)) * 100}%` }}
                    ></div>
                  </div>
                  <span className="text-sm font-semibold w-12 text-right">{count}</span>
                </div>
              ))}
            </div>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Revenue by Day */}
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.5 }} className="card p-6">
              <h2 className="text-xl font-bold mb-4">Doanh thu 7 ngày</h2>
              <div className="flex items-end gap-2 h-48">
                {data.revenueByDay.map((day, i) => (
                  <div key={i} className="flex-1 flex flex-col items-center gap-1">
                    <span className="text-xs text-gray-500">{Number(day.revenue).toLocaleString('vi-VN')}</span>
                    <div
                      className="w-full bg-green-400 rounded-t transition-all hover:bg-green-500"
                      style={{ height: `${(Number(day.revenue) / maxRevenue) * 140}px`, minHeight: '4px' }}
                    ></div>
                    <span className="text-xs text-gray-600">{day.date}</span>
                  </div>
                ))}
              </div>
            </motion.div>

            {/* Top Products */}
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.6 }} className="card p-6">
              <h2 className="text-xl font-bold mb-4">Sản phẩm bán chạy (30 ngày)</h2>
              {data.topProducts.length === 0 ? (
                <p className="text-gray-500">Chưa có dữ liệu</p>
              ) : (
                <div className="space-y-3">
                  {data.topProducts.slice(0, 8).map((product, i) => (
                    <div key={i} className="flex items-center gap-3">
                      <span className="text-sm font-bold text-gray-400 w-6">#{i + 1}</span>
                      <div className="flex-1">
                        <p className="text-sm font-medium truncate">{product.productName}</p>
                        <div className="flex-1 bg-gray-200 rounded-full h-3 mt-1 overflow-hidden">
                          <div
                            className="h-full bg-orange-400 rounded-full"
                            style={{ width: `${(product.totalSold / maxSold) * 100}%` }}
                          ></div>
                        </div>
                      </div>
                      <span className="text-sm font-semibold text-orange-600">{product.totalSold} SP</span>
                    </div>
                  ))}
                </div>
              )}
            </motion.div>
          </div>
        </div>
      ) : null}
    </div>
  )
}
