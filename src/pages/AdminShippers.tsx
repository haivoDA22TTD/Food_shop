import { useEffect, useState } from 'react';
import { useShipperStore } from '../store/shipperStore';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import CreateShipperForm from '../components/admin/CreateShipperForm';

export default function AdminShippers() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const {
    shippers,
    loading,
    error,
    clearError,
    fetchShippers,
  } = useShipperStore();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [showCreateAccountModal, setShowCreateAccountModal] = useState(false);

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/');
      return;
    }
    fetchShippers();
  }, [user, navigate, fetchShippers]);

  // Filter shippers locally based on search and status
  const filteredShippers = (shippers || []).filter((s: any) => {
    const matchSearch = !searchQuery ||
      s.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.phone?.includes(searchQuery);
    const matchStatus = !statusFilter || s.status === statusFilter;
    return matchSearch && matchStatus;
  });

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

      {/* Statistics - Temporarily disabled until backend API is ready */}
      {/*
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
      */}


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
            onClick={() => setShowCreateAccountModal(true)}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            + Tạo Shipper
          </button>
        </div>
      </div>

      {/* Message when no backend API */}
      <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 px-4 py-3 rounded mb-6">
        <p className="text-sm">
          <strong>Lưu ý:</strong> Hiện tại hệ thống chỉ hỗ trợ tạo tài khoản shipper với authentication (username/password). 
          Backend API quản lý danh sách shipper đang được phát triển.
        </p>
      </div>

      {/* Instructions */}
      <div className="bg-white rounded-lg shadow p-8">
        <h2 className="text-xl font-bold mb-4">Hướng dẫn tạo tài khoản Shipper</h2>
        <ol className="list-decimal list-inside space-y-2 text-gray-700">
          <li>Click button <strong>"+ Tạo Shipper"</strong> phía trên</li>
          <li>Điền đầy đủ thông tin:
            <ul className="list-disc list-inside ml-6 mt-1">
              <li>Username (tối thiểu 3 ký tự)</li>
              <li>Password (tối thiểu 8 ký tự)</li>
              <li>Email (định dạng email hợp lệ)</li>
              <li>Full Name (họ tên đầy đủ)</li>
              <li>Phone (10-15 số)</li>
              <li>Vehicle Type (tùy chọn: Motorbike, Car, Bicycle)</li>
              <li>Vehicle Number (tùy chọn: biển số xe)</li>
            </ul>
          </li>
          <li>Click <strong>"Create Shipper"</strong> để tạo tài khoản</li>
          <li>Shipper có thể đăng nhập ngay với username/password vừa tạo</li>
          <li>Sau khi login, shipper sẽ tự động chuyển đến trang quản lý đơn hàng</li>
        </ol>

        <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded">
          <p className="text-sm text-blue-800">
            <strong>Ví dụ:</strong><br/>
            Username: <code>shipper1</code><br/>
            Password: <code>Shipper@123</code><br/>
            Email: <code>shipper1@example.com</code><br/>
            Name: <code>Nguyễn Văn A</code><br/>
            Phone: <code>+84901234567</code>
          </p>
        </div>
      </div>

      {/* Shippers Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden mb-6">
        {loading ? (
          <div className="p-8 text-center text-gray-500">Đang tải...</div>
        ) : filteredShippers.length === 0 ? (
          <div className="p-8 text-center text-gray-500">Chưa có shipper nào</div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tên</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SĐT</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Phương tiện</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Đánh giá</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredShippers.map((s: any) => (
                <tr key={s.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{s.name}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{s.email}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{s.phone}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{s.vehicleType || '-'} {s.vehicleNumber || ''}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 text-xs rounded-full font-medium ${
                      s.status === 'AVAILABLE' ? 'bg-green-100 text-green-800' :
                      s.status === 'BUSY' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>{s.status}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-yellow-600">⭐ {s.rating?.toFixed(1) || '5.0'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Create Shipper Account Modal */}
      {showCreateAccountModal && (
        <CreateShipperForm
          onSuccess={() => {
            setShowCreateAccountModal(false);
            fetchShippers(); // Reload danh sách sau khi tạo thành công
          }}
          onCancel={() => setShowCreateAccountModal(false)}
        />
      )}
    </div>
  );
}