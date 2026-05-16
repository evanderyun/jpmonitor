package com.jpmonitor.api.mcp;

import com.jpmonitor.domains.production.dto.ProductionRecordDTO;
import com.jpmonitor.domains.production.entity.Pit;
import com.jpmonitor.domains.production.repository.PitRepository;
import com.jpmonitor.domains.production.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Production MCP Tools — production recap and pit lookups.
 */
@Component
@RequiredArgsConstructor
public class ProductionMcpTools {

    private final ProductionService productionService;
    private final PitRepository pitRepository;

    /**
     * Rekap produksi berdasarkan pit code dan rentang tanggal.
     */
    @Tool(description = "Melihat rekap produksi untuk suatu pit dalam rentang tanggal tertentu")
    public List<Map<String, Object>> rekap_produksi(
            @ToolParam(description = "Kode pit (optional — kosongkan untuk semua pit)") String pitCode,
            @ToolParam(description = "Tanggal mulai (format: YYYY-MM-DD)") String startDate,
            @ToolParam(description = "Tanggal selesai (format: YYYY-MM-DD)") String endDate
    ) {
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);

        return productionService.getAllRecords().stream()
                .filter(r -> {
                    java.time.LocalDate recordDate = java.time.LocalDate.parse(r.date());
                    return !recordDate.isBefore(start) && !recordDate.isAfter(end);
                })
                .filter(r -> pitCode == null || pitCode.isBlank()
                        || pitCode.equalsIgnoreCase(r.pitName())
                        || pitCode.equalsIgnoreCase(
                        pitRepository.findById(r.pitId()).map(Pit::getCode).orElse("")))
                .map(this::toProductionMap)
                .collect(Collectors.toList());
    }

    // --- helpers ---

    private Map<String, Object> toProductionMap(ProductionRecordDTO r) {
        return Map.of(
                "id", r.id(),
                "date", r.date(),
                "shift", r.shift(),
                "pitId", r.pitId(),
                "pitName", r.pitName(),
                "overburdenBcm", r.overburdenBcm(),
                "coalMt", r.coalMt(),
                "strippingRatio", r.strippingRatio(),
                "status", r.status(),
                "notes", r.notes()
        );
    }
}
