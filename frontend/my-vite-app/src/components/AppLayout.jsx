import { useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import requestService from '../service/requestService';
import { INITIAL_REQUESTS } from '../data';
import '../App.css';

export default function AppLayout({ currentUser, children }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();
  const [requests, setRequests] = useState([]);
  
  useEffect(() => {
    const loadRequests = async () => {
      try {
        const response = await requestService.getAllRequests();
        setRequests(response);
      } catch (error) {
        console.error('Erreur lors de la récupération des demandes :', error);
      }
    };

    loadRequests();
  }, []);

  const handleLogoutClick = () => {
    logout();
    navigate('/login');
  };

  const totalPending = requests.content?.filter(r => r.status === "PENDING").length || 0  ;

  const navItems = [
    { path: "/dashboard", icon: "▦", label: "Tableau de bord" , adminOnly: true},
    { path: "/profile", icon: "👤", label: "Mon profil" , adminOnly: false},
    { path: "/requests", icon: "◫", label: "Toutes les demandes" , visibleNavItemsForManagerAndAdmin: true},
    { path: "/new-request", icon: "+", label: "Nouvelle demande", adminOnly: false },
    { path: "/new-employee", icon: "+", label: "Ajouter un employé", adminOnly: true },
    { path: "/balances", icon: "◎", label: "Soldes de l'équipe", adminOnly: true },
    { path: "/holidays", icon: "📅", label: "Jours fériés", adminOnly: true }
  ];
  
  const visibleNavItems = navItems.filter(item => {
    if (item.adminOnly) {
      return user?.role === 'ROLE_ADMIN';
    }
    if (item.visibleNavItemsForManagerAndAdmin) {
      return user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_MANAGER' || user?.role === 'ROLE_HR';
    }
    return true;
  });

  return (
    <div className="app-container">
      <div className="header">
        <div className="flex-row gap-3">
          <div className="logo-box">✈</div>
          <span className="app-title">TimeAway</span>
        </div>
        <div className="flex-row gap-4">
          <div className="flex-row gap-2 profile-indicator">
            <span className="text-md text-medium">{currentUser?.name}</span>
            <button onClick={handleLogoutClick} className="btn-logout">
              Déconnexion
            </button>
          </div>
        </div>
      </div>

      <div className="main-layout">
        <div className="sidebar">
          {visibleNavItems.map(item => (
            <button
              key={item.path}
              className={`nav-btn ${location.pathname === item.path ? "active" : ""}`}
              onClick={() => navigate(item.path)}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.label}
            </button>
          ))}
        </div>

        <div className="content-area">
          {children}
        </div>
      </div>
    </div>
  );
}