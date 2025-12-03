import React, { useState, useMemo, useRef, useEffect, useCallback } from 'react';
import { employeesAPI, equipmentAPI, locationsAPI, dailyLogsAPI } from '../services/api';
import { Plus, Printer, Search, Calendar, Clock } from 'lucide-react'; // Removed User, Truck, MapPin
import SearchableSelect from './SearchableSelect';

const TimesheetView: React.FC = () => {
    const [logs, setLogs] = useState<any[]>([]);
    const [employees, setEmployees] = useState<any[]>([]);
    const [equipment, setEquipment] = useState<any[]>([]);
    const [locations, setLocations] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const printRef = useRef<HTMLDivElement>(null);

    // Filter State
    const [filterDate, setFilterDate] = useState('');
    const [filterUnit, setFilterUnit] = useState('');

    // Form State
    const [formData, setFormData] = useState({
        date: new Date().toISOString().split('T')[0],
        shift: 'Day',
        equipmentId: '',
        operatorId: '',
        locationId: '',
        startHM: 0,
        endHM: 0,
        activityCode: '',
        remarks: ''
    });

    const loadData = useCallback(async () => {
        try {
            setLoading(true);
            const [empData, eqData, locData, logsData] = await Promise.all([
                employeesAPI.getEmployees(),
                equipmentAPI.getEquipment(),
                locationsAPI.getLocations(),
                dailyLogsAPI.getDailyLogs()
            ]);
            setEmployees(empData);
            setEquipment(eqData);
            setLocations(locData);
            setLogs(logsData);
        } catch (err: any) {
            if (err?.name === 'AbortError') return;
            console.error('Failed to load data:', err);
        } finally {
            setLoading(false);
        }
    }, []);

    // Load reference data and daily logs from API
    useEffect(() => {
        loadData();
    }, [loadData]);

    const calculatedHours = formData.endHM > formData.startHM
        ? parseFloat((formData.endHM - formData.startHM).toFixed(1))
        : 0;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await dailyLogsAPI.createDailyLog({
                date: formData.date,
                equipmentId: formData.equipmentId,
                operatorName: formData.operatorId, // Using operatorId field as operator name
                locationId: formData.locationId,
                startHM: formData.startHM,
                endHM: formData.endHM,
                shift: formData.shift,
                activity: formData.activityCode,
                notes: formData.remarks
            });

            // Reload logs after creation
            const updatedLogs = await dailyLogsAPI.getDailyLogs();
            setLogs(updatedLogs);

            setIsModalOpen(false);
            // Reset form but keep date/shift for ease of next entry
            setFormData({ ...formData, equipmentId: '', operatorId: '', startHM: 0, endHM: 0, remarks: '' });
        } catch (err: any) {
            console.error('Failed to save daily log:', err);
            alert(`Error: ${err.message || 'Failed to save daily log'}`);
        }
    };

    const handleUnitSelect = (unitId: string) => {
        const unit = equipment.find(e => e.id === unitId);
        if (unit) {
            const hmValue = unit.hour_meter || unit.hourMeter || 0;
            setFormData({
                ...formData,
                equipmentId: unitId,
                startHM: hmValue,
                endHM: hmValue,
                locationId: unit.location_id || unit.locationId || ''
            });
        }
    };

    const handlePrint = () => {
        if (printRef.current) {
            const printWindow = window.open('', '', 'height=800,width=1000');
            if (printWindow) {
                printWindow.document.write('<html><head><title>Daily Equipment Report</title>');
                printWindow.document.write('<script src="https://cdn.tailwindcss.com"></script>');
                printWindow.document.write('<style>@media print { body { -webkit-print-color-adjust: exact; } table { font-size: 10px; } .page-break { page-break-before: always; } }</style>');
                printWindow.document.write('</head><body class="bg-white">');
                printWindow.document.write(printRef.current.innerHTML);
                printWindow.document.write('</body></html>');
                printWindow.document.close();
                setTimeout(() => { printWindow.print(); printWindow.close(); }, 1000);
            }
        }
    };

    const filteredLogs = useMemo(() => {
        return logs.filter(l => {
            const dateMatch = !filterDate || l.date === filterDate;
            const eqCode = equipment.find(e => e.id === l.equipmentId)?.code || l.equipmentId;
            const unitMatch = !filterUnit || eqCode.toLowerCase().includes(filterUnit.toLowerCase());
            return dateMatch && unitMatch;
        });
    }, [logs, filterDate, filterUnit, equipment]);

    // Summary for Report
    const unitSummary = useMemo(() => {
        const summary: Record<string, number> = {};
        filteredLogs.forEach(log => {
            const code = equipment.find(e => e.id === log.equipmentId)?.code || log.equipmentId;
            summary[code] = (summary[code] || 0) + log.totalHours;
        });
        return Object.entries(summary).sort((a, b) => b[1] - a[1]);
    }, [filteredLogs, equipment]);

    // Options
    const equipmentOptions = equipment.map(e => ({ value: e.id, label: e.code, subLabel: e.model }));
    const operatorOptions = employees
        .filter(e => e.role === 'Operator')
        .map(e => ({ value: e.id, label: e.name, subLabel: e.position }));
    const locationOptions = locations.map(l => ({ value: l.id, label: l.name }));

    const getLocationName = (locationId: string) => {
        return locations.find(l => l.id === locationId)?.name || '-';
    };

    if (loading) {
        return <div className="flex items-center justify-center h-64"><div className="text-slate-500">Loading...</div></div>;
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold text-slate-800">Timesheets & HM Control</h2>
                    <p className="text-slate-500 text-sm">Daily Equipment Hours & Operator Logging</p>
                </div>
                <div className="flex gap-2">
                    <button
                        onClick={handlePrint}
                        className="flex items-center gap-2 bg-slate-800 text-white px-4 py-2 rounded-lg shadow hover:bg-slate-700 transition-colors"
                    >
                        <Printer size={18} /> Print Report
                    </button>
                    <button
                        onClick={() => setIsModalOpen(true)}
                        className="flex items-center gap-2 bg-slate-900 text-white px-4 py-2 rounded-lg shadow hover:bg-slate-800 transition-colors"
                    >
                        <Plus size={18} />
                        Input Daily HM
                    </button>
                </div>
            </div>

            {/* Filters */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 flex gap-4 items-center shadow-sm">
                <div className="flex items-center gap-2 border rounded-lg px-3 py-2 bg-slate-50">
                    <Calendar size={16} className="text-slate-400" />
                    <label htmlFor="filter-date" className="sr-only">Filter by Date</label>
                    <input
                        id="filter-date"
                        type="date"
                        className="bg-transparent text-sm outline-none text-slate-700"
                        value={filterDate}
                        onChange={e => setFilterDate(e.target.value)}
                    />
                </div>
                <div className="flex items-center gap-2 border rounded-lg px-3 py-2 bg-slate-50 w-64">
                    <Search size={16} className="text-slate-400" />
                    <label htmlFor="filter-unit" className="sr-only">Filter by Unit Code</label>
                    <input
                        id="filter-unit"
                        type="text"
                        placeholder="Search Unit Code..."
                        className="bg-transparent text-sm outline-none w-full text-slate-700"
                        value={filterUnit}
                        onChange={e => setFilterUnit(e.target.value)}
                    />
                </div>
                <button onClick={() => { setFilterDate(''); setFilterUnit(''); }} className="text-xs text-blue-600 font-bold hover:underline">Clear Filters</button>
            </div>

            {/* Data Table */}
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
                <table className="w-full text-sm text-left">
                    <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                        <tr>
                            <th className="px-6 py-4">Date / Shift</th>
                            <th className="px-6 py-4">Unit Code</th>
                            <th className="px-6 py-4">Operator</th>
                            <th className="px-6 py-4">Location</th>
                            <th className="px-6 py-4 text-right">Start HM</th>
                            <th className="px-6 py-4 text-right">End HM</th>
                            <th className="px-6 py-4 text-right">Total Hours</th>
                            <th className="px-6 py-4">Activity</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {filteredLogs.map((log) => {
                            const operatorName = employees.find(e => e.id === log.operatorId)?.name || 'Unknown';
                            const locationName = getLocationName(log.locationId);
                            const unitCode = equipment.find(e => e.id === log.equipmentId)?.code || log.equipmentId;

                            return (
                                <tr key={log.id} className="hover:bg-slate-50 transition-colors">
                                    <td className="px-6 py-4">
                                        <div className="font-bold text-slate-800">{log.date}</div>
                                        <span className="text-[10px] font-bold uppercase bg-slate-100 text-slate-600 px-1.5 rounded">{log.shift}</span>
                                    </td>
                                    <td className="px-6 py-4 font-bold text-slate-900">{unitCode}</td>
                                    <td className="px-6 py-4 text-slate-600">{operatorName}</td>
                                    <td className="px-6 py-4 text-slate-500 text-xs">{locationName}</td>
                                    <td className="px-6 py-4 text-right font-mono text-slate-500">{log.startHM.toLocaleString()}</td>
                                    <td className="px-6 py-4 text-right font-mono text-slate-900 font-bold">{log.endHM.toLocaleString()}</td>
                                    <td className="px-6 py-4 text-right">
                                        <span className="bg-green-100 text-green-800 font-bold px-2 py-1 rounded text-xs">
                                            {log.totalHours} hrs
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-slate-500 text-xs">
                                        {log.activityCode || '-'}
                                        <div className="italic text-slate-400">{log.remarks}</div>
                                    </td>
                                </tr>
                            );
                        })}
                        {filteredLogs.length === 0 && (
                            <tr><td colSpan={8} className="p-8 text-center text-slate-400 italic">No timesheet records found.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>

            {/* Hidden PDF Report Template */}
            <div className="hidden">
                <div ref={printRef} className="p-8 max-w-[210mm] mx-auto bg-white text-slate-900 font-sans">
                    {/* JPM Standard Letterhead */}
                    <div className="flex justify-between items-start mb-2">
                        <div className="flex items-center gap-6">
                            <div className="flex flex-col items-center leading-none">
                                <div className="flex items-baseline font-black tracking-tighter text-red-600" style={{ fontFamily: 'Arial, sans-serif' }}>
                                    <span className="text-6xl">J</span><span className="text-6xl lowercase -ml-1">p</span><span className="text-6xl -ml-1">M</span>
                                </div>
                            </div>
                            <div className="h-16 w-px bg-slate-300"></div>
                            <div className="flex flex-col justify-center h-full pt-1">
                                <h1 className="text-2xl font-extrabold text-red-600 tracking-tight leading-none">PT JAVA PERSADA MANDIRI</h1>
                                <p className="text-xs font-bold text-blue-900 tracking-[0.35em] mt-1">GENERAL CONTRACTOR</p>
                            </div>
                        </div>
                        <div className="text-right text-[9px] text-slate-700 leading-tight font-medium">
                            <p className="font-bold text-slate-900 mb-1">HEAD OFFICE</p>
                            <p>Jl.Trikora RT.11 RW.02 No.57</p>
                            <p>Kel.Gt Manggis, Banjarbaru</p>
                            <p>Kalimantan Selatan 70721</p>
                        </div>
                    </div>
                    <div className="w-full border-t-4 border-slate-900 mb-0.5"></div>
                    <div className="w-full border-t border-slate-400 mb-8"></div>

                    {/* Report Title */}
                    <div className="flex justify-between items-end mb-6">
                        <div>
                            <h2 className="text-xl font-bold uppercase text-slate-900">Equipment Daily Timesheet</h2>
                            <p className="text-xs text-slate-500 uppercase font-bold">Report Period: {filterDate || 'All Time'}</p>
                        </div>
                        <div className="text-right text-xs">
                            <p>Generated: {new Date().toLocaleDateString()}</p>
                            <p>Unit Filter: {filterUnit || 'All Units'}</p>
                        </div>
                    </div>

                    <table className="w-full text-xs border-collapse border border-slate-300 mb-6">
                        <thead className="bg-slate-100">
                            <tr>
                                <th className="border border-slate-300 p-2 text-left">Date</th>
                                <th className="border border-slate-300 p-2 text-left">Shift</th>
                                <th className="border border-slate-300 p-2 text-left">Unit</th>
                                <th className="border border-slate-300 p-2 text-left">Operator</th>
                                <th className="border border-slate-300 p-2 text-right">Start HM</th>
                                <th className="border border-slate-300 p-2 text-right">End HM</th>
                                <th className="border border-slate-300 p-2 text-right">Hours</th>
                                <th className="border border-slate-300 p-2 text-left">Activity/Remarks</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredLogs.map(log => (
                                <tr key={log.id}>
                                    <td className="border border-slate-300 p-2">{log.date}</td>
                                    <td className="border border-slate-300 p-2">{log.shift}</td>
                                    <td className="border border-slate-300 p-2 font-bold">{equipment.find(e => e.id === log.equipmentId)?.code}</td>
                                    <td className="border border-slate-300 p-2">{employees.find(e => e.id === log.operatorId)?.name}</td>
                                    <td className="border border-slate-300 p-2 text-right">{log.startHM}</td>
                                    <td className="border border-slate-300 p-2 text-right">{log.endHM}</td>
                                    <td className="border border-slate-300 p-2 text-right font-bold">{log.totalHours}</td>
                                    <td className="border border-slate-300 p-2">{log.activityCode} {log.remarks ? `(${log.remarks})` : ''}</td>
                                </tr>
                            ))}
                        </tbody>
                        <tfoot>
                            <tr className="bg-slate-50 font-bold">
                                <td colSpan={6} className="border border-slate-300 p-2 text-right">TOTAL WORKING HOURS</td>
                                <td className="border border-slate-300 p-2 text-right">
                                    {filteredLogs.reduce((acc, curr) => acc + curr.totalHours, 0).toFixed(1)}
                                </td>
                                <td className="border border-slate-300 p-2"></td>
                            </tr>
                        </tfoot>
                    </table>

                    {/* Summary by Unit */}
                    <div className="mb-8 break-inside-avoid">
                        <h3 className="text-xs font-bold text-slate-500 uppercase mb-2">Summary by Unit</h3>
                        <div className="grid grid-cols-4 gap-4">
                            {unitSummary.map(([code, hours]) => (
                                <div key={code} className="border border-slate-200 p-2 flex justify-between items-center text-xs">
                                    <span className="font-bold">{code}</span>
                                    <span className="bg-slate-100 px-2 rounded">{hours.toFixed(1)} hrs</span>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="mt-12 flex justify-between px-12 text-center text-xs break-inside-avoid">
                        <div>
                            <div className="h-16 border-b border-black w-32 mb-1"></div>
                            <p>Supervisor</p>
                        </div>
                        <div>
                            <div className="h-16 border-b border-black w-32 mb-1"></div>
                            <p>Project Manager</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Input Modal */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg p-6 animate-fade-in">
                        <div className="flex justify-between items-start mb-6 border-b border-slate-100 pb-4">
                            <div>
                                <h3 className="text-xl font-bold text-slate-900">Input Daily HM</h3>
                                <p className="text-xs text-slate-500 mt-1">Updates Asset Master Data automatically</p>
                            </div>
                            <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
                        </div>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label htmlFor="ts-date" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Date</label>
                                    <input
                                        id="ts-date"
                                        type="date" required
                                        className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                                        value={formData.date}
                                        onChange={e => setFormData({ ...formData, date: e.target.value })}
                                    />
                                </div>
                                <div>
                                    <label htmlFor="ts-shift" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Shift</label>
                                    <select
                                        id="ts-shift"
                                        className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                                        value={formData.shift}
                                        onChange={e => setFormData({ ...formData, shift: e.target.value })}
                                    >
                                        <option value="Day">Day</option>
                                        <option value="Night">Night</option>
                                    </select>
                                </div>
                            </div>

                            <div>
                                <SearchableSelect
                                    label="Select Equipment"
                                    options={equipmentOptions}
                                    value={formData.equipmentId}
                                    onChange={handleUnitSelect}
                                    required
                                    id="ts-equipment"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4 bg-slate-50 p-3 rounded-lg border border-slate-200">
                                <div>
                                    <label htmlFor="ts-start-hm" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Start HM</label>
                                    <input
                                        id="ts-start-hm"
                                        type="number" required
                                        className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm bg-slate-100 text-slate-600"
                                        value={formData.startHM}
                                        readOnly
                                    />
                                </div>
                                <div>
                                    <label htmlFor="ts-end-hm" className="block text-xs font-bold text-green-700 mb-1 uppercase">End HM</label>
                                    <input
                                        id="ts-end-hm"
                                        type="number" required min={formData.startHM}
                                        className="w-full border border-green-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-green-500 font-bold text-slate-900"
                                        value={formData.endHM}
                                        onChange={e => setFormData({ ...formData, endHM: Number(e.target.value) })}
                                    />
                                </div>
                            </div>

                            <div className="flex items-center justify-between p-3 bg-blue-50 rounded text-blue-800 font-bold text-sm">
                                <span>Calculated Hours:</span>
                                <span className="text-xl">{calculatedHours} hrs</span>
                            </div>

                            <div>
                                <SearchableSelect label="Operator" options={operatorOptions} value={formData.operatorId} onChange={v => setFormData({ ...formData, operatorId: v })} required id="ts-operator" />
                            </div>

                            <div>
                                <SearchableSelect label="Location" options={locationOptions} value={formData.locationId} onChange={v => setFormData({ ...formData, locationId: v })} required id="ts-location" />
                            </div>

                            <div>
                                <label htmlFor="ts-activity" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Activity / Code</label>
                                <input
                                    id="ts-activity"
                                    type="text"
                                    className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                                    value={formData.activityCode}
                                    onChange={e => setFormData({ ...formData, activityCode: e.target.value })}
                                    placeholder="e.g. OB Removal"
                                />
                            </div>

                            <div>
                                <label htmlFor="ts-remarks" className="block text-xs font-bold text-slate-500 mb-1 uppercase">Remarks</label>
                                <input
                                    id="ts-remarks"
                                    type="text"
                                    className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                                    value={formData.remarks}
                                    onChange={e => setFormData({ ...formData, remarks: e.target.value })}
                                />
                            </div>

                            <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-slate-100">
                                <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-slate-600 font-medium hover:bg-slate-100 rounded-lg">Cancel</button>
                                <button type="submit" className="px-4 py-2 bg-slate-900 text-white font-medium rounded-lg hover:bg-slate-800 flex items-center gap-2">
                                    <Clock size={16} /> Save Timesheet
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default TimesheetView;