import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RequestsView } from '../components/Views';
import { useAuth } from '../context/AuthContext';
import requestService from '../service/requestService';

export const normalizeRequest = (request) => {
  const employeeName = request.employeeDTO 
    ? `${request.employeeDTO.firstName} ${request.employeeDTO.lastName}` 
    : `${request.firstName || ''} ${request.lastName || ''}`.trim();
    
  // FIXED: Optional chaining (?.) prevents crashes if employeeDTO is null
  const remainingdays = request.employeeDTO?.remainingVacationDays;
  let approved = "PENDING"
  if(request.approvedByResponsible) {
    approved="APPROVED";
  } else if(request.approvedByResponsible === null) {
    approved = "PENDING";
  } else {
    approved = "REJECTED";
  }
    
  return {
    ...request,
    id: request.id ?? request.requestId ?? request.requestID,
    name: employeeName,
    employeeName,
    firstName: request.firstName,
    lastName: request.lastName,
    from: request.fromDate || request.from || '',
    to: request.toDate || request.to || '',
    days: request.numberOfDays || request.days || 0,
    status: request.status || 'Pending',
    remainingVacationDays: remainingdays,
    approvedByResponsible: approved
  };
};

export default function RequestsPage() {
  const [requests, setRequests] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState(null); 
  
  const navigate = useNavigate();
  const { user } = useAuth();
  
  // SINGLE useEffect handles all data fetching. 
  useEffect(() => {
    loadRequests(currentPage, status);
  }, [currentPage, status]);

  const loadRequests = async (pageToLoad, currentStatus) => {
    setLoading(true);
    try {
      // Passed the "currentStatus" parameter down to the API
      const response = user.role === 'ROLE_ADMIN' ? await requestService.getAllRequestsByStatus(pageToLoad, 10, currentStatus): await requestService.getAllRequestsByStatusByUserDepartment(pageToLoad, 10, currentStatus);
      
      setRequests(response?.content?.map(normalizeRequest) || []);
      setTotalPages(response?.page.totalPages || 0);
    } catch (error) {
      console.error('Error fetching requests:', error);
      setRequests([]);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusFilterChange = (newStatus) => {
    const safeStatus = newStatus === "All" ? null : newStatus;
    setStatus(safeStatus);
    setCurrentPage(0); // This single reset is all you need to trigger the useEffect correctly
  };

  const getPageNumbers = () => {
    const maxButtons = 5; 
    let start = Math.max(0, currentPage - 2);
    let end = Math.min(totalPages, start + maxButtons);

    if (end - start < maxButtons) {
      start = Math.max(0, end - maxButtons);
    }

    const pages = [];
    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  };
  
  const handleUpdate = (id, newStatus) => {
    setRequests((prev) => prev.map((r) => (r.id === id ? { ...r, status: newStatus } : r)));
  };

  const handleSelect = (request) => {
    const requestId = request?.id ?? requests.find((r) => r.from === request?.from && r.to === request?.to && r.name === request?.name)?.id;
    if (!requestId) return;

    const selectedRequest = requests.find((r) => r.id === requestId) || request;
    navigate(`/requests/${requestId}`, { state: { request: selectedRequest, requests } });
  };

  return (
    <>
      {loading ? (
        <div className="loading-spinner">Chargement des demandes...</div>
      ) : (
        <RequestsView 
          requests={requests} 
          currentStatus={status} 
          onUpdate={handleUpdate} 
          onSelect={handleSelect} 
          onFilterChange={handleStatusFilterChange}
          role={user?.role} 
        />
      )}
      
      {/* FIXED: Changed to > 0 so the bar shows even if there is only 1 page */}
      {totalPages > 0 && (
        <div className="pagination">
          {currentPage > 2 && <button onClick={() => setCurrentPage(0)}>1</button>}
          {currentPage > 3 && <span>...</span>}

          <button disabled={currentPage === 0} onClick={() => setCurrentPage(prev => prev - 1)}>
            Précédent
          </button>

          {getPageNumbers().map((index) => (
            <button
              key={index}
              className={currentPage === index ? "active" : ""}
              onClick={() => setCurrentPage(index)}
            >
              {index + 1}
            </button>
          ))}

          <button disabled={currentPage >= totalPages - 1} onClick={() => setCurrentPage(prev => prev + 1)}>
            Suivant
          </button>

          {currentPage < totalPages - 4 && <span>...</span>}
          {currentPage < totalPages - 3 && <button onClick={() => setCurrentPage(totalPages - 1)}>{totalPages}</button>}
        </div>
      )}
    </>
  );
}