import { motion } from 'framer-motion'

export default function Products() {
  const products = [
    { id: 1, name: 'Burger', price: 50000, image: '🍔' },
    { id: 2, name: 'Pizza', price: 80000, image: '🍕' },
    { id: 3, name: 'Sushi', price: 120000, image: '🍣' },
    { id: 4, name: 'Mì Ý', price: 70000, image: '🍝' },
    { id: 5, name: 'Salad', price: 40000, image: '🥗' },
    { id: 6, name: 'Bít tết', price: 150000, image: '🥩' },
  ]

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-4xl font-bold mb-8">Sản phẩm của chúng tôi</h1>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {products.map((product, index) => (
          <motion.div
            key={product.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
            className="card p-6 hover:scale-105 transition-transform cursor-pointer"
          >
            <div className="text-6xl text-center mb-4">{product.image}</div>
            <h3 className="text-xl font-bold mb-2">{product.name}</h3>
            <p className="text-gray-600 mb-4">{product.price.toLocaleString('vi-VN')} đ</p>
            <button className="w-full btn-primary">
              Thêm vào giỏ
            </button>
          </motion.div>
        ))}
      </div>
    </div>
  )
}
