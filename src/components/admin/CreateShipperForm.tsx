import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { useAuthStore } from '../../store/authStore';
import axios from '../../api/axios';

interface CreateShipperFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

interface FormState {
  username: string;
  password: string;
  email: string;
  name: string;
  phone: string;
  vehicleType: string;
  vehicleNumber: string;
  errors: Record<string, string>;
  isSubmitting: boolean;
}

/**
 * Form component for creating shipper accounts
 * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5, 8.6
 */
const CreateShipperForm: React.FC<CreateShipperFormProps> = ({ onSuccess, onCancel }) => {
  const token = useAuthStore((state) => state.token);
  const [formState, setFormState] = useState<FormState>({
    username: '',
    password: '',
    email: '',
    name: '',
    phone: '',
    vehicleType: '',
    vehicleNumber: '',
    errors: {},
    isSubmitting: false,
  });

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    // Username validation
    if (!formState.username || formState.username.trim().length < 3) {
      errors.username = 'Username must be at least 3 characters';
    }

    // Password validation
    if (!formState.password || formState.password.length < 8) {
      errors.password = 'Password must be at least 8 characters';
    }

    // Email validation (simple)
    if (!formState.email || !formState.email.includes('@')) {
      errors.email = 'Valid email is required';
    }

    // Name validation
    if (!formState.name || formState.name.trim().length === 0) {
      errors.name = 'Name is required';
    }

    // Phone validation (simple - just check if has digits)
    if (!formState.phone || formState.phone.replace(/\D/g, '').length < 10) {
      errors.phone = 'Phone must have at least 10 digits';
    }

    console.log('Validation errors:', errors);
    setFormState((prev) => ({ ...prev, errors }));
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    e.stopPropagation();

    // Force validation check
    const isValid = validateForm();
    
    console.log('=== FORM VALIDATION ===');
    console.log('Is Valid:', isValid);
    console.log('Form State:', formState);
    console.log('Errors:', formState.errors);
    
    if (!isValid) {
      console.log('VALIDATION FAILED - Showing toast...');
      toast.error('Please fill in all required fields correctly');
      return;
    }

    console.log('VALIDATION PASSED - Calling API...');
    console.log('Token from store:', token ? 'EXISTS' : 'NULL');
    setFormState((prev) => ({ ...prev, isSubmitting: true }));

    try {
      if (!token) {
        console.error('NO TOKEN - User not authenticated');
        toast.error('Authentication token not found. Please login again.');
        setFormState((prev) => ({ ...prev, isSubmitting: false }));
        return;
      }

      const requestBody = {
        username: formState.username,
        password: formState.password,
        email: formState.email,
        name: formState.name,
        phone: formState.phone,
        vehicleType: formState.vehicleType || null,
        vehicleNumber: formState.vehicleNumber || null,
      };
      
      console.log('Request body:', requestBody);
      console.log('Sending request to /api/auth/create-shipper...');

      // Dùng axios thay fetch để tự động gắn baseURL của API Gateway
      const { data } = await axios.post('/api/auth/create-shipper', requestBody, {
        headers: { Authorization: `Bearer ${token}` },
      });

      console.log('Success response:', data);
      toast.success('Tạo tài khoản Shipper thành công!');
      setFormState((prev) => ({ ...prev, isSubmitting: false }));
      onSuccess();
    } catch (error: any) {
      console.error('Create shipper error:', error);
      const errData = error?.response?.data;
      // Hiện cả error lẫn detail để dễ debug
      const msg = [errData?.error, errData?.detail, errData?.message]
        .filter(Boolean)
        .join(' | ') || 'Không thể tạo Shipper. Vui lòng thử lại.';
      console.error('Error detail:', errData);
      toast.error(msg, { duration: 8000 });
      setFormState((prev) => ({ ...prev, isSubmitting: false }));
    }
  };

  const handleInputChange = (field: keyof FormState, value: string) => {
    setFormState((prev) => ({
      ...prev,
      [field]: value,
      errors: { ...prev.errors, [field]: '' },
    }));
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-8 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <h2 className="text-2xl font-bold mb-6">Create Shipper Account</h2>

        <form onSubmit={handleSubmit}>
          {/* Username */}
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Username <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formState.username}
              onChange={(e) => handleInputChange('username', e.target.value)}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                formState.errors.username
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
              disabled={formState.isSubmitting}
            />
            {formState.errors.username && (
              <p className="text-red-500 text-xs mt-1">{formState.errors.username}</p>
            )}
          </div>

          {/* Password */}
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Password <span className="text-red-500">*</span>
            </label>
            <input
              type="password"
              value={formState.password}
              onChange={(e) => handleInputChange('password', e.target.value)}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                formState.errors.password
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
              disabled={formState.isSubmitting}
            />
            {formState.errors.password && (
              <p className="text-red-500 text-xs mt-1">{formState.errors.password}</p>
            )}
          </div>

          {/* Email */}
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Email <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              value={formState.email}
              onChange={(e) => handleInputChange('email', e.target.value)}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                formState.errors.email
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
              disabled={formState.isSubmitting}
            />
            {formState.errors.email && (
              <p className="text-red-500 text-xs mt-1">{formState.errors.email}</p>
            )}
          </div>

          {/* Name */}
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Full Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formState.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                formState.errors.name
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
              disabled={formState.isSubmitting}
            />
            {formState.errors.name && (
              <p className="text-red-500 text-xs mt-1">{formState.errors.name}</p>
            )}
          </div>

          {/* Phone */}
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Phone <span className="text-red-500">*</span>
            </label>
            <input
              type="tel"
              value={formState.phone}
              onChange={(e) => handleInputChange('phone', e.target.value)}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                formState.errors.phone
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
              placeholder="+84901234567"
              disabled={formState.isSubmitting}
            />
            {formState.errors.phone && (
              <p className="text-red-500 text-xs mt-1">{formState.errors.phone}</p>
            )}
          </div>

          {/* Vehicle Type */}
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">Vehicle Type</label>
            <input
              type="text"
              value={formState.vehicleType}
              onChange={(e) => handleInputChange('vehicleType', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Motorbike, Car, Bicycle"
              disabled={formState.isSubmitting}
            />
          </div>

          {/* Vehicle Number */}
          <div className="mb-6">
            <label className="block text-gray-700 text-sm font-bold mb-2">Vehicle Number</label>
            <input
              type="text"
              value={formState.vehicleNumber}
              onChange={(e) => handleInputChange('vehicleNumber', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="License plate number"
              disabled={formState.isSubmitting}
            />
          </div>

          {/* Buttons */}
          <div className="flex justify-end gap-4">
            <button
              type="button"
              onClick={onCancel}
              className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors disabled:opacity-50"
              disabled={formState.isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
              disabled={formState.isSubmitting}
            >
              {formState.isSubmitting ? 'Creating...' : 'Create Shipper'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateShipperForm;