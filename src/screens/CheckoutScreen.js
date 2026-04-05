import React, { useState, useContext, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Alert,
  ActivityIndicator
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { LinearGradient } from 'expo-linear-gradient';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import api from '../config/api';

export default function CheckoutScreen({ navigation }) {
  const { cart, getCartTotal, clearCart } = useContext(CartContext);
  const { user } = useContext(AuthContext);
  
  // Check if user is logged in
  useEffect(() => {
    if (!user) {
      Alert.alert(
        '🔒 Yêu cầu đăng nhập',
        'Vui lòng đăng nhập để thanh toán',
        [
          {
            text: 'Đăng nhập',
            onPress: () => navigation.replace('Login')
          },
          {
            text: 'Hủy',
            onPress: () => navigation.goBack(),
            style: 'cancel'
          }
        ]
      );
    }
  }, [user]);

  const [address, setAddress] = useState('');
  const [phone, setPhone] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [voucherCode, setVoucherCode] = useState('');
  const [appliedVoucher, setAppliedVoucher] = useState(null);
  const [loading, setLoading] = useState(false);
  const [validatingVoucher, setValidatingVoucher] = useState(false);

  // If not logged in, show nothing (will redirect)
  if (!user) {
    return (
      <View style={styles.container}>
        <StatusBar style="light" />
        <LinearGradient
          colors={['#0ea5e9', '#0284c7']}
          style={styles.header}
        >
          <Text style={styles.headerTitle}>Thanh toán</Text>
        </LinearGradient>
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
          <ActivityIndicator size="large" color="#0ea5e9" />
          <Text style={{ marginTop: 16, color: '#64748b' }}>Đang kiểm tra...</Text>
        </View>
      </View>
    );
  }

  const subtotal = getCartTotal();
  const shipping = 30000;
  const discount = appliedVoucher ? appliedVoucher.discountAmount : 0;
  const total = subtotal + shipping - discount;

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN').format(price) + 'đ';
  };

  const handleValidateVoucher = async () => {
    if (!voucherCode.trim()) {
      Alert.alert('Thông báo', 'Vui lòng nhập mã giảm giá');
      return;
    }

    setValidatingVoucher(true);
    try {
      const response = await api.get('/api/vouchers/validate', {
        params: {
          code: voucherCode.trim(),
          orderTotal: subtotal
        }
      });

      if (response.data.valid) {
        setAppliedVoucher(response.data);
        Alert.alert('✅ Thành công', `Đã áp dụng mã giảm giá ${formatPrice(response.data.discountAmount)}`);
      } else {
        Alert.alert('❌ Lỗi', response.data.message || 'Mã giảm giá không hợp lệ');
      }
    } catch (error) {
      console.error('Validate voucher error:', error);
      Alert.alert('❌ Lỗi', error.response?.data?.message || 'Không thể xác thực mã giảm giá');
    } finally {
      setValidatingVoucher(false);
    }
  };

  const handlePlaceOrder = async () => {
    if (!address.trim()) {
      Alert.alert('Thông báo', 'Vui lòng nhập địa chỉ giao hàng');
      return;
    }

    if (!phone.trim()) {
      Alert.alert('Thông báo', 'Vui lòng nhập số điện thoại');
      return;
    }

    setLoading(true);
    try {
      const orderData = {
        items: cart.map(item => ({
          id: item.id,  // Backend expects "id" not "productId"
          quantity: item.quantity
        })),
        shippingAddress: address,
        phoneNumber: phone,
        paymentMethod: paymentMethod,
        voucherCode: appliedVoucher ? appliedVoucher.code : null
      };

      console.log('Sending order data:', orderData);
      const response = await api.post('/api/orders/create', orderData);
      
      clearCart();
      Alert.alert(
        '✅ Đặt hàng thành công!',
        `Mã đơn hàng: #${response.data.id}`,
        [
          { 
            text: 'Xem đơn hàng', 
            onPress: () => {
              navigation.reset({
                index: 0,
                routes: [{ name: 'Main', params: { screen: 'Orders' } }],
              });
            }
          },
          { 
            text: 'Về trang chủ', 
            onPress: () => {
              navigation.reset({
                index: 0,
                routes: [{ name: 'Main', params: { screen: 'Home' } }],
              });
            }
          }
        ]
      );
    } catch (error) {
      console.error('Place order error:', error);
      console.error('Error response:', error.response?.data);
      const errorMessage = error.response?.data?.error || error.response?.data?.message || error.response?.data || 'Không thể đặt hàng';
      Alert.alert('❌ Lỗi đặt hàng', typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage));
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <StatusBar style="light" />
      
      {/* Header */}
      <LinearGradient
        colors={['#0ea5e9', '#0284c7']}
        style={styles.header}
      >
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>← Quay lại</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Thanh toán</Text>
        <View style={styles.backButton} />
      </LinearGradient>

      <ScrollView style={styles.content}>
        {/* Shipping Info */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>📍 Thông tin giao hàng</Text>
          <TextInput
            style={styles.input}
            placeholder="Địa chỉ giao hàng"
            value={address}
            onChangeText={setAddress}
            multiline
          />
          <TextInput
            style={styles.input}
            placeholder="Số điện thoại"
            value={phone}
            onChangeText={setPhone}
            keyboardType="phone-pad"
          />
        </View>

        {/* Payment Method */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>💳 Phương thức thanh toán</Text>
          <TouchableOpacity
            style={[styles.paymentOption, paymentMethod === 'COD' && styles.paymentOptionActive]}
            onPress={() => setPaymentMethod('COD')}
          >
            <Text style={styles.paymentIcon}>💵</Text>
            <View style={styles.paymentInfo}>
              <Text style={styles.paymentName}>Thanh toán khi nhận hàng</Text>
              <Text style={styles.paymentDesc}>Tiền mặt (COD)</Text>
            </View>
            {paymentMethod === 'COD' && <Text style={styles.checkIcon}>✓</Text>}
          </TouchableOpacity>
          
          <TouchableOpacity
            style={[styles.paymentOption, paymentMethod === 'BANK' && styles.paymentOptionActive]}
            onPress={() => setPaymentMethod('BANK')}
          >
            <Text style={styles.paymentIcon}>🏦</Text>
            <View style={styles.paymentInfo}>
              <Text style={styles.paymentName}>Chuyển khoản ngân hàng</Text>
              <Text style={styles.paymentDesc}>Thanh toán online</Text>
            </View>
            {paymentMethod === 'BANK' && <Text style={styles.checkIcon}>✓</Text>}
          </TouchableOpacity>
        </View>

        {/* Voucher */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>🎟️ Mã giảm giá</Text>
          <View style={styles.voucherContainer}>
            <TextInput
              style={styles.voucherInput}
              placeholder="Nhập mã giảm giá"
              value={voucherCode}
              onChangeText={setVoucherCode}
              autoCapitalize="characters"
              editable={!appliedVoucher}
            />
            {appliedVoucher ? (
              <TouchableOpacity
                style={styles.removeVoucherButton}
                onPress={() => {
                  setAppliedVoucher(null);
                  setVoucherCode('');
                }}
              >
                <Text style={styles.removeVoucherText}>Xóa</Text>
              </TouchableOpacity>
            ) : (
              <TouchableOpacity
                style={styles.applyButton}
                onPress={handleValidateVoucher}
                disabled={validatingVoucher}
              >
                {validatingVoucher ? (
                  <ActivityIndicator size="small" color="white" />
                ) : (
                  <Text style={styles.applyButtonText}>Áp dụng</Text>
                )}
              </TouchableOpacity>
            )}
          </View>
          {appliedVoucher && (
            <View style={styles.voucherApplied}>
              <Text style={styles.voucherAppliedText}>
                ✅ Đã áp dụng mã {appliedVoucher.code}
              </Text>
            </View>
          )}
        </View>

        {/* Order Summary */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>📋 Chi tiết đơn hàng</Text>
          {cart.map((item) => (
            <View key={item.id} style={styles.orderItem}>
              <Text style={styles.orderItemName}>{item.name}</Text>
              <Text style={styles.orderItemQty}>x{item.quantity}</Text>
              <Text style={styles.orderItemPrice}>{formatPrice(item.price * item.quantity)}</Text>
            </View>
          ))}
          
          <View style={styles.divider} />
          
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Tạm tính:</Text>
            <Text style={styles.summaryValue}>{formatPrice(subtotal)}</Text>
          </View>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Phí vận chuyển:</Text>
            <Text style={styles.summaryValue}>{formatPrice(shipping)}</Text>
          </View>
          {discount > 0 && (
            <View style={styles.summaryRow}>
              <Text style={[styles.summaryLabel, styles.discountLabel]}>Giảm giá:</Text>
              <Text style={[styles.summaryValue, styles.discountValue]}>-{formatPrice(discount)}</Text>
            </View>
          )}
          <View style={[styles.summaryRow, styles.totalRow]}>
            <Text style={styles.totalLabel}>Tổng cộng:</Text>
            <Text style={styles.totalValue}>{formatPrice(total)}</Text>
          </View>
        </View>
      </ScrollView>

      {/* Place Order Button */}
      <View style={styles.footer}>
        <TouchableOpacity
          style={[styles.placeOrderButton, loading && styles.placeOrderButtonDisabled]}
          onPress={handlePlaceOrder}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator size="small" color="white" />
          ) : (
            <Text style={styles.placeOrderButtonText}>
              Đặt hàng • {formatPrice(total)}
            </Text>
          )}
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8fafc',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: 50,
    paddingBottom: 15,
    paddingHorizontal: 15,
  },
  backButton: {
    width: 80,
  },
  backButtonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '600',
  },
  headerTitle: {
    color: 'white',
    fontSize: 20,
    fontWeight: 'bold',
  },
  content: {
    flex: 1,
  },
  section: {
    backgroundColor: 'white',
    padding: 20,
    marginBottom: 10,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1e293b',
    marginBottom: 16,
  },
  input: {
    backgroundColor: '#f1f5f9',
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
    color: '#1e293b',
    marginBottom: 12,
  },
  paymentOption: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f8fafc',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 2,
    borderColor: 'transparent',
  },
  paymentOptionActive: {
    borderColor: '#0ea5e9',
    backgroundColor: '#f0f9ff',
  },
  paymentIcon: {
    fontSize: 32,
    marginRight: 12,
  },
  paymentInfo: {
    flex: 1,
  },
  paymentName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1e293b',
    marginBottom: 4,
  },
  paymentDesc: {
    fontSize: 14,
    color: '#64748b',
  },
  checkIcon: {
    fontSize: 24,
    color: '#0ea5e9',
    fontWeight: 'bold',
  },
  voucherContainer: {
    flexDirection: 'row',
    gap: 10,
  },
  voucherInput: {
    flex: 1,
    backgroundColor: '#f1f5f9',
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
    color: '#1e293b',
  },
  applyButton: {
    backgroundColor: '#0ea5e9',
    borderRadius: 12,
    paddingHorizontal: 24,
    justifyContent: 'center',
    alignItems: 'center',
    minWidth: 100,
  },
  applyButtonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
  removeVoucherButton: {
    backgroundColor: '#ef4444',
    borderRadius: 12,
    paddingHorizontal: 24,
    justifyContent: 'center',
    alignItems: 'center',
  },
  removeVoucherText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
  voucherApplied: {
    backgroundColor: '#dcfce7',
    borderRadius: 8,
    padding: 12,
    marginTop: 12,
  },
  voucherAppliedText: {
    color: '#16a34a',
    fontSize: 14,
    fontWeight: '600',
  },
  orderItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  orderItemName: {
    flex: 1,
    fontSize: 15,
    color: '#1e293b',
  },
  orderItemQty: {
    fontSize: 14,
    color: '#64748b',
    marginRight: 12,
  },
  orderItemPrice: {
    fontSize: 15,
    fontWeight: '600',
    color: '#0ea5e9',
  },
  divider: {
    height: 1,
    backgroundColor: '#e2e8f0',
    marginVertical: 16,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  summaryLabel: {
    fontSize: 15,
    color: '#64748b',
  },
  summaryValue: {
    fontSize: 15,
    fontWeight: '600',
    color: '#1e293b',
  },
  discountLabel: {
    color: '#16a34a',
  },
  discountValue: {
    color: '#16a34a',
  },
  totalRow: {
    borderTopWidth: 2,
    borderTopColor: '#0ea5e9',
    paddingTop: 12,
    marginTop: 8,
  },
  totalLabel: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1e293b',
  },
  totalValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#0ea5e9',
  },
  footer: {
    backgroundColor: 'white',
    padding: 20,
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
  },
  placeOrderButton: {
    backgroundColor: '#0ea5e9',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  placeOrderButtonDisabled: {
    backgroundColor: '#cbd5e1',
  },
  placeOrderButtonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
  },
});
