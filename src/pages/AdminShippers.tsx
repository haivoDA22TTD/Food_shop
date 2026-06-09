import { useEffect, useState } from 'react';
import { useShipperStore, Shipper } from '../store/shipperStore';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import CreateShipperForm from '../components/admin/CreateShipperForm';

export default function AdminShippers() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const {
    shippers,
    statistics,
    loading,
    error,
    fetchShippers,
    fetchShipperStatistics,
    createShipper,
    updateShipper,
    updateShipperStatus,
    toggleShipperActive,
    deleteShipper,
    clearError,
  } = useShipperStore();

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showCreateAccountModal, setShowCreateAccountModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedShipper, setSelectedShipper] = useState<Shipper | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    email: '',
    address: '',
    vehicleType: '',
    vehicleNumber: '',
    notes: '',
  });

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/');
      return;
    }
    loadShippers();
    fetchShipperStatistics();
  }, [user, currentPage, statusFilter, searchQuery]);

  const loadShippers = async () => {
    try {
      const response = await fetchShippers(
        currentPage,
        20,
        statusFilter || undefined,
        searchQuery || undefined
      );
      setTotalPages(response.totalPages);
    } catch (err) {
      console.error('Failed to load shippers:', err);
    }
  };

  const handleCreateShipper = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createShipper(formData);
      setShowCreateModal(false);
      resetForm();
      alert('Shipper created successfully!');
    } catch (err) {
      console.error('Failed to create shipper:', err);
    }
  };

  const handleUpdateShipper = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedShipper) return;
    try {
      await updateShipper(selectedShipper.id, formData);
      setShowEditModal(false);
      setSelectedShipper(null);
      resetForm();
      alert('Shipper updated successfully!');
    } catch (err) {
      console.error('Failed to update shipper:', err);
    }
  };

  const handleStatusChange = async (shipperId: number, newStatus: string) => {
    try {
      await updateShipperStatus(shipperId, newStatus);
      alert('Status updated successfully!');
    } catch (err) {
      console.error('Failed to update status:', err);
    }
  };

  const handleToggleActive = async (shipperId: number) => {
    if (confirm('Are you sure you want to toggle this shipper\'s active status?')) {
      try {
        await toggleShipperActive(shipperId);
        alert('Shipper status toggled successfully!');
      } catch (err) {
        console.error('Failed to toggle status:', err);
      }
    }
  };

  const handleDeleteShipper = async (shipperId: number) => {
    if (confirm('Are you sure you want to deactivate this shipper?')) {
      try {
        await deleteShipper(shipperId);
        alert('Shipper deactivated successfully!');
      } catch (err) {
        console.error('Failed to delete shipper:', err);
      }
    }
  };

  const openEditModal = (shipper: Shipper) => {
    setSelectedShipper(shipper);
    setFormData({
      name: shipper.name,
      phone: shipper.phone,
      email: shipper.email || '',
      address: shipper.address || '',
      vehicleType: shipper.vehicleType || '',
      vehicleNumber: shipper.vehicleNumber || '',
      notes: shipper.notes || '',
    });
    setShowEditModal(true);
  };

  const resetForm = () => {
    setFormData({
      name: '',
      phone: '',
      email: '',
      address: '',
      vehicleType: '',
      vehicleNumber: '',
      notes: '',
    });
  };

  const getStatusBadgeColor = (status: string) => {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-green-100 text-green-800';
      case 'BUSY':
        return 'bg-yellow-100 text-yellow-800';
      case 'OFFLINE':
        return 'bg-gray-100 text-gray-800';
      case 'ON_BREAK':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusDisplayName = (status: string) => {
    switch (status) {
      case 'AVAILABLE':
        return 'Sẵn sàng';
      case 'BUSY':
        return 'Đang giao hàng';
      case 'OFFLINE':
        return 'Ngoại tuyến';
      case 'ON_BREAK':
        return 'Đang nghỉ';
      default:
        return status;
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Quản Lý Shipper</h1>
        <p className="text-gray-600">Quản lý danh sách người giao hàng</p>
      </div>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative">
          <span className="block sm:inline">{error}</span>
          <button
            onClick={clearError}
            className="absolute top-0 bottom-0 right-0 px-4 py-3"
          >
            <span className="text-2xl">&times;</span>
          </button>
        </div>
      )}

      {/* Statistics */}
      {statistics && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="text-sm text-gray-600 mb-1">Tổng Shipper</div>
            <div className="text-3xl font-bold text-gray-900">{statistics.totalShippers}</div>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="text-sm text-gray-600 mb-1">Đang Hoạt Động</div>
            <div className="text-3xl font-bold text-green-600">{statistics.activeShippers}</div>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="text-sm text-gray-600 mb-1">Sẵn Sàng</div>
            <div className="text-3xl font-bold text-blue-600">{statistics.availableShippers}</div>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="text-sm text-gray-600 mb-1">Đánh Giá TB</div>
            <div className="text-3xl font-bold text-yellow-600">
              {statistics.averageRating.toFixed(1)} ⭐
            </div>
          </div>
        </div>
      )}

      {/* Filters and Actions */}
      <div className="bg-white p-4 rounded-lg shadow mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <input
            type="text"
            placeholder="Tìm theo tên hoặc SĐT..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">Tất cả trạng thái</option>
            <option value="AVAILABLE">Sẵn sàng</option>
            <option value="BUSY">Đang giao hàng</option>
            <option value="OFFLINE">Ngoại tuyến</option>
            <option value="ON_BREAK">Đang nghỉ</option>
          </select>
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
          >
            + Thêm Shipper Profile
          </button>
          <button
            onClick={() => setShowCreateAccountModal(true)}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            + Tạo Tài Khoản Shipper
          </button>
        </div>
      </div>

      {/* Shippers Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600">Đang tải...</p>
          </div>
        ) : shippers.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-600">Không có shipper nào</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Shipper
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Liên Hệ
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Phương Tiện
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Trạng Thái
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Thống Kê
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Thao Tác
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {shippers.map((shipper) => (
                  <tr key={shipper.id} className={!shipper.isActive ? 'bg-gray-50' : ''}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center">
                          <span className="text-blue-600 font-semibold">
                            {shipper.name.charAt(0).toUpperCase()}
                          </span>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{shipper.name}</div>
                          <div className="text-sm text-gray-500">ID: {shipper.id}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{shipper.phone}</div>
                      {shipper.email && (
                        <div className="text-sm text-gray-500">{shipper.email}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{shipper.vehicleType || 'N/A'}</div>
                      {shipper.vehicleNumber && (
                        <div className="text-sm text-gray-500">{shipper.vehicleNumber}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <select
                        value={shipper.status}
                        onChange={(e) => handleStatusChange(shipper.id, e.target.value)}
                        className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusBadgeColor(
                          shipper.status
                        )}`}
                        disabled={!shipper.isActive}
                      >
                        <option value="AVAILABLE">Sẵn sàng</option>
                        <option value="BUSY">Đang giao hàng</option>
                        <option value="OFFLINE">Ngoại tuyến</option>
                        <option value="ON_BREAK">Đang nghỉ</option>
                      </select>
                      {!shipper.isActive && (
                        <div className="text-xs text-red-600 mt-1">Đã vô hiệu hóa</div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {shipper.totalDeliveries} đơn ({shipper.successRate.toFixed(1)}%)
                      </div>
                      <div className="text-sm text-gray-500">
                        ⭐ {shipper.rating.toFixed(1)} ({shipper.totalRatings} đánh giá)
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <button
                        onClick={() => openEditModal(shipper)}
                        className="text-blue-600 hover:text-blue-900 mr-3"
                      >
                        Sửa
                      </button>
                      <button
                        onClick={() => handleToggleActive(shipper.id)}
                        className={`${
                          shipper.isActive ? 'text-yellow-600 hover:text-yellow-900' : 'text-green-600 hover:text-green-900'
                        } mr-3`}
                      >
                        {shipper.isActive ? 'Vô hiệu' : 'Kích hoạt'}
                      </button>
                      <button
                        onClick={() => handleDeleteShipper(shipper.id)}
                        className="text-red-600 hover:text-red-900"
                      >
                        Xóa
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
            <div className="flex-1 flex justify-between sm:hidden">
              <button
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
                className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
              >
                Trước
              </button>
              <button
                onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                disabled={currentPage === totalPages - 1}
                className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
              >
                Sau
              </button>
            </div>
            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
              <div>
                <p className="text-sm text-gray-700">
                  Trang <span className="font-medium">{currentPage + 1}</span> /{' '}
                  <span className="font-medium">{totalPages}</span>
                </p>
              </div>
              <div>
                <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                  <button
                    onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                    disabled={currentPage === 0}
                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                  >
                    ←
                  </button>
                  <button
                    onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                    disabled={currentPage === totalPages - 1}
                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                  >
                    →
                  </button>
                </nav>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h2 className="text-2xl font-bold mb-4">Thêm Shipper Mới</h2>
            <form onSubmit={handleCreateShipper}>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tên <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Số Điện Thoại <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="tel"
                    required
                    pattern="[0-9]{10,11}"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Địa Chỉ</label>
                  <textarea
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    rows={2}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Loại Phương Tiện
                  </label>
                  <input
                    type="text"
                    placeholder="Xe máy, Ô tô, Xe đạp..."
                    value={formData.vehicleType}
                    onChange={(e) => setFormData({ ...formData, vehicleType: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Biển Số</label>
                  <input
                    type="text"
                    value={formData.vehicleNumber}
                    onChange={(e) => setFormData({ ...formData, vehicleNumber: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Ghi Chú</label>
                  <textarea
                    value={formData.notes}
                    onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    rows={2}
                  />
                </div>
              </div>
              <div className="mt-6 flex gap-3">
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                  {loading ? 'Đang tạo...' : 'Tạo Shipper'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateModal(false);
                    resetForm();
                  }}
                  className="flex-1 bg-gray-200 text-gray-800 py-2 rounded-lg hover:bg-gray-300 transition-colors"
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Create Shipper Account Modal */}
      {showCreateAccountModal && (
        <CreateShipperForm
          onSuccess={() => {
            setShowCreateAccountModal(false);
            loadShippers();
          }}
          onCancel={() => setShowCreateAccountModal(false)}
        />
      )}

      {/* Edit Modal */}
      {showEditModal && selectedShipper && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h2 className="text-2xl font-bold mb-4">Sửa Thông Tin Shipper</h2>
            <form onSubmit={handleUpdateShipper}>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tên <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Số Điện Thoại <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="tel"
                    required
                    pattern="[0-9]{10,11}"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Địa Chỉ</label>
                  <textarea
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    rows={2}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Loại Phương Tiện
                  </label>
                  <input
                    type="text"
                    placeholder="Xe máy, Ô tô, Xe đạp..."
                    value={formData.vehicleType}
                    onChange={(e) => setFormData({ ...formData, vehicleType: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Biển Số</label>
                  <input
                    type="text"
                    value={formData.vehicleNumber}
                    onChange={(e) => setFormData({ ...formData, vehicleNumber: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Ghi Chú</label>
                  <textarea
                    value={formData.notes}
                    onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    rows={2}
                  />
                </div>
              </div>
              <div className="mt-6 flex gap-3">
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                  {loading ? 'Đang cập nhật...' : 'Cập Nhật'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowEditModal(false);
                    setSelectedShipper(null);
                    resetForm();
                  }}
                  className="flex-1 bg-gray-200 text-gray-800 py-2 rounded-lg hover:bg-gray-300 transition-colors"
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
