import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { MyProfileView } from '../components/Views';
import { getMyProfile } from '../service/employeeService';
import { useForm } from 'react-hook-form';
import requestService, { getRequestByEmployeeId } from '../service/requestService';

export default function ProfilePage() {
  const { user } = useAuth();
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // --- Pagination States ---
  const [historyPage, setHistoryPage] = useState(0);
  const [historyTotalPages, setHistoryTotalPages] = useState(0);
  const [historyLoading, setHistoryLoading] = useState(false);
  
  // --- Modal States ---
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingRequest, setEditingRequest] = useState(null);
  const [editError, setEditError] = useState('');

  // --- React Hook Form Setup ---
  const {
    register,
    handleSubmit,
    watch,
    reset,
    setValue,
    formState: { isValid, isSubmitting, errors }
  } = useForm({
    mode: 'onChange',
    defaultValues: {
      fromDate: '',
      toDate: '',
      reason: '',
      halfDayType: 'FULL_DAY'
    }
  });

  const watchedFromDate = watch("fromDate");
  const watchedHalfDayType = watch("halfDayType");
  const isHalfDay = watchedHalfDayType && watchedHalfDayType !== 'FULL_DAY';

  // Sync toDate with fromDate when switching to a half day
  useEffect(() => {
    if (isHalfDay && watchedFromDate) {
      setValue("toDate", watchedFromDate, { shouldValidate: true });
    }
  }, [isHalfDay, watchedFromDate, setValue]);

  // 1. Initial Load (Profile + Page 0 of Requests)
  useEffect(() => {
    const loadProfileAndRequests = async () => {
      try {
        setLoading(true);
        
        const data = await getMyProfile();
        const requestsData = await getRequestByEmployeeId(0, 10, data.id);

        setHistoryTotalPages(requestsData?.page?.totalPages || 0);

        const profile = {
          id: data.id,
          firstName: data.firstName || '',
          lastName: data.lastName || '',
          email: data.email || '',
          phone: data.phone || '',
          hireDate: data.createdAt ? new Date(data.createdAt).toLocaleDateString('fr-FR') : 'N/A',
          totalDays: Number((data.thisYearVacationDays || 0) + (data.lastYearVacationDays || 0) - (data.lastYearUsedVacationDays || 0) - (data.usedVacationDays || 0)),
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
    if (loading || !profileData?.id) return; 

    let isMounted = true;

    const loadJustHistory = async () => {
      try {
        setHistoryLoading(true);
        const requestsData = await getRequestByEmployeeId(historyPage, 10, profileData.id);
        
        if (!isMounted) return;

        setHistoryTotalPages(requestsData?.page?.totalPages || 0);
        
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
        if (isMounted) setHistoryLoading(false);
      }
    };

    loadJustHistory();

    return () => {
      isMounted = false;
    };
  }, [historyPage, loading, profileData?.id]);

  const openEditModal = (request) => {
    setEditingRequest(request);
    // Use hook-form's reset to populate default values dynamically
    reset({
      fromDate: request.fromDate || request.from || '',
      toDate: request.toDate || request.to || '',
      reason: request.reason || '',
      halfDayType: request.halfDayType || 'FULL_DAY'
    });
    setEditError('');
    setIsEditModalOpen(true);
  };

  const closeEditModal = () => {
    setIsEditModalOpen(false);
    setEditingRequest(null);
    setEditError('');
  };

  const onSubmit = async (data) => {
    if (!editingRequest) return;

    const halfDayType = data.halfDayType || 'FULL_DAY';
    const normalizedFrom = data.fromDate;
    const normalizedTo = halfDayType === 'FULL_DAY' ? data.toDate : data.fromDate;
    const payloadStatus = String(editingRequest.status || 'PENDING').toUpperCase();

    setEditError('');

    try {
      const updatedRequest = await requestService.updateRequest({
        id: editingRequest.id,
        fromDate: normalizedFrom,
        toDate: normalizedTo,
        reason: data.reason,
        halfDayType,
        status: payloadStatus
      });

      setProfileData(prev => ({
        ...prev,
        vacationHistory: (prev?.vacationHistory || []).map((req) => req.id === updatedRequest.id ? ({
          ...req,
          fromDate: updatedRequest.from || updatedRequest.fromDate || req.fromDate,
          toDate: updatedRequest.to || updatedRequest.toDate || req.toDate,
          days: updatedRequest.days || req.days,
          status: updatedRequest.status || req.status
        }) : req)
      }));

      closeEditModal();
    } catch (err) {
      setEditError(err.response?.data?.message || err.message || 'La modification de la demande a échoué.');
    }
  };

  if (loading) return <div>Chargement du profil...</div>;
  if (error) return <div className="alert-error">{error}</div>;

  return (
    <>
      <MyProfileView 
        currentUser={user} 
        userProfileData={profileData} 
        historyPage={historyPage}
        historyTotalPages={historyTotalPages}
        setHistoryPage={setHistoryPage}
        historyLoading={historyLoading}
        onEditRequest={openEditModal}
      />

      {isEditModalOpen && (
        <div className="modal-overlay" onClick={closeEditModal}>
          <div className="modal-card request-modal-card" onClick={(event) => event.stopPropagation()}>
            
            <div className="modal-header">
              <div>
                <h2 className="section-title" style={{ margin: 0 }}>Modifier la demande</h2>
                <p className="text-muted view-subtitle" style={{ marginTop: '6px' }}>
                  Modifiez les dates et le type de votre demande avant validation.
                </p>
              </div>
              <button type="button" className="btn-text" onClick={closeEditModal}>Fermer</button>
            </div>

            {/* Form is now handled entirely by react-hook-form's handleSubmit */}
            <form className="flex-col gap-5" onSubmit={handleSubmit(onSubmit)}>
              <div className="date-grid">
                <div>
                  <label className="field-label">Du</label>
                  <input
                    type="date"
                    className="input-field"
                    {...register("fromDate", { required: true })}
                  />
                </div>
                <div>
                  <label className="field-label">Au</label>
                  <input
                    type="date"
                    className={`input-field ${errors.toDate ? 'input-error' : ''}`}
                    {...register("toDate", { 
                      required: true,
                      validate: value => !watchedFromDate || value >= watchedFromDate || "La date de fin doit être après la date de début"
                    })}
                    disabled={isHalfDay}
                  />
                  {errors.toDate && <span className="error-text" style={{color: 'red', fontSize: '12px'}}>{errors.toDate.message}</span>}
                </div>
              </div>

              <div>
                <label className="field-label">Type de demande</label>
                <select className="input-field" {...register("halfDayType", { required: true })}>
                  <option value="FULL_DAY">Journée complète</option>
                  <option value="AM">Demi-journée (matin)</option>
                  <option value="PM">Demi-journée (après-midi)</option>
                </select>
              </div>

              <div>
                <label className="field-label">Motif</label>
                <select className="input-field" {...register("reason", { required: true })}>
                  <option value="">Sélectionner un motif…</option>
                  <option value="Vacances">Congés payés</option>
                  <option value="Maladie">Maladie</option>
                  <option value="Personnel">Personnel</option>
                </select>
              </div>

              {editError && <div className="alert-error">{editError}</div>}

              <div className="flex-row gap-2 justify-between">
                <button type="button" className="btn-text" onClick={closeEditModal}>Annuler</button>
                <button 
                  type="submit" 
                  className="btn-primary" 
                  disabled={!isValid || isSubmitting} 
                  style={{ width: 'auto' }}
                >
                  {isSubmitting ? 'Enregistrement...' : 'Enregistrer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}