import { motion } from 'framer-motion'

export default function Cart() {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-4xl font-bold mb-8">Giỏ hàng</h1>
      
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="text-center py-12"
      >
        <div className="text-6xl mb-4">🛒</div>
        <p className="text-xl text-gray-600">Giỏ hàng trống</p>
      </motion.div>
    </div>
  )
}
