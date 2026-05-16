import React, { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Hammer, Truck, FileText, Activity, Settings, PackageSearch, ArrowRightLeft, Users, ShoppingBag, MapPin, Clock, Landmark, LogOut, Moon, Sun } from 'lucide-react';

interface NavProps {
  currentUser?: any;
  onLogout?: () => void;
}

const linkClass = ({ isActive }: { isActive: boolean }) =>
  'w-full flex items-center gap-3 px-3 py-2.5 text-sm rounded-jpmonitor-md transition-all duration-200 ' +
  (isActive ? 'bg-jpmonitor-red text-white font-medium' : 'text-text-secondary hover:bg-bg-elevated hover:text-text-primary');

const Navigation: React.FC<NavProps> = ({ currentUser, onLogout }) => {
  const [darkMode, setDarkMode] = useState(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('jpmonitor-dark-mode');
      if (stored !== null) return stored === 'true';
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return false;
  });

  useEffect(() => {
    document.documentElement.classList.toggle('dark', darkMode);
    localStorage.setItem('jpmonitor-dark-mode', String(darkMode));
  }, [darkMode]);

  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'fleet', label: 'Fleet Management', icon: Truck },
    { id: 'mutation', label: 'Unit Mutation', icon: ArrowRightLeft },
    { id: 'inventory', label: 'Inventory', icon: PackageSearch },
    { id: 'production', label: 'Production', icon: Hammer },
    { id: 'timesheet', label: 'Timesheets', icon: Clock },
    { id: 'employee', label: 'Employee & HR', icon: Users },
    { id: 'supplier', label: 'Suppliers', icon: ShoppingBag },
    { id: 'debt', label: 'Finance', icon: Landmark },
    { id: 'location', label: 'Locations', icon: MapPin },
    { id: 'hse', label: 'HSE & Safety', icon: Activity },
    { id: 'audit', label: 'Audit Trails', icon: FileText },
  ];

  return (
    <div className="w-64 bg-bg-panel border-r border-border flex flex-col h-screen fixed left-0 top-0 z-20 transition-colors duration-300">
      <div className="p-5 border-b border-border">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex font-black text-2xl tracking-tighter leading-none">
              <span className="text-jpmonitor-red">J</span>
              <span className="text-jpmonitor-red transform translate-y-1">P</span>
              <span className="text-jpmonitor-red">M</span>
            </div>
            <div>
              <h1 className="text-xs font-semibold text-text-primary leading-tight">jpmonitor</h1>
              <p className="text-[10px] text-text-muted uppercase tracking-wider">Mining Operations</p>
            </div>
          </div>
          <button onClick={() => setDarkMode(!darkMode)} className="p-1.5 rounded-jpmonitor border border-border hover:bg-bg-elevated transition-colors text-text-muted" aria-label="Toggle dark mode">
            {darkMode ? <Sun size={14} /> : <Moon size={14} />}
          </button>
        </div>
      </div>

      <nav className="flex-1 py-4 space-y-0.5 px-3 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const to = item.id === 'dashboard' ? '/' : `/${item.id}`;
          return (
            <NavLink key={item.id} to={to} className={linkClass}>
              <Icon size={16} className="flex-shrink-0" />
              <span className="truncate">{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="p-4 border-t border-border">
        {currentUser && (
          <div className="mb-3 px-2">
            <p className="text-xs text-text-muted uppercase font-medium tracking-wider">{currentUser.fullName || 'Admin'}</p>
            <p className="text-xs text-text-muted mt-0.5">{currentUser.role || 'Super Admin'}</p>
          </div>
        )}
        {onLogout && (
          <button onClick={onLogout} className="w-full flex items-center gap-2 px-3 py-2 text-sm text-text-muted hover:text-jpmonitor-red hover:bg-jpmonitor-red-subtle rounded-jpmonitor-md transition-colors">
            <LogOut size={16} />
            <span>Sign Out</span>
          </button>
        )}
      </div>
    </div>
  );
};

export default Navigation;
