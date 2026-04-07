package com.jpm.erp.api.controller;
import com.jpm.erp.domains.logistics.dto.GoodsShipmentDTO;
import com.jpm.erp.domains.logistics.service.GoodsShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class GoodsShipmentController {
    private final GoodsShipmentService shipmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<GoodsShipmentDTO> getAllShipments(@RequestParam(required = false) String status, @RequestParam(required = false) String fromDate, @RequestParam(required = false) String toDate) {
        return shipmentService.getAllShipments();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<GoodsShipmentDTO> getShipment(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.getShipment(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<GoodsShipmentDTO> createShipment(@RequestBody GoodsShipmentDTO dto) {
        return ResponseEntity.ok(shipmentService.createShipment(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<GoodsShipmentDTO> updateShipment(@PathVariable UUID id, @RequestBody GoodsShipmentDTO dto) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteShipment(@PathVariable UUID id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<GoodsShipmentDTO> updateStatus(@PathVariable UUID id, @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(shipmentService.updateStatus(id, req.status()));
    }

    private record StatusUpdateRequest(String status) {}
}
