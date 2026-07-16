import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { MyProfileView } from '../components/Views';
import { getMyProfile } from '../service/employeeService';
import { getRequestByEmployeeId } from '../service/requestService';

export default function ProfilePage() {
  const { user } = useAuth();
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // --- Pagination States ---
  const [historyPage, setHistoryPage] = useState(0);
  const [historyTotalPages, setHistoryTotalPages] = useState(0);
  const [historyLoading, setHistoryLoading] = useState(false);

  // 1. Initial Load (Profile + Page 0 of Requests)
  useEffect(() => {
    const loadProfileAndRequests = async () => {
      try {
        setLoading(true);
        
        const data = await getMyProfile();
        const requestsData = await getRequestByEmployeeId(0, 10, data.id);

        setHistoryTotalPages(requestsData?.page.totalPages || 0);

        const profile = {
          id: data.id,
          firstName: data.firstName || '',
          lastName: data.lastName || '',
          email: data.email || '',
          phone: data.phone || '',
          hireDate: data.createdAt ? new Date(data.createdAt).toLocaleDateString('fr-FR') : 'N/A',
          totalDays: Number(data.thisYearVacationDays + data.lastYearVacationDays - data.lastYearUsedVacationDays - data.usedVacationDays  || 0),
          usedDays: Number(data.lastYearUsedVacationDays || 0) + Number(data.usedVacationDays || 0),
          
          vacationHistory: (requestsData?.content || []).map((req) => ({
            id: req.id,
            name: `${data.firstName || ''} ${data.lastName || ''}`.trim(),
            fromDate: req.fromDate || req.from || '',
            toDate: req.toDate || req.to || '',
            days: req.numberOfDays || req.days || 0,
            status: req.status || 'Pending',
          })),
          
          coverageTeam: (data.defaultBackups || []).map((backup) => ({
            id: backup.id,
            firstName: backup.firstName,
            lastName: backup.lastName,
          })),
        };

        setProfileData(profile);
      } catch (err) {
        setError(err.response?.data?.message || err.message || 'Impossible de charger le profil.');
      } finally {
        setLoading(false);
      }
    };

    loadProfileAndRequests();
  }, []); 

  // 2. Secondary Load (Fires only when the user changes pages)
  useEffect(() => {
    if (loading || !profileData?.id) return; // Skip during initial load

    const loadJustHistory = async () => {
      try {
        setHistoryLoading(true);
        const requestsData = await getRequestByEmployeeId(historyPage, 10, profileData.id);
        
        setHistoryTotalPages(requestsData?.page.totalPages || 0);
        
        // Update ONLY the vacationHistory array inside the existing profile object
        setProfileData(prev => ({
          ...prev,
          vacationHistory: (requestsData?.content || []).map((req) => ({
            id: req.id,
            name: `${prev.firstName || ''} ${prev.lastName || ''}`.trim(),
            fromDate: req.fromDate || req.from || '',
            toDate: req.toDate || req.to || '',
            days: req.numberOfDays || req.days || 0,
            status: req.status || 'Pending',
          }))
        }));
      } catch (err) {
        console.error("Erreur lors du chargement de l'historique:", err);
      } finally {
        setHistoryLoading(false);
      }
    };

    loadJustHistory();
  }, [historyPage]); // Re-runs when historyPage changes

  if (loading) {
    return <div>Chargement du profil...</div>;
  }

  if (error) {
    return <div className="alert-error">{error}</div>;
  }

  return (
    <MyProfileView 
      currentUser={user} 
      userProfileData={profileData} 
      historyPage={historyPage}
      historyTotalPages={historyTotalPages}
      setHistoryPage={setHistoryPage}
      historyLoading={historyLoading}
    />
  );
}