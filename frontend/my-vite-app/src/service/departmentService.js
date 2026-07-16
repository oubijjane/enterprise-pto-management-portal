import api from './api';

export const getAllDepartments = async () => {
    try {
        const response = await api.get('/v1/departments');
        return response.data;
    } catch (error) {
        console.error('Error fetching :', error);
        throw error;
    }
};

export default {getAllDepartments};