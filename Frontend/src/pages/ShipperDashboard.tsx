import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import Layout from '../components/Layout';
import { useAuthStore } from '../store/authStore';
import axios from '../api/axios';

interface OrderItem {
  id: number;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

interface Order {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  shippingAddress: string;
  phoneNumber: string;
  customerName: string;
  orderItems: OrderItem[];
  assignedAt: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  deliveryNotes?: string;
  createdAt: string;
}

interface DashboardState {
  orders: Order[];
  loading: boolean;
  error: string | null;
  pagination: {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
  };
  selectedStatus: string;
  expandedOrderId: number | null;
}

const STATUS_FILTERS = ['ALL', 'CONFIRMED', 'READY_FOR_PICKUP', 'DELIVERED'] as const;

const translateStatus = (status: string): string => {
  switch (status) {
    case 'ALL': return 'Tất cả';
    case 'CONFIRMED': return 'Đã xác nhận';
    case 'PREPARING': return 'Đang chuẩn bị';
    case 'READY_FOR_PICKUP': return 'Sẵn sàng lấy hàng';
    case 'DELIVERED': return 'Đã giao xong';
    case 'CANCELLED': return 'Đã hủy';
    default: return status;
  }
};

const getStatusColor = (status: string): string => {
  switch (status) {
    case 'CONFIRMED': return 'bg-yellow-100 text-yellow-800';
    case 'PREPARING': return 'bg-orange-100 text-orange-800';
    case 'READY_FOR_PICKUP': return 'bg-blue-100 text-blue-800';
    case 'DELIVERED': return 'bg-green-100 text-green-800';
    case 'CANCELLED': return 'bg-red-100 text-red-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const ShipperDashboard: React.FC = () => {
  const { token } = useAuthStore();
  const [state, setState] = useState<DashboardState>({
    orders: [],
    loading: true,
    error: null,
    pagination: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    selectedStatus: 'ALL',
    expandedOrderId: null,
  });

  useEffect(() => {
    fetchOrders(0, 'ALL');
  }, []);

  const fetchOrders = async (page: number, status: string) => {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      if (!token) {
        toast.error('Vui lòng đăng nhập lại.');
        return;
      }
      const params: Record<string, string> = {
        page: page.toString(),
        size: state.pagination.size.toString(),
      };
      if (status !== 'ALL') {
        params['status'] = status;
      }
      const { data } = await axios.get('/api/shipper/orders', { params });
      setState((prev) => ({
        ...prev,
        orders: data.content,
        pagination: {
          page: data.pageable.pageNumber,
          size: data.pageable.pageSize,
          totalPages: data.totalPages,
          totalElements: data.totalElements,
        },
        loading: false,
      }));
    } catch (error: any) {
      const msg = error?.response?.data?.error || 'Không thể tải danh sách đơn hàng';
      setState((prev) => ({ ...prev, error: msg, loading: false }));
      toast.error(msg);
    }
  };

  const handleAccept = async (orderId: number) => {
    try {
      await axios.put(`/api/shipper/orders/${orderId}/accept`);
      toast.success('Đã nhận đơn hàng!');
      fetchOrders(state.pagination.page, state.selectedStatus);
    } catch (error: any) {
      const msg = error?.response?.data?.error || 'Không thể nhận đơn hàng';
      toast.error(msg);
    }
  };

  const handlePickup = async (orderId: number) => {
    try {
      await axios.put(`/api/shipper/orders/${orderId}/pickup`);
      toast.success('Đã nhận đơn hàng!');
      fetchOrders(state.pagination.page, state.selectedStatus);
    } catch (error: any) {
      const msg = error?.response?.data?.error || 'Không thể nhận đơn hàng';
      toast.error(msg);
    }
  };

  const handleDeliver = async (orderId: number) => {
    try {
      await axios.put(`/api/shipper/orders/${orderId}/deliver`, {});
      toast.success('Đã giao hàng thành công!');
      fetchOrders(state.pagination.page, state.selectedStatus);
    } catch (error: any) {
      const msg = error?.response?.data?.error || 'Không thể giao hàng';
      toast.error(msg);
    }
  };

  const handleStatusFilter = (status: string) => {
    setState((prev) => ({ ...prev, selectedStatus: status }));
    fetchOrders(0, status);
  };

  const handlePageChange = (newPage: number) => {
    fetchOrders(newPage, state.selectedStatus);
  };

  const toggleOrderExpansion = (orderId: number) => {
    setState((prev) => ({
      ...prev,
      expandedOrderId: prev.expandedOrderId === orderId ? null : orderId,
    }));
  };

  return (
    <Layout>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-6">Đơn hàng giao hàng của tôi</h1>

        {/* Status Filter */}
        <div className="mb-6 flex gap-2 flex-wrap">
          {STATUS_FILTERS.map((status) => (
            <button
              key={status}
              onClick={() => handleStatusFilter(status)}
              className={`px-4 py-2 rounded-lg transition-colors ${
                state.selectedStatus === status
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              {translateStatus(status)}
            </button>
          ))}
        </div>

        {/* Loading State */}
        {state.loading && (
          <div className="text-center py-8">
            <p className="text-gray-600">Đang tải đơn hàng...</p>
          </div>
        )}

        {/* Error State */}
        {state.error && !state.loading && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {state.error}
          </div>
        )}

        {/* Orders Table */}
        {!state.loading && !state.error && (
          <>
            {state.orders.length === 0 ? (
              <div className="text-center py-8 bg-gray-50 rounded-lg">
                <p className="text-gray-600">Không có đơn hàng nào</p>
              </div>
            ) : (
              <div className="bg-white shadow-md rounded-lg overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Mã đơn
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Khách hàng
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Địa chỉ
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Trạng thái
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Thao tác
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {state.orders.map((order) => (
                      <React.Fragment key={order.id}>
                        <tr className="hover:bg-gray-50 cursor-pointer">
                          <td
                            className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900"
                            onClick={() => toggleOrderExpansion(order.id)}
                          >
                            {order.orderNumber}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {order.customerName}
                            <br />
                            <span className="text-xs">{order.phoneNumber}</span>
                          </td>
                          <td className="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">
                            {order.shippingAddress}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span
                              className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(order.status)}`}
                            >
                              {translateStatus(order.status)}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                            {order.status === 'CONFIRMED' && (
                              <button
                                onClick={() => handleAccept(order.id)}
                                className="text-blue-600 hover:text-blue-900"
                              >
                                Nhận đơn
                              </button>
                            )}
                            {order.status === 'READY_FOR_PICKUP' && !order.pickedUpAt && (
                              <button
                                onClick={() => handlePickup(order.id)}
                                className="text-blue-600 hover:text-blue-900"
                              >
                                Lấy hàng
                              </button>
                            )}
                            {order.status === 'READY_FOR_PICKUP' && order.pickedUpAt && (
                              <button
                                onClick={() => handleDeliver(order.id)}
                                className="text-green-600 hover:text-green-900"
                              >
                                Giao hàng
                              </button>
                            )}
                          </td>
                        </tr>

                        {/* Expanded Order Details */}
                        {state.expandedOrderId === order.id && (
                          <tr>
                            <td colSpan={5} className="px-6 py-4 bg-gray-50">
                              <div className="space-y-4">
                                <h4 className="font-semibold">Mặt hàng:</h4>
                                <ul className="list-disc list-inside space-y-1">
                                  {order.orderItems.map((item) => (
                                    <li key={item.id} className="text-sm text-gray-700">
                                      {item.productName} x {item.quantity} - ${item.subtotal.toFixed(2)}
                                    </li>
                                  ))}
                                </ul>
                                <p className="text-sm">
                                  <strong>Tổng tiền:</strong> ${order.totalAmount.toFixed(2)}
                                </p>
                                {order.deliveryNotes && (
                                  <p className="text-sm">
                                    <strong>Ghi chú:</strong> {order.deliveryNotes}
                                  </p>
                                )}
                                {order.pickedUpAt && (
                                  <p className="text-sm text-blue-600">
                                    Đã nhận lúc: {new Date(order.pickedUpAt).toLocaleString('vi-VN')}
                                  </p>
                                )}
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Pagination */}
            {state.pagination.totalPages > 1 && (
              <div className="flex justify-center items-center gap-4 mt-6">
                <button
                  onClick={() => handlePageChange(state.pagination.page - 1)}
                  disabled={state.pagination.page === 0}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors"
                >
                  Trang trước
                </button>
                <span className="text-gray-700">
                  Trang {state.pagination.page + 1} / {state.pagination.totalPages}
                </span>
                <button
                  onClick={() => handlePageChange(state.pagination.page + 1)}
                  disabled={state.pagination.page >= state.pagination.totalPages - 1}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors"
                >
                  Trang sau
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  );
};

export default ShipperDashboard;
