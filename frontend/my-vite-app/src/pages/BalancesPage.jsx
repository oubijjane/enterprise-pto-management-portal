import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BalancesView } from '../components/Views'; // Adjust if this is normally in the same file
import { getAllEmployees, getEmployeesBySearch } from '../service/employeeService';

export default function BalancesPage() {
  const [employees, setEmployees] = useState([]);
  const [searchTerm, setSearchTerm] = useState(""); 
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchTerm);
      setCurrentPage(0); // Always jump back to page 1 when starting a new search
    }, 500);
    return () => clearTimeout(timer);
  }, [searchTerm]);
  const loadEmployees = async (pageToLoad, pageSize) => {
    try {
      setLoading(true);
      const response = await getEmployeesBySearch(searchTerm,pageToLoad, pageSize);
      
      setEmployees(response?.content || []);
      setTotalPages(response?.page.totalPages || 0); // FIXED: Removed .page
      
    } catch (error) {
      console.error('Erreur lors de la récupération des soldes des employés :', error);
      setError("Une erreur est survenue lors du chargement des données.");
    } finally {
      setLoading(false);
    }
  };

  // FIXED: Removed the duplicate duplicate useEffect!
  useEffect(() => {
    loadEmployees(currentPage, 10, debouncedSearch);
  }, [currentPage, debouncedSearch]);

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

  return (
    <>
      {/* Optional: Add a loading state UI here if you don't want a blank screen while fetching */}
      {loading && employees.length === 0 ? (
        <div className="loading-spinner" style={{ padding: '20px', textAlign: 'center' }}>Chargement des soldes...</div>
      ) : (
        <BalancesView 
        employees={employees} 
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}/>
      )}
      
      {totalPages > 0 && (
        <div className="pagination">
          {currentPage > 2 && (
            <button onClick={() => setCurrentPage(0)}>1</button>
          )}
          
          {currentPage > 3 && <span>...</span>}

          <button 
            disabled={currentPage === 0}
            onClick={() => setCurrentPage(prev => prev - 1)}
          >
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

          <button 
            disabled={currentPage >= totalPages - 1}
            onClick={() => setCurrentPage(prev => prev + 1)}
          >
            Suivant
          </button>

          {currentPage < totalPages - 4 && <span>...</span>}

          {currentPage < totalPages - 3 && (
            <button onClick={() => setCurrentPage(totalPages - 1)}>{totalPages}</button>
          )}
        </div>
      )}
    </>
  );
}