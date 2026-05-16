import React from 'react';
import { PackageSearch, AlertTriangle, Archive } from 'lucide-react';

interface InventoryDashboardProps {
    totalItems: number;
    lowStockItems: number;
    totalValue: number;
}

const InventoryDashboard: React.FC<InventoryDashboardProps> = ({ totalItems, lowStockItems, totalValue }) => {
    return (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
                <div className="p-3 bg-blue-100 text-blue-600 rounded-full">
                    <PackageSearch size={24} />
                </div>
                <div>
                    <div className="text-2xl font-bold text-slate-900">{totalItems}</div>
                    <div className="text-sm text-slate-500">Unique SKU Items</div>
                </div>
            </div>
            <div className={`bg-white p-5 rounded-xl shadow-sm border ${lowStockItems > 0 ? 'border-red-200 bg-red-50' : 'border-slate-200'} flex items-center gap-4`}>
                <div className={`p-3 rounded-full ${lowStockItems > 0 ? 'bg-red-200 text-red-700' : 'bg-green-100 text-green-600'}`}>
                    <AlertTriangle size={24} />
                </div>
                <div>
                    <div className="text-2xl font-bold text-slate-900">{lowStockItems}</div>
                    <div className="text-sm text-slate-500">Low Stock Alerts</div>
                </div>
            </div>
            <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
                <div className="p-3 bg-indigo-100 text-indigo-600 rounded-full">
                    <Archive size={24} />
                </div>
                <div>
                    <div className="text-2xl font-bold text-slate-900">
                        Rp {(totalValue / 1000000).toLocaleString()} M
                    </div>
                    <div className="text-sm text-slate-500">Total Inventory Value</div>
                </div>
            </div>
        </div>
    );
};

export default InventoryDashboard;
