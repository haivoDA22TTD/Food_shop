import React, { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
  children: ReactNode;
  requiredRole: string;
}

/**
 * Protected route component for role-based access control
 * Validates: Requirements 7.5, 7.6
 */
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');

  // Check if token exists
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // Check if user data exists
  let user;
  try {
    user = userStr ? JSON.parse(userStr) : null;
  } catch (error) {
    console.error('Failed to parse user data:', error);
    return <Navigate to="/login" replace />;
  }

  // Check if user has a role
  if (!user || !user.role) {
    return <Navigate to="/login" replace />;
  }

  // Check if user has the required role
  if (user.role !== requiredRole) {
    return <Navigate to="/unauthorized" replace />;
  }

  // User is authenticated and has the required role
  return <>{children}</>;
};

export default ProtectedRoute;
