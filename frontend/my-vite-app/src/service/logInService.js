import api from './api';

const login = async (username, password) => {
    try {
        const response = await api.post('/v1/auth/login', {
            username: username,
            password: password
        });

        // 1. Get the whole object
        const data = response.data; 
        
        // 2. Store the token separately
        localStorage.setItem('token', data.token);
        
        // 3. Normalize the login response into a frontend user object
        const userWithoutToken = {
            name: data.username,
            username: data.username,
            role: data.roles?.[0] || 'Employee',
            expiresIn: data.expiresIn,
            roles: data.roles,
        };

        localStorage.setItem('user', JSON.stringify(userWithoutToken));
        return userWithoutToken; 
    } catch (error) {
        console.error("Login failed", error.response?.data);
        throw error;
    }
};

export default login