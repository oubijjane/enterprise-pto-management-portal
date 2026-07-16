import axios from 'axios';



// Create a configured instance of Axios
const api = axios.create({
    baseURL: '/api', 
});

// 1. Request Interceptor (You already have this)
// Adds the token to every outgoing request
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token'); 
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

// 2. Response Interceptor (ADD THIS PART)
// Catches errors coming BACK from the server
api.interceptors.response.use(
    (response) => {
        // If the request was successful, just return the data
        return response;
    },
    (error) => {
        // Check if the error is 401 (Unauthorized) or 403 (Forbidden)
        if (error.response && (error.response.status === 401)) {
            console.warn("Session expired or unauthorized. Logging out...");

            // 1. Remove the invalid token
            localStorage.removeItem('token');
            // Remove user details if you store them
            localStorage.removeItem('user'); 

            // 2. Redirect to Login Page
            // We use window.location instead of navigate() because this file is outside React components
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;