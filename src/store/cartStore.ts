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
  addToCart: (productId: number, quantity: number, productData?: any) => Promise<void>
  updateQuantity: (productId: number, quantity: number) => Promise<void>
  removeFromCart: (productId: number) => Promise<void>
  clearCart: () => void
  syncCartWithServer: () => Promise<void>
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
          // If not authenticated, use local cart
          console.log('Using local cart')
          set({ loading: false })
        }
      },

      addToCart: async (productId: number, quantity: number, productData?: any) => {
        set({ loading: true, error: null })
        
        // Try to add to server cart first
        try {
          await axios.post('/api/orders/cart/items', { productId, quantity })
          await get().fetchCart()
          set({ loading: false })
          return
        } catch (error: any) {
          console.log('Server cart failed, using local cart:', error?.response?.status)
          
          // Always use local cart if server fails (for any reason)
          const currentItems = get().items
          const existingItem = currentItems.find(item => item.productId === productId)
          
          let newItems: CartItem[]
          if (existingItem) {
            // Update quantity
            newItems = currentItems.map(item =>
              item.productId === productId
                ? { ...item, quantity: item.quantity + quantity, subtotal: item.productPrice * (item.quantity + quantity) }
                : item
            )
          } else {
            // Add new item
            const newItem: CartItem = {
              id: Date.now(),
              productId,
              productName: productData?.name || 'Sản phẩm',
              productPrice: productData?.price || 0,
              productImage: productData?.image,
              quantity,
              subtotal: (productData?.price || 0) * quantity,
              availableStock: productData?.stock || 999,
              inStock: true,
            }
            newItems = [...currentItems, newItem]
          }
          
          const totalItems = newItems.reduce((sum, item) => sum + item.quantity, 0)
          const totalAmount = newItems.reduce((sum, item) => sum + item.subtotal, 0)
          
          set({
            items: newItems,
            totalItems,
            totalAmount,
            loading: false,
          })
          return
        }
      },

      updateQuantity: async (productId: number, quantity: number) => {
        set({ loading: true, error: null })
        
        try {
          await axios.put(`/api/orders/cart/items/${productId}`, { quantity })
          await get().fetchCart()
          set({ loading: false })
        } catch (error: any) {
          console.log('Server update failed, using local cart')
          
          // Always use local cart if server fails
          const currentItems = get().items
          const newItems = currentItems.map(item =>
            item.productId === productId
              ? { ...item, quantity, subtotal: item.productPrice * quantity }
              : item
          )
          
          const totalItems = newItems.reduce((sum, item) => sum + item.quantity, 0)
          const totalAmount = newItems.reduce((sum, item) => sum + item.subtotal, 0)
          
          set({
            items: newItems,
            totalItems,
            totalAmount,
            loading: false,
          })
        }
      },

      removeFromCart: async (productId: number) => {
        set({ loading: true, error: null })
        
        try {
          await axios.delete(`/api/orders/cart/items/${productId}`)
          await get().fetchCart()
          set({ loading: false })
        } catch (error: any) {
          console.log('Server remove failed, using local cart')
          
          // Always use local cart if server fails
          const currentItems = get().items
          const newItems = currentItems.filter(item => item.productId !== productId)
          
          const totalItems = newItems.reduce((sum, item) => sum + item.quantity, 0)
          const totalAmount = newItems.reduce((sum, item) => sum + item.subtotal, 0)
          
          set({
            items: newItems,
            totalItems,
            totalAmount,
            loading: false,
          })
        }
      },

      clearCart: () => {
        set({ items: [], totalItems: 0, totalAmount: 0 })
      },

      syncCartWithServer: async () => {
        const localItems = get().items
        if (localItems.length === 0) return
        
        try {
          // Sync each item to server
          for (const item of localItems) {
            await axios.post('/api/orders/cart/items', {
              productId: item.productId,
              quantity: item.quantity,
            })
          }
          
          // Clear local cart and fetch from server
          await get().fetchCart()
        } catch (error) {
          console.error('Failed to sync cart:', error)
        }
      },
    }),
    {
      name: 'cart-storage',
    }
  )
)
