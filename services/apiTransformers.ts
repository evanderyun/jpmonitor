// API Data Transformers
// Converts backend snake_case API responses to frontend camelCase types

import { SparePart, InventoryTransaction, Equipment, Supplier, Employee, GoodsShipment, ShipmentItem } from '../types';

// ============================================================================
// SPARE PARTS TRANSFORMERS
// ============================================================================

export function transformSparePart(apiData: any): SparePart {
    return {
        id: apiData.id,
        partNumber: apiData.part_number || '',
        name: apiData.name || '',
        brand: apiData.brand || '',
        category: apiData.category || '',
        currentStock: Number(apiData.current_stock) || 0,
        minStockLevel: Number(apiData.min_stock_level) || 0,
        unit: apiData.unit || '',
        locationId: apiData.location_id || '',
        location: apiData.location || '',
        averageCost: Number(apiData.average_cost) || 0,
        preferredSupplierId: apiData.preferred_supplier_id || ''
    };
}

export function transformSparePartToAPI(data: Partial<SparePart>): any {
    return {
        part_number: data.partNumber,
        name: data.name,
        brand: data.brand,
        category: data.category,
        current_stock: data.currentStock,
        min_stock_level: data.minStockLevel,
        unit: data.unit,
        location_id: data.locationId,
        location: data.location,
        average_cost: data.averageCost,
        preferred_supplier_id: data.preferredSupplierId || undefined
    };
}

// ============================================================================
// INVENTORY TRANSACTION TRANSFORMERS
// ============================================================================

export function transformInventoryTransaction(apiData: any): InventoryTransaction {
    return {
        id: apiData.id,
        date: apiData.date,
        type: apiData.type,
        partId: apiData.part_id,
        quantity: Number(apiData.quantity),
        pricePerUnit: apiData.price_per_unit ? Number(apiData.price_per_unit) : undefined,
        referenceId: apiData.reference_id,
        equipmentId: apiData.equipment_id,
        supplierId: apiData.supplier_id,
        notes: apiData.notes,
        performedBy: apiData.performed_by,
        paymentType: apiData.payment_method,  // payment_method from backend
        paymentStatus: apiData.payment_status,
        dueDate: apiData.due_date,
        paidDate: apiData.paid_date
    };
}

export function transformInventoryTransactionToAPI(data: any): any {
    return {
        date: data.date,
        type: data.type,
        part_id: data.partId,
        quantity: data.quantity,
        price_per_unit: data.pricePerUnit,
        reference_id: data.referenceId || undefined,
        equipment_id: data.equipmentId || undefined,
        supplier_id: data.supplierId || undefined,
        notes: data.notes || undefined,
        payment_method: data.paymentType,  // Map to payment_method
        due_date: data.dueDate,
        paid_date: data.paidDate
    };
}

// ============================================================================
// EQUIPMENT TRANSFORMERS
// ============================================================================

export function transformEquipment(apiData: any): Equipment {
    return {
        id: apiData.id,
        code: apiData.code,
        type: apiData.type,
        model: apiData.model,
        manufactureYear: apiData.manufacture_year ? Number(apiData.manufacture_year) : undefined,
        status: apiData.status,
        hourMeter: Number(apiData.hour_meter || 0),
        kilometer: apiData.kilometer ? Number(apiData.kilometer) : undefined,
        location: apiData.location,
        locationId: apiData.location_id,
        owner: apiData.owner,
        serialNumber: apiData.serial_number,
        engineNumber: apiData.engine_number,
        chassisNumber: apiData.chassis_number,
        plateNumber: apiData.plate_number
    };
}

export function transformEquipmentToAPI(data: Partial<Equipment>): any {
    return {
        code: data.code,
        name: data.code, // Use code as name  
        type: data.type,
        model: data.model,
        brand: data.owner, // Map owner to brand for now
        manufacture_year: data.manufactureYear,
        status: data.status,
        hour_meter: data.hourMeter,
        kilometer: data.kilometer,
        location_id: data.locationId,
        owner: data.owner,
        serial_number: data.serialNumber,
        engine_number: data.engineNumber,
        chassis_number: data.chassisNumber,
        plate_number: data.plateNumber
    };
}

// ============================================================================
// SUPPLIER TRANSFORMERS
// ============================================================================

export function transformSupplier(apiData: any): Supplier {
    return {
        id: apiData.id,
        name: apiData.name,
        type: apiData.type,
        contactPerson: apiData.contact_person,
        phone: apiData.phone,
        address: apiData.address,
        rating: Number(apiData.rating ?? 0)
    };
}

export function transformSupplierToAPI(data: Partial<Supplier>): any {
    return {
        name: data.name,
        type: data.type,
        contact_person: data.contactPerson,
        phone: data.phone,
        address: data.address,
        rating: data.rating
    };
}

// ============================================================================
// EMPLOYEE TRANSFORMERS
// ============================================================================

export function transformEmployee(apiData: any): Employee {
    return {
        id: apiData.id,
        name: apiData.name,
        position: apiData.position,
        department: apiData.department,
        role: apiData.role,
        status: apiData.status,
        joinedDate: apiData.joined_date,
        locationId: apiData.location_id
    };
}

export function transformEmployeeToAPI(data: Partial<Employee>): any {
    return {
        name: data.name,
        position: data.position,
        department: data.department,
        role: data.role,
        status: data.status,
        joined_date: data.joinedDate,
        location_id: data.locationId
    };
}

// ============================================================================
// DASHBOARD TRANSFORMERS
// ============================================================================

export function transformDashboardStats(apiData: any): any {
    return {
        production: {
            totalCoal: Number(apiData.production?.total_coal || 0),
            totalOB: Number(apiData.production?.total_ob || 0),
            avgSR: Number(apiData.production?.avg_sr || 0),
            chartData: apiData.production?.chart_data || []
        },
        fleet: {
            total: Number(apiData.fleet?.total || 0),
            operational: Number(apiData.fleet?.operational || 0),
            availability: Number(apiData.fleet?.availability || 0)
        },
        inventory: {
            lowStockCount: Number(apiData.inventory?.low_stock_count || 0),
            lowStockItems: (apiData.inventory?.low_stock_items || []).map(transformSparePart)
        }
    };
}

// ============================================================================
// GOODS SHIPMENT TRANSFORMERS
// ============================================================================

export function transformShipmentItem(apiData: any): ShipmentItem {
    return {
        partId: apiData.part_id || '',
        partName: apiData.part_name || apiData.partName || '',
        partNumber: apiData.part_code || apiData.part_number || apiData.partNumber || '',
        quantity: Number(apiData.quantity) || 0,
        unit: apiData.unit || apiData.unit_code || '',
        notes: apiData.notes || '',
        unitCode: apiData.unit_code || ''
    };
}

export function transformGoodsShipment(apiData: any): GoodsShipment {
    return {
        id: apiData.id,
        doNumber: apiData.do_number || '',
        date: apiData.date || '',
        sourceLocationId: apiData.source_location_id || '',
        sourceLocationName: apiData.source_location_name || '',
        targetType: apiData.target_type || 'LOCATION',
        targetId: apiData.target_location_id || apiData.target_supplier_id || '',
        targetName: apiData.target_location_name || apiData.target_supplier_name || '',
        transportProvider: apiData.transport_provider || '',
        targetAddress: apiData.target_address || '',
        driverName: apiData.driver_employee_name || apiData.external_driver_name || '',
        transportUnit: apiData.vehicle_equipment_code || apiData.external_vehicle_desc || '',
        policeNumber: apiData.police_number || '',
        status: apiData.status || 'PENDING',
        notes: apiData.notes || '',
        items: (apiData.items || []).map(transformShipmentItem),
        createdBy: apiData.created_by || apiData.createdBy || ''
    };
}

export function transformShipmentToAPI(data: Partial<GoodsShipment>): any {
    const isInternal = data.transportProvider === 'INTERNAL';
    return {
        date: data.date,
        source_location_id: data.sourceLocationId,
        source_location_name: data.sourceLocationName,
        target_type: data.targetType,
        target_location_id: data.targetId,
        target_name: data.targetName,
        transport_provider: data.transportProvider,
        driver_employee_id: isInternal && data.driverName ? data.driverName : null,
        vehicle_equipment_id: isInternal && data.transportUnit ? data.transportUnit : null,
        external_driver_name: !isInternal ? (data.driverName || null) : null,
        external_vehicle_desc: !isInternal ? (data.transportUnit || null) : null,
        police_number: data.policeNumber,
        do_number: data.doNumber || undefined,
        status: data.status,
        notes: data.notes || '',
        items: (data.items || []).map(item => ({
            part_id: item.partId,
            part_code: item.partNumber,
            part_name: item.partName,
            quantity: item.quantity,
            notes: item.notes || '',
            unit_code: item.unitCode || ''
        }))
    };
}
