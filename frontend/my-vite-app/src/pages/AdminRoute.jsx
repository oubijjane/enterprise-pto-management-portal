import { Navigate, Outlet } from 'react-router';
import { useAuth } from '../context/AuthContext'; 
import { useEffect } from 'react';


const AdminRoute = () => {
    const { user, loading } = useAuth();

    // 1. Handle the loading state first
    if (loading) return <div>Chargement...</div>;

    // 2. Calculate the boolean directly in the component body
    // This variable is now "scoped" to the whole component
    const isAdminOrHr = user?.roles?.some(role => ['ROLE_ADMIN'].includes(role));

    // 3. Debugging (Optional, but now it will work)

    // 4. Return the logic
    return isAdminOrHr ? <Outlet /> : <Navigate to="/" replace />;
};

const SharedProtectedRoute = () => {
    const { user, loading } = useAuth();

    // 1. Handle the loading state first
    if (loading) return <div>Chargement...</div>;

    // 2. Calculate the boolean directly in the component body
    // This variable is now "scoped" to the whole component
    const isAdminOrHr = user?.roles?.some(role => ['ROLE_ADMIN', 'ROLE_HR','ROLE_MANAGER'].includes(role));

    // 3. Debugging (Optional, but now it will work)

    // 4. Return the logic
    return isAdminOrHr ? <Outlet /> : <Navigate to="/" replace />;
};
export {AdminRoute, SharedProtectedRoute} ;