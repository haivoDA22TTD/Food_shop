import { motion } from 'framer-motion'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import { useCartStore } from '../store/cartStore'
import { useAuthStore } from '../store/authStore'

interface SavedAddress {
  id: number
  fullAddress: string
  provinceCode: number | null
  districtCode: number | null
  wardCode: number | null
  street: string | null
  phoneNumber: string
  label: string
  isDefault: boolean
  createdAt: string
}

export default function Checkout() {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const { items, syncCartWithServer } = useCartStore()
  const [loading, setLoading] = useState(false)
  const [syncing, setSyncing] = useState(false)
  const [error, setError] = useState('')
  
  // Selected items for checkout
  const [checkoutItems, setCheckoutItems] = useState<typeof items>([])
  const [checkoutTotal, setCheckoutTotal] = useState(0)
  
  const [formData, setFormData] = useState({
    city: '',
    district: '',
    ward: '',
    street: '',
    phoneNumber: '',
    notes: '',
    paymentMethod: 'COD',
  })

  const [saveAddress, setSaveAddress] = useState(false)
  const [addressLabel, setAddressLabel] = useState('')
  const [savedAddresses, setSavedAddresses] = useState<SavedAddress[]>([])
  const [loadingAddresses, setLoadingAddresses] = useState(false)
  const [deletingAddressId, setDeletingAddressId] = useState<number | null>(null)

  // Location data from API
  const [provinces, setProvinces] = useState<Array<{ code: number; name: string }>>([])
  const [districts, setDistricts] = useState<Array<{ code: number; name: string }>>([])
  const [wards, setWards] = useState<Array<{ code: number; name: string }>>([])
  const [loadingLocations, setLoadingLocations] = useState(false)

  // Load provinces on mount
  useEffect(() => {
    const loadProvinces = async () => {
      try {
        const response = await fetch('https://provinces.open-api.vn/api/p/')
        const data = await response.json()
        setProvinces(data)
      } catch (error) {
        console.error('Failed to load provinces:', error)
      }
    }
    loadProvinces()
  }, [])
  
  // Load selected items from localStorage
  useEffect(() => {
    const selectedIds = JSON.parse(localStorage.getItem('selectedItems') || '[]')
    if (selectedIds.length === 0) {
      // If no selection, use all items (backward compatible)
      setCheckoutItems(items)
    } else {
      // Filter only selected items
      const selected = items.filter(item => selectedIds.includes(item.productId))
      setCheckoutItems(selected)
    }
    
    // Calculate total
    const total = checkoutItems.reduce((sum, item) => sum + item.subtotal, 0)
    setCheckoutTotal(total)
  }, [items])
  
  // Recalculate total when checkoutItems change
  useEffect(() => {
    const total = checkoutItems.reduce((sum, item) => sum + item.subtotal, 0)
    setCheckoutTotal(total)
  }, [checkoutItems])

  // Load districts when province changes
  useEffect(() => {
    if (formData.city) {
      const loadDistricts = async () => {
        setLoadingLocations(true)
        try {
          const response = await fetch(`https://provinces.open-api.vn/api/p/${formData.city}?depth=2`)
          const data = await response.json()
          setDistricts(data.districts || [])
          setWards([])
        } catch (error) {
          console.error('Failed to load districts:', error)
        } finally {
          setLoadingLocations(false)
        }
      }
      loadDistricts()
    } else {
      setDistricts([])
      setWards([])
    }
  }, [formData.city])

  // Load wards when district changes
  useEffect(() => {
    if (formData.district) {
      const loadWards = async () => {
        setLoadingLocations(true)
        try {
          const response = await fetch(`https://provinces.open-api.vn/api/d/${formData.district}?depth=2`)
          const data = await response.json()
          setWards(data.wards || [])
        } catch (error) {
          console.error('Failed to load wards:', error)
        } finally {
          setLoadingLocations(false)
        }
      }
      loadWards()
    } else {
      setWards([])
    }
  }, [formData.district])
  
  // Sync cart with server when component mounts
  useEffect(() => {
    const syncCart = async () => {
      if (user && items.length > 0) {
        setSyncing(true)
        try {
          await syncCartWithServer()
        } catch (error) {
          console.error('Failed to sync cart:', error)
        } finally {
          setSyncing(false)
        }
      }
    }
    syncCart()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]) // Only run when user changes, not syncCartWithServer

  // Fetch saved addresses
  useEffect(() => {
    const loadSavedAddresses = async () => {
      if (!user) return
      setLoadingAddresses(true)
      try {
        const response = await axios.get('/api/user/addresses')
        setSavedAddresses(response.data || [])
      } catch (err) {
        console.error('Failed to load saved addresses:', err)
      } finally {
        setLoadingAddresses(false)
      }
    }
    loadSavedAddresses()
  }, [user])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Validate required fields
    if (!formData.city || !formData.district || !formData.ward || !formData.street || !formData.phoneNumber) {
      setError('Vui lòng điền đầy đủ thông tin địa chỉ')
      return
    }
    
    // Validate street length
    if (formData.street.length < 5) {
      setError('Số nhà/Đường phải có ít nhất 5 ký tự')
      return
    }
    
    // Validate phone number format
    const phoneRegex = /^[0-9+\-\s()]{10,15}$/
    if (!phoneRegex.test(formData.phoneNumber)) {
      setError('Số điện thoại không hợp lệ (10-15 số)')
      return
    }
    
    // Validate checkout items
    if (checkoutItems.length === 0) {
      setError('Không có sản phẩm nào để thanh toán')
      return
    }

    setLoading(true)
    setError('')

    try {
      // Build full address from selected locations
      const provinceName = provinces.find(p => p.code === parseInt(formData.city))?.name || ''
      const districtName = districts.find(d => d.code === parseInt(formData.district))?.name || ''
      const wardName = wards.find(w => w.code === parseInt(formData.ward))?.name || ''
      
      const fullAddress = `${formData.street}, ${wardName}, ${districtName}, ${provinceName}`
      
      // Get selected product IDs
      const selectedProductIds = checkoutItems.map(item => item.productId)
      
      const response = await axios.post('/api/orders', {
        shippingAddress: fullAddress,
        phoneNumber: formData.phoneNumber,
        notes: formData.notes,
        paymentMethod: formData.paymentMethod,
        selectedProductIds: selectedProductIds,
        saveAddress: saveAddress,
        addressLabel: saveAddress ? addressLabel || undefined : undefined,
        provinceCode: saveAddress ? parseInt(formData.city) : undefined,
        districtCode: saveAddress ? parseInt(formData.district) : undefined,
        wardCode: saveAddress ? parseInt(formData.ward) : undefined,
        street: saveAddress ? formData.street : undefined,
      })
      
      // Clear selected items from localStorage
      localStorage.removeItem('selectedItems')
      
      const orderId = response.data.id
      
      // For online payment methods, create payment and redirect
      if (formData.paymentMethod === 'ZALOPAY' || formData.paymentMethod === 'BANK_TRANSFER') {
        try {
          const paymentResponse = await axios.post('/api/payments', {
            orderId: orderId,
            paymentMethod: formData.paymentMethod,
            returnUrl: `${window.location.origin}/payment/callback?orderId=${orderId}`,
          })
          
          if (paymentResponse.data.paymentUrl) {
            // Redirect to payment gateway immediately
            // Don't set loading to false - let the redirect happen
            window.location.href = paymentResponse.data.paymentUrl
            // Browser will redirect - no code below this should execute
          } else {
            // Payment URL not found - show error
            setError('Không thể tạo link thanh toán. Vui lòng thử lại.')
            setLoading(false)
          }
        } catch (paymentErr: any) {
          console.error('Payment creation error:', paymentErr)
          setError('Không thể tạo thanh toán. Vui lòng thử lại.')
          setLoading(false)
        }
        // Important: Always return here to prevent showing "success" message
        return
      }
      
      // Only show success for COD payments
      setLoading(false)
      alert(`Đặt hàng thành công! Mã đơn hàng: ${response.data.orderNumber}`)
      navigate('/orders')
    } catch (err: any) {
      console.error('Order creation error:', err)
      setLoading(false)
      const errorMessage = err?.response?.data?.error || err?.response?.data?.message || 'Không thể đặt hàng. Vui lòng thử lại.'
      setError(errorMessage)
      
      if (errorMessage.includes('cart') || errorMessage.includes('empty')) {
        setError('Giỏ hàng trống trên server. Vui lòng thêm sản phẩm vào giỏ hàng và thử lại.')
      }
    }
  }

  const handleDeleteAddress = async (e: React.MouseEvent, addressId: number) => {
    e.stopPropagation()
    if (!confirm('Xóa địa chỉ này?')) return
    setDeletingAddressId(addressId)
    try {
      await axios.delete(`/api/user/addresses/${addressId}`)
      setSavedAddresses(prev => prev.filter(a => a.id !== addressId))
    } catch (err: any) {
      alert('Không thể xóa địa chỉ')
    } finally {
      setDeletingAddressId(null)
    }
  }

  if (checkoutItems.length === 0 && !syncing) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4">Giỏ hàng trống</h2>
          <p className="text-gray-600 mb-6">Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán</p>
          <button onClick={() => navigate('/products')} className="btn-primary">
            Mua sắm ngay
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-4xl font-bold mb-8">Thanh toán</h1>
      
      {checkoutItems.length < items.length && (
        <div className="bg-blue-100 border border-blue-300 text-blue-700 px-4 py-3 rounded mb-6">
          <p className="font-medium">📦 Thanh toán một phần</p>
          <p className="text-sm">
            Bạn đang thanh toán {checkoutItems.length} trong {items.length} sản phẩm. 
            Các sản phẩm còn lại vẫn được giữ trong giỏ hàng.
          </p>
        </div>
      )}
      
      {syncing && (
        <div className="bg-blue-100 border border-blue-300 text-blue-700 px-4 py-3 rounded mb-6">
          Đang đồng bộ giỏ hàng với server...
        </div>
      )}

      {error && (
        <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded mb-6">
          {error}
        </div>
      )}

      {/* Saved Addresses */}
      {savedAddresses.length > 0 && (
        <div className="mb-8">
          <h2 className="text-lg font-semibold mb-1">Địa chỉ đã lưu</h2>
          <p className="text-sm text-gray-500 mb-3">Click vào địa chỉ để điền tự động vào form bên dưới</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {savedAddresses.map((addr) => (
              <div
                key={addr.id}
                onClick={() => {
                  const isSelected = formData.city === String(addr.provinceCode) && formData.street === (addr.street || '')
                  if (isSelected) {
                    setFormData(prev => ({ ...prev, city: '', district: '', ward: '', street: '', phoneNumber: '' }))
                    setAddressLabel('')
                  } else {
                    setFormData(prev => ({
                      ...prev,
                      city: addr.provinceCode ? String(addr.provinceCode) : prev.city,
                      district: addr.districtCode ? String(addr.districtCode) : prev.district,
                      ward: addr.wardCode ? String(addr.wardCode) : prev.ward,
                      street: addr.street || prev.street,
                      phoneNumber: addr.phoneNumber || prev.phoneNumber,
                    }))
                    setAddressLabel(addr.label || '')
                    setSaveAddress(false)
                  }
                }}
                className={`border rounded-lg p-4 cursor-pointer transition-all ${
                  formData.city === String(addr.provinceCode) && formData.street === (addr.street || '')
                    ? 'border-primary-500 bg-primary-50'
                    : 'border-gray-200 hover:border-blue-500 hover:bg-blue-50'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1 min-w-0">
                    {addr.label && (
                      <p className="text-sm font-semibold text-blue-700 mb-1">{addr.label}</p>
                    )}
                    <p className="text-sm text-gray-800 line-clamp-2">{addr.fullAddress}</p>
                    {addr.phoneNumber && (
                      <p className="text-sm text-gray-500 mt-1">{addr.phoneNumber}</p>
                    )}
                  </div>
                  <div className="flex flex-col items-center gap-1 ml-2">
                    <button
                      onClick={(e) => handleDeleteAddress(e, addr.id)}
                      disabled={deletingAddressId === addr.id}
                      className="px-2 py-0.5 text-red-500 hover:text-red-700 hover:bg-red-50 rounded text-xs disabled:opacity-50"
                      title="Xóa địa chỉ"
                    >
                      {deletingAddressId === addr.id ? '...' : '✕'}
                    </button>
                    {formData.city === String(addr.provinceCode) && formData.street === (addr.street || '') ? (
                      <button
                        onClick={(e) => {
                          e.stopPropagation()
                          setFormData(prev => ({ ...prev, city: '', district: '', ward: '', street: '', phoneNumber: '' }))
                          setAddressLabel('')
                        }}
                        className="px-2 py-0.5 text-xs rounded bg-primary-100 text-primary-700 hover:bg-primary-200"
                      >
                        Bỏ chọn
                      </button>
                    ) : (
                      <span className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded whitespace-nowrap">
                        Chọn
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Checkout Form */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          className="lg:col-span-2"
        >
          <form onSubmit={handleSubmit} className="card p-6 space-y-6">
            {/* Province Selection */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Tỉnh/Thành phố <span className="text-red-500">*</span>
              </label>
              <select
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value, district: '', ward: '' })}
                className="input-field"
                required
              >
                <option value="">-- Chọn Tỉnh/Thành phố --</option>
                {provinces.map(province => (
                  <option key={province.code} value={province.code}>{province.name}</option>
                ))}
              </select>
            </div>

            {/* District Selection */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Quận/Huyện <span className="text-red-500">*</span>
              </label>
              <select
                value={formData.district}
                onChange={(e) => setFormData({ ...formData, district: e.target.value, ward: '' })}
                className="input-field"
                disabled={!formData.city || loadingLocations}
                required
              >
                <option value="">
                  {loadingLocations ? 'Đang tải...' : '-- Chọn Quận/Huyện --'}
                </option>
                {districts.map(district => (
                  <option key={district.code} value={district.code}>{district.name}</option>
                ))}
              </select>
            </div>

            {/* Ward Selection */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Phường/Xã <span className="text-red-500">*</span>
              </label>
              <select
                value={formData.ward}
                onChange={(e) => setFormData({ ...formData, ward: e.target.value })}
                className="input-field"
                disabled={!formData.district || loadingLocations}
                required
              >
                <option value="">
                  {loadingLocations ? 'Đang tải...' : '-- Chọn Phường/Xã --'}
                </option>
                {wards.map(ward => (
                  <option key={ward.code} value={ward.code}>{ward.name}</option>
                ))}
              </select>
            </div>

            {/* Street Address */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Số nhà, Tên đường <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={formData.street}
                onChange={(e) => setFormData({ ...formData, street: e.target.value })}
                className="input-field"
                placeholder="Ví dụ: 123 Nguyễn Trãi"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">
                Số điện thoại <span className="text-red-500">*</span>
              </label>
              <input
                type="tel"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                className="input-field"
                placeholder="0123456789"
                required
              />
            </div>

            {/* Save Address */}
            <div className="border-t pt-4">
              <div className="flex items-start gap-3">
                <input
                  type="checkbox"
                  id="saveAddress"
                  checked={saveAddress}
                  onChange={(e) => setSaveAddress(e.target.checked)}
                  className="mt-1 h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
                <div className="flex-1">
                  <label htmlFor="saveAddress" className="text-sm font-medium text-gray-700 cursor-pointer">
                    Lưu địa chỉ này vào sổ địa chỉ
                  </label>
                  {saveAddress && (
                    <input
                      type="text"
                      value={addressLabel}
                      onChange={(e) => setAddressLabel(e.target.value)}
                      className="input-field mt-2"
                      placeholder="Ghi chú cho địa chỉ (VD: Nhà riêng, Văn phòng...)"
                    />
                  )}
                </div>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">
                Ghi chú (tùy chọn)
              </label>
              <textarea
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                rows={3}
                className="input-field"
                placeholder="Ghi chú cho đơn hàng..."
              />
            </div>

            {/* Payment Method Selection */}
            <div>
              <label className="block text-sm font-medium mb-3">
                Hình thức thanh toán <span className="text-red-500">*</span>
              </label>
              <div className="space-y-3">
                <div className="flex items-center">
                  <input
                    id="cod"
                    name="paymentMethod"
                    type="radio"
                    value="COD"
                    checked={formData.paymentMethod === 'COD'}
                    onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                  />
                  <label htmlFor="cod" className="ml-3 block text-sm font-medium text-gray-700">
                    💵 Thanh toán khi nhận hàng (COD)
                  </label>
                </div>
                
                <div className="flex items-center">
                  <input
                    id="bank_transfer"
                    name="paymentMethod"
                    type="radio"
                    value="BANK_TRANSFER"
                    checked={formData.paymentMethod === 'BANK_TRANSFER'}
                    onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                  />
                  <label htmlFor="bank_transfer" className="ml-3 block text-sm font-medium text-gray-700">
                    🏧 Chuyển khoản ngân hàng (QR Code VCB)
                  </label>
                </div>
                
                <div className="flex items-center">
                  <input
                    id="zalopay"
                    name="paymentMethod"
                    type="radio"
                    value="ZALOPAY"
                    checked={formData.paymentMethod === 'ZALOPAY'}
                    onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                  />
                  <label htmlFor="zalopay" className="ml-3 block text-sm font-medium text-gray-700">
                    💜 ZaloPay (Thẻ ATM/Visa/Master/JCB)
                  </label>
                </div>
              </div>
              
              {/* Payment Method Description */}
              <div className="mt-3 p-3 bg-gray-50 rounded-lg text-sm text-gray-600">
                {formData.paymentMethod === 'COD' && (
                  <div>
                    <p className="font-medium text-green-700 mb-1">💡 Thanh toán khi nhận hàng</p>
                    <p>• Thanh toán bằng tiền mặt khi shipper giao hàng</p>
                    <p>• Phí ship sẽ được tính thêm</p>
                    <p>• Không cần thẻ ngân hàng</p>
                  </div>
                )}
                {formData.paymentMethod === 'BANK_TRANSFER' && (
                  <div>
                    <p className="font-medium text-blue-700 mb-1">💡 Chuyển khoản QR Code</p>
                    <p>• Quét mã QR VietComBank để chuyển khoản</p>
                    <p>• Chỉ cần tài khoản ngân hàng nội địa</p>
                    <p>• Đơn hàng được xử lý sau khi nhận tiền (1-5 phút)</p>
                    <p>• Không cần thẻ Visa/MasterCard</p>
                  </div>
                )}
                {formData.paymentMethod === 'ZALOPAY' && (
                  <div>
                    <p className="font-medium text-purple-700 mb-1">💡 Thanh toán qua ZaloPay</p>
                    <p>• Hỗ trợ thẻ ATM nội địa, Visa, MasterCard, JCB</p>
                    <p>• Hỗ trợ ví ZaloPay và-app ZaloPay</p>
                    <p>• Bảo mật SSL, xác thực OTP</p>
                    <p>• Đơn hàng được xử lý ngay lập tức</p>
                  </div>
                )}
              </div>
            </div>

            <button
              type="submit"
              disabled={loading || syncing}
              className="w-full btn-primary disabled:opacity-50"
            >
              {loading ? 'Đang xử lý...' : syncing ? 'Đang đồng bộ...' : 
               formData.paymentMethod === 'COD' ? 'Đặt hàng' : 
               formData.paymentMethod === 'ZALOPAY' ? 'Thanh toán ZaloPay' : 'Thanh toán'}
            </button>
          </form>
        </motion.div>

        {/* Order Summary */}
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          className="lg:col-span-1"
        >
          <div className="card p-6 sticky top-4">
            <h2 className="text-xl font-bold mb-4">Đơn hàng</h2>
            <div className="space-y-3 mb-4">
              {checkoutItems.map((item) => (
                <div key={item.productId} className="flex justify-between text-sm">
                  <span className="text-gray-700">
                    {item.productName} x {item.quantity}
                  </span>
                  <span className="font-semibold">
                    {item.subtotal.toLocaleString('vi-VN')} đ
                  </span>
                </div>
              ))}
            </div>
            <div className="border-t pt-4">
              <div className="flex justify-between text-lg font-bold">
                <span>Tổng cộng:</span>
                <span className="text-primary-600">
                  {checkoutTotal.toLocaleString('vi-VN')} đ
                </span>
              </div>
              <p className="text-xs text-gray-500 mt-2">
                {checkoutItems.length} sản phẩm
              </p>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
