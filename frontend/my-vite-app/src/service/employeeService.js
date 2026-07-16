import api from './api';

export const getMyProfile = async () => {
    try {
        const response = await api.get('/v1/employees/me');
        return response.data;
    } catch (error) {
        console.error('Error fetching employee profile:', error);
        throw error;
    }
};

export const getEmployeeProfileById = async (employeeId) => {
    try {
        const response = await api.get(`/v1/employees/${employeeId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching employee profile:', error);
        throw error;
    }
};

export const getAllEmployees = async (page = 1, size = 10) => {
    try {
        const response = await api.get(`/v1/employees/all?page=${page}&size=${size}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching employees:', error);
        throw error;
    }
};

export const getAllEmployeesByDepartment = async () => {
    try {
        const response = await api.get(`/v1/employees/department`);
        return response.data;
    } catch (error) {
        console.error('Error fetching employees:', error);
        throw error;
    }
};

export const getEmployeesBySearch = async (keyword ="",page = 0, size = 10) => {
    try {
        const response = await api.get(`/v1/employees/search?keyword=${keyword}&page=${page}&size=${size}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching employees:', error);
        throw error;
    }
};

export const getAllEmployeesList = async () => {
    try {
        const response = await api.get(`/v1/employees/list`);
        return response.data;
    } catch (error) {
        console.error('Error fetching employees:', error);
        throw error;
    }
};

export const addNewEmployee = async (employeeData) => {
    try {
        const response = await api.post('/v1/employees', employeeData); 
        return response.data;
    } catch (error) {
        console.error('Error adding new employee:', error);
        throw error;
    };
};

export const updateEmployee = async (updatedData) => {
    try {
        const response = await api.put(`/v1/employees/update`, updatedData);
        return response.data;
    } catch (error) {
        console.error('Error updating employee:', error);
        throw error;
    };
}

export default { getMyProfile, getEmployeeProfileById, getAllEmployees, addNewEmployee, updateEmployee, getEmployeesBySearch, getAllEmployeesByDepartment };