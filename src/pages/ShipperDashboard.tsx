import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import Layout from '../components/Layout';
import { useAuthStore } from '../store/authStore';

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

interface PaginationInfo {
  pageNumber: number;
  pageSize: number;
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
  selectedStatus: 'ALL' | 'ASSIGNED' | 'IN_TRANSIT' | 'DELIVERED';
  expandedOrderId: number | null;
}

/**
 * Shipper dashboard component for viewing and managing assigned orders
 * Validates: Requirements 4.1, 4.2, 4.5, 4.6, 5.1, 5.6
 */
const ShipperDashboard: React.FC = () => {
  const { token } = useAuthStore();
  const [state, setState] = useState<DashboardState>({
    orders: [],
    loading: true,
    error: null,
    pagination: {
      page: 0,
      size: 20,
      totalPages: 0,
      totalElements: 0,
    },
    selectedStatus: 'ALL',
    expandedOrderId: null,
  });

  useEffect(() => {
    fetchOrders(state.pagination.page, state.selectedStatus);
  }, []);

  const fetchOrders = async (page: number, status: string) => {
    setState((prev) => ({ ...prev, loading: true, error: null }));

    try {
      if (!token) {
        toast.error('Authentication token not found. Please login again.');
        return;
      }

      const params = new URLSearchParams({
        page: page.toString(),
        size: state.pagination.size.toString(),
      });

      if (status !== 'ALL') {
        params.append('status', status);
      }

      const response = await fetch(`/api/orders/shipper/my-orders?${params}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
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
      } else {
        const errorData = await response.json();
        const errorMessage = errorData.error || 'Failed to fetch orders';
        setState((prev) => ({ ...prev, error: errorMessage, loading: false }));
        toast.error(errorMessage);
      }
    } catch (error) {
      console.error('Error fetching orders:', error);
      setState((prev) => ({ ...prev, error: 'Network error', loading: false }));
      toast.error('Network error. Please try again.');
    }
  };

  const updateOrderStatus = async (orderId: number, newStatus: string, notes?: string) => {
    try {
      if (!token) {
        toast.error('Authentication token not found. Please login again.');
        return;
      }

      const response = await fetch(`/api/orders/shipper/${orderId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ status: newStatus, notes }),
      });

      if (response.ok) {
        toast.success(`Order status updated to ${newStatus}`);
        // Refresh orders
        fetchOrders(state.pagination.page, state.selectedStatus);
      } else {
        const errorData = await response.json();
        const errorMessage = errorData.error || 'Failed to update order status';
        toast.error(errorMessage);
      }
    } catch (error) {
      console.error('Error updating order status:', error);
      toast.error('Network error. Please try again.');
    }
  };

  const handleStatusFilter = (status: 'ALL' | 'ASSIGNED' | 'IN_TRANSIT' | 'DELIVERED') => {
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

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ASSIGNED':
        return 'bg-yellow-100 text-yellow-800';
      case 'IN_TRANSIT':
        return 'bg-blue-100 text-blue-800';
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const canMarkInTransit = (order: Order) => {
    return order.status === 'ASSIGNED' || order.status === 'READY_FOR_PICKUP';
  };

  const canMarkDelivered = (order: Order) => {
    return order.status === 'IN_TRANSIT';
  };

  return (
    <Layout>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-6">My Delivery Orders</h1>

        {/* Status Filter */}
        <div className="mb-6 flex gap-2">
          {['ALL', 'ASSIGNED', 'IN_TRANSIT', 'DELIVERED'].map((status) => (
            <button
              key={status}
              onClick={() => handleStatusFilter(status as any)}
              className={`px-4 py-2 rounded-lg transition-colors ${
                state.selectedStatus === status
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              {status.replace('_', ' ')}
            </button>
          ))}
        </div>

        {/* Loading State */}
        {state.loading && (
          <div className="text-center py-8">
            <p className="text-gray-600">Loading orders...</p>
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
                <p className="text-gray-600">No orders found</p>
              </div>
            ) : (
              <div className="bg-white shadow-md rounded-lg overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Order Number
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Customer
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Address
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Actions
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
                              className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(
                                order.status
                              )}`}
                            >
                              {order.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                            {canMarkInTransit(order) && (
                              <button
                                onClick={() => updateOrderStatus(order.id, 'IN_TRANSIT')}
                                className="text-blue-600 hover:text-blue-900"
                              >
                                Mark In Transit
                              </button>
                            )}
                            {canMarkDelivered(order) && (
                              <button
                                onClick={() => updateOrderStatus(order.id, 'DELIVERED')}
                                className="text-green-600 hover:text-green-900"
                              >
                                Mark Delivered
                              </button>
                            )}
                          </td>
                        </tr>

                        {/* Expanded Order Details */}
                        {state.expandedOrderId === order.id && (
                          <tr>
                            <td colSpan={5} className="px-6 py-4 bg-gray-50">
                              <div className="space-y-4">
                                <h4 className="font-semibold">Order Items:</h4>
                                <ul className="list-disc list-inside space-y-1">
                                  {order.orderItems.map((item) => (
                                    <li key={item.id} className="text-sm text-gray-700">
                                      {item.productName} x {item.quantity} - $
                                      {item.subtotal.toFixed(2)}
                                    </li>
                                  ))}
                                </ul>
                                <p className="text-sm">
                                  <strong>Total Amount:</strong> ${order.totalAmount.toFixed(2)}
                                </p>
                                {order.deliveryNotes && (
                                  <p className="text-sm">
                                    <strong>Delivery Notes:</strong> {order.deliveryNotes}
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
                  Previous
                </button>
                <span className="text-gray-700">
                  Page {state.pagination.page + 1} of {state.pagination.totalPages}
                </span>
                <button
                  onClick={() => handlePageChange(state.pagination.page + 1)}
                  disabled={state.pagination.page >= state.pagination.totalPages - 1}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors"
                >
                  Next
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
