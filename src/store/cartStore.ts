import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import axios from '../api/axios'

interface CartItem {
  id: number
  productId: number
  productName: string
  productPrice: number
  productImage?: string
  quantity: number
  subtotal: number
  availableStock: number
  inStock: boolean
}

interface CartState {
  items: CartItem[]
  totalItems: number
  totalAmount: number
  loading: boolean
  error: string | null
  
  // Actions
  fetchCart: () => Promise<void>
  addToCart: (productId: number, quantity: number) => Promise<void>
  updateQuantity: (productId: number, quantity: number) => Promise<void>
  removeFromCart: (productId: number) => Promise<void>
  clearCart: () => void
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      totalItems: 0,
      totalAmount: 0,
      loading: false,
      error: null,

      fetchCart: async () => {
        set({ loading: true, error: null })
        try {
          const response = await axios.get('/api/orders/cart')
          const cart = response.data
          set({
            items: cart.cartItems || [],
            totalItems: cart.totalItems || 0,
            totalAmount: cart.totalAmount || 0,
            loading: false,
          })
        } catch (error: any) {
          console.error('Failed to fetch cart:', error)
          set({ 
            error: error?.response?.data?.error || 'Không thể tải giỏ hàng',
            loading: false 
          })
        }
      },

      addToCart: async (productId: number, quantity: number) => {
        set({ loading: true, error: null })
        try {
          await axios.post('/api/orders/cart/items', { productId, quantity })
          await get().fetchCart()
        } catch (error: any) {
          set({ 
            error: error?.response?.data?.error || 'Không thể thêm vào giỏ hàng',
            loading: false 
          })
          throw error
        }
      },

      updateQuantity: async (productId: number, quantity: number) => {
        set({ loading: true, error: null })
        try {
          await axios.put(`/api/orders/cart/items/${productId}`, { quantity })
          await get().fetchCart()
        } catch (error: any) {
          set({ 
            error: error?.response?.data?.error || 'Không thể cập nhật số lượng',
            loading: false 
          })
          throw error
        }
      },

      removeFromCart: async (productId: number) => {
        set({ loading: true, error: null })
        try {
          await axios.delete(`/api/orders/cart/items/${productId}`)
          await get().fetchCart()
        } catch (error: any) {
          set({ 
            error: error?.response?.data?.error || 'Không thể xóa sản phẩm',
            loading: false 
          })
          throw error
        }
      },

      clearCart: () => {
        set({ items: [], totalItems: 0, totalAmount: 0 })
      },
    }),
    {
      name: 'cart-storage',
      partialize: (state) => ({ 
        totalItems: state.totalItems,
        totalAmount: state.totalAmount 
      }),
    }
  )
)
