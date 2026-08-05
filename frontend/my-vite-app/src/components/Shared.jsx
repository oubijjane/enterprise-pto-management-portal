import React from 'react';

export function Field({ label, children }) {
  return (
    <div>
      <label className="field-label">{label}</label>
      {children}
    </div>
  );
}

export function Stat({ label, value, color }) {
  return (
    <div>
      <p className="text-muted" style={{ fontSize: "11px", marginBottom: "2px" }}>{label}</p>
      <p className="text-mono" style={{ color: color, fontWeight: 700, fontSize: "20px" }}>{value}</p>
    </div>
  );
}

export function RequestRow({ r, onUpdate, onSelect, compact, role, onEditRequest }) {
  const displayName = r.name || r.firstName || r.employeeName || r.requesterName ||r.employeeDTO.firstName + " " + r.employeeDTO.lastName ||"Inconnu";
  const initials = displayName
    .split(" ")
    .filter(Boolean)
    .map(namePart => namePart[0])
    .join("") || String(r.id || "?");

  const handleRowClick = () => {
    if (onSelect) {
      onSelect(r);
    }
  };
  // Dictionnaire pour traduire les statuts provenant de l'API pour l'affichage
  const statusLabels = {
    "PENDING": "En attente",
    "Pending": "En attente",
    "APPROVED": "Approuvé",
    "Approved": "Approuvé",
    "REJECTED": "Rejeté",
    "Rejected": "Rejeté"
  };

  const displayStatus = statusLabels[r.status] || r.status || 'En attente';

  return (
    <div
      className={`${compact ? "row-compact" : "row-standard"} ${onSelect ? "request-row-clickable" : ""}`}
      onClick={handleRowClick}
      style={{ cursor: onSelect ? 'pointer' : 'default' }}
    >
      <div className="flex-row gap-3 w-full" style={{ flex: 1 }}>
        <div className="avatar-sm">
          {initials}
        </div>
        <div>
          <p className="text-strong" style={{ marginBottom: "4px" }}>{displayName}</p>
          <p className="text-muted text-mono text-sm" style={{ marginTop: "2px" }}>
            {r.fromDate || '--'} → {r.toDate || '--'} <span style={{ color: "#8B7FFF" }}>({r.days != null ? `${r.days}j` : `${r.numberOfDays}j` })</span>
          </p>
        </div>
      </div>
      
      <div className="flex-row gap-2">
        <span className={`status-badge status-${r.status}`}>
          <span className="status-dot" />
          {displayStatus}
        </span>
        {(r.status === "Pending" || r.status === "PENDING") && onEditRequest && (
          <button
            className="action-btn edit"
            onClick={(event) => {
              event.stopPropagation();
              onEditRequest(r);
            }}
          >
            Modifier
          </button>
        )}
        {r.status === "Pending" && !compact && onUpdate && (
          <div className="flex-row gap-2" style={{ marginLeft: "6px" }}>         
                <button className="action-btn approve" onClick={(event) => { event.stopPropagation(); onUpdate(r.id, "Approved"); }}>Approuver</button>
                <button className="action-btn reject" onClick={(event) => { event.stopPropagation(); onUpdate(r.id, "Rejected"); }}>Rejeter</button>
          </div>
        )}
      </div>
    </div>
  );
}