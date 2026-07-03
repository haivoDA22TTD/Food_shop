import { useState, useEffect, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import axios from '../api/axios'

interface ZaloPayModalProps {
  isOpen: boolean
  onClose: () => void
  orderId: number
  paymentUrl: string
  onSuccess: () => void
}

export default function ZaloPayModal({ isOpen, onClose, orderId, paymentUrl, onSuccess }: ZaloPayModalProps) {
  const [status, setStatus] = useState<string>('PENDING')
  const [pollCount, setPollCount] = useState(0)

  const pollStatus = useCallback(async () => {
    try {
      const res = await axios.get(`/api/payments/order/${orderId}`)
      const paymentStatus = res.data.paymentStatus
      setStatus(paymentStatus)

      if (paymentStatus === 'PAID' || paymentStatus === 'COMPLETED') {
        setTimeout(() => onSuccess(), 1500)
        return
      }
      if (paymentStatus === 'CANCELLED' || paymentStatus === 'FAILED') {
        return
      }
    } catch (err) {
      console.error('Poll payment status error:', err)
    }
  }, [orderId, onSuccess])

  useEffect(() => {
    if (!isOpen) return
    setStatus('PENDING')
    setPollCount(0)

    const interval = setInterval(() => {
      setPollCount(c => c + 1)
      pollStatus()
    }, 5000)

    pollStatus()

    return () => clearInterval(interval)
  }, [isOpen, pollStatus])

  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(paymentUrl)}`
  const isSuccess = status === 'PAID' || status === 'COMPLETED'
  const isError = status === 'CANCELLED' || status === 'FAILED'

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4"
          onClick={(e) => { if (e.target === e.currentTarget && !isSuccess) onClose() }}
        >
          <motion.div
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.9, opacity: 0 }}
            className="bg-white rounded-2xl p-8 max-w-md w-full text-center shadow-2xl"
          >
            {isSuccess ? (
              <>
                <div className="text-6xl mb-4">✅</div>
                <h3 className="text-2xl font-bold text-green-600 mb-2">Thanh toán thành công!</h3>
                <p className="text-gray-500">Đang chuyển đến trang đơn hàng...</p>
                <div className="mt-6 w-12 h-12 border-4 border-green-200 border-t-green-600 rounded-full animate-spin mx-auto" />
              </>
            ) : isError ? (
              <>
                <div className="text-6xl mb-4">❌</div>
                <h3 className="text-2xl font-bold text-red-600 mb-2">Thanh toán thất bại</h3>
                <p className="text-gray-500 mb-6">Giao dịch đã bị hủy hoặc thất bại. Vui lòng thử lại.</p>
                <button onClick={onClose} className="btn-primary px-6 py-2 rounded-lg">Đóng</button>
              </>
            ) : (
              <>
                <div className="text-5xl mb-4">💜</div>
                <h3 className="text-xl font-bold mb-2">Quét QR bằng ZaloPay</h3>
                <p className="text-sm text-gray-500 mb-4">Mở ZaloPay trên điện thoại và quét mã QR bên dưới</p>

                <div className="bg-white rounded-xl p-4 inline-block shadow-md mb-4">
                  <img
                    src={qrUrl}
                    alt="ZaloPay QR"
                    className="w-[250px] h-[250px] mx-auto"
                  />
                </div>

                <div className="text-sm text-gray-400 mb-4">
                  <p>Mã đơn hàng: <strong>#{orderId}</strong></p>
                  <p className="flex items-center justify-center gap-2 mt-1">
                    <span className={`w-2 h-2 rounded-full ${status === 'PENDING' ? 'bg-yellow-400 animate-pulse' : 'bg-green-500'}`} />
                    {status === 'PENDING' ? 'Đang chờ thanh toán...' : `Đã quét: ${pollCount}`}
                  </p>
                </div>

                <div className="flex gap-3 justify-center">
                  <button onClick={onClose} className="px-4 py-2 border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50">
                    Hủy
                  </button>
                </div>
              </>
            )}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
