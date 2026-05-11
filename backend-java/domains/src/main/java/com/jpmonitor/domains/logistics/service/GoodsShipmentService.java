package com.jpmonitor.domains.logistics.service;

import com.jpmonitor.domains.logistics.dto.GoodsShipmentDTO;
import java.util.List;
import java.util.UUID;

public interface GoodsShipmentService {
    List<GoodsShipmentDTO> getAllShipments();

    GoodsShipmentDTO getShipment(UUID id);

    GoodsShipmentDTO createShipment(GoodsShipmentDTO dto);

    GoodsShipmentDTO updateShipment(UUID id, GoodsShipmentDTO dto);

    GoodsShipmentDTO updateShipmentStatus(UUID id, String status);

    void deleteShipment(UUID id);
}
