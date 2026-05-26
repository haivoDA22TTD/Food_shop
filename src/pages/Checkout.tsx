import { motion } from 'framer-motion'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import { useCartStore } from '../store/cartStore'
import { useAuthStore } from '../store/authStore'

export default function Checkout() {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const { items, totalAmount, clearCart, syncCartWithServer } = useCartStore()
  const [loading, setLoading] = useState(false)
  const [syncing, setSyncing] = useState(false)
  const [error, setError] = useState('')
  
  const [formData, setFormData] = useState({
    city: '',
    district: '',
    ward: '',
    street: '',
    phoneNumber: '',
    notes: '',
    paymentMethod: 'COD',
  })

  // Danh sách địa điểm (có thể mở rộng)
  const cities = [
    { value: 'hanoi', label: 'Hà Nội' },
    { value: 'hcm', label: 'TP. Hồ Chí Minh' },
    { value: 'danang', label: 'Đà Nẵng' },
    { value: 'haiphong', label: 'Hải Phòng' },
    { value: 'cantho', label: 'Cần Thơ' },
  ]

  const districts: Record<string, Array<{ value: string; label: string }>> = {
    hanoi: [
      { value: 'ba-dinh', label: 'Ba Đình' },
      { value: 'hoan-kiem', label: 'Hoàn Kiếm' },
      { value: 'dong-da', label: 'Đống Đa' },
      { value: 'hai-ba-trung', label: 'Hai Bà Trưng' },
      { value: 'cau-giay', label: 'Cầu Giấy' },
      { value: 'thanh-xuan', label: 'Thanh Xuân' },
      { value: 'long-bien', label: 'Long Biên' },
      { value: 'ha-dong', label: 'Hà Đông' },
    ],
    hcm: [
      { value: 'quan-1', label: 'Quận 1' },
      { value: 'quan-2', label: 'Quận 2' },
      { value: 'quan-3', label: 'Quận 3' },
      { value: 'quan-4', label: 'Quận 4' },
      { value: 'quan-5', label: 'Quận 5' },
      { value: 'quan-6', label: 'Quận 6' },
      { value: 'quan-7', label: 'Quận 7' },
      { value: 'quan-8', label: 'Quận 8' },
      { value: 'quan-9', label: 'Quận 9' },
      { value: 'quan-10', label: 'Quận 10' },
      { value: 'quan-11', label: 'Quận 11' },
      { value: 'quan-12', label: 'Quận 12' },
      { value: 'binh-thanh', label: 'Bình Thạnh' },
      { value: 'tan-binh', label: 'Tân Bình' },
      { value: 'phu-nhuan', label: 'Phú Nhuận' },
      { value: 'go-vap', label: 'Gò Vấp' },
      { value: 'thu-duc', label: 'Thủ Đức' },
    ],
    danang: [
      { value: 'hai-chau', label: 'Hải Châu' },
      { value: 'thanh-khe', label: 'Thanh Khê' },
      { value: 'son-tra', label: 'Sơn Trà' },
      { value: 'ngu-hanh-son', label: 'Ngũ Hành Sơn' },
      { value: 'lien-chieu', label: 'Liên Chiểu' },
      { value: 'cam-le', label: 'Cẩm Lệ' },
    ],
    haiphong: [
      { value: 'hong-bang', label: 'Hồng Bàng' },
      { value: 'ngo-quyen', label: 'Ngô Quyền' },
      { value: 'le-chan', label: 'Lê Chân' },
      { value: 'hai-an', label: 'Hải An' },
      { value: 'kien-an', label: 'Kiến An' },
    ],
    cantho: [
      { value: 'ninh-kieu', label: 'Ninh Kiều' },
      { value: 'binh-thuy', label: 'Bình Thủy' },
      { value: 'cai-rang', label: 'Cái Răng' },
      { value: 'o-mon', label: 'Ô Môn' },
    ],
  }

  const wards: Record<string, Array<{ value: string; label: string }>> = {
    'ba-dinh': [
      { value: 'dien-bien', label: 'Điện Biên' },
      { value: 'doi-can', label: 'Đội Cấn' },
      { value: 'lieu-giai', label: 'Liễu Giai' },
      { value: 'ngoc-ha', label: 'Ngọc Hà' },
      { value: 'kim-ma', label: 'Kim Mã' },
    ],
    'hoan-kiem': [
      { value: 'hang-bac', label: 'Hàng Bạc' },
      { value: 'hang-bo', label: 'Hàng Bồ' },
      { value: 'hang-dao', label: 'Hàng Đào' },
      { value: 'hang-gai', label: 'Hàng Gai' },
      { value: 'trang-tien', label: 'Tràng Tiền' },
    ],
    'quan-1': [
      { value: 'ben-nghe', label: 'Bến Nghé' },
      { value: 'ben-thanh', label: 'Bến Thành' },
      { value: 'nguyen-thai-binh', label: 'Nguyễn Thái Bình' },
      { value: 'pham-ngu-lao', label: 'Phạm Ngũ Lão' },
      { value: 'nguyen-cu-trinh', label: 'Nguyễn Cư Trinh' },
    ],
    // Thêm wards cho các quận/huyện khác nếu cần
  }
  
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

    setLoading(true)
    setError('')

    try {
      // Build full address
      const cityLabel = cities.find(c => c.value === formData.city)?.label || formData.city
      const districtLabel = districts[formData.city]?.find(d => d.value === formData.district)?.label || formData.district
      const wardLabel = wards[formData.district]?.find(w => w.value === formData.ward)?.label || formData.ward
      
      const fullAddress = `${formData.street}, ${wardLabel}, ${districtLabel}, ${cityLabel}`
      
      const response = await axios.post('/api/orders', {
        shippingAddress: fullAddress,
        phoneNumber: formData.phoneNumber,
        notes: formData.notes,
        paymentMethod: formData.paymentMethod,
      })
      clearCart()
      alert(`Đặt hàng thành công! Mã đơn hàng: ${response.data.orderNumber}`)
      navigate('/orders')
    } catch (err: any) {
      console.error('Order creation error:', err)
      const errorMessage = err?.response?.data?.error || err?.response?.data?.message || 'Không thể đặt hàng. Vui lòng thử lại.'
      setError(errorMessage)
      
      if (errorMessage.includes('cart') || errorMessage.includes('empty')) {
        setError('Giỏ hàng trống trên server. Vui lòng thêm sản phẩm vào giỏ hàng và thử lại.')
      }
    } finally {
      setLoading(false)
    }
  }

  if (items.length === 0) {
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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Checkout Form */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          className="lg:col-span-2"
        >
          <form onSubmit={handleSubmit} className="card p-6 space-y-6">
            {/* City Selection */}
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
                {cities.map(city => (
                  <option key={city.value} value={city.value}>{city.label}</option>
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
                disabled={!formData.city}
                required
              >
                <option value="">-- Chọn Quận/Huyện --</option>
                {formData.city && districts[formData.city]?.map(district => (
                  <option key={district.value} value={district.value}>{district.label}</option>
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
                disabled={!formData.district}
                required
              >
                <option value="">-- Chọn Phường/Xã --</option>
                {formData.district && wards[formData.district]?.map(ward => (
                  <option key={ward.value} value={ward.value}>{ward.label}</option>
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
              </div>
            </div>

            <button
              type="submit"
              disabled={loading || syncing}
              className="w-full btn-primary disabled:opacity-50"
            >
              {loading ? 'Đang xử lý...' : syncing ? 'Đang đồng bộ...' : 
               formData.paymentMethod === 'COD' ? 'Đặt hàng' : 'Thanh toán'}
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
              {items.map((item) => (
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
                  {totalAmount.toLocaleString('vi-VN')} đ
                </span>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
