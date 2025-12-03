

import React from 'react';
import { LayoutDashboard, Hammer, Truck, FileText, Activity, Settings, PackageSearch, ArrowRightLeft, Users, ShoppingBag, MapPin, Clock, Landmark, LogOut } from 'lucide-react';

interface NavProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  currentUser?: any;
  onLogout?: () => void;
}

const Navigation: React.FC<NavProps> = ({ activeTab, setActiveTab, currentUser, onLogout }) => {
  const menuItems = [
    { id: 'dashboard', label: 'Executive Dashboard', icon: LayoutDashboard },
    { id: 'timesheet', label: 'Timesheets (HM)', icon: Clock },
    { id: 'location', label: 'Project Locations', icon: MapPin },
    { id: 'production', label: 'Production Control', icon: Hammer },
    { id: 'fleet', label: 'Fleet Management', icon: Truck },
    { id: 'mutation', label: 'Unit Mutation', icon: ArrowRightLeft },
    { id: 'inventory', label: 'Inventory & Spareparts', icon: PackageSearch },
    { id: 'supplier', label: 'Suppliers & Vendors', icon: ShoppingBag },
    { id: 'debt', label: 'Finance & Debt', icon: Landmark }, // New Item
    { id: 'employee', label: 'Employee & HR', icon: Users },
    { id: 'audit', label: 'Audit Trails', icon: FileText },
    { id: 'hse', label: 'HSE & Safety', icon: Activity },
  ];

  return (
    <div className="w-64 bg-slate-900 text-white flex flex-col h-screen fixed left-0 top-0 shadow-xl z-20" role="navigation" aria-label="Main Navigation">
      <div className="p-6 border-b border-slate-700">
        <div className="flex items-center gap-3 mb-1">
          {/* Simulated JPM Logo */}
          <div className="flex font-black text-2xl tracking-tighter leading-none">
            <span className="text-red-500">J</span>
            <span className="text-red-500 transform translate-y-1">P</span>
            <span className="text-red-500">M</span>
          </div>
          <div>
            <h1 className="text-sm font-bold leading-tight text-white tracking-wide">PT JAVA PERSADA MANDIRI</h1>
          </div>
        </div>
        <div className="text-[10px] font-bold text-blue-400 uppercase tracking-widest pl-1">General Contractor</div>
      </div>

      <nav className="flex-1 py-6 space-y-1 px-3 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`w-full flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200 ${isActive
                  ? 'bg-red-600 text-white shadow-md'
                  : 'text-slate-400 hover:bg-slate-800 hover:text-white'
                }`}
            >
              <Icon size={18} />
              {item.label}
            </button>
          );
        })}
      </nav>

      <div className="p-4 border-t border-slate-700">
        <div className="flex items-center gap-3 px-2 py-2 text-slate-400 hover:text-white cursor-pointer">
          <Settings size={18} />
          <span className="text-sm font-medium">System Config</span>
        </div>
        <div className="mt-4 px-2">
          <p className="text-xs text-slate-500 uppercase font-bold tracking-wider">Current User</p>
          <p className="text-sm text-white mt-1">{currentUser?.fullName || 'System Administrator'}</p>
          <p className="text-xs text-slate-400">Role: {currentUser?.role || 'Admin'}</p>
        </div>
        {onLogout && (
          <button
            onClick={onLogout}
            className="mt-3 w-full flex items-center gap-2 px-4 py-2 text-sm bg-slate-800 hover:bg-red-600 text-slate-400 hover:text-white rounded-lg transition-colors"
          >
            <LogOut size={16} />
            Logout
          </button>
        )}
      </div>
    </div>
  );
};

export default Navigation;
