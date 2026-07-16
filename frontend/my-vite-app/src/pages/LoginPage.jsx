import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import loginService from '../service/logInService';
import { LoginView } from '../components/Views';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState(null);

  const handleLoginSubmit = async ({ username, password }) => {
    try {
      const user = await loginService(username, password);
      login(user);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Login failed. Please check your credentials.');
    }
  };

  return <LoginView onLogin={handleLoginSubmit} error={error} />;
}
