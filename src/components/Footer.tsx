export default function Footer() {
  return (
    <footer className="bg-gray-800 text-white mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 className="text-xl font-bold mb-4">Food Shop</h3>
            <p className="text-gray-400">
              Dịch vụ giao đồ ăn yêu thích của bạn
            </p>
          </div>
          <div>
            <h4 className="text-lg font-semibold mb-4">Liên kết nhanh</h4>
            <ul className="space-y-2 text-gray-400">
              <li><a href="/products" className="hover:text-white transition-colors">Sản phẩm</a></li>
              <li><a href="/about" className="hover:text-white transition-colors">Về chúng tôi</a></li>
              <li><a href="/contact" className="hover:text-white transition-colors">Liên hệ</a></li>
            </ul>
          </div>
          <div>
            <h4 className="text-lg font-semibold mb-4">Liên hệ</h4>
            <ul className="space-y-2 text-gray-400">
              <li>Email: info@foodshop.com</li>
              <li>Điện thoại: +84 123 456 789</li>
            </ul>
          </div>
        </div>
        <div className="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
          <p>&copy; 2026 Food Shop. Bảo lưu mọi quyền.</p>
        </div>
      </div>
    </footer>
  )
}
