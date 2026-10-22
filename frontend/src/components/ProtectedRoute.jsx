import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Spinner from './Spinner';

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user, isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <Spinner message="Checking authentication..." />;
  }

  // if not logged in, redirect to login page
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // if role restriction applies and user's role does not match, redirect to their role home
  if (allowedRoles && allowedRoles.length > 0) {
    const userRole = user?.role ? user.role.replace('ROLE_', '') : '';
    const hasRole = allowedRoles.some((role) => role.replace('ROLE_', '') === userRole);

    if (!hasRole) {
      if (userRole === 'STUDENT') {
        return <Navigate to="/student/dashboard" replace />;
      } else if (userRole === 'FACULTY') {
        return <Navigate to="/faculty/queue" replace />;
      } else if (userRole === 'ADMIN') {
        return <Navigate to="/admin/dashboard" replace />;
      }
      return <Navigate to="/" replace />;
    }
  }

  return children;
}
