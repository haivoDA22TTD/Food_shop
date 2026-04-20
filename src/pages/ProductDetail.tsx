import { motion } from 'framer-motion'
import { useParams } from 'react-router-dom'

export default function ProductDetail() {
  const { id } = useParams()

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="grid grid-cols-1 md:grid-cols-2 gap-8"
      >
        <div className="text-9xl text-center">🍔</div>
        <div>
          <h1 className="text-4xl font-bold mb-4">Product {id}</h1>
          <p className="text-2xl text-primary-600 mb-4">50,000 đ</p>
          <p className="text-gray-600 mb-6">
            Delicious burger with fresh ingredients
          </p>
          <button className="btn-primary">Add to Cart</button>
        </div>
      </motion.div>
    </div>
  )
}
