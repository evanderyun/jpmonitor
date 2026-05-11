
import React, { useState, useMemo, useEffect, useRef, useCallback } from 'react';
import { employeesAPI, suppliersAPI, maintenanceAPI, equipmentAPI, inventoryAPI, mutationsAPI } from '../services/api';
import { useQueryClient } from '@tanstack/react-query'
import { Wrench, Clock, ClipboardList, Plus, Edit, X, Printer, PieChart, Car, Filter, Trash2, Fuel, Utensils, UserPlus, ShoppingBag, Users, Search, Gauge, ArrowRightLeft, Truck, CheckCircle } from 'lucide-react'; // Removed History
import { Equipment, MaintenanceRecord, InventoryTransaction, SparePart } from '../types';
import SearchableSelect from './SearchableSelect';
import FleetDashboard from './FleetDashboard';

// Helper Functions defined outside component to be stable
const calculateEquipmentFinancials = (
    equipmentId: string,
    startDate: string,
    endDate: string,
    maintenanceRecords: MaintenanceRecord[],
    partsHistory: InventoryTransaction[]
) => {
    const start = new Date(startDate);
    const end = new Date(endDate);

    const periodLogs = maintenanceRecords.filter(log => {
        const d = new Date(log.startDate);
        return log.equipmentId === equipmentId && d >= start && d <= end;
    });

    const periodParts = partsHistory.filter(tx => {
        const d = new Date(tx.date);
        return tx.equipmentId === equipmentId &&
            tx.type === 'USAGE' &&
            d >= start && d <= end;
    });

    let partsCost = 0;
    let storingCost = 0;
    let externalServiceCost = 0;

    const details: any[] = [];

    periodLogs.forEach(log => {
        const mCost = (log.mechanicStoringCost || 0) + (log.mechanicMealCost || 0) + (log.driverStoringCost || 0);
        const eCost = log.externalCost || 0;

        storingCost += mCost;
        externalServiceCost += eCost;

        if (mCost > 0) {
            details.push({
                date: log.startDate,
                type: 'Operational Overhead',
                description: `WO: ${log.woNumber} (Meals/Allowances)`,
                downtime: 0,
                cost: mCost,
                isCash: true
            });
        }

        if (eCost > 0) {
            details.push({
                date: log.startDate,
                type: 'External Service',
                description: `WO: ${log.woNumber} - ${log.description}`,
                downtime: 0,
                cost: eCost,
                isCash: true
            });
        }
    });

    periodParts.forEach(tx => {
        const cost = (tx.quantity || 0) * (tx.pricePerUnit || 0);
        partsCost += cost;

        details.push({
            date: tx.date,
            type: 'Spare Parts',
            description: `Part Usage: ${tx.partId} (Qty: ${tx.quantity})`,
            downtime: 0,
            cost: cost,
            isCash: true
        });
    });

    const totalCashCost = partsCost + storingCost + externalServiceCost;

    return {
        totalCashCost,
        partsCost,
        storingCost,
        externalServiceCost,
        details: details.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
    };
};

const calculateAssetReliabilityMetrics = (
    equipmentId: string,
    startDate: string,
    endDate: string,
    maintenanceRecords: MaintenanceRecord[]
) => {
    const start = new Date(startDate);
    const end = new Date(endDate);

    const totalDays = Math.max(1, (end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
    const totalPeriodHours = totalDays * 24;

    const periodLogs = maintenanceRecords.filter(log => {
        const d = new Date(log.startDate);
        return log.equipmentId === equipmentId && d >= start && d <= end;
    });

    let totalDowntimeHours = 0;
    let failureCount = 0;

    periodLogs.forEach(log => {
        let duration = log.durationHours || 0;
        if (!duration && log.endDate && log.endTime && log.startTime) {
            const s = new Date(`${log.startDate}T${log.startTime}`);
            const e = new Date(`${log.endDate}T${log.endTime}`);
            duration = (e.getTime() - s.getTime()) / (1000 * 60 * 60);
        }

        totalDowntimeHours += duration;

        if (log.type === 'Corrective') {
            failureCount++;
        }
    });

    const operationalHours = Math.max(0, totalPeriodHours - totalDowntimeHours);
    const pa = totalPeriodHours > 0 ? Math.round((operationalHours / totalPeriodHours) * 100) : 100;
    const mtbf = failureCount > 0 ? Math.round(operationalHours / failureCount) : operationalHours;
    const mttr = failureCount > 0 ? Math.round(totalDowntimeHours / failureCount) : 0;

    return {
        pa,
        mtbf,
        mttr,
        totalDowntimeHours: Math.round(totalDowntimeHours),
        failureCount
    };
};

const FleetView: React.FC = () => {
    const [viewMode, setViewMode] = useState<'dashboard' | 'list'>('dashboard');

    const [equipment, setEquipment] = useState<Equipment[]>([]);
    const [employees, setEmployees] = useState<any[]>([]);
    const [suppliers, setSuppliers] = useState<any[]>([]);
    const [maintenanceRecords, setMaintenanceRecords] = useState<any[]>([]);
    const [spareParts, setSpareParts] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    
    const [selectedEquipment, setSelectedEquipment] = useState<Equipment | null>(null);
    const [partsHistory, setPartsHistory] = useState<InventoryTransaction[]>([]);
    const [mutationHistory, setMutationHistory] = useState<any[]>([]);
    const [activeModalTab, setActiveModalTab] = useState<'maintenance' | 'parts' | 'finance' | 'mutations'>('maintenance');

    // Filter State
    const [filterType, setFilterType] = useState<string>('All');
    const [filterStatus, setFilterStatus] = useState<string>('All');
    const [woFilterStatus, setWoFilterStatus] = useState<string>('ALL');
    const [searchTerm, setSearchTerm] = useState('');

    // Reporting State
    const [costStartDate, setCostStartDate] = useState(new Date().getFullYear() + '-01-01');
    const [costEndDate, setCostEndDate] = useState(new Date().toISOString().split('T')[0]);
    const [financialData, setFinancialData] = useState<any>(null);
    const [reliabilityData, setReliabilityData] = useState<any>(null);

    const printRef = useRef<HTMLDivElement>(null);

    // Form State
    interface LogFormState {
        startDate: string;
        startTime: string;
        endDate: string;
        endTime: string;
        type: string;
        priority: string;
        damageType: string;
        description: string;
        notes: string;
        status: string;
        serviceProvider: 'INTERNAL' | 'EXTERNAL';
        technicians: string[];
        mechanicStoringCost: number;
        mechanicMealCost: number;
        driverStoringCost: number;
        supplierId: string;
        externalInvoiceNumber: string;
        externalCost: number;
        useDriver: boolean;
    }

    const [logForm, setLogForm] = useState<LogFormState>({
        startDate: new Date().toISOString().split('T')[0],
        startTime: '08:00',
        endDate: new Date().toISOString().split('T')[0],
        endTime: '17:00',
        type: 'Corrective',
        priority: 'HIGH',
        damageType: 'Hydraulic',
        description: '',
        notes: '',
        status: 'OPEN',
        serviceProvider: 'INTERNAL',
        technicians: [],
        mechanicStoringCost: 0,
        mechanicMealCost: 0,
        driverStoringCost: 0,
        supplierId: '',
        externalInvoiceNumber: '',
        externalCost: 0,
        useDriver: false
    });

    const [editingLog, setEditingLog] = useState<MaintenanceRecord | null>(null);
    const [editForm, setEditForm] = useState({
        status: '',
        notes: '',
        endDate: '',
        endTime: '',
        hmAtStart: 0,
        mechanicStoringCost: 0,
        mechanicMealCost: 0,
        driverStoringCost: 0,
        useDriver: false,
        externalInvoiceNumber: '',
        externalCost: 0
    });

    const [prediction, setPrediction] = useState<{ nextHM: number; type: string; isMajor: boolean } | null>(null);
    const [selectedTechToAdd, setSelectedTechToAdd] = useState<string>('');
    const [selectedPartId, setSelectedPartId] = useState<string>('');
    const [selectedPartQty, setSelectedPartQty] = useState<number>(1);
    const [tempUsedParts, setTempUsedParts] = useState<{ part: SparePart, qty: number }[]>([]);

    const qc = useQueryClient();

    const loadReferenceData = useCallback(async () => {
        try {
            setLoading(true);
            const empP = employeesAPI.getEmployees()
            const supP = suppliersAPI.getSuppliers()
            const maintP = maintenanceAPI.getMaintenanceRecords()
            await Promise.all([
                qc.prefetchQuery({ queryKey: ['equipment'], queryFn: () => equipmentAPI.getEquipment() }),
                qc.prefetchQuery({ queryKey: ['inventory', 'parts'], queryFn: () => inventoryAPI.getParts() }),
                empP,
                supP,
                maintP
            ])
            const equipData = qc.getQueryData<any[]>(['equipment']) || []
            const partsData = qc.getQueryData<any[]>(['inventory', 'parts']) || []
            const [empData, supData, maintData] = await Promise.all([empP, supP, maintP])
            setEmployees(empData)
            setSuppliers(supData)
            setMaintenanceRecords(maintData)
            setEquipment(equipData)
            setSpareParts(partsData)
        } catch (err: any) {
            if (err?.name === 'AbortError') return;
            console.error('Failed to load reference data:', err);
        } finally {
            setLoading(false);
        }
    }, [qc]);

    useEffect(() => {
        loadReferenceData();
    }, [loadReferenceData]);

    const filteredEquipment = useMemo(() => {
        return equipment.filter(eq => {
            const typeMatch = filterType === 'All' || eq.type === filterType;
            const statusMatch = filterStatus === 'All' || eq.status === filterStatus;
            const hideSoldDefault = filterStatus === 'All' ? (eq.status !== 'Sold' && eq.status !== 'Scrapped') : true;

            const searchLower = searchTerm.toLowerCase();
            const searchMatch = eq.code.toLowerCase().includes(searchLower) ||
                eq.model.toLowerCase().includes(searchLower) ||
                (eq.owner || '').toLowerCase().includes(searchLower) ||
                (eq.chassisNumber || '').toLowerCase().includes(searchLower) ||
                (eq.serialNumber || '').toLowerCase().includes(searchLower) ||
                (eq.engineNumber || '').toLowerCase().includes(searchLower);

            return typeMatch && statusMatch && searchMatch && (filterStatus !== 'All' ? true : hideSoldDefault);
        });
    }, [equipment, filterType, filterStatus, searchTerm]);

    const filteredLogs = useMemo(() => {
        return maintenanceRecords.filter(log => {
            return log.equipmentId === selectedEquipment?.id && (woFilterStatus === 'ALL' || log.status === woFilterStatus);
        });
    }, [maintenanceRecords, selectedEquipment, woFilterStatus]);

    const reportLogs = useMemo(() => {
        return maintenanceRecords.filter(log => {
            const d = log.endDate || log.startDate;
            return log.equipmentId === selectedEquipment?.id && d >= costStartDate && d <= costEndDate;
        }).sort((a, b) => new Date(a.startDate).getTime() - new Date(b.startDate).getTime());
    }, [maintenanceRecords, selectedEquipment, costStartDate, costEndDate]);

    useEffect(() => {
        if (selectedEquipment && logForm.type === 'Preventive') {
            const currentHM = selectedEquipment.hourMeter > 0 ? selectedEquipment.hourMeter : (selectedEquipment.kilometer || 0);
            const nextInterval = Math.ceil((currentHM + 1) / 250) * 250;
            let pmType = 'PM250 (General)';
            let isMajor = false;
            if (nextInterval % 2000 === 0) { pmType = 'PM2000 (Major Overhaul)'; isMajor = true; }
            else if (nextInterval % 1000 === 0) { pmType = 'PM1000 (Major)'; isMajor = true; }
            else if (nextInterval % 500 === 0) { pmType = 'PM500 (Medium Service)'; }
            setPrediction({ nextHM: nextInterval, type: pmType, isMajor });
        } else {
            setPrediction(null);
        }
    }, [selectedEquipment, logForm.type]);

    useEffect(() => {
        if (selectedEquipment && activeModalTab === 'finance') {
            const finData = calculateEquipmentFinancials(selectedEquipment.id, costStartDate, costEndDate, maintenanceRecords, partsHistory);
            setFinancialData(finData);
            const relData = calculateAssetReliabilityMetrics(selectedEquipment.id, costStartDate, costEndDate, maintenanceRecords);
            setReliabilityData(relData);
        }
    }, [selectedEquipment, activeModalTab, costStartDate, costEndDate, maintenanceRecords, partsHistory]);

    const toggleStatus = async (id: string, currentStatus: string) => {
        if (currentStatus === 'Sold' || currentStatus === 'Scrapped') return;
        const newStatus = currentStatus === 'Operational' ? 'Breakdown' : 'Operational';
        const reason = newStatus === 'Breakdown' ? 'Reported by operator' : 'Repaired';
        try {
            await equipmentAPI.updateEquipment(id, { status: newStatus, notes: reason });
            await loadReferenceData();
        } catch (err: any) {
            alert('Failed to update status: ' + (err.message || 'Unknown error'));
        }
    };

    const openDetails = async (eq: Equipment) => {
        setSelectedEquipment(eq);
        
        try {
            const partsData = await inventoryAPI.getTransactions();
            const equipmentParts = partsData.filter((tx: any) => tx.equipmentId === eq.id);
            setPartsHistory(equipmentParts);
        } catch (err) {
            console.error('Failed to load parts history:', err);
            setPartsHistory([]);
        }

        try {
            const mutData = await mutationsAPI.getMutations({ equipmentId: eq.id });
            setMutationHistory(mutData.map((m: any) => ({
                ...m,
                departureDate: m.departure_date,
                arrivalDate: m.arrival_date,
                equipmentCode: m.equipment_code,
                mutationHM: m.mutation_hm,
                sourceLocation: m.source_location,
                targetLocation: m.target_location,
                referenceDocument: m.reference_document
            })));
        } catch (err) {
            console.error("Failed to fetch mutations", err);
            setMutationHistory([]);
        }

        setActiveModalTab('maintenance');
        setEditingLog(null);
        setLogForm({
            startDate: new Date().toISOString().split('T')[0],
            startTime: '08:00',
            endDate: new Date().toISOString().split('T')[0],
            endTime: '17:00',
            type: 'Corrective',
            priority: 'HIGH',
            damageType: 'Hydraulic',
            description: '',
            notes: '',
            status: 'OPEN',
            serviceProvider: 'INTERNAL',
            technicians: [],
            mechanicStoringCost: 0,
            mechanicMealCost: 0,
            driverStoringCost: 0,
            supplierId: '',
            externalInvoiceNumber: '',
            externalCost: 0,
            useDriver: false
        });
        setTempUsedParts([]);
    };

    const handleOpenEdit = (log: MaintenanceRecord) => {
        setEditingLog(log);
        setTempUsedParts([]);
        setEditForm({
            status: log.status,
            notes: log.notes || '',
            endDate: log.endDate || new Date().toISOString().split('T')[0],
            endTime: log.endTime || new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }),
            hmAtStart: log.hmAtStart,
            mechanicStoringCost: log.mechanicStoringCost || 0,
            mechanicMealCost: log.mechanicMealCost || 0,
            driverStoringCost: log.driverStoringCost || 0,
            useDriver: (log.driverStoringCost || 0) > 0,
            externalInvoiceNumber: log.externalInvoiceNumber || '',
            externalCost: log.externalCost || 0
        });
    };

    const handleSubmitEdit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!editingLog) return;
        try {
            await maintenanceAPI.updateMaintenanceRecord(
                editingLog.id,
                {
                    status: editForm.status as any,
                    notes: editForm.notes,
                    endDate: editForm.endDate,
                    endTime: editForm.endTime,
                    mechanicStoringCost: Number(editForm.mechanicStoringCost),
                    mechanicMealCost: Number(editForm.mechanicMealCost),
                    driverStoringCost: editForm.useDriver ? Number(editForm.driverStoringCost) : 0,
                    externalInvoiceNumber: editForm.externalInvoiceNumber,
                    externalCost: Number(editForm.externalCost)
                }
            );

            const updatedMaintData = await maintenanceAPI.getMaintenanceRecords();
            setMaintenanceRecords(updatedMaintData);

            if (selectedEquipment) {
                const partsData = await inventoryAPI.getTransactions();
                setPartsHistory(partsData.filter((tx: any) => tx.equipmentId === selectedEquipment.id));
            }
            setEditingLog(null);
            setTempUsedParts([]);
        } catch (err: any) {
            alert("Error updating WO: " + err.message);
        }
    };

    const handleDeleteLog = async () => {
        if (!editingLog || !selectedEquipment) return;
        if (window.confirm(`Delete Work Order ${editingLog.woNumber}?`)) {
            try {
                await maintenanceAPI.deleteMaintenanceRecord(editingLog.id);
                const updatedMaintData = await maintenanceAPI.getMaintenanceRecords();
                setMaintenanceRecords(updatedMaintData);
                setEditingLog(null);
            } catch (err: any) {
                alert("Error deleting WO: " + err.message);
            }
        }
    };

    const handleAddLog = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedEquipment) return;
        if (logForm.serviceProvider === 'INTERNAL' && logForm.technicians.length === 0) {
            alert("Please assign at least one technician for internal jobs.");
            return;
        }
        if (logForm.serviceProvider === 'EXTERNAL' && !logForm.supplierId) {
            alert("Please select a vendor/supplier for external jobs.");
            return;
        }

        try {
            await maintenanceAPI.createMaintenanceRecord({
                equipmentId: selectedEquipment.id,
                startDate: logForm.startDate,
                startTime: logForm.startTime,
                endDate: logForm.endDate,
                endTime: logForm.endTime,
                hmAtStart: selectedEquipment.hourMeter,
                type: logForm.type as any,
                damageType: logForm.damageType,
                priority: logForm.priority as any,
                status: logForm.status as any,
                description: logForm.description,
                notes: logForm.notes,
                serviceProvider: logForm.serviceProvider as any,
                technicians: logForm.technicians,
                mechanicStoringCost: Number(logForm.mechanicStoringCost),
                mechanicMealCost: Number(logForm.mechanicMealCost),
                driverStoringCost: logForm.useDriver ? Number(logForm.driverStoringCost) : 0,
                supplierId: logForm.supplierId || undefined,
                externalInvoiceNumber: logForm.externalInvoiceNumber,
                externalCost: Number(logForm.externalCost),
                partsReplaced: ''
            });

            const updatedMaintData = await maintenanceAPI.getMaintenanceRecords();
            setMaintenanceRecords(updatedMaintData);
            
            const partsData = await inventoryAPI.getTransactions();
            setPartsHistory(partsData.filter((tx: any) => tx.equipmentId === selectedEquipment.id));
            setLogForm(prev => ({ ...prev, description: '', notes: '', technicians: [], useDriver: false, driverStoringCost: 0 }));
        } catch (e: any) {
            alert(`Failed to save log: ${e.message}`);
        }
    };

    const handlePrint = () => {
        if (printRef.current) {
            const printWindow = window.open('', '', 'height=800,width=1000');
            if (printWindow) {
                printWindow.document.write('<html><head><title>JPM - Asset Performance Report</title>');
                printWindow.document.write('<script src="https://cdn.tailwindcss.com"></script>');
                printWindow.document.write('<style>@media print { body { -webkit-print-color-adjust: exact; } .page-break { page-break-before: always; } }</style>');
                printWindow.document.write('</head><body class="bg-white">');
                printWindow.document.write(printRef.current.innerHTML); // Need to preserve wrapper
                printWindow.document.write('</body></html>');
                printWindow.document.close();
                setTimeout(() => { printWindow.print(); printWindow.close(); }, 1000);
            }
        }
    };

    const handleAddPartToLog = () => {
        if (!selectedPartId) return;
        const part = spareParts.find(p => p.id === selectedPartId);
        if (part && selectedPartQty <= part.currentStock) {
            setTempUsedParts([...tempUsedParts, { part, qty: selectedPartQty }]);
            setSelectedPartId('');
            setSelectedPartQty(1);
        } else {
            alert("Insufficient stock");
        }
    };

    const partOptions = useMemo(() => spareParts.filter(p => !tempUsedParts.some(used => used.part.id === p.id)).map(p => ({ value: p.id, label: `${p.name} (Stock: ${p.currentStock})`, subLabel: p.partNumber })), [tempUsedParts, spareParts]);
    const technicianOptions = useMemo(() => employees.filter((e: any) => (e.department === 'Maintenance' || e.role === 'Mechanic') && !logForm.technicians.includes(e.name)).map((e: any) => ({ value: e.name, label: e.name, subLabel: e.position })), [logForm.technicians, employees]);
    const supplierOptions = useMemo(() => suppliers.map((s: any) => ({ value: s.id, label: s.name, subLabel: s.type })), [suppliers]);

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'OPEN': return 'bg-blue-100 text-blue-800 border-blue-200';
            case 'IN_PROGRESS': return 'bg-amber-100 text-amber-800 border-amber-200';
            case 'WAITING_PART': return 'bg-purple-100 text-purple-800 border-purple-200';
            case 'CLOSED': return 'bg-green-100 text-green-800 border-green-200';
            case 'CANCEL': return 'bg-slate-100 text-slate-600 border-slate-200';
            default: return 'bg-slate-100 text-slate-600 border-slate-200';
        }
    };

    const getFinancialRatios = () => {
        if (!financialData || financialData.totalCashCost === 0) return { partsPct: 0, fuelPct: 0, extPct: 0, primaryDriver: 'None' };
        const partsPct = Math.round((financialData.partsCost / financialData.totalCashCost) * 100);
        const fuelPct = Math.round((financialData.storingCost / financialData.totalCashCost) * 100);
        const extPct = Math.round((financialData.externalServiceCost / financialData.totalCashCost) * 100);

        let primaryDriver = 'Balanced';
        if (partsPct > 50) primaryDriver = 'Spare Parts (Aging Unit)';
        if (fuelPct > 50) primaryDriver = 'Operational Overhead';
        if (extPct > 50) primaryDriver = 'External Repairs';

        return { partsPct, fuelPct, extPct, primaryDriver };
    }

    const ratios = getFinancialRatios();

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold text-slate-800">Fleet & Plant Management</h2>
                <div className="flex bg-white rounded-lg p-1 border border-slate-200 shadow-sm">
                    <button
                        onClick={() => setViewMode('dashboard')}
                        className={`px-4 py-2 text-sm font-bold rounded-md transition-all ${viewMode === 'dashboard' ? 'bg-slate-900 text-white' : 'text-slate-500 hover:bg-slate-100'}`}
                    >
                        Analytics Dashboard
                    </button>
                    <button
                        onClick={() => setViewMode('list')}
                        className={`px-4 py-2 text-sm font-bold rounded-md transition-all ${viewMode === 'list' ? 'bg-slate-900 text-white' : 'text-slate-500 hover:bg-slate-100'}`}
                    >
                        Asset List
                    </button>
                </div>
            </div>

            {loading && !equipment.length ? (
                <div className="flex items-center justify-center h-64"><div className="text-slate-500">Loading...</div></div>
            ) : (
                viewMode === 'dashboard' ? (
                    <FleetDashboard />
                ) : (
                    <>
                        {/* Filters */}
                        <div className="bg-white p-4 rounded-lg border border-slate-200 flex gap-4 items-center shadow-sm justify-between">
                            <div className="flex gap-4 items-center">
                                <Filter size={16} className="text-slate-500" />
                                <label htmlFor="fleet-filter-type" className="sr-only">Filter Type</label>
                                <select id="fleet-filter-type" className="border rounded px-2 py-1 text-sm bg-white text-slate-800 outline-none" value={filterType} onChange={e => setFilterType(e.target.value)}>
                                    <option value="All">All Types</option>
                                    {['Excavator', 'Dump Truck', 'Dozer', 'Grader', 'LV', 'Water Truck', 'Tower Lamp', 'Pump'].map(t => <option key={t} value={t}>{t}</option>)}
                                </select>
                                <label htmlFor="fleet-filter-status" className="sr-only">Filter Status</label>
                                <select id="fleet-filter-status" className="border rounded px-2 py-1 text-sm bg-white text-slate-800 outline-none" value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
                                    <option value="All">Active Status</option>
                                    {['Operational', 'Breakdown', 'Maintenance', 'Sold', 'Scrapped'].map(s => <option key={s} value={s}>{s}</option>)}
                                </select>
                            </div>
                            {/* Search */}
                            <div className="relative">
                                <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                                <label htmlFor="fleet-search-input" className="sr-only">Search Unit</label>
                                <input
                                    id="fleet-search-input"
                                    type="text"
                                    placeholder="Search Unit, Owner, Serial..."
                                    className="text-sm border border-slate-300 rounded-lg pl-9 pr-3 py-1.5 outline-none focus:ring-2 focus:ring-blue-500 w-64"
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </div>
                        </div>

                        {/* Equipment Grid */}
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                            {filteredEquipment.map(eq => (
                                <div key={eq.id} className={`bg-white p-5 rounded-xl shadow-sm border border-slate-200 ${eq.status === 'Sold' ? 'opacity-75 bg-slate-50' : ''}`}>
                                    <div className="flex justify-between items-start mb-2">
                                        <div>
                                            <h3 className="font-bold text-lg text-slate-900">{eq.code}</h3>
                                            <div className="flex items-center gap-2">
                                                <p className="text-slate-500 text-sm">{eq.model} • {eq.manufactureYear || 'N/A'}</p>
                                                <span className="text-[10px] bg-slate-100 text-slate-500 px-1.5 rounded font-bold uppercase">{eq.type}</span>
                                            </div>
                                        </div>
                                        <span className={`px-2 py-1 rounded text-xs font-bold border ${eq.status === 'Operational' ? 'bg-green-100 text-green-800 border-green-200' :
                                            eq.status === 'Breakdown' ? 'bg-red-100 text-red-800 border-red-200' :
                                                'bg-slate-100 text-slate-800'
                                            }`}>{eq.status}</span>
                                    </div>

                                    <div className="grid grid-cols-2 gap-2 text-xs text-slate-600 mb-4 border-t border-b border-slate-50 py-2">
                                        <div>
                                            <span className="block text-[10px] text-slate-400 uppercase">Owner</span>
                                            <span className="font-bold">{eq.owner || '-'}</span>
                                        </div>
                                        <div>
                                            <span className="block text-[10px] text-slate-400 uppercase">Chassis No.</span>
                                            <span className="font-mono">{eq.chassisNumber || '-'}</span>
                                        </div>
                                        <div>
                                            <span className="block text-[10px] text-slate-400 uppercase">Serial No.</span>
                                            <span className="font-mono">{eq.serialNumber || '-'}</span>
                                        </div>
                                        <div>
                                            <span className="block text-[10px] text-slate-400 uppercase">Engine No.</span>
                                            <span className="font-mono">{eq.engineNumber || '-'}</span>
                                        </div>
                                        {(eq.type === 'LV' || eq.type === 'Dump Truck' || eq.type === 'Water Truck') && (
                                            <div className="col-span-2">
                                                <span className="block text-[10px] text-slate-400 uppercase">Plate No.</span>
                                                <span className="bg-slate-100 px-1 rounded font-mono font-bold">{eq.plateNumber || '-'}</span>
                                            </div>
                                        )}
                                    </div>

                                    <div className="flex justify-between items-center text-sm text-slate-600 mb-4">
                                        <span className="flex items-center font-bold text-slate-800">
                                            <Gauge size={16} className="inline mr-1 text-blue-500" />
                                            {eq.hourMeter > 0 ? `${eq.hourMeter.toLocaleString()} HM` : `${(eq.kilometer || 0).toLocaleString()} KM`}
                                        </span>
                                        <span className="text-xs bg-slate-50 px-2 py-1 rounded">{eq.location}</span>
                                    </div>
                                    <div className="flex justify-between pt-4 border-t border-slate-100">
                                        <button onClick={() => openDetails(eq)} className="text-blue-600 text-xs font-bold flex items-center gap-1 hover:underline">
                                            <ClipboardList size={14} /> View History
                                        </button>
                                        {eq.status !== 'Sold' && eq.status !== 'Scrapped' && (
                                            <button onClick={() => toggleStatus(eq.id, eq.status)} className="text-xs font-medium text-slate-500 hover:text-slate-800">
                                                Change Status
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))}
                            {filteredEquipment.length === 0 && (
                                <div className="col-span-full text-center py-10 text-slate-400">
                                    No units found matching your filters.
                                </div>
                            )}
                        </div>
                    </>
                )
            )}

            {/* Modal */}
            {selectedEquipment && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-6xl h-[95vh] flex flex-col animate-fade-in overflow-hidden">
                        <div className="bg-slate-900 text-white p-4 flex justify-between items-center shrink-0">
                            <h3 className="font-bold flex items-center gap-2"><Wrench size={20} /> {selectedEquipment.code} Details</h3>
                            <button onClick={() => setSelectedEquipment(null)}><X size={20} className="text-slate-400 hover:text-white" /></button>
                        </div>

                        <div className="flex border-b border-slate-200 bg-slate-50">
                            {['maintenance', 'parts', 'finance', 'mutations'].map(tab => (
                                <button key={tab} onClick={() => setActiveModalTab(tab as any)}
                                    className={`px-6 py-3 text-sm font-medium capitalize border-b-2 ${activeModalTab === tab ? 'border-blue-600 text-blue-600 bg-white' : 'border-transparent text-slate-500'}`}>
                                    {tab === 'finance' ? 'Cost & ROI' : tab}
                                </button>
                            ))}
                        </div>

                        <div className="flex-1 flex overflow-hidden bg-white">
                            {activeModalTab === 'maintenance' ? (
                                <div className="flex w-full h-full">
                                    {/* Left: List */}
                                    <div className="w-3/5 border-r border-slate-200 overflow-y-auto bg-slate-50">
                                        <div className="bg-white p-3 border-b flex gap-2 sticky top-0 z-10 shadow-sm">
                                            <label htmlFor="wo-filter-status" className="sr-only">Filter Work Order Status</label>
                                            <select id="wo-filter-status" className="text-xs border rounded bg-white text-slate-800 p-1" value={woFilterStatus} onChange={e => setWoFilterStatus(e.target.value)}>
                                                <option value="ALL">All Status</option>
                                                <option value="OPEN">Open</option>
                                                <option value="CLOSED">Closed</option>
                                            </select>
                                        </div>
                                        {filteredLogs.map(log => {
                                            const isSelected = editingLog?.id === log.id;
                                            return (
                                                <div key={log.id}
                                                    className={`p-4 border-b border-slate-100 cursor-pointer group relative transition-colors ${isSelected ? 'bg-blue-50 border-l-4 border-l-blue-500' : 'bg-white hover:bg-slate-50'}`}
                                                    role="button"
                                                    tabIndex={0}
                                                    onClick={() => handleOpenEdit(log)}
                                                    onKeyDown={(e) => {
                                                        if (e.key === 'Enter' || e.key === ' ') handleOpenEdit(log)
                                                    }}
                                                >
                                                    <div className="flex justify-between items-start mb-1">
                                                        <span className="font-mono font-bold text-xs text-slate-700">{log.woNumber}</span>
                                                        <span className={`text-[10px] px-2 py-0.5 rounded border font-bold ${getStatusColor(log.status)}`}>
                                                            {log.status.replace('_', ' ')}
                                                        </span>
                                                    </div>
                                                    <p className="text-sm font-medium text-slate-900 mt-1 line-clamp-2">{log.description}</p>
                                                    <div className="flex gap-2 mt-2 text-xs text-slate-500">
                                                        <span className="font-semibold">{log.serviceProvider === 'INTERNAL' ? 'Internal Team' : 'External Vendor'}</span>
                                                        <span>•</span>
                                                        <span>{log.startDate}</span>
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>

                                    {/* Right: Form */}
                                    <div className="w-2/5 p-6 overflow-y-auto bg-white">
                                        {editingLog ? (
                                            // UPDATE FORM
                                            <form onSubmit={handleSubmitEdit} className="space-y-4">
                                                <h4 className="font-bold text-blue-800 flex items-center gap-2 pb-2 border-b border-blue-100">
                                                    <Edit size={16} /> Update {editingLog.woNumber}
                                                </h4>
                                                <div className="bg-blue-50 p-3 rounded text-xs text-blue-900 mb-2 border border-blue-100">
                                                    Provider: <strong>{editingLog.serviceProvider}</strong>
                                                </div>

                                                <div>
                                                    <label htmlFor="edit-status" className="block text-xs font-bold text-slate-600 mb-1 uppercase">Status</label>
                                                    <select
                                                        id="edit-status"
                                                        className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900 outline-none focus:ring-2 focus:ring-blue-500"
                                                        value={editForm.status}
                                                        onChange={e => setEditForm({ ...editForm, status: e.target.value })}
                                                    >
                                                        <option value="OPEN">OPEN</option>
                                                        <option value="IN_PROGRESS">IN PROGRESS</option>
                                                        <option value="WAITING_PART">WAITING PART</option>
                                                        <option value="CLOSED">CLOSED</option>
                                                        <option value="CANCEL">CANCEL</option>
                                                    </select>
                                                </div>

                                                <div>
                                                    <label className="block text-xs font-bold text-slate-600 mb-1 uppercase">Finish Time</label>
                                                    <div className="grid grid-cols-2 gap-2">
                                                        <label htmlFor="edit-end-date" className="sr-only">End Date</label>
                                                        <input id="edit-end-date" type="date" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900" value={editForm.endDate} onChange={e => setEditForm({ ...editForm, endDate: e.target.value })} />
                                                        <label htmlFor="edit-end-time" className="sr-only">End Time</label>
                                                        <input id="edit-end-time" type="time" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900" value={editForm.endTime} onChange={e => setEditForm({ ...editForm, endTime: e.target.value })} />
                                                    </div>
                                                </div>

                                                <div>
                                                    <label htmlFor="edit-hm-start" className="block text-xs font-bold text-slate-600 mb-1 uppercase">HM Correction (Start)</label>
                                                    <input id="edit-hm-start" type="number" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900" value={editForm.hmAtStart} onChange={e => setEditForm({ ...editForm, hmAtStart: Number(e.target.value) })} />
                                                </div>

                                                <div>
                                                    <label htmlFor="edit-notes" className="block text-xs font-bold text-slate-600 mb-1 uppercase">Notes / Report</label>
                                                    <textarea id="edit-notes" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900" rows={3} value={editForm.notes} onChange={e => setEditForm({ ...editForm, notes: e.target.value })} />
                                                </div>

                                                {/* Internal Specifics Update */}
                                                {editingLog.serviceProvider === 'INTERNAL' && (
                                                    <div className="space-y-3 border-t border-slate-200 pt-3">
                                                        <label className="text-xs font-bold text-slate-600 uppercase">Mechanic Storing Cost</label>
                                                        <div className="grid grid-cols-2 gap-2">
                                                            <div>
                                                                <label htmlFor="edit-fuel-cost" className="text-[10px] text-slate-500 font-bold block mb-1"><Fuel size={10} /> Minyak Sarana (Fuel)</label>
                                                                <input id="edit-fuel-cost" type="number" className="w-full border border-slate-300 rounded p-1 text-sm bg-white text-slate-900" value={editForm.mechanicStoringCost} onChange={e => setEditForm({ ...editForm, mechanicStoringCost: Number(e.target.value) })} />
                                                            </div>
                                                            <div>
                                                                <label htmlFor="edit-meal-cost" className="text-[10px] text-slate-500 font-bold block mb-1"><Utensils size={10} /> Uang Makan (Meals)</label>
                                                                <input id="edit-meal-cost" type="number" className="w-full border border-slate-300 rounded p-1 text-sm bg-white text-slate-900" value={editForm.mechanicMealCost} onChange={e => setEditForm({ ...editForm, mechanicMealCost: Number(e.target.value) })} />
                                                            </div>
                                                        </div>

                                                        {/* DRIVER UPDATE */}
                                                        <div className="bg-slate-50 p-2 rounded border border-slate-200 mt-2">
                                                            <div className="flex items-center gap-2 mb-2">
                                                                <input
                                                                    type="checkbox"
                                                                    id="useDriverEdit"
                                                                    checked={editForm.useDriver}
                                                                    onChange={e => setEditForm({ ...editForm, useDriver: e.target.checked })}
                                                                    className="w-4 h-4 text-blue-600 rounded"
                                                                />
                                                                <label htmlFor="useDriverEdit" className="text-xs font-bold text-slate-600 flex items-center gap-1 cursor-pointer">
                                                                    <Car size={12} /> Using Dedicated Driver?
                                                                </label>
                                                            </div>
                                                            {editForm.useDriver && (
                                                                <div>
                                                                    <label htmlFor="edit-driver-cost" className="text-[10px] text-slate-500 font-bold block mb-1">Driver Meal/Allowance</label>
                                                                    <input id="edit-driver-cost" type="number" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900"
                                                                        value={editForm.driverStoringCost} onChange={e => setEditForm({ ...editForm, driverStoringCost: Number(e.target.value) })} placeholder="0" />
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                )}

                                                {/* External Specifics Update */}
                                                {editingLog.serviceProvider === 'EXTERNAL' && (
                                                    <div className="space-y-3 border-t border-slate-200 pt-3">
                                                        <label className="text-xs font-bold text-slate-600 uppercase">Vendor Invoice</label>
                                                        <label htmlFor="edit-ext-invoice" className="sr-only">Invoice Number</label>
                                                        <input id="edit-ext-invoice" type="text" placeholder="Invoice Number" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900" value={editForm.externalInvoiceNumber} onChange={e => setEditForm({ ...editForm, externalInvoiceNumber: e.target.value })} />
                                                        <label htmlFor="edit-ext-cost" className="sr-only">Total Cost</label>
                                                        <input id="edit-ext-cost" type="number" placeholder="Total Cost (IDR)" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900" value={editForm.externalCost} onChange={e => setEditForm({ ...editForm, externalCost: Number(e.target.value) })} />
                                                    </div>
                                                )}

                                                {/* Parts Usage */}
                                                {editingLog.serviceProvider === 'INTERNAL' && (
                                                    <div className="bg-slate-50 p-3 rounded border border-slate-200">
                                                        <label htmlFor="edit-part-select" className="text-xs font-bold text-slate-600 uppercase mb-2 block">Use Parts from Inventory</label>
                                                        <div className="flex gap-2 mb-2">
                                                            <div className="flex-1"><SearchableSelect options={partOptions} value={selectedPartId} onChange={setSelectedPartId} id="edit-part-select" className="bg-white" /></div>
                                                            <label htmlFor="edit-part-qty" className="sr-only">Quantity</label>
                                                            <input id="edit-part-qty" type="number" className="w-16 border border-slate-300 rounded p-1 text-center bg-white text-slate-900" value={selectedPartQty} onChange={e => setSelectedPartQty(Number(e.target.value))} />
                                                            <button type="button" onClick={handleAddPartToLog} className="bg-blue-600 text-white p-2 rounded hover:bg-blue-700"><Plus size={16} /></button>
                                                        </div>
                                                        {tempUsedParts.map((p, i) => (
                                                            <div key={i} className="text-xs flex justify-between bg-white p-2 mb-1 border border-slate-200 rounded shadow-sm">
                                                                <span className="font-bold text-slate-700">{p.part.name}</span><span className="text-blue-600 font-mono">x{p.qty}</span>
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}

                                                <div className="flex gap-2 pt-4">
                                                    <button type="submit" className="flex-1 bg-blue-600 text-white py-2 rounded shadow font-medium hover:bg-blue-700">Save Update</button>
                                                    <button type="button" onClick={handleDeleteLog} className="text-red-500 border border-red-200 px-3 rounded hover:bg-red-50"><Trash2 size={16} /></button>
                                                    <button type="button" onClick={() => setEditingLog(null)} className="text-slate-500 px-3 hover:text-slate-700">Cancel</button>
                                                </div>
                                            </form>
                                        ) : (
                                            // CREATE FORM
                                            <form onSubmit={handleAddLog} className="space-y-4">
                                                <h4 className="font-bold text-slate-800 flex items-center gap-2 pb-2 border-b border-slate-100"><Plus size={16} /> Create Work Order</h4>

                                                {/* Service Provider Toggle */}
                                                <div className="flex bg-slate-100 p-1 rounded-lg border border-slate-200">
                                                    <button type="button" onClick={() => setLogForm({ ...logForm, serviceProvider: 'INTERNAL' })}
                                                        className={`flex-1 py-1.5 text-xs font-bold rounded-md transition-all ${logForm.serviceProvider === 'INTERNAL' ? 'bg-white text-blue-700 shadow-sm border border-slate-200' : 'text-slate-500'}`}>
                                                        INTERNAL TEAM
                                                    </button>
                                                    <button type="button" onClick={() => setLogForm({ ...logForm, serviceProvider: 'EXTERNAL' })}
                                                        className={`flex-1 py-1.5 text-xs font-bold rounded-md transition-all ${logForm.serviceProvider === 'EXTERNAL' ? 'bg-white text-purple-700 shadow-sm border border-slate-200' : 'text-slate-500'}`}>
                                                        EXTERNAL VENDOR
                                                    </button>
                                                </div>

                                                <div className="grid grid-cols-2 gap-2">
                                                    <div>
                                                        <label htmlFor="create-type" className="block text-[10px] font-bold text-slate-500 mb-1 uppercase">Type</label>
                                                        <select id="create-type" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900 outline-none focus:ring-2 focus:ring-blue-500" value={logForm.type} onChange={e => setLogForm({ ...logForm, type: e.target.value })}>
                                                            <option value="Corrective">Corrective</option>
                                                            <option value="Preventive">Preventive</option>
                                                        </select>
                                                    </div>
                                                    <div>
                                                        <label htmlFor="create-priority" className="block text-[10px] font-bold text-slate-500 mb-1 uppercase">Priority</label>
                                                        <select id="create-priority" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900 outline-none focus:ring-2 focus:ring-blue-500" value={logForm.priority} onChange={e => setLogForm({ ...logForm, priority: e.target.value })}>
                                                            <option value="HIGH">High</option>
                                                            <option value="MEDIUM">Medium</option>
                                                            <option value="CRITICAL">Critical</option>
                                                        </select>
                                                    </div>
                                                </div>

                                                {prediction && logForm.type === 'Preventive' && (
                                                    <div className={`p-3 rounded border text-xs flex items-center gap-2 ${prediction.isMajor ? 'bg-red-50 text-red-700 border-red-200' : 'bg-green-50 text-green-700 border-green-200'}`}>
                                                        <Clock size={16} />
                                                        <div>
                                                            <span className="font-bold block">Recommended: {prediction.type}</span>
                                                            <span>Target HM: {prediction.nextHM}</span>
                                                        </div>
                                                    </div>
                                                )}

                                                <div>
                                                    <label htmlFor="create-description" className="block text-xs font-bold text-slate-600 mb-1 uppercase">Description</label>
                                                    <textarea id="create-description" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900 outline-none focus:ring-2 focus:ring-blue-500" value={logForm.description} onChange={e => setLogForm({ ...logForm, description: e.target.value })} required placeholder="Describe the issue..." />
                                                </div>

                                                {/* Dynamic Fields based on Provider */}
                                                {logForm.serviceProvider === 'INTERNAL' ? (
                                                    <div className="space-y-3 bg-slate-50 p-3 rounded border border-slate-200">
                                                        <label htmlFor="create-technician-select" className="text-xs font-bold text-slate-600 uppercase flex items-center gap-1"><Users size={12} /> Crew Assignment</label>
                                                        <div className="flex gap-2">
                                                            <div className="flex-1"><SearchableSelect placeholder="Select Mechanic" options={technicianOptions} value={selectedTechToAdd} onChange={setSelectedTechToAdd} className="bg-white" id="create-technician-select"/></div>
                                                            <button type="button" onClick={() => { if (selectedTechToAdd) { setLogForm({ ...logForm, technicians: [...logForm.technicians, selectedTechToAdd] }); setSelectedTechToAdd('') } }} className="bg-white border border-slate-300 p-2 rounded hover:bg-slate-100"><UserPlus size={16} className="text-slate-600" /></button>
                                                        </div>
                                                        <div className="flex flex-wrap gap-1">
                                                            {logForm.technicians.map(t => (
                                                                <span key={t} className="text-xs bg-white border border-slate-300 px-2 py-1 rounded text-slate-700 font-medium shadow-sm">{t}</span>
                                                            ))}
                                                            {logForm.technicians.length === 0 && <span className="text-xs text-slate-400 italic">No crew assigned</span>}
                                                        </div>

                                                        <div className="grid grid-cols-2 gap-2 mt-2 pt-2 border-t border-slate-200">
                                                            <div>
                                                                <label htmlFor="create-fuel-cost" className="text-[10px] text-slate-500 font-bold flex items-center gap-1 mb-1"><Fuel size={10} /> Minyak Sarana</label>
                                                                <input id="create-fuel-cost" type="number" className="w-full border border-slate-300 rounded p-1 text-sm bg-white text-slate-900" value={logForm.mechanicStoringCost} onChange={e => setLogForm({ ...logForm, mechanicStoringCost: Number(e.target.value) })} placeholder="0" />
                                                            </div>
                                                            <div>
                                                                <label htmlFor="create-meal-cost" className="text-[10px] text-slate-500 font-bold flex items-center gap-1 mb-1"><Utensils size={10} /> Uang Makan (Meals)</label>
                                                                <input id="create-meal-cost" type="number" className="w-full border border-slate-300 rounded p-1 text-sm bg-white text-slate-900" value={logForm.mechanicMealCost} onChange={e => setLogForm({ ...logForm, mechanicMealCost: Number(e.target.value) })} placeholder="0" />
                                                            </div>
                                                        </div>

                                                        {/* DRIVER CREATE */}
                                                        <div className="bg-white p-2 rounded border border-slate-200 mt-2">
                                                            <div className="flex items-center gap-2 mb-2">
                                                                <input
                                                                    type="checkbox"
                                                                    id="useDriver"
                                                                    checked={logForm.useDriver}
                                                                    onChange={e => setLogForm({ ...logForm, useDriver: e.target.checked })}
                                                                    className="w-4 h-4 text-blue-600 rounded"
                                                                />
                                                                <label htmlFor="useDriver" className="text-xs font-bold text-slate-600 flex items-center gap-1 cursor-pointer">
                                                                    <Car size={12} /> Using Dedicated Driver?
                                                                </label>
                                                            </div>
                                                            {logForm.useDriver && (
                                                                <div>
                                                                    <label htmlFor="create-driver-cost" className="text-[10px] text-slate-500 font-bold block mb-1">Biaya Driver (Meals/Allowances)</label>
                                                                    <input id="create-driver-cost" type="number" className="w-full border border-slate-300 rounded p-2 text-sm bg-slate-50 text-slate-900"
                                                                        value={logForm.driverStoringCost} onChange={e => setLogForm({ ...logForm, driverStoringCost: Number(e.target.value) })} placeholder="0" />
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                ) : (
                                                    <div className="space-y-3 bg-purple-50 p-3 rounded border border-purple-200">
                                                        <label htmlFor="create-supplier-select" className="text-xs font-bold text-purple-700 uppercase flex items-center gap-1"><ShoppingBag size={12} /> Vendor Details</label>
                                                        <SearchableSelect label="Select Supplier" options={supplierOptions} value={logForm.supplierId} onChange={v => setLogForm({ ...logForm, supplierId: v })} required className="bg-white" id="create-supplier-select"/>
                                                        <div className="grid grid-cols-2 gap-2">
                                                            <label htmlFor="create-invoice" className="sr-only">Invoice Number</label>
                                                            <input id="create-invoice" type="text" placeholder="Invoice #" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900" value={logForm.externalInvoiceNumber} onChange={e => setLogForm({ ...logForm, externalInvoiceNumber: e.target.value })} />
                                                            <label htmlFor="create-ext-cost" className="sr-only">Estimated Cost</label>
                                                            <input id="create-ext-cost" type="number" placeholder="Est. Cost" className="w-full border border-slate-300 rounded p-2 text-sm bg-white text-slate-900" value={logForm.externalCost} onChange={e => setLogForm({ ...logForm, externalCost: Number(e.target.value) })} />
                                                        </div>
                                                    </div>
                                                )}

                                                <button type="submit" className="w-full bg-slate-900 text-white py-3 rounded-lg shadow-md hover:bg-slate-800 font-bold transition-colors">Create Order</button>
                                            </form>
                                        )}
                                    </div>
                                </div>
                            ) : activeModalTab === 'parts' ? (
                                <div className="p-6 overflow-y-auto bg-white">
                                    <table className="w-full text-sm text-left border rounded-lg overflow-hidden">
                                        <thead className="bg-slate-50 border-b border-slate-200">
                                            <tr><th className="p-3 text-slate-600 font-bold">Date</th><th className="p-3 text-slate-600 font-bold">Type</th><th className="p-3 text-slate-600 font-bold">Part</th><th className="p-3 text-slate-600 font-bold">Qty</th></tr>
                                        </thead>
                                        <tbody>
                                            {partsHistory.map(tx => (
                                                <tr key={tx.id} className="border-b border-slate-100 hover:bg-slate-50">
                                                    <td className="p-3 text-slate-600">{tx.date}</td>
                                                    <td className="p-3"><span className="text-xs font-bold bg-slate-100 px-2 py-1 rounded text-slate-600">{tx.type}</span></td>
                                                    <td className="p-3 font-medium text-slate-800">{spareParts.find(p => p.id === tx.partId)?.name || 'Unknown Part'}</td>
                                                    <td className="p-3 font-bold text-slate-700">{tx.quantity}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            ) : activeModalTab === 'mutations' ? (
                                <div className="p-6 overflow-y-auto h-full w-full">
                                    <h4 className="font-bold text-slate-800 mb-4 flex items-center gap-2">
                                        <ArrowRightLeft size={18} /> Mutation History
                                    </h4>
                                    <div className="border rounded-lg overflow-hidden">
                                        <table className="w-full text-sm text-left">
                                            <thead className="bg-slate-50 text-slate-600 font-bold border-b">
                                                <tr>
                                                    <th className="px-4 py-3">Date</th>
                                                    <th className="px-4 py-3">Type</th>
                                                    <th className="px-4 py-3">Route</th>
                                                    <th className="px-4 py-3">Reference</th>
                                                    <th className="px-4 py-3">HM</th>
                                                    <th className="px-4 py-3">Status</th>
                                                </tr>
                                            </thead>
                                            <tbody className="divide-y divide-slate-100">
                                                {mutationHistory.length === 0 ? (
                                                    <tr>
                                                        <td colSpan={6} className="px-4 py-8 text-center text-slate-400">No mutation history found.</td>
                                                    </tr>
                                                ) : (
                                                    mutationHistory.map((mut: any) => (
                                                        <tr key={mut.id} className="hover:bg-slate-50">
                                                            <td className="px-4 py-3 font-mono text-slate-600">{mut.departureDate}</td>
                                                            <td className="px-4 py-3">
                                                                <span className={`text-[10px] font-bold uppercase px-2 py-1 rounded ${mut.type === 'ACQUISITION' ? 'bg-green-100 text-green-700' :
                                                                    mut.type === 'TRANSFER' ? 'bg-blue-100 text-blue-700' : 'bg-red-100 text-red-700'
                                                                    }`}>
                                                                    {mut.type}
                                                                </span>
                                                            </td>
                                                            <td className="px-4 py-3">
                                                                <div className="flex items-center gap-2 text-xs">
                                                                    <span className="text-slate-500 bg-slate-100 px-1.5 rounded">{mut.sourceLocation || '-'}</span>
                                                                    <span className="text-slate-300">➝</span>
                                                                    <span className="font-bold text-slate-700">{mut.targetLocation || '-'}</span>
                                                                </div>
                                                            </td>
                                                            <td className="px-4 py-3 text-slate-600">{mut.referenceDocument}</td>
                                                            <td className="px-4 py-3 font-mono">{mut.mutationHM?.toLocaleString()}</td>
                                                            <td className="px-4 py-3">
                                                                {mut.status === 'CANCELLED' ? (
                                                                    <span className="flex items-center gap-1 text-red-600 text-xs font-bold">
                                                                        <X size={14} /> Cancelled
                                                                    </span>
                                                                ) : (mut.arrivalDate || mut.status === 'COMPLETED') ? (
                                                                    <span className="flex items-center gap-1 text-green-600 text-xs font-bold">
                                                                        <CheckCircle size={14} /> Arrived
                                                                    </span>
                                                                ) : (
                                                                    <span className="flex items-center gap-1 text-amber-600 text-xs font-bold">
                                                                        <Truck size={14} /> In Transit
                                                                    </span>
                                                                )}
                                                            </td>
                                                        </tr>
                                                    ))
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            ) : (
                                // FINANCIAL TAB
                                <div className="p-6 overflow-y-auto w-full bg-white">
                                    <div className="flex justify-between mb-6">
                                        <h4 className="font-bold text-lg text-slate-800">Cost Analysis</h4>
                                        <div className="flex gap-2">
                                            <input type="date" className="border border-slate-300 rounded p-1 text-sm bg-white text-slate-900" value={costStartDate} onChange={e => setCostStartDate(e.target.value)} />
                                            <input type="date" className="border border-slate-300 rounded p-1 text-sm bg-white text-slate-900" value={costEndDate} onChange={e => setCostEndDate(e.target.value)} />
                                            <button onClick={handlePrint} className="bg-slate-800 text-white px-3 py-1 rounded flex items-center gap-2 hover:bg-slate-700 text-sm font-medium"><Printer size={14} /> Export Professional PDF</button>
                                        </div>
                                    </div>

                                    {financialData && (
                                        <div className="space-y-6">
                                            <div className="grid grid-cols-4 gap-4">
                                                <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                                                    <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Total Cash Spent (Real)</span>
                                                    <p className="text-2xl font-bold text-slate-900 mt-1">Rp {financialData.totalCashCost.toLocaleString()}</p>
                                                    <span className="text-xs text-green-600 bg-green-50 px-1 rounded">Direct Opex</span>
                                                </div>
                                                <div className="bg-blue-50 p-4 rounded-xl border border-blue-100">
                                                    <span className="text-[10px] uppercase font-bold text-blue-600 tracking-wider">Parts (Inventory)</span>
                                                    <p className="text-lg font-bold text-slate-800 mt-1">Rp {financialData.partsCost.toLocaleString()}</p>
                                                </div>
                                                <div className="bg-amber-50 p-4 rounded-xl border border-amber-100">
                                                    <span className="text-[10px] uppercase font-bold text-amber-600 tracking-wider">Operational OH</span>
                                                    <p className="text-lg font-bold text-slate-800 mt-1">Rp {financialData.storingCost.toLocaleString()}</p>
                                                    <span className="text-[10px] text-slate-500">Fuel & Meals</span>
                                                </div>
                                                <div className="bg-purple-50 p-4 rounded-xl border border-purple-100">
                                                    <span className="text-[10px] uppercase font-bold text-purple-600 tracking-wider">Ext. Vendor</span>
                                                    <p className="text-lg font-bold text-slate-800 mt-1">Rp {financialData.externalServiceCost.toLocaleString()}</p>
                                                </div>
                                            </div>

                                            {/* Inferential Stats */}
                                            <div className="bg-slate-50 p-4 rounded-lg border border-slate-200 flex gap-8 items-center">
                                                <div>
                                                    <span className="text-xs text-slate-500 font-bold block">Internal Labor Estimate</span>
                                                    <span className="text-sm font-mono text-slate-700">Rp {financialData.estimatedManpowerCost.toLocaleString()} (Non-Cash)</span>
                                                </div>
                                                <div className="h-8 w-px bg-slate-300"></div>
                                                <div>
                                                    <span className="text-xs text-slate-500 font-bold block">Cost Per Hour (CPH)</span>
                                                    <span className="text-sm font-mono text-slate-700">Rp {Math.round(financialData.totalCashCost / (selectedEquipment.hourMeter > 0 ? 100 : 1)).toLocaleString()}/hr</span>
                                                </div>
                                            </div>

                                            <table className="w-full text-sm border border-slate-200 rounded-lg overflow-hidden">
                                                <thead className="bg-slate-100 border-b border-slate-200">
                                                    <tr>
                                                        <th className="p-3 text-left text-slate-600 font-bold">Date</th>
                                                        <th className="p-3 text-left text-slate-600 font-bold">Ref</th>
                                                        <th className="p-3 text-left text-slate-600 font-bold">Category</th>
                                                        <th className="p-3 text-left text-slate-600 font-bold">Description</th>
                                                        <th className="p-3 text-right text-slate-600 font-bold">Amount (IDR)</th>
                                                    </tr>
                                                </thead>
                                                <tbody className="divide-y divide-slate-100">
                                                    {financialData.details.map((d: any, i: number) => (
                                                        <tr key={i} className={`hover:bg-slate-50 ${!d.isCash ? 'opacity-60 italic' : ''} `}>
                                                            <td className="p-3 text-slate-600">{d.date}</td>
                                                            <td className="p-3 font-mono text-xs text-slate-500">{d.ref}</td>
                                                            <td className="p-3">
                                                                <span className={`text-[10px] font-bold px-2 py-1 rounded ${d.type === 'Spare Parts' ? 'bg-blue-100 text-blue-700' :
                                                                    d.type === 'Operational OH' ? 'bg-amber-100 text-amber-700' :
                                                                        d.type === 'Vendor Invoice' ? 'bg-purple-100 text-purple-700' :
                                                                            'bg-slate-200 text-slate-600'
                                                                    } `}>
                                                                    {d.type}
                                                                </span>
                                                            </td>
                                                            <td className="p-3 text-slate-800">{d.description}</td>
                                                            <td className="p-3 text-right font-mono text-slate-800 font-medium">Rp {d.cost.toLocaleString()}</td>
                                                        </tr>
                                                    ))}
                                                    {financialData.details.length === 0 && <tr><td colSpan={5} className="p-4 text-center text-slate-400">No records in selected period.</td></tr>}
                                                </tbody>
                                                <tfoot className="bg-slate-50 font-bold border-t border-slate-200">
                                                    <tr>
                                                        <td colSpan={4} className="p-3 text-right text-slate-800">TOTAL CASH EXPENDITURE</td>
                                                        <td className="p-3 text-right text-slate-900 border-t-2 border-slate-300">Rp {financialData.totalCashCost.toLocaleString()}</td>
                                                    </tr>
                                                </tfoot>
                                            </table>
                                        </div>
                                    )}

                                    {/* PROFESSIONAL PDF EXPORT TEMPLATE (HIDDEN ON SCREEN) */}
                                    <div className="hidden">
                                        {/* Wrapper for ref */}
                                        <div ref={printRef}>
                                            <div className="p-8 max-w-[210mm] mx-auto bg-white text-slate-900 font-sans min-h-[297mm] flex flex-col relative" style={{ minHeight: '297mm' }}>

                                                {/* CONTENT WRAPPER (Takes available space) */}
                                                <div className="flex-1">
                                                    {/* LETTERHEAD Section */}
                                                    <div className="flex justify-between items-start mb-2">
                                                        <div className="flex items-center gap-6">
                                                            {/* Stylized Logo "JpM" */}
                                                            <div className="flex flex-col items-center leading-none">
                                                                <div className="flex items-baseline font-black tracking-tighter text-red-600" style={{ fontFamily: 'Arial, sans-serif' }}>
                                                                    <span className="text-6xl">J</span>
                                                                    <span className="text-6xl lowercase -ml-1">p</span>
                                                                    <span className="text-6xl -ml-1">M</span>
                                                                </div>
                                                            </div>

                                                            {/* Vertical Divider */}
                                                            <div className="h-16 w-px bg-slate-300"></div>

                                                            {/* Company Name */}
                                                            <div className="flex flex-col justify-center h-full pt-1">
                                                                <h1 className="text-2xl font-extrabold text-red-600 tracking-tight leading-none">PT JAVA PERSADA MANDIRI</h1>
                                                                <p className="text-xs font-bold text-blue-900 tracking-[0.35em] mt-1">GENERAL CONTRACTOR</p>
                                                            </div>
                                                        </div>

                                                        {/* Address Block - Right Aligned & Proportional */}
                                                        <div className="text-right text-[9px] text-slate-700 leading-tight font-medium">
                                                            <p className="font-bold text-slate-900 mb-1">HEAD OFFICE</p>
                                                            <p>Jl.Trikora RT.11 RW.02 No.57</p>
                                                            <p>Kel.Gt Manggis, Banjarbaru</p>
                                                            <p>Kalimantan Selatan 70721</p>
                                                            <div className="mt-1">
                                                                <p>Telp : 0511-4770113 / 0511-7553662</p>
                                                                <p>Fax : 0511-4770112</p>
                                                                <p>Email : contact@example.com</p>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    {/* Double Line Border */}
                                                    <div className="w-full border-t-4 border-slate-900 mb-0.5"></div>
                                                    <div className="w-full border-t border-slate-400 mb-8"></div>

                                                    {/* Report Title */}
                                                    <div className="mb-6 flex justify-between items-end">
                                                        <div>
                                                            <h2 className="text-xl font-bold uppercase text-slate-900">Asset Reliability & Cost Report</h2>
                                                            <p className="text-xs font-bold text-slate-500 uppercase">Generated: {new Date().toLocaleDateString()}</p>
                                                        </div>
                                                        <div className="text-right">
                                                            <div className="text-4xl font-black text-slate-900">{selectedEquipment.code}</div>
                                                            <div className="text-sm font-medium text-slate-500">{selectedEquipment.model} • {selectedEquipment.type}</div>
                                                        </div>
                                                    </div>

                                                    {/* Reliability Dashboard */}
                                                    {reliabilityData && (
                                                        <div className="grid grid-cols-4 gap-4 mb-6">
                                                            <div className={`border p-4 rounded ${reliabilityData.pa >= 85 ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'} `}>
                                                                <span className="text-[10px] font-bold uppercase block mb-1">Physical Availability (PA)</span>
                                                                <div className="text-2xl font-black">{reliabilityData.pa}%</div>
                                                                <span className="text-[10px] font-medium">{reliabilityData.pa >= 85 ? 'Target Met (>85%)' : 'Below Target'}</span>
                                                            </div>
                                                            <div className="border border-slate-200 p-4 rounded bg-slate-50">
                                                                <span className="text-[10px] font-bold text-slate-500 uppercase block mb-1">MTBF (Mean Time Between Failures)</span>
                                                                <div className="text-2xl font-black text-slate-900">{reliabilityData.mtbf} <span className="text-sm font-normal text-slate-500">hrs</span></div>
                                                            </div>
                                                            <div className="border border-slate-200 p-4 rounded bg-slate-50">
                                                                <span className="text-[10px] font-bold text-slate-500 uppercase block mb-1">MTTR (Mean Time To Repair)</span>
                                                                <div className="text-2xl font-black text-slate-900">{reliabilityData.mttr} <span className="text-sm font-normal text-slate-500">hrs</span></div>
                                                            </div>
                                                            <div className="border border-slate-200 p-4 rounded bg-slate-50">
                                                                <span className="text-[10px] font-bold text-slate-500 uppercase block mb-1">Total Downtime</span>
                                                                <div className="text-2xl font-black text-slate-900">{reliabilityData.totalDowntimeHours} <span className="text-sm font-normal text-slate-500">hrs</span></div>
                                                            </div>
                                                        </div>
                                                    )}

                                                    {/* Cost Summary Grid */}
                                                    <div className="grid grid-cols-4 gap-4 mb-8">
                                                        <div className="border border-slate-200 p-4 rounded bg-slate-50">
                                                            <span className="text-[10px] font-bold text-slate-500 uppercase block mb-1">Total Period Cost</span>
                                                            <span className="text-xl font-bold text-slate-900">Rp {(financialData?.totalCashCost || 0).toLocaleString()}</span>
                                                        </div>
                                                        <div className="border border-slate-200 p-4 rounded bg-slate-50">
                                                            <span className="text-[10px] font-bold text-slate-500 uppercase block mb-1">Cost Per Hour (CPH)</span>
                                                            <span className="text-xl font-bold text-slate-900">Rp {Math.round((financialData?.totalCashCost || 0) / (selectedEquipment.hourMeter > 0 ? 100 : 1)).toLocaleString()}</span>
                                                        </div>
                                                        <div className="border border-slate-200 p-4 rounded bg-slate-50">
                                                            <span className="text-[10px] font-bold text-slate-500 uppercase block mb-1">Current HM</span>
                                                            <span className="text-xl font-bold text-slate-900">{selectedEquipment.hourMeter.toLocaleString()}</span>
                                                        </div>
                                                        <div className="border border-slate-200 p-4 rounded bg-slate-50">
                                                            <span className="text-[10px] font-bold text-slate-500 uppercase block mb-1">Primary Cost Driver</span>
                                                            <span className="text-sm font-bold text-red-600">{ratios.primaryDriver}</span>
                                                        </div>
                                                    </div>

                                                    {/* Inferential Visuals */}
                                                    <div className="mb-8 p-4 border border-slate-200 rounded">
                                                        <h3 className="text-xs font-bold text-slate-500 uppercase mb-4 flex items-center gap-2">
                                                            <PieChart size={14} /> Operational Cost Composition
                                                        </h3>

                                                        {/* CSS Stacked Bar Chart */}
                                                        <div className="w-full h-8 bg-slate-100 rounded flex overflow-hidden mb-2">
                                                            {ratios.partsPct > 0 && (
                                                                <div style={{ width: `${ratios.partsPct}% ` }} className="bg-blue-600 h-full flex items-center justify-center text-[10px] font-bold text-white relative group">
                                                                    {ratios.partsPct}%
                                                                </div>
                                                            )}
                                                            {ratios.fuelPct > 0 && (
                                                                <div style={{ width: `${ratios.fuelPct}% ` }} className="bg-amber-500 h-full flex items-center justify-center text-[10px] font-bold text-white relative group">
                                                                    {ratios.fuelPct}%
                                                                </div>
                                                            )}
                                                            {ratios.extPct > 0 && (
                                                                <div style={{ width: `${ratios.extPct}% ` }} className="bg-purple-600 h-full flex items-center justify-center text-[10px] font-bold text-white relative group">
                                                                    {ratios.extPct}%
                                                                </div>
                                                            )}
                                                        </div>
                                                        <div className="flex justify-between text-[10px] text-slate-500 font-bold uppercase">
                                                            <div className="flex items-center gap-1"><div className="w-2 h-2 bg-blue-600 rounded-full"></div>Spare Parts</div>
                                                            <div className="flex items-center gap-1"><div className="w-2 h-2 bg-amber-500 rounded-full"></div>Op. Overhead</div>
                                                            <div className="flex items-center gap-1"><div className="w-2 h-2 bg-purple-600 rounded-full"></div>External Svc</div>
                                                        </div>
                                                    </div>

                                                    {/* DETAILED MAINTENANCE HISTORY TABLE (NEW) */}
                                                    <div className="mb-8 break-inside-avoid">
                                                        <h3 className="text-xs font-bold text-slate-500 uppercase mb-2">Maintenance Activity Log</h3>
                                                        <table className="w-full text-[10px] border-collapse border border-slate-200">
                                                            <thead>
                                                                <tr className="bg-slate-100">
                                                                    <th className="p-2 border border-slate-200 text-left w-24">Date / WO</th>
                                                                    <th className="p-2 border border-slate-200 text-left w-24">Type</th>
                                                                    <th className="p-2 border border-slate-200 text-left">Problem Description</th>
                                                                    <th className="p-2 border border-slate-200 text-left">Action Taken / Parts</th>
                                                                    <th className="p-2 border border-slate-200 text-left w-20">Executor</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                                {reportLogs.length > 0 ? (
                                                                    reportLogs.map(log => (
                                                                        <tr key={log.id} className={`border-b border-slate-200`}>
                                                                            <td className="p-2 border border-slate-200 align-top">
                                                                                <div className="font-bold">{log.startDate}</div>
                                                                                <div className="font-mono text-slate-500">{log.woNumber}</div>
                                                                                <div className={`mt - 1 font - bold ${log.status === 'CLOSED' ? 'text-green-600' : 'text-red-600'} `}>
                                                                                    {log.status}
                                                                                </div>
                                                                            </td>
                                                                            <td className="p-2 border border-slate-200 align-top">
                                                                                <div className="font-bold">{log.type}</div>
                                                                                <div className="text-slate-500">{log.damageType}</div>
                                                                                <div className="mt-1 text-slate-400">HM: {log.hmAtStart}</div>
                                                                            </td>
                                                                            <td className="p-2 border border-slate-200 align-top">
                                                                                {log.description}
                                                                            </td>
                                                                            <td className="p-2 border border-slate-200 align-top">
                                                                                <div className="italic mb-1">{log.notes || '-'}</div>
                                                                                {log.partsReplaced && (
                                                                                    <div className="bg-slate-50 p-1 border border-slate-100 rounded text-[9px] font-mono text-slate-600">
                                                                                        PARTS: {log.partsReplaced}
                                                                                    </div>
                                                                                )}
                                                                            </td>
                                                                            <td className="p-2 border border-slate-200 align-top">
                                                                                {log.serviceProvider === 'INTERNAL' ? (
                                                                                    <div className="text-blue-700 font-bold">Internal Team</div>
                                                                                ) : (
                                                                                    <div className="text-purple-700 font-bold">External Vendor</div>
                                                                                )}
                                                                                <div className="text-[9px] text-slate-500 mt-1">
                                                                                    {log.technicians && log.technicians.length > 0 ? log.technicians.join(', ') : (suppliers.find((s: any) => s.id === log.supplierId)?.name || '-')}
                                                                                </div>
                                                                            </td>
                                                                        </tr>
                                                                    ))
                                                                ) : (
                                                                    <tr>
                                                                        <td colSpan={5} className="p-4 text-center text-slate-400 italic">No maintenance activities recorded in this period.</td>
                                                                    </tr>
                                                                )}
                                                            </tbody>
                                                        </table>
                                                    </div>

                                                    {/* Descriptive Ledger */}
                                                    {financialData && (
                                                        <div className="break-inside-avoid">
                                                            <h3 className="text-xs font-bold text-slate-500 uppercase mb-2">Detailed Expenditure Ledger</h3>
                                                            <table className="w-full text-xs border-collapse mb-8">
                                                                <thead>
                                                                    <tr className="bg-slate-100 border-y border-slate-300">
                                                                        <th className="p-2 text-left font-bold uppercase text-slate-700 w-20">Date</th>
                                                                        <th className="p-2 text-left font-bold uppercase text-slate-700 w-24">Category</th>
                                                                        <th className="p-2 text-left font-bold uppercase text-slate-700">Description</th>
                                                                        <th className="p-2 text-right font-bold uppercase text-slate-700 w-20">Down (Hrs)</th>
                                                                        <th className="p-2 text-right font-bold uppercase text-slate-700 w-28">Cost (IDR)</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    {financialData.details.filter((d: any) => d.isCash).map((d: any, i: number) => (
                                                                        <tr key={i} className="border-b border-slate-200">
                                                                            <td className="p-2 text-slate-600 font-mono">{d.date}</td>
                                                                            <td className="p-2 font-bold text-slate-700">{d.type}</td>
                                                                            <td className="p-2 text-slate-600">{d.description}</td>
                                                                            <td className={`p - 2 text - right font - mono font - bold ${d.downtime > 0 ? 'text-red-600' : 'text-slate-300'} `}>{d.downtime > 0 ? d.downtime : '-'}</td>
                                                                            <td className="p-2 text-right font-mono font-medium text-slate-800">{d.cost.toLocaleString()}</td>
                                                                        </tr>
                                                                    ))}
                                                                    <tr className="bg-slate-50 font-bold border-t-2 border-slate-800 text-sm">
                                                                        <td colSpan={4} className="p-3 text-right">TOTAL DIRECT COST</td>
                                                                        <td className="p-3 text-right">Rp {financialData.totalCashCost.toLocaleString()}</td>
                                                                    </tr>
                                                                </tbody>
                                                            </table>
                                                        </div>
                                                    )}
                                                </div>

                                                {/* FOOTER SECTION: Pushed to bottom via flex-col & mt-auto */}
                                                <div className="mt-auto pt-8 break-inside-avoid w-full">
                                                    <div className="grid grid-cols-3 gap-8 text-center">
                                                        <div>
                                                            <div className="h-16 border-b border-slate-400 mb-2"></div>
                                                            <span className="text-xs font-bold uppercase text-slate-500">Prepared By</span>
                                                        </div>
                                                        <div>
                                                            <div className="h-16 border-b border-slate-400 mb-2"></div>
                                                            <span className="text-xs font-bold uppercase text-slate-500">Head of Plant</span>
                                                        </div>
                                                        <div>
                                                            <div className="h-16 border-b border-slate-400 mb-2"></div>
                                                            <span className="text-xs font-bold uppercase text-slate-500">Cost Control</span>
                                                        </div>
                                                    </div>

                                                    <div className="text-[10px] text-slate-400 mt-8 text-center border-t border-slate-100 pt-2">
                                                        Generated by jpmonitor on {new Date().toLocaleDateString()} • {costStartDate} to {costEndDate}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default FleetView;