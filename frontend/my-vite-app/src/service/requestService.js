import api from './api';

const normalizeRequest = (request) => {
    const employeeName = request.employeeDTO ? `${request.employeeDTO.firstName} ${request.employeeDTO.lastName}` : `${request.firstName || ''} ${request.lastName || ''}`.trim();
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
    };
};

const getAllRequests = async (page = 0, size = 7) => {
    try {
        const response = await api.get(`/v1/request/all?page=${page}&size=${size}`);
        const requests = response.data || [];
        return requests;
    } catch (error) {
        console.error('Error fetching requests:', error);
        throw error;
    }
};

export const getAllRequestsByStatus = async (page = 0, size = 7, status) => {
    try {
        // Safely build the query string. If status is null, it is simply omitted.
        const queryParams = new URLSearchParams({ page, size });
        if (status) {
            queryParams.append('status', status);
        }

        const response = await api.get(`/v1/request/status?${queryParams.toString()}`);
        const requests = response.data || [];
        return requests;
    } catch (error) {
        console.error('Error fetching requests:', error);
        throw error;
    }
};

export const getAllRequestsByStatusByUserDepartment = async (page = 0, size = 7, status) => {
    try {
        // Safely build the query string. If status is null, it is simply omitted.
        const queryParams = new URLSearchParams({ page, size });
        if (status) {
            queryParams.append('status', status);
        }

        const response = await api.get(`/v1/request/department?${queryParams.toString()}`);
        const requests = response.data || [];
        return requests;
    } catch (error) {
        console.error('Error fetching requests:', error);
        throw error;
    }
};

export const getAllRequestsUserDepartmentbyStatus = async (page = 0, size = 7, id) => {
    try {
        // Safely build the query string. If status is null, it is simply omitted.
        const queryParams = new URLSearchParams({ page, size });
       

        const response = await api.get(`/v1/request/department&status/${id}?${queryParams.toString()}`);
        const requests = response.data || [];
        return requests;
    } catch (error) {
        console.error('Error fetching requests:', error);
        throw error;
    }
};

const getPendingRequests = async (page = 0, size=10) => {
    try {
        const response = await api.get(`/v1/request/pendings?page=${page}&size=${size}`);
        const requests = response.data || [];
        return requests;
    } catch (error) {
        console.error('Error fetching requests:', error);
        throw error;
    }
};

const getNonRejectedRequests = async (page=5, size=0) => {
    try {
        const response = await api.get(`/v1/request/non-rejected?size=${500}&page=${0}`);
        const requests = response.data || [];
        return requests;
    } catch (error) {
        console.error('Error fetching requests:', error);
        throw error;
    }
};

const getRequestById = async (id) => {
    try{
    const requests = await api.get(`/v1/request/${id}`);
    return requests.data;
    }catch {
        console.error('Error fetching requests:', error);
        throw error;
    }
};

const countRequestByStatus = async (status) => {
    try{
    const request = await api.get(`/v1/request/count?status=${status}`);
    return request;
    }catch {
        console.error('Error fetching request:', error);
        throw error;
    }
};

export const getRequestByEmployeeId = async (page = 0, size = 10, id) => {
    try {
    const requests = await api.get(`/v1/request/employee/${id}?size=${size}&page=${page}`);
    return requests.data;
     } catch (error) {
        console.error('Error fetching requests:', error);
        throw error;
    }
};

export const updateRequest = async (requestPayload) => {
    try {
        const response = await api.put('/v1/request', requestPayload);
        return normalizeRequest(response.data);
    } catch (error) {
        console.error('Error updating request:', error);
        throw error;
    }
};

const updateRequestStatus = async (id, endpoint) => {
    try {
        const response = await api.put(`/v1/request/${endpoint}/${id}`);
        return normalizeRequest(response.data);
    } catch (error) {
        console.error(`Error updating request status to ${endpoint}:`, error);
        throw error;
    }
};

const approveByManger = async (id, endpoint) => {
    try {
        const response = await api.put(`/v1/request/approvedByResponsible/${id}`);
        return normalizeRequest(response.data);
    } catch (error) {
        console.error(`Error updating request status to ${endpoint}:`, error);
        throw error;
    }
};

const rejectByManger = async (id, endpoint) => {
    try {
        const response = await api.put(`/v1/request/rejectByResponsible/${id}`);
        return normalizeRequest(response.data);
    } catch (error) {
        console.error(`Error updating request status to ${endpoint}:`, error);
        throw error;
    }
};

const approveRequest = async (id) => updateRequestStatus(id, 'approved');
const rejectRequest = async (id) => updateRequestStatus(id, 'rejected');

export default { getPendingRequests, getAllRequests, getRequestById, approveRequest, 
    rejectRequest, getNonRejectedRequests,getAllRequestsByStatus, 
    getRequestByEmployeeId, countRequestByStatus, getAllRequestsByStatusByUserDepartment, approveByManger, rejectByManger, getAllRequestsUserDepartmentbyStatus, updateRequest };