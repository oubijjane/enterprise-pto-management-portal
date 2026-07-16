import { useState, useEffect } from 'react';
import api from '../service/api';
import { EMPLOYEES } from '../data';
import { getAllEmployeesList, getMyProfile, getAllEmployeesByDepartment } from '../service/employeeService';
import { NewRequestView } from '../components/Views';
import { useAuth } from '../context/AuthContext';

export default function NewRequestPage() {
  const [submitted, setSubmitted] = useState(false);
  const [employees, setEmployees] = useState([]);
  const { user } = useAuth();

  const handleAddRequest = async (form, requestedDays, selectedEmployee) => {
    const isHalfDay = form.halfDayType && form.halfDayType !== 'FULL_DAY';
    const requestPayload = {
      fromDate: isHalfDay ? form.from || form.to : form.from,
      toDate: isHalfDay ? form.from || form.to : form.to,
      reason: form.reason,
      status: 'PENDING',
      numberOfDays: requestedDays,
      halfDayType: form.halfDayType || 'FULL_DAY',
      employeeId: form.employeeId,
    };
    await api.post(`/v1/request/${form.employeeId}`, requestPayload);
    setSubmitted(true);
    setTimeout(() => setSubmitted(false), 2000);
  };
  const loadEmployees = async () => {
            try {
                let response = [];
                
                if (user.role === 'ROLE_EMPLOYEE' || user.role === 'ROLE_MANAGER') {
                  response = await getMyProfile();
                    response = [response]; // Wrap in array for consistency
                } else if(user.role === 'ROLE_HR') {
                  response = await getAllEmployeesByDepartment();
                } else if (user.role === 'ROLE_ADMIN') {
                  response = await getAllEmployeesList();
                }
                setEmployees(response);
            } catch (error) {
                console.error('Error fetching employee balances:', error);
            }
        };
    
        useEffect(() => {
            loadEmployees();
        }, []);
  return <NewRequestView employees={employees} onAddRequest={handleAddRequest} />;
}
