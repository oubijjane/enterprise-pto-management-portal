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
  const [isExporting, setIsExporting] = useState(false);
  
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

  const handleDownloadExcelReport = async () => {
    if (isExporting) return;

    setIsExporting(true);

    try {
      const response = await requestService.downloadExcelReport();
      const blob = new Blob([response.data], {
        type: response.headers?.['content-type'] || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });

      const contentDisposition = response.headers?.['content-disposition'] || '';
      const match = contentDisposition.match(/filename\s*=\s*"?([^";]+)"?/i);
      const fileName = match?.[1] || 'timeaway-report.xlsx';

      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Erreur lors du téléchargement du fichier Excel :', error);
    } finally {
      setTimeout(() => setIsExporting(false), 500);
    }
  };

  const totalPending = requests.content?.filter(r => r.status === "PENDING").length || 0  ;

  const navItems = [
    { path: "/dashboard", icon: "▦", label: "Tableau de bord" , adminOnly: true},
    { path: "/profile", icon: "👤", label: "Mon profil" , adminOnly: false},
    { path: "/requests", icon: "◫", label: "Toutes les demandes" , visibleNavItemsForManagerAndAdmin: true},
    { path: "/new-request", icon: "+", label: "Nouvelle demande", adminOnly: false },
    { path: "/new-employee", icon: "+", label: "Ajouter un employé", adminOnly: true },
    { path: "/balances", icon: "◎", label: "Soldes de l'équipe", adminOnly: true },
    { path: "/holidays", icon: "📅", label: "Jours fériés", adminOnly: true },
    { action: 'export', icon: '↓', label: 'Exporter Excel', adminOnly: true }
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
              key={item.path || item.action || item.label}
              className={`nav-btn ${item.path && location.pathname === item.path ? "active" : ""}`}
              disabled={item.action === 'export' && isExporting}
              onClick={() => {
                if (item.action === 'export') {
                  handleDownloadExcelReport();
                  return;
                }
                navigate(item.path);
              }}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.action === 'export' && isExporting ? 'Fichier Excel généré...' : item.label}
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