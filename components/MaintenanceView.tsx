import React from 'react';
import { Edit, Plus, Clock, Users, UserPlus, Fuel, Utensils, Car, ShoppingBag, Trash2 } from 'lucide-react';
import SearchableSelect from './SearchableSelect';

interface MaintenanceViewProps {
    filteredLogs: any[];
    woFilterStatus: string;
    setWoFilterStatus: (v: string) => void;
    editingLog: any | null;
    setEditingLog: (v: any | null) => void;
    editForm: any;
    setEditForm: (v: any) => void;
    logForm: any;
    setLogForm: (v: any) => void;
    selectedEquipment: any;
    prediction: { nextHM: number; type: string; isMajor: boolean } | null;
    selectedTechToAdd: string;
    setSelectedTechToAdd: (v: string) => void;
    selectedPartId: string;
    setSelectedPartId: (v: string) => void;
    selectedPartQty: number;
    setSelectedPartQty: (v: number) => void;
    tempUsedParts: any[];
    partOptions: any[];
    technicianOptions: any[];
    supplierOptions: any[];
    handleSubmitEdit: (e: React.FormEvent) => void;
    handleDeleteLog: () => void;
    handleAddLog: (e: React.FormEvent) => void;
    handleAddPartToLog: () => void;
    handleOpenEdit: (log: any) => void;
    getStatusColor: (status: string) => string;
}

const MaintenanceView: React.FC<MaintenanceViewProps> = ({
    filteredLogs,
    woFilterStatus,
    setWoFilterStatus,
    editingLog,
    setEditingLog,
    editForm,
    setEditForm,
    logForm,
    setLogForm,
    prediction,
    selectedTechToAdd,
    setSelectedTechToAdd,
    selectedPartId,
    setSelectedPartId,
    selectedPartQty,
    setSelectedPartQty,
    tempUsedParts,
    partOptions,
    technicianOptions,
    supplierOptions,
    handleSubmitEdit,
    handleDeleteLog,
    handleAddLog,
    handleAddPartToLog,
    handleOpenEdit,
    getStatusColor,
}) => {
    return (
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
                {filteredLogs.map((log: any) => {
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
                                <option value="CANCELLED">CANCELLED</option>
                            </select>
                        </div>

                        <div>
                            <span className="block text-xs font-bold text-slate-600 mb-1 uppercase">Finish Time</span>
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
                                <span className="text-xs font-bold text-slate-600 uppercase">Mechanic Storing Cost</span>
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
                                <span className="text-xs font-bold text-slate-600 uppercase">Vendor Invoice</span>
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
                                {tempUsedParts.map((p: any, i: number) => (
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
                                    <div className="flex-1"><SearchableSelect placeholder="Select Mechanic" options={technicianOptions} value={selectedTechToAdd} onChange={setSelectedTechToAdd} className="bg-white" id="create-technician-select" /></div>
                                    <button type="button" onClick={() => { if (selectedTechToAdd) { setLogForm({ ...logForm, technicians: [...logForm.technicians, selectedTechToAdd] }); setSelectedTechToAdd('') } }} className="bg-white border border-slate-300 p-2 rounded hover:bg-slate-100"><UserPlus size={16} className="text-slate-600" /></button>
                                </div>
                                <div className="flex flex-wrap gap-1">
                                    {logForm.technicians.map((t: string) => (
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
                                <SearchableSelect label="Select Supplier" options={supplierOptions} value={logForm.supplierId} onChange={v => setLogForm({ ...logForm, supplierId: v })} required className="bg-white" id="create-supplier-select" />
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
    );
};

export default MaintenanceView;