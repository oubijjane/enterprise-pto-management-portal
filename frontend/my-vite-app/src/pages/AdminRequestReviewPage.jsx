import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import requestService from '../service/requestService';
import { useAuth } from '../context/AuthContext';
import { RequestRow } from '../components/Shared';
import holidayService from '../service/holidayService';
import { normalizeRequest } from './RequestsPage';

// --- Utility Functions ---

const parseDate = (value) => {
  const date = new Date(value);
  return Number.isNaN(date.valueOf()) ? null : date;
};

// Updated to fr-FR for French date formatting
const formatDate = (value) => {
  const date = parseDate(value);
  if (!date) return value || '--';
  return date.toLocaleDateString('fr-FR', { month: 'short', day: 'numeric', year: 'numeric' });
};

// Format date with time (hour and minute)
const formatDateWithTime = (value) => {
  const date = parseDate(value);
  if (!date) return value || '--';
  const dateStr = date.toLocaleDateString('fr-FR', { month: 'short', day: 'numeric', year: 'numeric' });
  const timeStr = date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  return `${dateStr} à ${timeStr}`;
};

const dateKey = (date) => date.toISOString().slice(0, 10);

const isOverlapping = (a, b) => {
  const aStart = parseDate(a.from);
  const aEnd = parseDate(a.to);
  const bStart = parseDate(b.from);
  const bEnd = parseDate(b.to);
  if (!aStart || !aEnd || !bStart || !bEnd) return false;
  return aStart <= bEnd && bStart <= aEnd;
};

const daysBetween = (start, end) => {
  const a = parseDate(start);
  const b = parseDate(end);
  if (!a || !b) return 0;
  return Math.round((b - a) / (1000 * 60 * 60 * 24)) + 1;
};

const buildCalendarRange = (requests) => {
  const dates = requests
    .flatMap((req) => [parseDate(req.from), parseDate(req.to)])
    .filter(Boolean);

  if (dates.length === 0) return { start: new Date(), end: new Date() };

  const start = new Date(Math.min(...dates.map((d) => d.valueOf())));
  const end = new Date(Math.max(...dates.map((d) => d.valueOf())));

  return { start, end };
};

const createCalendarDays = (start, end) => {
  const days = [];
  const current = new Date(start);

  while (current <= end && days.length < 90) {
    days.push(new Date(current));
    current.setDate(current.getDate() + 1);
  }

  return days;
};

const buildCalendarWeeks = (days) => {
  if (!days || days.length === 0) return [];

  const firstDayIndex = (days[0].getDay() + 6) % 7;
  const cells = Array(firstDayIndex).fill(null).concat(days);
  const remainder = cells.length % 7;
  const padding = remainder === 0 ? 0 : 7 - remainder;
  for (let i = 0; i < padding; i += 1) cells.push(null);

  const weeks = [];
  for (let i = 0; i < cells.length; i += 7) {
    weeks.push(cells.slice(i, i + 7));
  }

  return weeks;
};

// Translated days to French
const dayNames = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

const getHalfDayLabel = (halfDayType) => {
  if (!halfDayType || halfDayType === 'FULL_DAY') return null;
  if (halfDayType === 'AM') return 'Demi-journée (matin)';
  if (halfDayType === 'PM') return 'Demi-journée (après-midi)';
  return halfDayType;
};

// --- Main Component ---
export default function AdminRequestReviewPage() {
  const { requestId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const role = user?.roles?.[0] || null;
  
  const [requests, setRequests] = useState([]);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [departmentRequests, setDepartmentRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusUpdating, setStatusUpdating] = useState(false);
  const [departmentRequestsLoading, setDepartmentRequestsLoading] = useState(false);
  
  const isPending = selectedRequest?.status?.toLowerCase() === 'pending';
  useEffect(() => {
    const loadAll = async () => {
      try {
        const all = await requestService.getNonRejectedRequests(0, 500);
        setRequests(all?.content?.map(normalizeRequest) || []);
        setTotalPages(all?.totalPages || 0);
      } catch (error) {
        console.error('Failed to load requests list:', error);
      } finally {
        setLoading(false);
      }
    };
    loadAll();
  }, []); 

  useEffect(() => {
    const loadSingle = async () => {
      try {
        const request = await requestService.getRequestById(requestId);
        setSelectedRequest(normalizeRequest(request));
      } catch (error) {
        console.error('Failed to load specific request:', error);
      }
    };
  
    loadSingle(); 
  }, [requestId]);

  useEffect(() => {
    const loadDepartmentRequests = async () => {
      if (!selectedRequest?.from || !selectedRequest?.to) {
        setDepartmentRequests([]);
        return;
      }

      try {
        setDepartmentRequestsLoading(true);
        const response = await requestService.getAllRequestsUserDepartmentbyStatus(0, 100, requestId);
        const list = Array.isArray(response) ? response : response?.content || [];
        setDepartmentRequests(list.map(normalizeRequest));
      } catch (error) {
        console.error('Failed to load department requests:', error);
        setDepartmentRequests([]);
      } finally {
        setDepartmentRequestsLoading(false);
      }
    };

    loadDepartmentRequests();
  }, [selectedRequest?.from, selectedRequest?.to]);

  const handleStatusChange = async (newStatus) => {
    if (!selectedRequest || statusUpdating) return;
    setStatusUpdating(true);
    let finalUpdatedRequest;
    try {
      if(role === "ROLE_ADMIN") {
      const updatedRequest = newStatus === 'Approved'
        ? await requestService.approveRequest(selectedRequest.id)
        : await requestService.rejectRequest(selectedRequest.id);
        finalUpdatedRequest = updatedRequest;
      }else if(role === "ROLE_MANAGER") {
         const updatedRequest = newStatus === 'Approved'
        ? await requestService.approveByManger(selectedRequest.id)
        : await requestService.rejectByManger(selectedRequest.id);
        finalUpdatedRequest = updatedRequest;
      }
      const normalizedUpdate = normalizeRequest(finalUpdatedRequest);
      setRequests(requests.map((req) => req.id === selectedRequest.id ? normalizedUpdate : req));
      setSelectedRequest(normalizedUpdate)
    } catch (error) {
      console.error('Failed to update request status:', error);
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleSelectRequest = (req) => {
    if (!req?.id) return;
    navigate(`/requests/${req.id}`, { state: { request: req, requests } });
  };

  const otherRequests = useMemo(() => {
    if (!selectedRequest) return [];
    return departmentRequests
      .filter((req) => req.id !== selectedRequest.id)
      .sort((a, b) => new Date(a.from) - new Date(b.from));
  }, [departmentRequests, selectedRequest]);

  const overlappingRequests = useMemo(() => {
    if (!selectedRequest) return [];
    return otherRequests.filter((req) => isOverlapping(req, selectedRequest));
  }, [otherRequests, selectedRequest]);

  const calendarRequests = useMemo(() => {
    if (!selectedRequest) return { days: [], requests: [], totalDays: 0 };
    const items = [selectedRequest, ...overlappingRequests];
    const { start, end } = buildCalendarRange(items);
    return { days: createCalendarDays(start, end), requests: items, start, end, totalDays: daysBetween(start, end) };
  }, [selectedRequest, overlappingRequests]);

  const calendarWeeks = useMemo(() => buildCalendarWeeks(calendarRequests.days), [calendarRequests.days]);
  
  const [holidays, setHolidays] = useState([]);
  const startStr = calendarRequests.start ? dateKey(calendarRequests.start) : null;
  const endStr = calendarRequests.end ? dateKey(calendarRequests.end) : null;
  
  useEffect(() => {
    const loadHolidays = async () => {
      if (!startStr || !endStr) return;
      const holidaysData = await holidayService.getAllHolidaysInBetween(startStr, endStr);
      setHolidays(holidaysData);
    };
    loadHolidays();
  }, [startStr, endStr]);

  const isApprovalEnabled = selectedRequest 
    ? (selectedRequest.remainingVacationDays >= selectedRequest.days)
    : false;

  // --- Render ---
  if (loading) return <div className="request-detail-page"><div className="card">Chargement des détails de la demande…</div></div>;
  if (!selectedRequest) return <div className="request-detail-page"><div className="card"><p className="text-muted">Demande introuvable.</p><button className="btn-text" onClick={() => navigate('/requests')}>Retour aux demandes</button></div></div>;

  return (
    <div className="request-detail-page">
      <div className="view-header" style={{ marginBottom: '24px' }}>
        <h1>Validation administrateur</h1>
        <p className="text-muted view-subtitle">Approuvez ou rejetez cette demande et inspectez les demandes proches pour détecter les chevauchements.</p>
      </div>

      <TargetRequestCard 
        request={selectedRequest} 
        isPending={isPending} 
        statusUpdating={statusUpdating} 
        onStatusChange={handleStatusChange} 
        onBack={() => navigate('/requests')} 
        role={role}
        enable={isApprovalEnabled} 
      />

      <CalendarPanel 
        calendarWeeks={calendarWeeks} 
        calendarRequests={calendarRequests} 
        selectedRequest={selectedRequest} 
        overlappingRequests={overlappingRequests} 
        onSelectRequest={handleSelectRequest} 
        holidays={holidays} 
      />

      {overlappingRequests.length > 0 && (
        <OverlappingRequestsList 
          overlappingRequests={overlappingRequests} 
          onSelectRequest={handleSelectRequest} 
        />
      )}
    </div>
  );
}

// --- Sub-Component 1: Target Request Card ---
const TargetRequestCard = ({ request, isPending, statusUpdating, onStatusChange, onBack, role, enable, approvedByManager }) => (
  <div className="review-card">
    <div className="review-card-header">
      <div>
        <p className="review-label text-sm">Examen de la demande</p>
        <h2 className="section-title review-title">{request.name}</h2>
        <p className="text-muted">
          {formatDate(request.from)} → {formatDate(request.to)} · {request.days} jour(s)
          {getHalfDayLabel(request.halfDayType) ? ` · ${getHalfDayLabel(request.halfDayType)}` : ''}
        </p>
      </div>
      
      <div className="review-actions" style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        {isPending && (role === "ROLE_ADMIN" || (role === "ROLE_MANAGER" && request.approvedByResponsible === "PENDING"))  ? (   
          <>
            {enable ? (
              <button className="review-btn approve" onClick={() => onStatusChange('Approved')} disabled={statusUpdating}>
                {statusUpdating ? 'Approbation…' : 'Approuver'}
              </button> 
            ) : (
              <span style={{ color: '#d97706', fontSize: '0.875rem', fontWeight: '500' }}>
                ⚠️ Jours insuffisants ({request.remainingVacationDays || 0} restants)
              </span>
            )}
            <button className="review-btn reject" onClick={() => onStatusChange('Rejected')} disabled={statusUpdating}>
              {statusUpdating ? 'Rejet…' : 'Rejeter'}
            </button>
          </>
        ) : (
          <div className="review-status-text">
            <span>La demande est {request.status || 'mise à jour'}.</span>
          </div>
        )}
        <button className="review-btn-back" onClick={onBack}>Retour</button>
      </div>
    </div>

    <div className="details-grid">
      <div className="detail-col">
        <div className="review-label">Statut</div>
        <div className={`status-badge status-${request.status}`}>{request.status}</div>
      </div>
      <div className="detail-col">
        <div className="review-label">approuvé par le responsable</div>
        <div className={`status-badge status-${request.approvedByResponsible}`}>{request.approvedByResponsible}</div>
      </div>
      <div className="detail-col">
        <div className="review-label">Motif</div>
        <div>{request.reason || 'Aucun motif fourni'}</div>
      </div>
      <div className="detail-col">
        <div className="review-label">Soumise le</div>
        <div>{formatDateWithTime(request.submittedAt || request.createdAt || '')}</div>
      </div>
    </div>
  </div>
);

// --- Utility Helper for Multi-Day Holidays ---
const findHolidayForDay = (day, holidays) => {
  if (!day || !holidays) return null;
  const targetStr = dateKey(day);

  return holidays.find(h => {
    const duration = h.numberOfDays ? Number(h.numberOfDays) : 1;
    
    if (duration <= 1) return h.date === targetStr;

    const start = parseDate(h.date);
    if (!start) return false;

    for (let i = 0; i < duration; i++) {
      const dayOfHoliday = new Date(start);
      dayOfHoliday.setDate(start.getDate() + i);
      
      if (dateKey(dayOfHoliday) === targetStr) {
        return true; 
      }
    }
    return false;
  });
};

// --- Sub-Component 2: Calendar Panel ---
const CalendarPanel = ({ calendarWeeks, calendarRequests, selectedRequest, overlappingRequests, onSelectRequest, holidays }) => {
  const colorPalette = [
    'rgba(59, 130, 246, 0.9)',   // 0: Target Request (Blue)
    'rgba(239, 68, 68, 0.85)',   // 1: Overlap 1 (Red)
    'rgba(16, 185, 129, 0.85)',  // 2: Overlap 2 (Emerald)
    'rgba(245, 158, 11, 0.85)',  // 3: Overlap 3 (Orange)
    'rgba(139, 92, 246, 0.85)',  // 4: Overlap 4 (Purple)
    'rgba(236, 72, 153, 0.85)',  // 5: Overlap 5 (Pink)
    'rgba(6, 182, 212, 0.85)'    // 6: Overlap 6 (Cyan)
  ];

  return (
    <div className="calendar-panel">
      <div className="calendar-header">
        <div>
          <h2 className="section-title calendar-title">
            {calendarWeeks[0]?.find(d => d)?.toLocaleString('fr-FR', { month: 'long', year: 'numeric' }) || 'Calendrier mensuel'}
          </h2>
          <p className="text-muted calendar-subtitle">Visualisation des chevauchements sur la période demandée.</p>
        </div>
        <div>
           <span className="overlap-badge">
             {overlappingRequests.length} demande{overlappingRequests.length === 1 ? '' : 's'} en chevauchement
           </span>
        </div>
      </div>

      <div className="calendar-body">
        <div className="calendar-days-row" style={{ display: 'grid', gridTemplateColumns: 'repeat(7, minmax(0, 1fr))', width: '100%' }}>
          {dayNames.map((d, i) => <div key={i} className="calendar-day-name">{d}</div>)}
        </div>

        {calendarWeeks.map((week, wIndex) => (
          <div key={wIndex} className="calendar-week-row" style={{ position: 'relative', width: '100%' }}>
            
            {/* --- LOCKED Background Grid --- */}
            <div 
              className="calendar-bg-grid"
              style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(7, minmax(0, 1fr))', 
                width: '100%' 
              }}
            >
              {week.map((day, dIndex) => {
                const holiday = findHolidayForDay(day, holidays);
                const isSunday = dIndex === 6; 

                return (
                  <div 
                    key={dIndex} 
                    className="calendar-bg-cell" 
                    style={{ 
                      backgroundColor: holiday ? 'rgba(239, 68, 68, 0.1)' : (isSunday ? '#f3f4f6' : 'transparent'),
                      position: 'relative'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', position: 'relative', zIndex: 50, padding: '2px 4px' }}>
                      <span className={`calendar-date-number ${day ? 'valid' : 'invalid'}`}>
                        {day ? day.getDate() : ''}
                      </span>
                      {holiday && (
                        <span 
                          style={{ 
                            color: 'rgba(239, 68, 68, 0.9)', fontSize: '0.65rem', fontWeight: '600',
                            whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
                            marginLeft: '8px', textAlign: 'right'
                          }}
                          title={holiday.name} 
                        >
                          {holiday.name} {holiday.status === 'TENTATIVE' && <span style={{ opacity: 0.75 }}>(Est.)</span>}
                        </span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* --- LOCKED Foreground Grid --- */}
            <div 
              className="calendar-fg-grid" 
              style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(7, minmax(0, 1fr))', 
                width: '100%', 
                paddingTop: '30px', 
                gridAutoRows: '28px' 
              }}
            >
              {calendarRequests.requests.map((req, rIndex) => {
                const validDays = week.filter(d => d);
                if (validDays.length === 0) return null;
                
                const weekStart = new Date(validDays[0]); weekStart.setHours(0,0,0,0);
                const weekEnd = new Date(validDays[validDays.length - 1]); weekEnd.setHours(23,59,59,999);
                const reqStart = parseDate(req.from); if (reqStart) reqStart.setHours(0,0,0,0);
                const reqEnd = parseDate(req.to); if (reqEnd) reqEnd.setHours(23,59,59,999);

                if (!reqStart || !reqEnd || reqEnd < weekStart || reqStart > weekEnd) return null;

                const isSameDay = (d1, d2) => d1 && d2 && d1.getFullYear() === d2.getFullYear() && d1.getMonth() === d2.getMonth() && d1.getDate() === d2.getDate();

                const startCol = (reqStart > weekStart) ? week.findIndex(d => isSameDay(d, reqStart)) + 1 : week.findIndex(d => isSameDay(d, validDays[0])) + 1;
                const endCol = (reqEnd < weekEnd) ? week.findIndex(d => isSameDay(d, reqEnd)) + 1 : week.findIndex(d => isSameDay(d, validDays[validDays.length - 1])) + 1;
                const span = endCol - startCol + 1;
                
                if (span <= 0 || startCol === 0) return null;

                const isSelected = req.id === selectedRequest.id;
                const bg = colorPalette[rIndex % colorPalette.length];

                return (
                  <div 
                    key={`${req.id}-${wIndex}`} 
                    onClick={() => onSelectRequest(req)}
                    className="calendar-pill"
                    style={{ gridColumn: `${startCol} / span ${span}`, gridRow: rIndex + 1, backgroundColor: bg, zIndex: isSelected ? 20 : 10, opacity: 0.9 }}
                  >
                    {req.name} {span >= 2 ? `(${req.days}j)` : ''}
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

// --- Sub-Component 3: Overlapping Requests List ---
const OverlappingRequestsList = ({ overlappingRequests, onSelectRequest }) => (
  <div className="overlap-list-card">
    <h2 className="section-title overlap-list-title">Détails des chevauchements</h2>
    <div className="overlap-list-container">
      {overlappingRequests.map((req) => (
        <div key={req.id} className="overlap-item" onClick={() => onSelectRequest(req)}>
          <div>
            <p className="overlap-item-title"><strong>{req.name}</strong> · {formatDate(req.from)} → {formatDate(req.to)}</p>
            <p className="text-muted overlap-item-subtitle">{req.days} jour(s) · {req.status}</p>
          </div>
          <div><span className={`status-badge status-${req.status}`}>{req.status}</span></div>
        </div>
      ))}
    </div>
  </div>
);