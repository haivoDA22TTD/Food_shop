import React, { createContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import api from '../config/api';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkLoginStatus();
  }, []);

  const checkLoginStatus = async () => {
    try {
      const token = await AsyncStorage.getItem('token');
      const username = await AsyncStorage.getItem('username');
      const role = await AsyncStorage.getItem('role');
      
      if (token && username) {
        setUser({ username, role, token });
      }
    } catch (error) {
      console.error('Check login error:', error);
    } finally {
      setLoading(false);
    }
  };

  const login = async (username, password) => {
    try {
      const response = await api.post('/api/auth/login', { username, password });
      const { token, role } = response.data;
      
      await AsyncStorage.setItem('token', token);
      await AsyncStorage.setItem('username', username);
      await AsyncStorage.setItem('role', role);
      
      setUser({ username, role, token });
      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        message: error.response?.data?.message || 'Đăng nhập thất bại' 
      };
    }
  };

  const register = async (username, password, email, fullName, phone, address) => {
    try {
      await api.post('/api/auth/register', {
        username,
        password,
        email,
        fullName,
        phone,
        address
      });
      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        message: error.response?.data?.message || 'Đăng ký thất bại' 
      };
    }
  };

  const logout = async () => {
    await AsyncStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
