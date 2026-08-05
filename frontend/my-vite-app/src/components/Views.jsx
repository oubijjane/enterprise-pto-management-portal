import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Field, Stat, RequestRow } from './Shared';
import { addNewEmployee, getMyProfile } from '../service/employeeService';

export function DashboardView({ requests, employees, onUpdate, setView, pendingCount, approvedCount }) {
  const totalPending = pendingCount ;
  const totalApproved = approvedCount ;

  return (
    <div>
      <div className="view-header">
        <h1>Bonjour 👋</h1>
        <p className="text-muted view-subtitle">Voici l'actualité des congés de votre équipe.</p>
      </div>

      <div className="stats-grid">
        <div className="card">
          <p className="field-label">En attente d'examen</p>
          <p className="text-mono stat-large" style={{ color: "#F59E0B" }}>{totalPending}</p>
        </div>
        <div className="card">
          <p className="field-label">Approuvés cette année</p>
          <p className="text-mono stat-large" style={{ color: "#10B981" }}>{totalApproved}</p>
        </div>
      </div>

      <div className="card">
        <div className="flex-row justify-between" style={{ marginBottom: "20px" }}>
          <h2 className="section-title" style={{ margin: 0 }}>Demandes récentes</h2>
          <button onClick={() => setView("requests")} className="btn-text">Voir tout →</button>
        </div>
        <div className="flex-col">
          {requests.content?.slice(0, 4).map((r, index) => (
            <RequestRow key={r.id != null ? r.id : `request-${index}`} r={r} onUpdate={onUpdate} compact />
          ))}
        </div>
      </div>
    </div>
  );
}

export function RequestsView({ requests, currentStatus, onUpdate, onSelect, role, onFilterChange }) {

  const statusLabels = {
    "All": "Tous",
    "PENDING": "En attente",
    "APPROVED": "Approuvé",
    "REJECTED": "Rejeté"
  };

  return (
    <div>
      <div className="flex-row justify-between" style={{ marginBottom: "24px" }}>
        <div>
          <h1>Demandes de congés</h1>
          {/* Note: If the backend filters the list, requests.length is just the items on this page (max 10). 
              If you want total database items, use a 'totalElements' variable from your API response instead. */}
          <p className="text-muted view-subtitle">{requests.length} demandes affichées</p>
        </div>

        <div className="flex-row gap-2">
          {["All", "PENDING", "APPROVED", "REJECTED"].map(s => (
            <button
              key={s}
              onClick={() => onFilterChange(s)} // FIXED: Wrapped in an arrow function to prevent render loop
              className={`btn-filter ${currentStatus === s ? 'active' : ''}`}
            >
              {statusLabels[s]}
            </button>
          ))}
        </div>
      </div>

      <div className="flex-col">
        {/* Render directly. Filtering is now handled by the API */}
        {requests.map((r, index) => (
          <RequestRow
            key={r.id != null ? r.id : `request-${index}`}
            r={r}
            onUpdate={onUpdate}
            onSelect={onSelect}
            role={role}
          />
        ))}
      </div>
    </div>
  );
}

export function NewRequestView({ employees, onAddRequest }) {
  const navigate = useNavigate();

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
      employeeId: "",
      from: "",
      to: "",
      reason: "",
      halfDayType: "FULL_DAY"
    }
  });

  const [submitted, setSubmitted] = useState(false);
  const [apiError, setApiError] = useState(null);

  const watchedEmployeeId = watch("employeeId");
  const watchedFrom = watch("from");
  const watchedTo = watch("to");
  const watchedHalfDayType = watch("halfDayType");

  const isHalfDay = watchedHalfDayType && watchedHalfDayType !== "FULL_DAY";

  // Improved date calculation: handles invalid chronological order
  const getDaysBetween = (from, to, halfDayType) => {
    if (!from) return 0;
    
    if (halfDayType && halfDayType !== "FULL_DAY") {
      return 0.5;
    }
    
    if (!to) return 0;

    const fromDate = new Date(from);
    const toDate = new Date(to);

    if (toDate < fromDate) return 0; // Prevent negative days

    // Note: If you need to exclude weekends, you would implement a loop here 
    // to count only Mon-Fri instead of simple math.
    const diff = Math.ceil((toDate - fromDate) / (1000 * 60 * 60 * 24)) + 1;
    return diff;
  };

  useEffect(() => {
    if (isHalfDay && watchedFrom) {
      setValue("to", watchedFrom, { shouldValidate: true });
    }
  }, [isHalfDay, watchedFrom, setValue]);

  const requestedDays = getDaysBetween(watchedFrom, watchedTo, watchedHalfDayType);
  const selectedEmployee = employees.find(e => e.id === parseInt(watchedEmployeeId, 10));
  
  const remainingDays = selectedEmployee
    ? (selectedEmployee.thisYearVacationDays + selectedEmployee.lastYearVacationDays) - 
      (selectedEmployee.usedVacationDays + selectedEmployee.lastYearUsedVacationDays)
    : 0;

  const onSubmit = async (data) => {
    setApiError(null);
    try {
      const normalizedData = {
        ...data,
        employeeId: parseInt(data.employeeId, 10), // Ensure integer for API
        from: isHalfDay ? data.from : data.from,
        to: isHalfDay ? data.from : data.to,
      };
      
      const normalizedDays = getDaysBetween(normalizedData.from, normalizedData.to, normalizedData.halfDayType);

      await onAddRequest(normalizedData, normalizedDays, selectedEmployee);

      setSubmitted(true);
      reset();
      setTimeout(() => setSubmitted(false), 3000);
    } catch (err) {
      setApiError(err.response?.data?.message || err.message || 'La soumission de la demande a échoué.');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-card request-modal-card">
        {/* Header logic remains the same */}
        <div className="modal-header">
          <div>
            <h2 className="section-title" style={{ margin: 0 }}>Nouvelle demande de congé</h2>
            <p className="text-muted view-subtitle" style={{ marginTop: '6px' }}>
              Soumettez une nouvelle demande de congé pour un employé.
            </p>
          </div>
          <button type="button" className="btn-text" onClick={() => navigate(-1)}>
            Fermer
          </button>
        </div>

        {submitted && (
          <div className="alert-success">
            ✓ Demande soumise avec succès !
          </div>
        )}
        {apiError && (
          <div className="alert-error">
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="flex-col gap-5">

          <Field label="Employé">
            <select
              className="input-field"
              {...register("employeeId", { required: true })}
            >
              <option value="">Sélectionner un employé…</option>
              {employees.map(e => (
                <option key={e.id} value={e.id}>{e.firstName} {e.lastName}</option>
              ))}
            </select>
          </Field>

          {selectedEmployee && (
            <div className="balance-preview">
              <Stat label="Jours totaux" value={selectedEmployee.thisYearVacationDays + selectedEmployee.lastYearVacationDays} color="#6C63FF" />
              <Stat label="Utilisés" value={selectedEmployee.usedVacationDays + selectedEmployee.lastYearUsedVacationDays} color="#F59E0B" />
              <Stat label="Restants" value={remainingDays} color="#10B981" />
            </div>
          )}

          {isHalfDay ? (
            <Field label="Date">
              <input
                type="date"
                className="input-field"
                {...register("from", { required: true })}
              />
            </Field>
          ) : (
            <div className="date-grid">
              <Field label="Du">
                <input
                  type="date"
                  className="input-field"
                  {...register("from", { required: true })}
                />
              </Field>
              <Field label="Au">
                <input
                  type="date"
                  className={`input-field ${errors.to ? 'input-error' : ''}`}
                  {...register("to", { 
                    required: true,
                    validate: value => !watchedFrom || value >= watchedFrom || "La date de fin doit être après la date de début"
                  })}
                />
                {errors.to && <span className="error-text" style={{color: 'red', fontSize: '12px'}}>{errors.to.message}</span>}
              </Field>
            </div>
          )}

          {/* Type and Reason fields remain the same */}
          <Field label="Type de demande">
            <select className="input-field" {...register("halfDayType", { required: true })}>
              <option value="FULL_DAY">Journée complète</option>
              <option value="AM">Demi-journée (matin)</option>
              <option value="PM">Demi-journée (après-midi)</option>
            </select>
          </Field>

          <Field label="Motif">
            <select className="input-field" {...register("reason", { required: true })}>
              <option value="">Sélectionner un motif…</option>
              <option value="Vacances">Congés payés</option>
              <option value="Maladie">Maladie</option>
              <option value="Personnel">Personnel</option>
            </select>
          </Field>

          <div className="flex-row justify-between gap-3">
            <button type="button" className="btn-text" onClick={() => navigate(-1)}>
              Annuler
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={!isValid || isSubmitting || requestedDays <= 0}
              style={{ width: 'auto' }}
            >
              {isSubmitting ? 'Soumission...' : 'Soumettre la demande ✈'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export const formatEmployeePayload = (data) => {
  return {
    firstName: data.firstName || null,
    lastName: data.lastName || null,
    email: data.email || null,
    loginName: data.loginName || null,
    password: data.password || null,
    phone: data.phone || null,
    hiringDate: data.hiringDate || null,
    departmentDTO: {
      // Cast to number if the API expects an integer ID
      id: data.department ? parseInt(data.department, 10) : null
    },
    role: [{
      id: data.role ? parseInt(data.role, 10) : null
    }]
  };
};

export function AddNewEmployee({ onAddEmployee, departs = [] }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid, isSubmitting }
  } = useForm({
    mode: 'onChange',
    defaultValues: {
      firstName: "",
      lastName: "",
      loginName: "",
      password: "",
      email: "",
      phone: "",
      department: "",
      hiringDate: ""
    }
  });
  const [submitted, setSubmitted] = useState(false);
  const [apiError, setApiError] = useState(null);

  const onSubmit = async (data) => {
    setApiError(null);
    try {
      const employeeData = formatEmployeePayload(data);
      await onAddEmployee(employeeData);

      setSubmitted(true);
      reset();

      setTimeout(() => setSubmitted(false), 3000);
    } catch (err) {
      setApiError(err.response?.data?.message || err.message || 'Échec de l\'ajout de l\'employé.');
    }
  };

  return (
    <div className="form-container">
      <div className="view-header">
        <h1>Ajouter un nouvel employé</h1>
        <p className="text-muted view-subtitle">Enregistrer un nouvel employé dans le système.</p>
      </div>

      {submitted && (
        <div className="alert-success">
          ✓ Employé ajouté avec succès !
        </div>
      )}
      {apiError && (
        <div className="alert-error">
          {apiError}
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} autoComplete="nope" className="card flex-col gap-5">
        <input type="text" name="hidden" style={{ display: 'none' }} autoComplete="false" />

        <div className="date-grid">
          <Field label="Prénom">
            <input
              type="text"
              className="input-field"
              {...register("firstName", { required: true })}
              autoCorrect="off"
              placeholder="ex: Zakaria"
            />
          </Field>
          <Field label="Nom">
            <input
              type="text"
              className="input-field"
              {...register("lastName", { required: true })}
              autoCorrect="off"
              placeholder="ex: Dupont"
            />
          </Field>
        </div>

        <div className="date-grid">
          <Field label="Identifiant">
            <input
              type="text"
              className="input-field"
              {...register("loginName", { required: true })}
            />
          </Field>
          <Field label="Mot de passe">
            <input
              type="password"
              className="input-field"
              autoComplete='new-password'
              {...register("password", { required: false })}
              placeholder="••••••••"
            />
          </Field>
        </div>

        <Field label="Adresse e-mail">
          <input
            type="email"
            className="input-field"
            {...register("email", { required: false })}
            placeholder="zakaria@entreprise.com"
          />
        </Field>

        <Field label="Numéro de téléphone">
          <input
            type="tel"
            className="input-field"
            {...register("phone", { required: false })}
            placeholder="06xxxxxxxx"
          />
        </Field>

        <Field label="Date d'embauche">
          <input
            type="date"
            className="input-field"
            {...register("hiringDate", { required: true })}
          />
        </Field>

        <div className="date-grid">
          <Field label="Ville">
            <select
              className="input-field"
              {...register("department", { required: true })}
            >
              <option value="">Sélectionner une ville…</option>

              {departs.map((dept) => (
                <option key={dept.id} value={dept.id}>
                  {dept.name}
                </option>
              ))}

            </select>
          </Field>
        </div>


        <button
          type="submit"
          className="btn-primary"
          disabled={!isValid || isSubmitting}
        >
          {isSubmitting ? 'Ajout en cours...' : 'Ajouter l\'employé 👤'}
        </button>
      </form>
    </div>
  );
}

export function BalancesView({ employees, searchTerm, onSearchChange }) {
  const navigate = useNavigate();

  return (
    <div className="balances-view-container">
      <div className="balances-header-wrapper">
        <div className="view-header balances-header-text">
          <h1>Soldes de congés</h1>
          <p className="text-muted view-subtitle">Aperçu des soldes de congés de tous les employés.</p>
        </div>

        {/* --- SEARCH INPUT --- */}
        <div className="search-container">
          <input
            type="text"
            placeholder="Rechercher un employé..."
            value={searchTerm}
            onChange={(e) => onSearchChange(e.target.value)}
            className="search-input"
          />
        </div>
      </div>

      {/* Table Card */}
      <div className="card table-card">
        {employees.length === 0 ? (
          <div className="empty-state">
            Aucun employé trouvé.
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Employé</th>
                  <th>Département</th>
                  <th className="col-progress">Consommation</th>
                  <th className="text-right">Solde (Jours)</th>
                </tr>
              </thead>
              <tbody>
                {employees.map((emp, index) => {
                  const totalVacation = (emp.thisYearVacationDays || 0) + (emp.lastYearVacationDays || 0);
                  const usedVacation = (emp.usedVacationDays || 0) + (emp.lastYearUsedVacationDays || 0);
                  const remainingVacation = totalVacation - usedVacation;

                  const pct = totalVacation > 0
                    ? Math.min(100, Math.round((usedVacation / totalVacation) * 100))
                    : 0;

                  // Dynamic color calculation remains in JS
                  const color = pct >= 80 ? "#EF4444" : pct >= 50 ? "#F59E0B" : "#10B981";

                  const initialFirst = emp.firstName ? emp.firstName[0].toUpperCase() : '?';
                  const initialLast = emp.lastName ? emp.lastName[0].toUpperCase() : '';

                  return (
                    <tr
                      key={emp.id}
                      onClick={() => navigate(`/employee/${emp.id}`)}
                      className="table-row"
                    >
                      {/* Name & Avatar Column */}
                      <td>
                        <div className="flex-row gap-3 items-center">
                          <div className="avatar-md avatar-small">
                            {initialFirst}{initialLast}
                          </div>
                          <span className="user-name">
                            {emp.firstName || 'Unknown'} {emp.lastName || 'Employee'}
                          </span>
                        </div>
                      </td>

                      {/* Department Column */}
                      <td className="department-cell">
                        {emp.department || '--'}
                      </td>

                      {/* Progress Bar Column */}
                      <td>
                        <div className="flex-row items-center gap-3">
                          <div className="progress-track flex-1">
                            <div
                              className="progress-fill"
                              style={{ width: `${pct}%`, backgroundColor: color }}
                            />
                          </div>
                          <span className="progress-text">
                            {pct}%
                          </span>
                        </div>
                      </td>

                      {/* Balance Column */}
                      <td className="text-right">
                        <span className="text-mono balance-text" style={{ color }}>
                          {remainingVacation}
                          <span className="balance-total">
                            / {totalVacation}
                          </span>
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
export function LoginView({ onLogin, error }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    onLogin({ username: email, password });
  };

  return (
    <div className="login-wrapper">
      <div className="card login-card">
        <div className="flex-col items-center" style={{ marginBottom: "32px", textAlign: "center" }}>
          <div className="login-icon">✈</div>
          <h1>Bon retour</h1>
          <p className="text-muted view-subtitle">Connectez-vous à TimeAway HR</p>
        </div>

        {error && <div className="alert-error" style={{ marginBottom: 16 }}>{error}</div>}

        <form onSubmit={handleSubmit} className="flex-col gap-5">
          <Field label="Adresse e-mail">
            <input type="text" className="input-field" onChange={e => setEmail(e.target.value)} placeholder="zakaria" required />
          </Field>
          <Field label="Mot de passe">
            <input type="password" className="input-field" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" required />
          </Field>
          <button type="submit" className="btn-primary" style={{ marginTop: "8px" }}>Se connecter</button>
        </form>
      </div>
    </div>
  );
}

export function EmployeeView({ userProfileData, onSave, departments, isHistoryLoading }) {
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({
    defaultValues: {
      firstName: userProfileData.firstName || null,
      lastName: userProfileData.lastName || null,
      loginName: userProfileData.loginName || null,
      accrualRatePerMonth: userProfileData.accrualRatePerMonth || 0,
      email: userProfileData.email || null,
      phone: userProfileData.phone || null,
      password: null,
      role: userProfileData.role || userProfileData.department || null,
      thisYearDays: userProfileData.thisYearDays || 0,
      lastYearDays: userProfileData.lastYearDays || 0,
      thisYearUsedDays: userProfileData.thisYearUsedDays || 0,
      lastYearUsedDays: userProfileData.lastYearUsedDays || 0,
      nextYearVacationDays: userProfileData.nextYearVacationDays || 0,
      hiringDate: userProfileData.hiringDate,
      department: userProfileData.department
    }
  });

  const onValidSubmit = async (data) => {
    try {
      const updatedEmployee = {
        ...userProfileData,
        ...data,
        name: `${data.firstName} ${data.lastName}`.trim()
      };
      await onSave(updatedEmployee);
    } catch (error) {
      console.error("Échec de l'enregistrement des données de l'employé :", error);
    }
  };

  // Safe fallback for custom Field component if it expects standard children
  const Field = ({ label, children }) => (
    <div className="field-wrapper">
      <label className="text-sm text-medium mb-1 block">{label}</label>
      {children}
    </div>
  );

  return (
    <div className="profile-container">
      <form onSubmit={handleSubmit(onValidSubmit)} autoComplete="nope">

        {/* Responsive Header Profile Section */}
        <div className="view-header profile-header-wrapper">
          <div className="avatar-lg profile-avatar">
            {userProfileData.firstName?.[0] || ''}{userProfileData.lastName?.[0] || ''}
          </div>

          <div className="profile-name-inputs">
            <div className="input-group">
              <label className="text-sm text-muted">Prénom</label>
              <input
                type="text"
                className="input-field name-input-lg"
                {...register("firstName")}
              />
            </div>
            <div className="input-group">
              <label className="text-sm text-muted">Nom</label>
              <input
                type="text"
                className="input-field name-input-lg"
                {...register("lastName")}
              />
            </div>
          </div>

          <div className="profile-save-action">
            <button
              type="submit"
              className="btn-primary w-full"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Enregistrement...' : 'Enregistrer les modifications'}
            </button>
          </div>
        </div>

        <div className="profile-layout">
          <div className="flex-col gap-6">

            {/* Personal Info Card */}
            <div className="card">
              <h2 className="section-title">Informations personnelles</h2>
              <div className="info-grid profile-info-grid">
                <div className="input-group">
                  <label className="text-sm text-medium mb-1 block">E-mail</label>
                  <input type="email" className="input-field w-full" {...register("email")} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-medium mb-1 block">Téléphone</label>
                  <input type="tel" className="input-field w-full" {...register("phone")} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-medium mb-1 block">Identifiant</label>
                  <input type="text" className="input-field w-full" autoComplete="nope" {...register("loginName")} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-medium mb-1 block">Mot de passe</label>
                  <input type="password" className="input-field w-full" autoComplete='new-password' {...register("password")} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-medium mb-1 block">Rôle</label>
                  <select className="input-field w-full" {...register("role")}>
                    <option value="0">Sélectionner un rôle…</option>
                    <option value="1">Administrateur</option>
                    <option value="2">RH</option>
                    <option value="3">Employé</option>
                    <option value="4">Manager</option>
                  </select>
                  <Field label="Ville">
                    <select
                      className="input-field"
                      {...register("department", { required: "Ce champ est obligatoire" })}
                    >
                      <option value="">Sélectionner une ville…</option>

                      {departments.map((dept) => (
                        <option key={dept.id} value={dept.id}>
                          {dept.name}
                        </option>
                      ))}

                    </select>
                  </Field>
                </div>
                <Field label="Date d'embauche">
                  <input type="date" className="input-field w-full" {...register("hiringDate", { required: false })} />
                </Field>
              </div>
            </div>

            {/* Leave History Card */}
            <div className="card">
              <h2 className="section-title">Historique des congés</h2>
              <div className="flex-col">
                {/* Assuming RequestRow is imported/defined elsewhere */}
                {(userProfileData.vacationHistory || []).map(req => (
                  <RequestRow key={req.id} r={req} compact />
                ))}
                {(!userProfileData.vacationHistory || userProfileData.vacationHistory.length === 0) && (
                  <p className="text-muted text-sm">Aucun historique de congés trouvé.</p>
                )}
              </div>
            </div>
          </div>

          <div className="flex-col gap-6">

            {/* Balances Card */}
            <div className="card card-bg-light">
              <h2 className="section-title">Soldes</h2>
              <div className="balances-input-grid">
                <div className="input-group">
                  <label className="text-sm text-bold mb-1 block label-primary">
                    Congés cette année
                  </label>
                  <input type="number" className="input-field w-full" step="0.05" {...register("thisYearDays", { valueAsNumber: true })} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-bold mb-1 block label-warning">
                    Utilisés cette année
                  </label>
                  <input type="number" className="input-field w-full" step="0.05" {...register("thisYearUsedDays", { valueAsNumber: true })} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-bold mb-1 block label-primary">
                    Congés l'année dernière
                  </label>
                  <input type="number" className="input-field w-full" step="0.5" {...register("lastYearDays", { valueAsNumber: true })} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-bold mb-1 block label-warning">
                    Utilisés l'année dernière
                  </label>
                  <input type="number" className="input-field w-full" step="0.05" {...register("lastYearUsedDays", { valueAsNumber: true })} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-bold mb-1 block label-primary">
                    Congés l'année prochaine
                  </label>
                  <input type="number" className="input-field w-full" step="0.005" {...register("nextYearVacationDays", { valueAsNumber: true })} />
                </div>
                <div className="input-group">
                  <label className="text-sm text-bold mb-1 block label-warning">
                    Taux d'acquisition (jours/mois)
                  </label>
                  <input type="number" step="0.001" className="input-field w-full" {...register("accrualRatePerMonth", { valueAsNumber: true })} />
                </div>
              </div>
            </div>

            {/* Backup Team Card */}
            <div className="card">
              <h2 className="section-title">Équipe de remplacement</h2>
              <p className="text-muted text-sm mb-4">
                Ces employés agissent comme vos remplaçants par défaut pendant votre absence.
              </p>
              <div className="flex-col gap-3">
                {(userProfileData.coverageTeam || []).map(backup => (
                  <div key={backup.id} className="backup-row flex-row items-center gap-3">
                    <div className="avatar-xs">
                      {backup.firstName[0]}{backup.lastName[0]}
                    </div>
                    <span className="text-md text-medium">{backup.firstName} {backup.lastName}</span>
                  </div>
                ))}
                {(!userProfileData.coverageTeam || userProfileData.coverageTeam.length === 0) && (
                  <p className="text-muted text-sm">Aucune équipe de remplacement assignée.</p>
                )}
              </div>
            </div>

          </div>
        </div>
      </form>
    </div>
  );
}

export function MyProfileView({
  currentUser,
  userProfileData,
  historyPage,
  historyTotalPages,
  setHistoryPage,
  historyLoading,
  onEditRequest
}) {

  // Pagination Window Logic
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

  return (
    <div className="profile-container">
      <div className="flex-row gap-6 view-header">
        <div className="avatar-lg">
          {currentUser.name.split(" ").map(n => n[0]).join("")}
        </div>
        <div>
          <h1 style={{ fontSize: "28px" }}>{currentUser.name}</h1>
        </div>
      </div>

      <div className="profile-layout">
        <div className="flex-col gap-6">
          <div className="card">
            <h2 className="section-title">Informations personnelles</h2>
            <div className="info-grid">
              <ProfileItem label="E-mail" value={userProfileData.email} />
              <ProfileItem label="Téléphone" value={userProfileData.phone} />
            </div>
          </div>

          <div className="card">
            <h2 className="section-title">Historique des congés</h2>

            {/* Show a subtle loading indicator during page transitions */}
            {historyLoading ? (
              <div style={{ textAlign: 'center', padding: '20px', color: '#6b7280' }}>
                Chargement en cours...
              </div>
            ) : (
              <div className="flex-col">
                {userProfileData.vacationHistory.map(req => (
                  <RequestRow key={req.id} r={req} compact onEditRequest={onEditRequest} />
                ))}
                {userProfileData.vacationHistory.length === 0 && (
                  <p className="text-muted">Aucun congé trouvé.</p>
                )}
              </div>
            )}

            {/* Pagination Controls */}
            {historyTotalPages > 0 && (
              <div className="pagination" style={{ marginTop: '20px', justifyContent: 'center' }}>
                {historyPage > 2 && <button onClick={() => setHistoryPage(0)}>1</button>}
                {historyPage > 3 && <span>...</span>}

                <button disabled={historyPage === 0} onClick={() => setHistoryPage(prev => prev - 1)}>
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

                <button disabled={historyPage >= historyTotalPages - 1} onClick={() => setHistoryPage(prev => prev + 1)}>
                  Suivant
                </button>

                {historyPage < historyTotalPages - 4 && <span>...</span>}
                {historyPage < historyTotalPages - 3 && <button onClick={() => setHistoryPage(historyTotalPages - 1)}>{historyTotalPages}</button>}
              </div>
            )}

          </div>
        </div>

        <div className="flex-col gap-6">
          <div className="card card-bg-light">
            <h2 className="section-title">Soldes</h2>
            <div className="flex-row justify-between" style={{ marginBottom: "12px" }}>
              <Stat label="Total disponible" value={userProfileData.totalDays} color="#6C63FF" />
              <Stat label="Utilisés cette année" value={userProfileData.usedDays} color="#F59E0B" />
            </div>
          </div>

          <div className="card">
            <h2 className="section-title">Équipe de remplacement</h2>
            <p className="text-muted text-sm" style={{ marginBottom: "16px" }}>Ces employés agissent comme vos remplaçants par défaut pendant votre absence.</p>
            <div className="flex-col gap-3">
              {userProfileData.coverageTeam.map(backup => (
                <div key={backup.id} className="backup-row">
                  <div className="avatar-xs">
                    {backup.firstName[0]}{backup.lastName[0]}
                  </div>
                  <span className="text-md text-medium">{backup.firstName} {backup.lastName}</span>
                </div>
              ))}
              {userProfileData.coverageTeam.length === 0 && (
                <p className="text-muted text-sm">Aucun remplaçant défini.</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export function HolidaysView({ holidays = [], onSave, onDelete }) {
  const [apiFeedback, setApiFeedback] = useState({ type: null, message: null });
  const [editingId, setEditingId] = useState(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { isValid, isSubmitting }
  } = useForm({
    mode: 'onChange',
    defaultValues: {
      name: "",
      date: "",
      numberOfDays: 1,
      type: "PUBLIC",
      status: "CONFIRMED"
    }
  });

  const showFeedback = (type, message) => {
    setApiFeedback({ type, message });
    setTimeout(() => setApiFeedback({ type: null, message: null }), 3000);
  };

  const onSubmit = async (data) => {
    try {
      const payload = editingId ? { id: editingId, ...data } : data;
      await onSave(payload);

      showFeedback('success', editingId ? 'Jour férié mis à jour avec succès !' : 'Nouveau jour férié ajouté !');
      cancelEdit();
    } catch (error) {
      showFeedback('error', error.response?.data?.message || 'Échec de l\'enregistrement du jour férié.');
    }
  };

  const handleEditClick = (holiday) => {
    setEditingId(holiday.id);
    const [year, month, day] = holiday.date.split('-');
    const htmlSafeDate = `${year}-${month}-${day}`;
    setValue('name', holiday.name, { shouldValidate: true });
    setValue('date', htmlSafeDate, { shouldValidate: true });
    setValue('type', holiday.type, { shouldValidate: true });
    setValue('numberOfDays', holiday.numberOfDays, { shouldValidate: true });
    setValue('status', holiday.status, { shouldValidate: true });
  };

  const cancelEdit = () => {
    setEditingId(null);
    reset();
  };

  const handleDeleteClick = async (id) => {
    if (!window.confirm("Êtes-vous sûr de vouloir supprimer ce jour férié ?")) return;

    try {
      await onDelete(id);
      showFeedback('success', 'Jour férié supprimé.');
    } catch (error) {
      showFeedback('error', 'Échec de la suppression du jour férié.');
    }
  };

  return (
    <div className="form-container">
      <div className="view-header">
        <h1>Jours fériés de l'entreprise</h1>
        <p className="text-muted view-subtitle">Gérez les jours fériés et les congés de l'entreprise.</p>
      </div>

      {apiFeedback.message && (
        <div className={`alert-${apiFeedback.type}`} style={{ marginBottom: '16px' }}>
          {apiFeedback.type === 'success' ? '✓ ' : '⚠ '} {apiFeedback.message}
        </div>
      )}

      <div className="card" style={{ marginBottom: '24px', background: editingId ? '#f8fafc' : 'white' }}>
        <h2 className="section-title">{editingId ? 'Modifier le jour férié' : 'Ajouter un nouveau jour férié'}</h2>

        <form onSubmit={handleSubmit(onSubmit)} className="flex-row gap-4 items-end" style={{ flexWrap: 'wrap' }}>

          <div style={{ flex: '1 1 200px' }}>
            <label className="text-sm text-medium mb-1 block">Nom du jour férié</label>
            <input
              type="text"
              className="input-field w-full"
              placeholder="ex: Fête du Travail"
              {...register("name", { required: true })}
            />
          </div>

          <div style={{ flex: '1 1 150px' }}>
            <label className="text-sm text-medium mb-1 block">Date</label>
            <input
              type="date"
              className="input-field w-full"
              {...register("date", { required: true })}
            />
          </div>

          <div style={{ flex: '1 1 150px' }}>
            <label className="text-sm text-medium mb-1 block">Nombre de jours</label>
            <input
              type="number"
              className="input-field w-full"
              {...register("numberOfDays", { required: true })}
            />
          </div>

          <div style={{ flex: '1 1 150px' }}>
            <label className="text-sm text-medium mb-1 block">Type</label>
            <select className="input-field w-full" {...register("type", { required: true })}>
              <option value="CIVILE">Civile</option>
              <option value="RELIGIEUSE">Religieuse</option>
            </select>
          </div>

          <div style={{ flex: '1 1 150px' }}>
            <label className="text-sm text-medium mb-1 block">Statut</label>
            <select className="input-field w-full" {...register("status", { required: true })}>
              <option value="CONFIRMED">Confirmé</option>
              <option value="TENTATIVE">Provisoire</option>
            </select>
          </div>

          <div className="flex-row gap-2" style={{ flex: '0 0 auto' }}>
            {editingId && (
              <button type="button" className="btn-secondary review-btn reject" onClick={cancelEdit}>
                Annuler
              </button>
            )}
            <button
              type="submit"
              className="review-btn approve"
              disabled={!isValid || isSubmitting}
            >
              {isSubmitting ? 'Enregistrement...' : (editingId ? 'Mettre à jour' : 'Ajouter')}
            </button>
          </div>
        </form>
      </div>

      <div className="card">
        <h2 className="section-title">Jours fériés à venir</h2>
        <div className="flex-col gap-3">
          {holidays.length === 0 ? (
            <p className="text-muted text-sm">Aucun jour férié configuré pour le moment.</p>
          ) : (
            holidays.map(holiday => (
              <div key={holiday.id} className="flex-row justify-between items-center" style={{ padding: '12px', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
                <div>
                  <p className="text-md text-bold">{holiday.name} ({holiday.numberOfDays}J)</p>
                  <p className="text-sm text-muted">
                    {new Date(holiday.date).toLocaleDateString()} • <span style={{ color: holiday.type === 'CIVILE' ? '#6C63FF' : '#F59E0B' }}>{holiday.type} ({holiday.status === "CONFIRMED" ? "Confirmé" : "Provisoire"})</span>
                  </p>
                </div>
                <div className="flex-row gap-2">
                  <button
                    className="review-btn approve"
                    style={{ padding: '6px 12px', fontSize: '14px' }}
                    onClick={() => handleEditClick(holiday)}
                  >
                    Modifier
                  </button>
                  <button
                    className="review-btn reject"
                    style={{ padding: '6px 12px', fontSize: '14px' }}
                    onClick={() => handleDeleteClick(holiday.id)}
                  >
                    Supprimer
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

function ProfileItem({ label, value }) {
  return (
    <div>
      <p className="text-muted info-label">{label}</p>
      <p className="info-value">{value}</p>
    </div>
  );
}