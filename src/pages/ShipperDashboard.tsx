import { useEffect, useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import axios from '../api/axios';
import { motion } from 'framer-motion';

interface ShipperProfile {
  id: number;
  name: string;
  phone: string;
  email?: string;
  status: string;
  vehicleType?: string;
  vehicleNumber?: string;
  totalDeliveries: number;
  successfulDeliveries: number;
  rating: number;
  totalRatings: number;
  successRate: number;
}

interface Order {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  shippingAddress: string;
  phoneNumber: string;
  username?: string;
  assignedAt: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  deliveryNotes?: string;
  createdAt: string;
}

export default function ShipperDashboard() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [profile, setProfile] = useState<ShipperProfile | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'pending' | 'completed'>('pending');
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [deliveryNotes, setDeliveryNotes] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    if (!user || user.role !== 'SHIPPER') {
      navigate('/shipper/login');
      return;
    }
    loadData();
  }, [user, activeTab]);

  const loadData = async () => {
    setLoading(true);
    try {
      // Load profile
      const profileRes = await axios.get('/api/shipper/profile');
      setProfile(profileRes.data);

      // Load orders
      const status = activeTab === 'pending' ? 'READY_FOR_PICKUP' : 'DELIVERED';
      const ordersRes = await axios.get('/api/shipper/orders', {
        params: { status, size: 50 },
      });
      setOrders(ordersRes.data.content || []);
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Không thể tải dữ liệu');
    } finally {
      setLoading(false);
    }
  };

  const handlePickup = async (orderId: number) => {
    if (!confirm('Xác nhận đã lấy hàng?')) return;
    setActionLoading(true);
    try {
      await axios.put(`/api/shipper/orders/${orderId}/pickup`);
      alert('Đã xác nhận lấy hàng!');
      loadData();
    } catch (err: any) {
      alert(err?.response?.data?.error || 'Không thể cập nhật');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeliver = async (orderId: number) => {
    if (!confirm('Xác nhận đã giao hàng thành công?')) return;
    setActionLoading(true);
    try {
      await axios.put(`/api/shipper/orders/${orderId}/deliver`, {
        notes: deliveryNotes || undefined,
      });
      alert('Đã xác nhận giao hàng thành công!');
      setDeliveryNotes('');
      setSelectedOrder(null);
      loadData();
    } catch (err: any) {
      alert(err?.response?.data?.error || 'Không thể cập nhật');
    } finally {
      setActionLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'READY_FOR_PICKUP':
        return 'bg-indigo-100 text-indigo-800';
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'READY_FOR_PICKUP':
        return 'Sẵn sàng lấy hàng';
      case 'DELIVERED':
        return 'Đã giao';
      default:
        return status;
    }
  };

  if (!user || user.role !== 'SHIPPER') {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <span className="text-3xl mr-3">🚚</span>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Shipper Dashboard</h1>
                <p className="text-sm text-gray-600">Xin chào, {profile?.name || 'Shipper'}!</p>
              </div>
            </div>
            <button
              onClick={() => {
                useAuthStore.getState().logout();
                navigate('/shipper/login');
              }}
              className="px-4 py-2 text-sm text-gray-700 hover:text-gray-900"
            >
              Đăng xuất
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Statistics */}
        {profile && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-white p-6 rounded-lg shadow"
            >
              <div className="text-sm text-gray-600 mb-1">Tổng Đơn Giao</div>
              <div className="text-3xl font-bold text-gray-900">{profile.totalDeliveries}</div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
              className="bg-white p-6 rounded-lg shadow"
            >
              <div className="text-sm text-gray-600 mb-1">Thành Công</div>
              <div className="text-3xl font-bold text-green-600">
                {profile.successfulDeliveries}
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              className="bg-white p-6 rounded-lg shadow"
            >
              <div className="text-sm text-gray-600 mb-1">Tỷ Lệ Thành Công</div>
              <div className="text-3xl font-bold text-blue-600">
                {profile.successRate.toFixed(1)}%
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
              className="bg-white p-6 rounded-lg shadow"
            >
              <div className="text-sm text-gray-600 mb-1">Đánh Giá</div>
              <div className="text-3xl font-bold text-yellow-600">
                {profile.rating.toFixed(1)} ⭐
              </div>
              <div className="text-xs text-gray-500 mt-1">
                {profile.totalRatings} đánh giá
              </div>
            </motion.div>
          </div>
        )}

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow mb-6">
          <div className="border-b border-gray-200">
            <nav className="flex -mb-px">
              <button
                onClick={() => setActiveTab('pending')}
                className={`px-6 py-4 text-sm font-medium border-b-2 ${
                  activeTab === 'pending'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Đơn Cần Giao ({orders.length})
              </button>
              <button
                onClick={() => setActiveTab('completed')}
                className={`px-6 py-4 text-sm font-medium border-b-2 ${
                  activeTab === 'completed'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Đã Giao
              </button>
            </nav>
          </div>
        </div>

        {/* Orders List */}
        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600">Đang tải...</p>
          </div>
        ) : orders.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <span className="text-6xl mb-4 block">📦</span>
            <p className="text-xl text-gray-600">
              {activeTab === 'pending' ? 'Chưa có đơn hàng nào cần giao' : 'Chưa có đơn đã giao'}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order, index) => (
              <motion.div
                key={order.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
                className="bg-white rounded-lg shadow p-6"
              >
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900">{order.orderNumber}</h3>
                    <p className="text-sm text-gray-600">
                      Khách hàng: {order.username || 'N/A'}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 text-xs font-semibold rounded-full ${getStatusColor(
                      order.status
                    )}`}
                  >
                    {getStatusText(order.status)}
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <div>
                    <p className="text-sm text-gray-600">Địa chỉ giao hàng:</p>
                    <p className="font-medium">{order.shippingAddress}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Số điện thoại:</p>
                    <p className="font-medium">{order.phoneNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tổng tiền:</p>
                    <p className="font-medium text-green-600">
                      {order.totalAmount.toLocaleString('vi-VN')} đ
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Thời gian phân công:</p>
                    <p className="font-medium">
                      {new Date(order.assignedAt).toLocaleString('vi-VN')}
                    </p>
                  </div>
                </div>

                {order.pickedUpAt && (
                  <div className="mb-4 p-3 bg-blue-50 rounded-lg">
                    <p className="text-sm text-blue-800">
                      ✓ Đã lấy hàng: {new Date(order.pickedUpAt).toLocaleString('vi-VN')}
                    </p>
                  </div>
                )}

                {order.deliveredAt && (
                  <div className="mb-4 p-3 bg-green-50 rounded-lg">
                    <p className="text-sm text-green-800">
                      ✓ Đã giao: {new Date(order.deliveredAt).toLocaleString('vi-VN')}
                    </p>
                    {order.deliveryNotes && (
                      <p className="text-sm text-gray-600 mt-1">Ghi chú: {order.deliveryNotes}</p>
                    )}
                  </div>
                )}

                {activeTab === 'pending' && (
                  <div className="flex gap-3">
                    {!order.pickedUpAt && (
                      <button
                        onClick={() => handlePickup(order.id)}
                        disabled={actionLoading}
                        className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                      >
                        ✓ Đã Lấy Hàng
                      </button>
                    )}
                    {order.pickedUpAt && (
                      <button
                        onClick={() => setSelectedOrder(order)}
                        disabled={actionLoading}
                        className="flex-1 bg-green-600 text-white py-2 rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                      >
                        ✓ Đã Giao Hàng
                      </button>
                    )}
                  </div>
                )}
              </motion.div>
            ))}
          </div>
        )}
      </div>

      {/* Delivery Modal */}
      {selectedOrder && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h2 className="text-2xl font-bold mb-4">Xác Nhận Giao Hàng</h2>
            <p className="text-gray-600 mb-4">
              Đơn hàng: <span className="font-bold">{selectedOrder.orderNumber}</span>
            </p>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Ghi chú giao hàng (optional)
              </label>
              <textarea
                value={deliveryNotes}
                onChange={(e) => setDeliveryNotes(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500"
                rows={3}
                placeholder="Ví dụ: Đã giao cho người nhà, để trước cửa..."
              />
            </div>
            <div className="flex gap-3">
              <button
                onClick={() => handleDeliver(selectedOrder.id)}
                disabled={actionLoading}
                className="flex-1 bg-green-600 text-white py-2 rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
              >
                {actionLoading ? 'Đang xử lý...' : 'Xác Nhận'}
              </button>
              <button
                onClick={() => {
                  setSelectedOrder(null);
                  setDeliveryNotes('');
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
  );
}
