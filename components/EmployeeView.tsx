import React, { useState, useEffect, useCallback } from 'react';
import { employeesAPI, locationsAPI } from '../services/api';
import { Employee } from '../types';
import { Users, UserPlus, Briefcase, Building2, Calendar, Plus, MapPin, Trash2, Search } from 'lucide-react';
import SearchableSelect from './SearchableSelect';

const EmployeeView: React.FC = () => {
    const [employees, setEmployees] = useState<Employee[]>([]);
    const [locations, setLocations] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    const [formData, setFormData] = useState({
        name: '',
        position: '',
        department: 'Production',
        role: 'Operator',
        status: 'Active',
        joinedDate: new Date().toISOString().split('T')[0],
        locationId: ''
    });

    const loadData = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const [empData, locData] = await Promise.all([
                employeesAPI.getEmployees(),
                locationsAPI.getLocations()
            ]);
            setEmployees(empData);
            setLocations(locData);
        } catch (err: any) {
            setError(err.message || 'Failed to load data');
            console.error('Load data error:', err);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadData();
    }, [loadData]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.locationId) {
            alert("Please assign a location.");
            return;
        }
        try {
            await employeesAPI.createEmployee(formData);
            await loadData();
            setIsModalOpen(false);
            setFormData({
                name: '', position: '', department: 'Production', role: 'Operator', status: 'Active', joinedDate: new Date().toISOString().split('T')[0], locationId: ''
            });
        } catch (err: any) {
            alert(err.message || 'Failed to create employee');
        }
    };

    const handleDelete = async (id: string, name: string) => {
        if (window.confirm(`Are you sure you want to remove employee: ${name}? This action is audited.`)) {
            try {
                await employeesAPI.deleteEmployee(id);
                await loadData();
            } catch (e: any) {
                alert("Error deleting employee: " + e.message);
            }
        }
    };

    const filteredEmployees = employees.filter(emp =>
        emp.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        emp.id?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        emp.position.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const departmentOptions = [
        { value: 'Production', label: 'Production' },
        { value: 'Maintenance', label: 'Maintenance / Plant' },
        { value: 'HSE', label: 'HSE & Safety' },
        { value: 'Logistics', label: 'Logistics' },
        { value: 'Office', label: 'Office / Admin' }
    ];

    const roleOptions = [
        { value: 'Operator', label: 'Operator (Driver)' },
        { value: 'Mechanic', label: 'Mechanic / Technician' },
        { value: 'Staff', label: 'Staff / Admin' },
        { value: 'Manager', label: 'Manager / Supervisor' }
    ];

    const locationOptions = locations.map(l => ({
        value: l.id,
        label: l.name,
        subLabel: l.code
    }));

    const getLocationName = (locationId?: string) => {
        const loc = locations.find(l => l.id === locationId);
        return loc ? loc.name : '-';
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <div className="text-slate-500">Loading employees...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-lg">
                <p className="font-bold">Error loading employees</p>
                <p className="text-sm">{error}</p>
                <button onClick={loadData} className="mt-2 text-sm underline">Retry</button>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold text-slate-800">Employee Management</h2>
                    <p className="text-slate-500 text-sm">Human Resources, Operators & Technicians</p>
                </div>
                <div className="flex gap-3">
                    <div className="relative">
                        <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                        <label htmlFor="employee-search-input" className="sr-only">Search employees...</label>
                        <input
                            id="employee-search-input"
                            type="text"
                            placeholder="Search employees..."
                            className="pl-10 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none w-64"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <button
                        onClick={() => setIsModalOpen(true)}
                        className="flex items-center gap-2 bg-slate-900 text-white px-4 py-2 rounded-lg shadow hover:bg-slate-800 transition-colors"
                    >
                        <Plus size={18} />
                        Register Employee
                    </button>
                </div>
            </div>

            {/* KPI Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
                    <div className="p-3 bg-blue-100 text-blue-600 rounded-full">
                        <Users size={24} />
                    </div>
                    <div>
                        <div className="text-2xl font-bold text-slate-900">{employees.length}</div>
                        <div className="text-sm text-slate-500">Total Headcount</div>
                    </div>
                </div>
                <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
                    <div className="p-3 bg-amber-100 text-amber-600 rounded-full">
                        <Briefcase size={24} />
                    </div>
                    <div>
                        <div className="text-2xl font-bold text-slate-900">
                            {employees.filter(e => e.department === 'Production').length}
                        </div>
                        <div className="text-sm text-slate-500">Production Team</div>
                    </div>
                </div>
                <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
                    <div className="p-3 bg-green-100 text-green-600 rounded-full">
                        <Wrench size={24} />
                    </div>
                    <div>
                        <div className="text-2xl font-bold text-slate-900">
                            {employees.filter(e => e.department === 'Maintenance').length}
                        </div>
                        <div className="text-sm text-slate-500">Maintenance Team</div>
                    </div>
                </div>
            </div>

            {/* Table */}
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
                <table className="w-full text-sm text-left">
                    <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                        <tr>
                            <th className="px-6 py-4">Employee ID</th>
                            <th className="px-6 py-4">Name & Position</th>
                            <th className="px-6 py-4">Department</th>
                            <th className="px-6 py-4">Location (Site)</th>
                            <th className="px-6 py-4">Status</th>
                            <th className="px-6 py-4">Joined Date</th>
                            <th className="px-6 py-4 text-center">Action</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {filteredEmployees.map((emp) => (
                            <tr key={emp.id} className="hover:bg-slate-50 transition-colors">
                                <td className="px-6 py-4 font-mono text-slate-500 text-xs">{emp.id}</td>
                                <td className="px-6 py-4">
                                    <div className="font-bold text-slate-800">{emp.name}</div>
                                    <div className="text-xs text-slate-500">{emp.position}</div>
                                </td>
                                <td className="px-6 py-4">
                                    <span className="flex items-center gap-1 text-slate-600">
                                        <Building2 size={14} className="text-slate-400" />
                                        {emp.department}
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <span className="flex items-center gap-1 text-slate-600">
                                        <MapPin size={14} className="text-slate-400" />
                                        {getLocationName(emp.locationId)}
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`text-xs font-bold px-2 py-1 rounded-full ${emp.status === 'Active' ? 'bg-green-100 text-green-700' : 'bg-slate-200 text-slate-600'
                                        }`}>
                                        {emp.status}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-slate-500 text-xs flex items-center gap-1">
                                    <Calendar size={14} className="text-slate-400" />
                                    {emp.joinedDate}
                                </td>
                                <td className="px-6 py-4 text-center">
                                    <button
                                        onClick={() => handleDelete(emp.id, emp.name)}
                                        className="text-red-500 bg-red-50 border border-red-100 hover:bg-red-100 p-1.5 rounded transition-colors"
                                        title="Delete Employee"
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {filteredEmployees.length === 0 && (
                            <tr><td colSpan={7} className="px-6 py-8 text-center text-slate-400">No employees found matching &quot;{searchTerm}&quot;.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>

            {/* Modal */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg p-6 animate-fade-in">
                        <div className="flex justify-between items-start mb-6 border-b border-slate-100 pb-4">
                            <div>
                                <h3 className="text-xl font-bold text-slate-900">Add Employee</h3>
                                <p className="text-xs text-slate-500 mt-1">Create new personnel record</p>
                            </div>
                            <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
                        </div>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label htmlFor="employee-full-name" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Full Name</label>
                                <input type="text" required className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                                    value={formData.name} onChange={e => setFormData({ ...formData, name: e.target.value })} id="employee-full-name" />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label htmlFor="employee-position" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Position / Title</label>
                                    <input type="text" required className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                                        value={formData.position} onChange={e => setFormData({ ...formData, position: e.target.value })} placeholder="e.g. Senior Mechanic" id="employee-position" />
                                </div>
                                <div>
                                    <SearchableSelect label="Role (System)" options={roleOptions} value={formData.role} onChange={v => setFormData({ ...formData, role: v })} id="employee-role" />
                                </div>
                            </div>

                            <div>
                                <SearchableSelect label="Department" options={departmentOptions} value={formData.department} onChange={v => setFormData({ ...formData, department: v })} id="employee-department" />
                            </div>

                            <div>
                                <SearchableSelect label="Site Assignment" options={locationOptions} value={formData.locationId} onChange={v => setFormData({ ...formData, locationId: v })} required id="employee-site-assignment" />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label htmlFor="employee-join-date" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Join Date</label>
                                    <input type="date" required className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                                        value={formData.joinedDate} onChange={e => setFormData({ ...formData, joinedDate: e.target.value })} id="employee-join-date" />
                                </div>
                                <div>
                                    <label htmlFor="employee-status" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Status</label>
                                    <select className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                                        value={formData.status} onChange={e => setFormData({ ...formData, status: e.target.value })} id="employee-status">
                                        <option value="Active">Active</option>
                                        <option value="OnLeave">On Leave</option>
                                        <option value="Resigned">Resigned</option>
                                    </select>
                                </div>
                            </div>

                            <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-slate-100">
                                <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-slate-600 font-medium hover:bg-slate-100 rounded-lg">Cancel</button>
                                <button type="submit" className="px-4 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 flex items-center gap-2">
                                    <UserPlus size={16} /> Add Employee
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

function Wrench(props: any) {
    return (
        <svg
            {...props}
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
        >
            <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
        </svg>
    )
}

export default EmployeeView;