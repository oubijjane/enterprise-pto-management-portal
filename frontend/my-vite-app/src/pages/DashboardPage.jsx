import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllEmployees } from '../service/employeeService';
import requestService from '../service/requestService';
import { useAuth } from '../context/AuthContext';
import { DashboardView } from '../components/Views';

export default function DashboardPage() {
  const [requests, setRequests] = useState([]);
  const [employees, setEmployees] = useState([]);
  const navigate = useNavigate();
  const { user } = useAuth();
  const [pendingCount, setPendingCount] = useState();
  const [approvedCount, setApprovedCount] = useState();

  useEffect(() => {
    const loadData = async () => {
      try {
        const employeesResponse = user.role === 'ROLE_EMPLOYEE' ? [] : await getAllEmployees();
        setEmployees(employeesResponse);
        const requestsResponse =  user.role === 'ROLE_ADMIN' ? await requestService.getAllRequestsByStatus(): await requestService.getAllRequestsByStatusByUserDepartment();
        setRequests(requestsResponse);
      } catch (err) {
        console.error('Dashboard employee fetch failed:', err);
      }
    };
    const loadCounts = async () => {
      try {
        const pendingResponse = await requestService.countRequestByStatus('PENDING');
        const approvedResponse = await requestService.countRequestByStatus('APPROVED');
        setPendingCount(pendingResponse.data);
        setApprovedCount(approvedResponse.data);
      } catch (err) {
        console.error('Dashboard count fetch failed:', err);
      }
    };
    loadCounts();

    loadData();
  }, []);

  const handleUpdate = (id, newStatus) => {
    setRequests(requests.map(r => r.id === id ? { ...r, status: newStatus } : r));
  };

  return (
    <DashboardView
      requests={requests}
      employees={employees}
      onUpdate={handleUpdate}
      pendingCount={pendingCount}
      approvedCount={approvedCount}
      setView={(view) => navigate(`/${view === 'requests' ? 'requests' : view}`)}
    />
  );
}
