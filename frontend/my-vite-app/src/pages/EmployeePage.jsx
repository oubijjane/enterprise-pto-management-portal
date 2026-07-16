import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { EmployeeView } from '../components/Views';
import { getEmployeeProfileById, updateEmployee } from '../service/employeeService';
import { getRequestByEmployeeId } from '../service/requestService';
import {getAllDepartments} from '../service/departmentService'

export default function ProfilePage() {
  const { user } = useAuth();
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null); 
  const { employeeId } = useParams();

  const [Deps, setDeps] = useState([]);

  const loadDeps = async () => {
    const respons = await getAllDepartments();
    setDeps(respons)
  }
  useEffect(() => {
    loadDeps();
  },[])

  // --- Pagination State for Vacation History ---
  const [historyPage, setHistoryPage] = useState(0);
  const [historyTotalPages, setHistoryTotalPages] = useState(0);
  const [historyLoading, setHistoryLoading] = useState(false);

  const role = user?.roles?.[0] || null;

  // 1. Main Effect to Load Profile and Initial Requests Page
  useEffect(() => {
    const loadProfileAndHistory = async () => {
      try {
        setLoading(true);
        const data = await getEmployeeProfileById(employeeId);
        
        // Fetch specific page of vacation requests (10 items per page)
        const requests = await getRequestByEmployeeId(historyPage, 10, employeeId); 
        setHistoryTotalPages(requests?.page.totalPages || 0);

        const profile = {
          email: data.email || null,
          phone: data.phone || null,
          firstName: data.firstName || null,
          lastName: data.lastName || null,
          loginName: data.loginName || null,
          hiringDate: data.hiringDate,
          name: `${data.firstName || null} ${data.lastName || null}`.trim(),
          nextYearVacationDays: Number(data.nextYearVacationDays || 0),
          
          totalDays: Number(data.thisYearVacationDays + data.lastYearUsedVacationDays || 0) + Number(data.lastYearVacationDays + data.lastYearUsedVacationDays || 0),
          thisYearDays: Number(data.thisYearVacationDays || 0),
          lastYearDays: Number(data.lastYearVacationDays || 0),
          thisYearUsedDays: Number(data.usedVacationDays || 0),
          lastYearUsedDays: Number(data.lastYearUsedVacationDays || 0),
          usedDays: Number(data.lastYearUsedVacationDays || 0) + Number(data.usedVacationDays || 0),
          accrualRatePerMonth: Number(data.accrualRatePerMonth || 0),
          
          vacationHistory: (requests.content || []).map((req) => ({
            id: req.id,
            name: `${data.firstName || null} ${data.lastName || null}`.trim(),
            fromDate: req.fromDate || req.from || null,
            toDate: req.toDate || req.to || null,
            days: req.numberOfDays || req.days || 0,
            status: req.status || 'Pending',
          })),
          coverageTeam: (data.defaultBackups || []).map((backup) => ({
            id: backup.id,
            firstName: backup.firstName || null,
            lastName: backup.lastName || null,
          })),
          role: data.role?.[0]?.id|| null,
          department: data.departmentDTO?.id || null,
        };

        setProfileData(profile);
      } catch (err) {
        setError(err.response?.data?.message || err.message || 'Unable to load profile.');
      } finally {
        setLoading(false);
      }
    };

    loadProfileAndHistory();
  }, [employeeId]); // Triggers only on employee context switch

  // 2. Secondary Effect to Load subsequent Vacation History pages independently
  useEffect(() => {
    // Skip on initial mount since loadProfileAndHistory covers it
    if (loading) return; 

    const loadJustHistory = async () => {
      try {
        setHistoryLoading(true);
        const requests = await getRequestByEmployeeId(historyPage, 10, employeeId);
        
        setHistoryTotalPages(requests?.page.totalPages || 0);
        setProfileData(prev => ({
          ...prev,
          vacationHistory: (requests.content || []).map((req) => ({
            id: req.id,
            name: prev.name,
            fromDate: req.fromDate || req.from || null,
            toDate: req.toDate || req.to || null,
            days: req.numberOfDays || req.days || 0,
            status: req.status || 'Pending',
          }))
        }));
      } catch (err) {
        console.error("Failed to load subsequent vacation history page:", err);
      } finally {
        setHistoryLoading(false);
      }
    };

    loadJustHistory();
  }, [historyPage]); // Re-runs cleanly when user shifts pages

  // --- Pagination Window Calculation ---
  const getPageNumbers = () => {
    const maxButtons = 5; 
    let start = Math.max(0, historyPage - 2);
    let end = Math.min(historyTotalPages, start + maxButtons);

    if (end - start < maxButtons) {
      start = Math.max(0, end - maxButtons);
    }

    const pages = [];
    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  };

  const onSave = async (updatedData) => {
    setError(null);
    setSuccessMsg(null);
    
    try {
      const payload = {
        id: employeeId,
        firstName: updatedData.firstName,
        lastName: updatedData.lastName,
        email: updatedData.email,
        phoneNumber: updatedData.phone,
        password: updatedData.password, 
        loginName: updatedData.loginName,
        departmentDTO: { id: updatedData.department },
        thisYearVacationDays: updatedData.thisYearDays,
        lastYearVacationDays: updatedData.lastYearDays,
        usedVacationDays: updatedData.thisYearUsedDays,
        lastYearUsedVacationDays: updatedData.lastYearUsedDays,
        accrualRatePerMonth: updatedData.accrualRatePerMonth,
        nextYearVacationDays: updatedData.nextYearVacationDays,
        role: [{ id: updatedData.role }],
        hiringDate: updatedData.hiringDate
      };
      await updateEmployee(payload);
      
      const updatedProfile = {
        ...profileData, // Keep unchanged nested collections intact
        ...updatedData,
        name: `${updatedData.firstName} ${updatedData.lastName}`.trim(),
        totalDays: Number(updatedData.thisYearDays || 0) + Number(updatedData.lastYearDays || 0),
        usedDays: Number(updatedData.thisYearUsedDays || 0) + Number(updatedData.lastYearUsedDays || 0)
      };
      
      setProfileData(updatedProfile);
      setSuccessMsg("Employee profile updated successfully!");
      setTimeout(() => setSuccessMsg(null), 3000);
      
    } catch (err) {
      console.error('Saving updated profile data failed:', err);
      setError(err.response?.data?.message || 'Failed to save changes. Please try again.');
    }
  };

  if (loading) return <div>Loading profile...</div>;
  if (error) return <div className="alert-error">{error}</div>;

  return (
    <>
      {successMsg && <div className="alert-success" style={{ marginBottom: '16px' }}>✓ {successMsg}</div>}
      
      {/* If secondary history loading indicator is needed, pass historyLoading flag to View */}
      <EmployeeView userProfileData={profileData} onSave={onSave} isHistoryLoading={historyLoading} departments={Deps} />
      
      {/* Dedicated Pagination Block for the History Grid */}
      {historyTotalPages > 1 && (
        <div className="pagination" style={{ marginTop: '24px' }}>
          {historyPage > 2 && (
            <button onClick={() => setHistoryPage(0)}>1</button>
          )}
          
          {historyPage > 3 && <span>...</span>}

          <button 
            disabled={historyPage === 0}
            onClick={() => setHistoryPage(prev => prev - 1)}
          >
            Précédent
          </button>

          {getPageNumbers().map((index) => (
            <button
              key={index}
              className={historyPage === index ? "active" : ""}
              onClick={() => setHistoryPage(index)}
            >
              {index + 1}
            </button>
          ))}

          <button 
            disabled={historyPage >= historyTotalPages - 1}
            onClick={() => setHistoryPage(prev => prev + 1)}
          >
            Suivant
          </button>

          {historyPage < historyTotalPages - 4 && <span>...</span>}

          {historyPage < historyTotalPages - 3 && (
            <button onClick={() => setHistoryPage(historyTotalPages - 1)}>{historyTotalPages}</button>
          )}
        </div>
      )}
    </>
  );
}