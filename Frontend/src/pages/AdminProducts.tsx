import { FormEvent, useEffect, useMemo, useState } from 'react'
import { Navigate } from 'react-router-dom'
import axios from '../api/axios'
import { useAuthStore } from '../store/authStore'

interface Product {
  id: number
  name: string
  description?: string
  price: number
  image?: string
  stock: number
  category?: string
}

const EMPTY_FORM = {
  name: '',
  description: '',
  price: '',
  stock: '',
  category: '',
}

export default function AdminProducts() {
  const user = useAuthStore((state) => state.user)
  const isAdmin = useMemo(() => user?.role?.toUpperCase() === 'ADMIN', [user])

  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadProducts = async () => {
    setLoading(true)
    try {
      const response = await axios.get('/api/products')
      setProducts(Array.isArray(response.data) ? response.data : [])
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Khong the tai danh sach san pham.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadProducts()
  }, [])

  const resetForm = () => {
    setEditingId(null)
    setImageFile(null)
    setForm(EMPTY_FORM)
  }

  const startEdit = (product: Product) => {
    setEditingId(product.id)
    setImageFile(null)
    setForm({
      name: product.name || '',
      description: product.description || '',
      price: String(product.price ?? ''),
      stock: String(product.stock ?? ''),
      category: product.category || '',
    })
    setError('')
    setMessage('')
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setMessage('')

    if (!form.name || !form.price || !form.stock) {
      setError('Vui long nhap day du ten, gia va ton kho.')
      return
    }

    setSubmitting(true)
    try {
      const payload = new FormData()
      payload.append('name', form.name)
      payload.append('description', form.description)
      payload.append('price', form.price)
      payload.append('stock', form.stock)
      payload.append('category', form.category)
      if (imageFile) {
        payload.append('imageFile', imageFile)
      }

      if (editingId) {
        await axios.put(`/api/admin/products/${editingId}`, payload, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
        setMessage('Cap nhat san pham thanh cong.')
      } else {
        await axios.post('/api/admin/products', payload, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
        setMessage('Tao san pham moi thanh cong.')
      }

      resetForm()
      await loadProducts()
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Khong the luu san pham.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (productId: number) => {
    setError('')
    setMessage('')
    setDeletingId(productId)
    try {
      await axios.delete(`/api/admin/products/${productId}`)
      setMessage('Da xoa san pham.')
      if (editingId === productId) {
        resetForm()
      }
      await loadProducts()
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Khong the xoa san pham.')
    } finally {
      setDeletingId(null)
    }
  }

  if (!user) {
    return <Navigate to="/login" />
  }

  if (!isAdmin) {
    return <Navigate to="/" />
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 space-y-6">
      <h1 className="text-4xl font-bold">Quan ly san pham</h1>

      {error && (
        <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-2 rounded">
          {error}
        </div>
      )}

      {message && (
        <div className="bg-green-100 border border-green-300 text-green-700 px-4 py-2 rounded">
          {message}
        </div>
      )}

      <div className="card p-6">
        <h2 className="text-2xl font-bold mb-4">{editingId ? 'Cap nhat san pham' : 'Them san pham moi'}</h2>
        <form onSubmit={handleSubmit} className="space-y-3">
          <input
            className="input-field"
            placeholder="Ten san pham"
            value={form.name}
            onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
          />
          <textarea
            className="input-field"
            placeholder="Mo ta"
            rows={4}
            value={form.description}
            onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
          />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <input
              type="number"
              className="input-field"
              placeholder="Gia"
              min={0}
              value={form.price}
              onChange={(event) => setForm((prev) => ({ ...prev, price: event.target.value }))}
            />
            <input
              type="number"
              className="input-field"
              placeholder="Ton kho"
              min={0}
              value={form.stock}
              onChange={(event) => setForm((prev) => ({ ...prev, stock: event.target.value }))}
            />
            <input
              className="input-field"
              placeholder="Danh muc"
              value={form.category}
              onChange={(event) => setForm((prev) => ({ ...prev, category: event.target.value }))}
            />
          </div>
          <input
            type="file"
            accept="image/*"
            className="input-field"
            onChange={(event) => setImageFile(event.target.files?.[0] || null)}
          />
          <div className="flex gap-2">
            <button type="submit" disabled={submitting} className="btn-primary disabled:opacity-50">
              {submitting ? 'Dang luu...' : editingId ? 'Cap nhat' : 'Tao moi'}
            </button>
            {editingId && (
              <button type="button" className="btn-secondary" onClick={resetForm}>
                Huy sua
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="card p-6">
        <h2 className="text-2xl font-bold mb-4">Danh sach san pham</h2>
        {loading ? (
          <p className="text-gray-600">Dang tai san pham...</p>
        ) : products.length === 0 ? (
          <p className="text-gray-600">Chua co san pham nao.</p>
        ) : (
          <div className="space-y-3">
            {products.map((product) => (
              <div key={product.id} className="border rounded-lg p-3 flex items-center gap-3 justify-between">
                <div className="flex items-center gap-3">
                  <img
                    src={product.image || 'https://via.placeholder.com/64x64?text=No+Image'}
                    alt={product.name}
                    className="w-16 h-16 object-cover rounded"
                  />
                  <div>
                    <p className="font-semibold">{product.name}</p>
                    <p className="text-sm text-gray-600">
                      {product.price?.toLocaleString('vi-VN')} đ - Ton kho: {product.stock}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <button type="button" className="btn-secondary" onClick={() => startEdit(product)}>
                    Sua
                  </button>
                  <button
                    type="button"
                    className="text-red-600 hover:text-red-700"
                    disabled={deletingId === product.id}
                    onClick={() => void handleDelete(product.id)}
                  >
                    {deletingId === product.id ? 'Dang xoa...' : 'Xoa'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
