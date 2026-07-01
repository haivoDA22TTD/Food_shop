import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import Layout from './components/Layout'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import Products from './pages/Products'
import ProductDetail from './pages/ProductDetail'
import Cart from './pages/Cart'
import Checkout from './pages/Checkout'
import Orders from './pages/Orders'
import Profile from './pages/Profile'
import OAuth2Redirect from './pages/OAuth2Redirect'
import PaymentCallback from './pages/PaymentCallback'
import AdminProducts from './pages/AdminProducts'
import AdminOrders from './pages/AdminOrders'
import AdminShippers from './pages/AdminShippers'
import AdminDashboard from './pages/AdminDashboard'
import ShipperLogin from './pages/ShipperLogin'
import ShipperDashboard from './pages/ShipperDashboard'
import Chatbot from './components/Chatbot'
import ProtectedRoute from './components/auth/ProtectedRoute'

function App() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
            <Route path="oauth2/redirect" element={<OAuth2Redirect />} />
            <Route path="products" element={<Products />} />
            <Route path="products/:id" element={<ProductDetail />} />
            <Route path="cart" element={<Cart />} />
            <Route path="checkout" element={<Checkout />} />
            <Route path="orders" element={<Orders />} />
            <Route path="payment/callback" element={<PaymentCallback />} />
            <Route path="profile" element={<Profile />} />
            <Route path="admin/dashboard" element={<AdminDashboard />} />
            <Route path="admin/products" element={<AdminProducts />} />
            <Route path="admin/orders" element={<AdminOrders />} />
            <Route path="admin/shippers" element={<AdminShippers />} />
          </Route>
          {/* Shipper Routes (No Layout) */}
          <Route path="shipper/login" element={<ShipperLogin />} />
          <Route 
            path="shipper/dashboard" 
            element={
              <ProtectedRoute requiredRole="SHIPPER">
                <ShipperDashboard />
              </ProtectedRoute>
            } 
          />
        </Routes>
        <Chatbot />
      </BrowserRouter>
      <ToastContainer 
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
      />
    </>
  )
}

export default App
