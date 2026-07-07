import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import axios from '../api/axios';

export interface Shipper {
  id: number;
  name: string;
  phone: string;
  email?: string;
  address?: string;
  status: 'AVAILABLE' | 'BUSY' | 'OFFLINE' | 'ON_BREAK';
  vehicleType?: string;
  vehicleNumber?: string;
  totalDeliveries: number;
  successfulDeliveries: number;
  rating: number;
  totalRatings: number;
  isActive: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  successRate: number;
}

export interface ShipperStatistics {
  totalShippers: number;
  activeShippers: number;
  availableShippers: number;
  busyShippers: number;
  averageRating: number;
  totalDeliveries: number;
  successfulDeliveries: number;
  overallSuccessRate: number;
}

interface ShipperStore {
  shippers: Shipper[];
  currentShipper: Shipper | null;
  statistics: ShipperStatistics | null;
  loading: boolean;
  error: string | null;

  // Actions
  fetchShippers: (page?: number, size?: number, status?: string, search?: string) => Promise<any>;
  fetchShipperById: (id: number) => Promise<void>;
  fetchAvailableShippers: () => Promise<Shipper[]>;
  fetchTopRatedShippers: (limit?: number) => Promise<Shipper[]>;
  fetchShipperStatistics: () => Promise<void>;
  createShipper: (data: Partial<Shipper>) => Promise<Shipper>;
  updateShipper: (id: number, data: Partial<Shipper>) => Promise<Shipper>;
  updateShipperStatus: (id: number, status: string) => Promise<Shipper>;
  toggleShipperActive: (id: number) => Promise<Shipper>;
  deleteShipper: (id: number) => Promise<void>;
  clearError: () => void;
}

export const useShipperStore = create<ShipperStore>()(
  persist(
    (set, get) => ({
      shippers: [],
      currentShipper: null,
      statistics: null,
      loading: false,
      error: null,

      fetchShippers: async (page = 0, size = 20, status?: string, search?: string) => {
        set({ loading: true, error: null });
        try {
          const params: any = { page, size };
          if (status) params.status = status;
          if (search) params.search = search;

          const response = await axios.get('/api/admin/shippers', { params });
          set({ shippers: response.data.content, loading: false });
          return response.data;
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to fetch shippers';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      fetchShipperById: async (id: number) => {
        set({ loading: true, error: null });
        try {
          const response = await axios.get(`/api/admin/shippers/${id}`);
          set({ currentShipper: response.data, loading: false });
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to fetch shipper';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      fetchAvailableShippers: async () => {
        set({ loading: true, error: null });
        try {
          const response = await axios.get('/api/admin/shippers/available');
          set({ loading: false });
          return response.data;
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to fetch available shippers';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      fetchTopRatedShippers: async (limit = 10) => {
        set({ loading: true, error: null });
        try {
          const response = await axios.get('/api/admin/shippers/top-rated', {
            params: { limit }
          });
          set({ loading: false });
          return response.data;
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to fetch top rated shippers';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      fetchShipperStatistics: async () => {
        set({ loading: true, error: null });
        try {
          const response = await axios.get('/api/admin/shippers/statistics');
          set({ statistics: response.data, loading: false });
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to fetch statistics';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      createShipper: async (data: Partial<Shipper>) => {
        set({ loading: true, error: null });
        try {
          const response = await axios.post('/api/admin/shippers', data);
          set({ loading: false });
          // Refresh shippers list
          get().fetchShippers();
          return response.data;
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to create shipper';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      updateShipper: async (id: number, data: Partial<Shipper>) => {
        set({ loading: true, error: null });
        try {
          const response = await axios.put(`/api/admin/shippers/${id}`, data);
          set({ loading: false });
          // Refresh shippers list
          get().fetchShippers();
          return response.data;
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to update shipper';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      updateShipperStatus: async (id: number, status: string) => {
        set({ loading: true, error: null });
        try {
          const response = await axios.put(`/api/admin/shippers/${id}/status`, { status });
          set({ loading: false });
          // Refresh shippers list
          get().fetchShippers();
          return response.data;
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to update shipper status';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      toggleShipperActive: async (id: number) => {
        set({ loading: true, error: null });
        try {
          const response = await axios.put(`/api/admin/shippers/${id}/toggle-active`);
          set({ loading: false });
          // Refresh shippers list
          get().fetchShippers();
          return response.data;
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to toggle shipper status';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      deleteShipper: async (id: number) => {
        set({ loading: true, error: null });
        try {
          await axios.delete(`/api/admin/shippers/${id}`);
          set({ loading: false });
          // Refresh shippers list
          get().fetchShippers();
        } catch (error: any) {
          const errorMessage = error.response?.data?.error || 'Failed to delete shipper';
          set({ error: errorMessage, loading: false });
          throw error;
        }
      },

      clearError: () => set({ error: null }),
    }),
    {
      name: 'shipper-storage',
      partialize: (state) => ({
        // Only persist non-sensitive data
        statistics: state.statistics,
      }),
    }
  )
);
