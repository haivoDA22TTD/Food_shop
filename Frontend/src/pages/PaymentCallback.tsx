import { motion } from 'framer-motion'
import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import axios from '../api/axios'

export default function PaymentCallback() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [loading, setLoading] = useState(true)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')
  const [orderNumber, setOrderNumber] = useState('')

  useEffect(() => {
    const checkPaymentStatus = async () => {
      const orderId = searchParams.get('orderId')

      // ZaloPay trả về status=1 (thành công) hoặc status=0 (thất bại)
      // Một số gateway trả về status=success/failed
      const statusParam = searchParams.get('status')
      const appTransId = searchParams.get('apptransid') || searchParams.get('app_trans_id')

      console.log('Payment callback params:', { orderId, statusParam, appTransId })

      if (!orderId) {
        setError('Không tìm thấy thông tin đơn hàng')
        setLoading(false)
        return
      }

      // Xác định kết quả từ params ZaloPay
      // ZaloPay: status=1 thành công, status=0 thất bại
      const isSuccessFromParam =
        statusParam === '1' ||
        statusParam === 'success' ||
        statusParam === 'SUCCESS'

      const isFailedFromParam =
        statusParam === '0' ||
        statusParam === '-49' ||
        statusParam === 'failed' ||
        statusParam === 'cancelled' ||
        statusParam === 'FAILED'

      try {
        // Luôn check order status từ server để đảm bảo chính xác
        const response = await axios.get(`/api/orders/${orderId}`)
        const order = response.data
        setOrderNumber(order.orderNumber || '')

        if (order.status === 'CONFIRMED' || order.status === 'PREPARING' ||
            order.status === 'READY_FOR_PICKUP' || order.status === 'DELIVERED') {
          setSuccess(true)
        } else if (isSuccessFromParam) {
          // ZaloPay nói thành công nhưng order chưa update → đợi callback IPN
          // Vẫn hiện thành công cho user
          setSuccess(true)
        } else if (isFailedFromParam) {
          setError('Thanh toán không thành công hoặc đã bị hủy')
        } else if (order.status === 'PENDING' || order.status === 'CONFIRMED') {
          // Đang xử lý
          setSuccess(true)
        } else if (order.status === 'CANCELLED') {
          setError('Đơn hàng đã bị hủy do thanh toán không thành công')
        } else {
          setError('Thanh toán đang được xử lý. Vui lòng kiểm tra đơn hàng sau.')
        }
      } catch (err: any) {
        console.error('Error checking payment status:', err)
        // Nếu không check được server, dùng param từ ZaloPay
        if (isSuccessFromParam) {
          setSuccess(true)
        } else {
          setError('Không thể kiểm tra trạng thái thanh toán. Vui lòng kiểm tra đơn hàng.')
        }
      } finally {
        setLoading(false)
      }
    }

    checkPaymentStatus()
  }, [searchParams])

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-primary-600 mx-auto mb-4" />
          <p className="text-lg text-gray-700">Đang kiểm tra trạng thái thanh toán...</p>
          <p className="text-sm text-gray-500 mt-2">Vui lòng đợi trong giây lát</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        className="max-w-md w-full bg-white rounded-xl shadow-lg p-8 text-center"
      >
        {success ? (
          <>
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-12 h-12 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-gray-800 mb-3">Thanh toán thành công!</h1>
            {orderNumber && (
              <p className="text-gray-600 mb-2">
                Mã đơn hàng: <span className="font-mono font-semibold">{orderNumber}</span>
              </p>
            )}
            <p className="text-gray-600 mb-8">
              Đơn hàng của bạn đã được xác nhận và đang được xử lý.
            </p>
            <button onClick={() => navigate('/orders')} className="btn-primary w-full mb-3">
              Xem đơn hàng
            </button>
            <button
              onClick={() => navigate('/products')}
              className="w-full px-6 py-3 border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
            >
              Tiếp tục mua sắm
            </button>
          </>
        ) : (
          <>
            <div className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-12 h-12 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-gray-800 mb-3">Thanh toán không thành công</h1>
            <p className="text-gray-600 mb-8">
              {error || 'Đã có lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại.'}
            </p>
            <button onClick={() => navigate('/orders')} className="btn-primary w-full mb-3">
              Xem đơn hàng
            </button>
            <button
              onClick={() => navigate('/cart')}
              className="w-full px-6 py-3 border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
            >
              Quay lại giỏ hàng
            </button>
          </>
        )}
      </motion.div>
    </div>
  )
}