package com.jpmonitor.api.mcp;

import com.jpmonitor.domains.fleet.entity.Equipment;
import com.jpmonitor.domains.fleet.entity.WorkOrder;
import com.jpmonitor.domains.fleet.repository.EquipmentRepository;
import com.jpmonitor.domains.fleet.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fleet MCP Tools — equipment lookups and work order creation.
 */
@Component
@RequiredArgsConstructor
public class FleetMcpTools {

    private final EquipmentRepository equipmentRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * Cari equipment/unit berdasarkan kode, status, atau lokasi.
     */
    @Tool(description = "Mencari equipment/unit armada berdasarkan kode, status, atau lokasi")
    public List<Map<String, Object>> cari_equipment(
            @ToolParam(description = "Kode unit (optional)") String code,
            @ToolParam(description = "Status unit (optional, contoh: Operational, Breakdown, Maintenance)") String status,
            @ToolParam(description = "Nama lokasi (optional)") String location
    ) {
        return equipmentRepository.findAll().stream()
                .filter(e -> code == null || code.isBlank()
                        || e.getCode().toLowerCase().contains(code.toLowerCase()))
                .filter(e -> status == null || status.isBlank()
                        || e.getStatus() != null && e.getStatus().equalsIgnoreCase(status))
                .filter(e -> location == null || location.isBlank()
                        || (e.getLocation() != null
                        && e.getLocation().getName().toLowerCase().contains(location.toLowerCase())))
                .map(this::toEquipmentMap)
                .collect(Collectors.toList());
    }

    /**
     * Buat work order baru untuk suatu unit.
     */
    @Tool(description = "Membuat work order baru untuk suatu equipment/unit")
    public Map<String, Object> buat_work_order(
            @ToolParam(description = "Kode unit/equipment") String equipmentCode,
            @ToolParam(description = "Deskripsi pekerjaan") String description,
            @ToolParam(description = "Prioritas (HIGH, MEDIUM, LOW)") String priority
    ) {
        Equipment eq = equipmentRepository.findByCode(equipmentCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Equipment dengan kode '" + equipmentCode + "' tidak ditemukan"));

        WorkOrder wo = new WorkOrder();
        wo.setWoNumber("WO-" + System.currentTimeMillis());
        wo.setEquipment(eq);
        wo.setDescription(description);
        wo.setPriority(priority != null ? priority.toUpperCase() : "MEDIUM");
        wo.setStatus("OPEN");
        wo.setType("CORRECTIVE");

        WorkOrder saved = workOrderRepository.save(wo);

        return Map.of(
                "woNumber", saved.getWoNumber(),
                "equipmentCode", eq.getCode(),
                "equipmentName", eq.getName(),
                "description", saved.getDescription(),
                "priority", saved.getPriority(),
                "status", saved.getStatus(),
                "id", saved.getId()
        );
    }

    /**
     * Rekap status seluruh armada (jumlah per status).
     */
    @Tool(description = "Melihat rekap status seluruh armada/unit")
    public Map<String, Object> status_armada() {
        List<Equipment> all = equipmentRepository.findAll();

        long operational = all.stream().filter(e -> "Operational".equalsIgnoreCase(e.getStatus())).count();
        long breakdown = all.stream().filter(e -> "Breakdown".equalsIgnoreCase(e.getStatus())).count();
        long maintenance = all.stream().filter(e -> "Maintenance".equalsIgnoreCase(e.getStatus())).count();
        long other = all.size() - operational - breakdown - maintenance;

        return Map.of(
                "total", all.size(),
                "operational", operational,
                "breakdown", breakdown,
                "maintenance", maintenance,
                "other", other
        );
    }

    // --- helpers ---

    private Map<String, Object> toEquipmentMap(Equipment e) {
        return Map.of(
                "id", e.getId(),
                "code", e.getCode(),
                "name", e.getName(),
                "type", e.getType(),
                "status", e.getStatus(),
                "brand", e.getBrand(),
                "model", e.getModel(),
                "location", e.getLocation() != null ? e.getLocation().getName() : null,
                "hourMeter", e.getHourMeter(),
                "owner", e.getOwner()
        );
    }
}
