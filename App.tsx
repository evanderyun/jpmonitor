import React, { useState, useEffect } from 'react';
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
import { getCurrentUser, clearAuthData } from './services/authStorage';

const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isChecking, setIsChecking] = useState(true);
  const [currentUser, setCurrentUser] = useState<any>(null);

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
    setActiveTab('dashboard');
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

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <DashboardView />
      case 'timesheet':
        return <TimesheetView />
      case 'production':
        return <ProductionView />
      case 'fleet':
        return <FleetView />
      case 'mutation':
        return <MutationView />
      case 'inventory':
        return <InventoryView />
      case 'employee':
        return <EmployeeView />
      case 'supplier':
        return <SupplierView />
      case 'debt':
        return <DebtView />
      case 'location':
        return <LocationView />
      case 'hse':
        return <HSEView />
      case 'audit':
        return <AuditLogView />
      default:
        return (
          <div className="flex flex-col items-center justify-center h-full text-slate-400">
            <div className="w-16 h-16 bg-slate-200 rounded-full flex items-center justify-center mb-4">
              <span className="text-3xl">🛠️</span>
            </div>
            <h3 className="text-lg font-medium text-slate-600">Module under development</h3>
            <p className="text-sm">This feature is coming in the next sprint.</p>
          </div>
        );
    }
  };

  return (
    <div className="flex min-h-screen transition-colors duration-300 bg-bg-page">
      <Navigation
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        currentUser={currentUser}
        onLogout={handleLogout}
      />

      <main className="flex-1 ml-64 p-8 overflow-y-auto h-screen">
        <div className="max-w-7xl mx-auto animate-fade-in">
          {renderContent()}
        </div>
      </main>
    </div>
  );
};

export default App;