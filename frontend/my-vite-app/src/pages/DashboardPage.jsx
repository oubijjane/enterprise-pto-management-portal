import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllEmployees } from '../service/employeeService';
import requestService from '../service/requestService';
import { useAuth } from '../context/AuthContext';
import { DashboardView } from '../components/Views';

export default function DashboardPage() {
  const [requests, setRequests] = useState([]);
  const [approvedRequests, setApprovedRequests] = useState({ content: [] });
  const [employees, setEmployees] = useState([]);
  const [approvedPage, setApprovedPage] = useState(0);
  const [approvedTotalPages, setApprovedTotalPages] = useState(0);
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

  useEffect(() => {
    const loadApprovedRequests = async () => {
      try {
        const response = await requestService.getCurrentApprovedRequests(approvedPage, 7);
        setApprovedRequests(response);
        setApprovedTotalPages(response?.page?.totalPages || 0);
      } catch (err) {
        console.error('Dashboard approved requests fetch failed:', err);
        setApprovedRequests({ content: [] });
        setApprovedTotalPages(0);
      }
    };

    loadApprovedRequests();
  }, [approvedPage]);

  const handleUpdate = (id, newStatus) => {
    setRequests(requests.map(r => r.id === id ? { ...r, status: newStatus } : r));
  };

  const handleSelectApprovedRequest = (request) => {
    if (request?.id != null) {
      navigate(`/requests/${request.id}`);
    }
  };

  return (
    <DashboardView
      requests={requests}
      approvedRequests={approvedRequests}
      approvedPage={approvedPage}
      approvedTotalPages={approvedTotalPages}
      setApprovedPage={setApprovedPage}
      onSelectApprovedRequest={handleSelectApprovedRequest}
      employees={employees}
      onUpdate={handleUpdate}
      pendingCount={pendingCount}
      approvedCount={approvedCount}
      setView={(view) => navigate(`/${view === 'requests' ? 'requests' : view}`)}
    />
  );
}
