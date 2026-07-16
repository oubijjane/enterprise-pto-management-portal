import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import AppLayout from './components/AppLayout';

export default function ProtectedLayout({ isAuthenticated, currentUser, onLogout }) {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  if (!isAuthenticated) {
    return null;
  }

  return (
    <AppLayout currentUser={currentUser} onLogout={onLogout}>
      <Outlet />
    </AppLayout>
  );
}
