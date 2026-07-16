import { useState, useEffect } from 'react';
import api from '../service/api';
import { EMPLOYEES } from '../data';
import { addNewEmployee } from '../service/employeeService';
import { AddNewEmployee } from '../components/Views';
import {getAllDepartments} from '../service/departmentService'

export default function NewEmployeePage() {
  const [submitted, setSubmitted] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [Deps, setDeps] = useState([]);

  const handleAddRequest = async (data) => {
   await addNewEmployee(data);
    setSubmitted(true);
    setTimeout(() => setSubmitted(false), 2000);
  };
  const loadDeps = async () => {
    const respons = await getAllDepartments();
    setDeps(respons)
  }
  useEffect(() => {
    loadDeps();
  },[])
  //console.log('Employees in NewRequestPage:', employees);
  return <AddNewEmployee onAddEmployee={handleAddRequest} departs={Deps} />;
}
