import { Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext'; // Adjust path if needed

export function HomeRedirect() {
  const { user, loading } = useAuth();

  // Wait for auth to finish checking local storage
  if (loading) return <div>Loading...</div>;

  // Safety catch: If they aren't logged in at all, send to login
  if (!user) return <Navigate to="/login" replace />;

  // Calculate the correct path based on their role
  const redirectPath = (user.role === 'ROLE_ADMIN') 
    ? '/dashboard' 
    : '/profile';

  // Navigate WITHOUT quotes around the variable
  return <Navigate to={redirectPath} replace />;
}