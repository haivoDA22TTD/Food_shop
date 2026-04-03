import React, { useState, useEffect, useContext } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  Image,
  TouchableOpacity,
  ActivityIndicator,
  ScrollView,
  RefreshControl
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import api from '../config/api';
import { CartContext } from '../context/CartContext';

const CATEGORIES = [
  { id: 'all', name: 'Tất cả', icon: '🍽️' },
  { id: 'Đồ ăn nhanh', name: 'Đồ ăn nhanh', icon: '🍔' },
  { id: 'Đồ ngọt', name: 'Đồ ngọt', icon: '🍰' },
  { id: 'Đồ uống', name: 'Đồ uống', icon: '🥤' },
];

export default function HomeScreen({ navigation }) {
  const [products, setProducts] = useState([]);
  const [filteredProducts, setFilteredProducts] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const { addToCart } = useContext(CartContext);

  useEffect(() => {
    fetchProducts();
  }, []);

  useEffect(() => {
    filterProducts();
  }, [selectedCategory, products]);

  const fetchProducts = async () => {
    try {
      const response = await api.get('/api/products');
      setProducts(response.data);
    } catch (error) {
      console.error('Fetch products error:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const onRefresh = () => {
    setRefreshing(true);
    fetchProducts();
  };

  const filterProducts = () => {
    if (selectedCategory === 'all') {
      setFilteredProducts(products);
    } else {
      setFilteredProducts(products.filter(p => p.category === selectedCategory));
    }
  };

  const handleAddToCart = (product) => {
    if (product.stock > 0) {
      addToCart(product, 1);
      alert('Đã thêm vào giỏ hàng!');
    } else {
      alert('Sản phẩm đã hết hàng!');
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN').format(price) + 'đ';
  };

  const getStockBadge = (stock) => {
    if (stock > 20) return { text: `Còn ${stock}`, color: '#10b981' };
    if (stock > 0) return { text: `Còn ${stock}`, color: '#f59e0b' };
    return { text: 'Hết hàng', color: '#ef4444' };
  };

  const renderProduct = ({ item }) => {
    const stockBadge = getStockBadge(item.stock);
    const imageUrl = item.image.startsWith('http') 
      ? item.image 
      : `https://food-shop-iswi.onrender.com/img/${item.image}`;

    return (
      <TouchableOpacity
        style={styles.productCard}
        onPress={() => navigation.navigate('ProductDetail', { productId: item.id })}
      >
        <Image source={{ uri: imageUrl }} style={styles.productImage} />
        <View style={styles.productInfo}>
          <Text style={styles.categoryBadge}>{item.category}</Text>
          <Text style={styles.productName} numberOfLines={2}>{item.name}</Text>
          <Text style={styles.productDesc} numberOfLines={2}>{item.description}</Text>
          <View style={styles.productFooter}>
            <Text style={styles.price}>{formatPrice(item.price)}</Text>
            <TouchableOpacity
              style={[styles.addButton, item.stock === 0 && styles.addButtonDisabled]}
              onPress={() => handleAddToCart(item)}
              disabled={item.stock === 0}
            >
              <Text style={styles.addButtonText}>
                {item.stock > 0 ? 'Thêm' : 'Hết'}
              </Text>
            </TouchableOpacity>
          </View>
          <View style={[styles.stockBadge, { backgroundColor: stockBadge.color }]}>
            <Text style={styles.stockText}>{stockBadge.text}</Text>
          </View>
        </View>
      </TouchableOpacity>
    );
  };

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#0ea5e9" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar style="light" />
      
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>🍔 Food Shop</Text>
        <Text style={styles.headerSubtitle}>Đặt món ngon mỗi ngày</Text>
      </View>

      {/* Categories */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        style={styles.categoryScroll}
        contentContainerStyle={styles.categoryContainer}
      >
        {CATEGORIES.map(cat => (
          <TouchableOpacity
            key={cat.id}
            style={[
              styles.categoryButton,
              selectedCategory === cat.id && styles.categoryButtonActive
            ]}
            onPress={() => setSelectedCategory(cat.id)}
          >
            <Text style={styles.categoryIcon}>{cat.icon}</Text>
            <Text style={[
              styles.categoryText,
              selectedCategory === cat.id && styles.categoryTextActive
            ]}>
              {cat.name}
            </Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {/* Products */}
      <FlatList
        data={filteredProducts}
        renderItem={renderProduct}
        keyExtractor={item => item.id.toString()}
        numColumns={2}
        contentContainerStyle={styles.productList}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f0f9ff',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
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
  categoryScroll: {
    backgroundColor: 'white',
  },
  categoryContainer: {
    padding: 15,
    gap: 10,
  },
  categoryButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 20,
    backgroundColor: '#f1f5f9',
    marginRight: 10,
  },
  categoryButtonActive: {
    backgroundColor: '#0ea5e9',
  },
  categoryIcon: {
    fontSize: 18,
    marginRight: 6,
  },
  categoryText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#64748b',
  },
  categoryTextActive: {
    color: 'white',
  },
  productList: {
    padding: 10,
  },
  productCard: {
    flex: 1,
    backgroundColor: 'white',
    borderRadius: 12,
    margin: 6,
    overflow: 'hidden',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  productImage: {
    width: '100%',
    height: 140,
    resizeMode: 'cover',
  },
  productInfo: {
    padding: 12,
  },
  categoryBadge: {
    fontSize: 11,
    color: '#0ea5e9',
    fontWeight: '600',
    marginBottom: 4,
  },
  productName: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#1e293b',
    marginBottom: 4,
  },
  productDesc: {
    fontSize: 12,
    color: '#64748b',
    marginBottom: 8,
  },
  productFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  price: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#0ea5e9',
  },
  addButton: {
    backgroundColor: '#0ea5e9',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  addButtonDisabled: {
    backgroundColor: '#cbd5e1',
  },
  addButtonText: {
    color: 'white',
    fontWeight: '600',
    fontSize: 13,
  },
  stockBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  stockText: {
    color: 'white',
    fontSize: 11,
    fontWeight: '600',
  },
});
