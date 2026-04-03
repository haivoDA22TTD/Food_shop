import React, { useContext } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  Image,
  TouchableOpacity,
  ScrollView
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';

export default function CartScreen({ navigation }) {
  const { cart, updateQuantity, removeFromCart, getCartTotal } = useContext(CartContext);
  const { user } = useContext(AuthContext);

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN').format(price) + 'đ';
  };

  const handleCheckout = () => {
    if (!user) {
      alert('Vui lòng đăng nhập để thanh toán!');
      navigation.navigate('Login');
      return;
    }

    if (cart.length === 0) {
      alert('Giỏ hàng trống!');
      return;
    }

    navigation.navigate('Checkout');
  };

  const renderCartItem = ({ item }) => {
    const imageUrl = item.image.startsWith('http')
      ? item.image
      : `https://food-shop-iswi.onrender.com/img/${item.image}`;

    return (
      <View style={styles.cartItem}>
        <Image source={{ uri: imageUrl }} style={styles.itemImage} />
        <View style={styles.itemInfo}>
          <Text style={styles.itemName} numberOfLines={2}>{item.name}</Text>
          <Text style={styles.itemPrice}>{formatPrice(item.price)}</Text>
          <View style={styles.quantityControl}>
            <TouchableOpacity
              style={styles.quantityBtn}
              onPress={() => {
                if (item.quantity === 1) {
                  if (confirm('Xóa sản phẩm khỏi giỏ hàng?')) {
                    removeFromCart(item.id);
                  }
                } else {
                  updateQuantity(item.id, -1);
                }
              }}
            >
              <Text style={styles.quantityBtnText}>−</Text>
            </TouchableOpacity>
            <Text style={styles.quantity}>{item.quantity}</Text>
            <TouchableOpacity
              style={styles.quantityBtn}
              onPress={() => updateQuantity(item.id, 1)}
            >
              <Text style={styles.quantityBtnText}>+</Text>
            </TouchableOpacity>
          </View>
        </View>
        <View style={styles.itemActions}>
          <Text style={styles.itemTotal}>
            {formatPrice(item.price * item.quantity)}
          </Text>
          <TouchableOpacity
            style={styles.removeBtn}
            onPress={() => {
              if (confirm('Xóa sản phẩm khỏi giỏ hàng?')) {
                removeFromCart(item.id);
              }
            }}
          >
            <Text style={styles.removeBtnText}>Xóa</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  if (cart.length === 0) {
    return (
      <View style={styles.container}>
        <StatusBar style="light" />
        <View style={styles.header}>
          <Text style={styles.headerTitle}>🛒 Giỏ hàng</Text>
        </View>
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyIcon}>🛒</Text>
          <Text style={styles.emptyTitle}>Giỏ hàng trống</Text>
          <Text style={styles.emptyText}>Bạn chưa có sản phẩm nào trong giỏ hàng</Text>
          <TouchableOpacity
            style={styles.continueBtn}
            onPress={() => navigation.navigate('Home')}
          >
            <Text style={styles.continueBtnText}>Tiếp tục mua sắm</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  const subtotal = getCartTotal();
  const shipping = 30000;
  const total = subtotal + shipping;

  return (
    <View style={styles.container}>
      <StatusBar style="light" />
      <View style={styles.header}>
        <Text style={styles.headerTitle}>🛒 Giỏ hàng</Text>
        <Text style={styles.headerSubtitle}>{cart.length} sản phẩm</Text>
      </View>

      <FlatList
        data={cart}
        renderItem={renderCartItem}
        keyExtractor={item => item.id.toString()}
        contentContainerStyle={styles.cartList}
      />

      <View style={styles.summary}>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Tạm tính:</Text>
          <Text style={styles.summaryValue}>{formatPrice(subtotal)}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Phí vận chuyển:</Text>
          <Text style={styles.summaryValue}>{formatPrice(shipping)}</Text>
        </View>
        <View style={[styles.summaryRow, styles.summaryTotal]}>
          <Text style={styles.totalLabel}>Tổng cộng:</Text>
          <Text style={styles.totalValue}>{formatPrice(total)}</Text>
        </View>
        <TouchableOpacity style={styles.checkoutBtn} onPress={handleCheckout}>
          <Text style={styles.checkoutBtnText}>Thanh toán</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f0f9ff',
  },
  header: {
    backgroundColor: '#0ea5e9',
    padding: 20,
    paddingTop: 50,
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: 'bold',
    color: 'white',
  },
  headerSubtitle: {
    fontSize: 14,
    color: 'rgba(255,255,255,0.9)',
    marginTop: 4,
  },
  cartList: {
    padding: 10,
  },
  cartItem: {
    flexDirection: 'row',
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 12,
    marginBottom: 10,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  itemImage: {
    width: 80,
    height: 80,
    borderRadius: 8,
    resizeMode: 'cover',
  },
  itemInfo: {
    flex: 1,
    marginLeft: 12,
  },
  itemName: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#1e293b',
    marginBottom: 4,
  },
  itemPrice: {
    fontSize: 14,
    color: '#0ea5e9',
    fontWeight: '600',
    marginBottom: 8,
  },
  quantityControl: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f1f5f9',
    borderRadius: 8,
    alignSelf: 'flex-start',
    paddingHorizontal: 4,
  },
  quantityBtn: {
    width: 32,
    height: 32,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#0ea5e9',
    borderRadius: 6,
    margin: 2,
  },
  quantityBtnText: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
  },
  quantity: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#1e293b',
    marginHorizontal: 12,
  },
  itemActions: {
    alignItems: 'flex-end',
    justifyContent: 'space-between',
  },
  itemTotal: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#0ea5e9',
  },
  removeBtn: {
    backgroundColor: '#ef4444',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 6,
  },
  removeBtnText: {
    color: 'white',
    fontSize: 12,
    fontWeight: '600',
  },
  summary: {
    backgroundColor: 'white',
    padding: 20,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  summaryLabel: {
    fontSize: 14,
    color: '#64748b',
  },
  summaryValue: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1e293b',
  },
  summaryTotal: {
    borderTopWidth: 2,
    borderTopColor: '#0ea5e9',
    paddingTop: 12,
    marginTop: 8,
  },
  totalLabel: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1e293b',
  },
  totalValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#0ea5e9',
  },
  checkoutBtn: {
    backgroundColor: '#0ea5e9',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginTop: 16,
  },
  checkoutBtnText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  emptyIcon: {
    fontSize: 80,
    marginBottom: 20,
  },
  emptyTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1e293b',
    marginBottom: 10,
  },
  emptyText: {
    fontSize: 16,
    color: '#64748b',
    textAlign: 'center',
    marginBottom: 30,
  },
  continueBtn: {
    backgroundColor: '#0ea5e9',
    paddingHorizontal: 30,
    paddingVertical: 14,
    borderRadius: 12,
  },
  continueBtnText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
});
