import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
  Alert
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { LinearGradient } from 'expo-linear-gradient';
import api from '../config/api';

const SUGGESTIONS = [
  '🎟️ Cho tôi mã giảm giá',
  '🍔 Món ăn nào ngon?',
  '📦 Kiểm tra đơn hàng',
  '💰 Khuyến mãi hôm nay',
];

export default function ChatbotScreen({ navigation }) {
  const [messages, setMessages] = useState([
    {
      id: '1',
      text: 'Xin chào! 👋 Tôi là trợ lý AI của Food Shop. Tôi có thể giúp gì cho bạn?',
      isBot: true,
      timestamp: new Date(),
    },
  ]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const flatListRef = useRef(null);

  useEffect(() => {
    // Scroll to bottom when new message
    if (flatListRef.current && messages.length > 0) {
      setTimeout(() => {
        flatListRef.current?.scrollToEnd({ animated: true });
      }, 100);
    }
  }, [messages]);

  const sendMessage = async (text) => {
    if (!text.trim()) return;

    const userMessage = {
      id: Date.now().toString(),
      text: text.trim(),
      isBot: false,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputText('');
    setLoading(true);

    try {
      const response = await api.post('/api/chatbot/chat', {
        message: text.trim(),
      });

      const botMessage = {
        id: (Date.now() + 1).toString(),
        text: response.data.message || 'Xin lỗi, tôi không hiểu câu hỏi của bạn.',
        isBot: true,
        timestamp: new Date(),
        voucher: response.data.voucher,
      };

      setMessages((prev) => [...prev, botMessage]);
    } catch (error) {
      console.error('Chat error:', error);
      const errorMessage = {
        id: (Date.now() + 1).toString(),
        text: '❌ Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.',
        isBot: true,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  const handleSuggestionPress = (suggestion) => {
    sendMessage(suggestion);
  };

  const copyVoucherCode = (code) => {
    Alert.alert(
      '✅ Đã sao chép',
      `Mã giảm giá "${code}" đã được sao chép!`,
      [{ text: 'OK' }]
    );
  };

  const renderMessage = ({ item }) => {
    const isBot = item.isBot;

    return (
      <View
        style={[
          styles.messageContainer,
          isBot ? styles.botMessageContainer : styles.userMessageContainer,
        ]}
      >
        {isBot && <Text style={styles.botAvatar}>🤖</Text>}
        
        <View
          style={[
            styles.messageBubble,
            isBot ? styles.botBubble : styles.userBubble,
          ]}
        >
          <Text
            style={[
              styles.messageText,
              isBot ? styles.botText : styles.userText,
            ]}
          >
            {item.text}
          </Text>

          {item.voucher && (
            <TouchableOpacity
              style={styles.voucherCard}
              onPress={() => copyVoucherCode(item.voucher.code)}
            >
              <View style={styles.voucherHeader}>
                <Text style={styles.voucherIcon}>🎟️</Text>
                <Text style={styles.voucherCode}>{item.voucher.code}</Text>
              </View>
              <Text style={styles.voucherDiscount}>
                Giảm {item.voucher.discountType === 'PERCENTAGE' 
                  ? `${item.voucher.discountValue}%` 
                  : `${item.voucher.discountValue.toLocaleString('vi-VN')}đ`}
              </Text>
              <Text style={styles.voucherExpiry}>
                HSD: {new Date(item.voucher.expiryDate).toLocaleDateString('vi-VN')}
              </Text>
              <Text style={styles.voucherCopy}>Nhấn để sao chép</Text>
            </TouchableOpacity>
          )}

          <Text style={styles.timestamp}>
            {item.timestamp.toLocaleTimeString('vi-VN', {
              hour: '2-digit',
              minute: '2-digit',
            })}
          </Text>
        </View>

        {!isBot && <Text style={styles.userAvatar}>👤</Text>}
      </View>
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={0}
    >
      <StatusBar style="light" />

      {/* Header */}
      <LinearGradient colors={['#0ea5e9', '#0284c7']} style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>← Quay lại</Text>
        </TouchableOpacity>
        <View style={styles.headerCenter}>
          <Text style={styles.headerTitle}>🤖 AI Trợ lý</Text>
          <Text style={styles.headerSubtitle}>Powered by Gemini</Text>
        </View>
        <View style={styles.backButton} />
      </LinearGradient>

      {/* Messages */}
      <FlatList
        ref={flatListRef}
        data={messages}
        renderItem={renderMessage}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.messagesList}
        onContentSizeChange={() =>
          flatListRef.current?.scrollToEnd({ animated: true })
        }
      />

      {/* Suggestions */}
      {messages.length === 1 && (
        <View style={styles.suggestionsContainer}>
          <Text style={styles.suggestionsTitle}>Gợi ý câu hỏi:</Text>
          <View style={styles.suggestionsGrid}>
            {SUGGESTIONS.map((suggestion, index) => (
              <TouchableOpacity
                key={index}
                style={styles.suggestionButton}
                onPress={() => handleSuggestionPress(suggestion)}
              >
                <Text style={styles.suggestionText}>{suggestion}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      )}

      {/* Loading */}
      {loading && (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="small" color="#0ea5e9" />
          <Text style={styles.loadingText}>Đang suy nghĩ...</Text>
        </View>
      )}

      {/* Input */}
      <View style={styles.inputContainer}>
        <TextInput
          style={styles.input}
          placeholder="Nhập tin nhắn..."
          placeholderTextColor="#94a3b8"
          value={inputText}
          onChangeText={setInputText}
          multiline
          maxLength={500}
          editable={!loading}
        />
        <TouchableOpacity
          style={[styles.sendButton, (!inputText.trim() || loading) && styles.sendButtonDisabled]}
          onPress={() => sendMessage(inputText)}
          disabled={!inputText.trim() || loading}
        >
          <Text style={styles.sendButtonText}>📤</Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
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
  headerCenter: {
    alignItems: 'center',
  },
  headerTitle: {
    color: 'white',
    fontSize: 20,
    fontWeight: 'bold',
  },
  headerSubtitle: {
    color: 'rgba(255,255,255,0.8)',
    fontSize: 12,
    marginTop: 2,
  },
  messagesList: {
    padding: 16,
    paddingBottom: 8,
  },
  messageContainer: {
    flexDirection: 'row',
    marginBottom: 16,
    alignItems: 'flex-end',
  },
  botMessageContainer: {
    justifyContent: 'flex-start',
  },
  userMessageContainer: {
    justifyContent: 'flex-end',
  },
  botAvatar: {
    fontSize: 32,
    marginRight: 8,
    marginBottom: 4,
  },
  userAvatar: {
    fontSize: 32,
    marginLeft: 8,
    marginBottom: 4,
  },
  messageBubble: {
    maxWidth: '75%',
    padding: 12,
    borderRadius: 16,
  },
  botBubble: {
    backgroundColor: 'white',
    borderBottomLeftRadius: 4,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  userBubble: {
    backgroundColor: '#0ea5e9',
    borderBottomRightRadius: 4,
  },
  messageText: {
    fontSize: 15,
    lineHeight: 22,
  },
  botText: {
    color: '#1e293b',
  },
  userText: {
    color: 'white',
  },
  timestamp: {
    fontSize: 11,
    color: '#94a3b8',
    marginTop: 4,
  },
  voucherCard: {
    backgroundColor: '#dcfce7',
    borderRadius: 12,
    padding: 12,
    marginTop: 8,
    borderWidth: 2,
    borderColor: '#16a34a',
    borderStyle: 'dashed',
  },
  voucherHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  voucherIcon: {
    fontSize: 24,
    marginRight: 8,
  },
  voucherCode: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#16a34a',
  },
  voucherDiscount: {
    fontSize: 16,
    fontWeight: '600',
    color: '#15803d',
    marginBottom: 4,
  },
  voucherExpiry: {
    fontSize: 13,
    color: '#166534',
    marginBottom: 6,
  },
  voucherCopy: {
    fontSize: 12,
    color: '#16a34a',
    fontStyle: 'italic',
  },
  suggestionsContainer: {
    padding: 16,
    backgroundColor: 'white',
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
  },
  suggestionsTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#64748b',
    marginBottom: 12,
  },
  suggestionsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  suggestionButton: {
    backgroundColor: '#f1f5f9',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#cbd5e1',
  },
  suggestionText: {
    fontSize: 14,
    color: '#475569',
    fontWeight: '500',
  },
  loadingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 12,
    backgroundColor: 'white',
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
  },
  loadingText: {
    marginLeft: 8,
    fontSize: 14,
    color: '#64748b',
    fontStyle: 'italic',
  },
  inputContainer: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: 'white',
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
    alignItems: 'flex-end',
  },
  input: {
    flex: 1,
    backgroundColor: '#f1f5f9',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 10,
    fontSize: 15,
    color: '#1e293b',
    maxHeight: 100,
    marginRight: 8,
  },
  sendButton: {
    width: 44,
    height: 44,
    backgroundColor: '#0ea5e9',
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sendButtonDisabled: {
    backgroundColor: '#cbd5e1',
  },
  sendButtonText: {
    fontSize: 20,
  },
});
