import React, { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
  children: ReactNode;
  requiredRole: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  // Zustand persist stores under key "auth-storage" as { state: { user, token }, version }
  let authStr = localStorage.getItem('auth-storage');
  let token: string | null = null;
  let user: any = null;

  try {
    if (authStr) {
      const parsed = JSON.parse(authStr);
      const state = parsed.state || parsed;
      token = state.token;
      user = state.user;
    }
  } catch (error) {
    console.error('Failed to parse auth data:', error);
  }

  if (!token || !user || !user.role) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== requiredRole) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
