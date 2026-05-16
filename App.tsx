import React, { useEffect } from 'react';
import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import Navigation from './components/Navigation';
const DashboardView = React.lazy(() => import('./components/DashboardView'))
const ProductionView = React.lazy(() => import('./components/ProductionView'))
const FleetView = React.lazy(() => import('./components/FleetView'))
const AuditLogView = React.lazy(() => import('./components/AuditLogView'))
const HSEView = React.lazy(() => import('./components/HSEView'))
const InventoryView = React.lazy(() => import('./components/InventoryView'))
const MutationView = React.lazy(() => import('./components/MutationView'))
const EmployeeView = React.lazy(() => import('./components/EmployeeView'))
const SupplierView = React.lazy(() => import('./components/SupplierView'))
const LocationView = React.lazy(() => import('./components/LocationView'))
const TimesheetView = React.lazy(() => import('./components/TimesheetView'))
const DebtView = React.lazy(() => import('./components/DebtView'))
import LoginPage from './components/LoginPage';
import AIChatWidget from './components/AIChatWidget';
import { getCurrentUser, clearAuthData } from './services/authStorage';

const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = React.useState(false);
  const [isChecking, setIsChecking] = React.useState(true);
  const [currentUser, setCurrentUser] = React.useState<any>(null);
  const navigate = useNavigate();
  const location = useLocation();

  // Check authentication on mount
  useEffect(() => {
    const checkAuth = async () => {
      const user = getCurrentUser();
      if (user) {
        setCurrentUser(user);
        setIsAuthenticated(true);
      }
      // Auto-login disabled - users must login manually
      setIsChecking(false);
    };

    checkAuth();
  }, []);

  const handleLoginSuccess = () => {
    const user = getCurrentUser();
    setCurrentUser(user);
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    clearAuthData();
    setIsAuthenticated(false);
    setCurrentUser(null);
    navigate('/');
  };

  // Show loading while checking auth
  if (isChecking) {
    return (
      <div className="min-h-screen transition-colors duration-300 bg-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-white">Loading...</p>
        </div>
      </div>
    );
  }

  // Show login page if not authenticated
  if (!isAuthenticated) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="flex min-h-screen transition-colors duration-300 bg-bg-page">
      <Navigation
        currentUser={currentUser}
        onLogout={handleLogout}
      />

      <main className="flex-1 ml-64 p-8 overflow-y-auto h-screen">
        <div className="max-w-7xl mx-auto animate-fade-in">
          <React.Suspense fallback={<div className="text-center py-20 text-text-muted">Loading...</div>}>
            <Routes>
              <Route path="/" element={<DashboardView />} />
              <Route path="/fleet" element={<FleetView />} />
              <Route path="/mutation" element={<MutationView />} />
              <Route path="/inventory" element={<InventoryView />} />
              <Route path="/production" element={<ProductionView />} />
              <Route path="/timesheet" element={<TimesheetView />} />
              <Route path="/employee" element={<EmployeeView />} />
              <Route path="/supplier" element={<SupplierView />} />
              <Route path="/debt" element={<DebtView />} />
              <Route path="/location" element={<LocationView />} />
              <Route path="/hse" element={<HSEView />} />
              <Route path="/audit" element={<AuditLogView />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </React.Suspense>
        </div>
      </main>
      <AIChatWidget />
    </div>
  );
};

export default App;
