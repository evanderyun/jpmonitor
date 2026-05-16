package com.jpmonitor.api.mcp;

import com.jpmonitor.domains.inventory.entity.SparePart;
import com.jpmonitor.domains.inventory.repository.SparePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory MCP Tools — spare parts stock checks and alerts.
 */
@Component
@RequiredArgsConstructor
public class InventoryMcpTools {

    private final SparePartRepository sparePartRepository;

    /**
     * Cek stok spare part berdasarkan keyword (part number atau nama).
     */
    @Tool(description = "Mengecek stok spare part berdasarkan part number atau nama")
    public List<Map<String, Object>> cek_stok_sparepart(
            @ToolParam(description = "Kata kunci pencarian (part number atau nama part)") String keyword
    ) {
        return sparePartRepository.findAll().stream()
                .filter(p -> p.getPartNumber().toLowerCase().contains(keyword.toLowerCase())
                        || p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::toSparePartMap)
                .collect(Collectors.toList());
    }

    /**
     * Laporan spare part yang stoknya menipis (di bawah minimum).
     */
    @Tool(description = "Menampilkan daftar spare part yang stoknya menipis (di bawah batas minimum)")
    public List<Map<String, Object>> laporan_stok_menipis() {
        return sparePartRepository.findAll().stream()
                .filter(p -> p.getCurrentStock() < p.getMinStockLevel())
                .map(this::toSparePartMap)
                .collect(Collectors.toList());
    }

    // --- helpers ---

    private Map<String, Object> toSparePartMap(SparePart p) {
        return Map.of(
                "id", p.getId(),
                "partNumber", p.getPartNumber(),
                "name", p.getName(),
                "brand", p.getBrand(),
                "category", p.getCategory(),
                "currentStock", p.getCurrentStock(),
                "minStockLevel", p.getMinStockLevel(),
                "unit", p.getUnit(),
                "location", p.getLocation() != null ? p.getLocation().getName() : null,
                "rackCode", p.getRackCode()
        );
    }
}
