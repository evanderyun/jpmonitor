package com.jpmonitor.api.controller;

import com.jpmonitor.domains.logistics.dto.GoodsShipmentDTO;
import com.jpmonitor.domains.logistics.service.GoodsShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipments") // Removed alias, use single canonical path
@RequiredArgsConstructor
public class GoodsShipmentController {

    private final GoodsShipmentService shipmentService;

    @GetMapping
    public List<GoodsShipmentDTO> getAllShipments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return shipmentService.getAllShipments();
        // TODO: Add filtering logic in service layer if needed
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoodsShipmentDTO> getShipment(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.getShipment(id));
    }

    @PostMapping
    public ResponseEntity<GoodsShipmentDTO> createShipment(@Valid @RequestBody GoodsShipmentDTO dto) {
        return ResponseEntity.ok(shipmentService.createShipment(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoodsShipmentDTO> updateShipment(@PathVariable UUID id,
            @Valid @RequestBody GoodsShipmentDTO dto) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<GoodsShipmentDTO> updateShipmentStatus(
            @PathVariable UUID id,
            @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable UUID id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.ok().build();
    }

    // Request DTO for PATCH endpoint
    public record StatusUpdateRequest(String status) {
    }
}
