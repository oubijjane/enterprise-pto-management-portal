import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from './App';
import { useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ProfilePage from './pages/ProfilePage';
import RequestsPage from './pages/RequestsPage';
import AdminRequestReviewPage from './pages/AdminRequestReviewPage';
import NewRequestPage from './pages/NewRequestPage';
import BalancesPage from './pages/BalancesPage';
import NewEmployeePage from './pages/NewEmployeePage';
import EmployeePage from './pages/EmployeePage';
import HolidaysPage from './pages/HolidaysPage';
import {AdminRoute, SharedProtectedRoute} from "./pages/AdminRoute";
import { HomeRedirect } from './HomeRedirect';



export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />
  },
  {
    path: '/',
    element: <App />,
    children: [
      {
        index: true,
        element: <HomeRedirect />
      },
      {
        path: 'profile',
        element: <ProfilePage />
      },
      {
        path: 'requests',
        element: <RequestsPage />
      },
      {
        path: 'new-request',
        element: <NewRequestPage />
      },
      {
        path: 'balances',
        element: <BalancesPage />
      },
      {
        path: '*',
        element: <Navigate to="/dashboard" replace />
      },

      {
        element: <AdminRoute />,
        children: [
          {
            path: 'holidays',
            element: <HolidaysPage />
          },
          {
            path: 'new-employee',
            element: <NewEmployeePage />
          },
          {
            path: 'dashboard',
            element: <DashboardPage />
          },
          {
            path: 'employee/:employeeId',
            element: <EmployeePage />
          }
        ]
      },
      {
        element: <SharedProtectedRoute />,
        children: [
          {
            path: 'employee/:employeeId',
            element: <EmployeePage />
          },
          {
        path: 'requests/:requestId',
        element: <AdminRequestReviewPage />
      }
        ]
      }
    ]
  }
]);
