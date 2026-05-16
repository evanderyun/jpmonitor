package com.jpmonitor.domains.fleet.service.impl;

import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.core.repository.LocationRepository;
import com.jpmonitor.domains.fleet.dto.EquipmentDTO;
import com.jpmonitor.domains.fleet.dto.MaintenanceRecordDTO;
import com.jpmonitor.domains.fleet.entity.Equipment;
import com.jpmonitor.domains.fleet.entity.MaintenanceRecord;
import com.jpmonitor.domains.fleet.entity.WorkOrder;
import com.jpmonitor.domains.fleet.repository.EquipmentRepository;
import com.jpmonitor.domains.fleet.repository.MaintenanceRecordRepository;
import com.jpmonitor.domains.fleet.repository.WorkOrderRepository;
import com.jpmonitor.platform.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Fleet Service Tests")
class FleetServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private MaintenanceRecordRepository maintenanceRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private FleetServiceImpl fleetService;

    @Captor
    private ArgumentCaptor<Equipment> equipmentCaptor;

    @Captor
    private ArgumentCaptor<WorkOrder> workOrderCaptor;

    @Captor
    private ArgumentCaptor<MaintenanceRecord> maintRecordCaptor;

    @Test
    @DisplayName("Should search equipment by code via findById")
    void testEquipmentSearchByCode() {
        // Given
        UUID equipmentId = UUID.randomUUID();
        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setCode("EXC-001");
        equipment.setName("Excavator 001");
        equipment.setType("Excavator");
        equipment.setStatus("Operational");
        equipment.setHourMeter(BigDecimal.valueOf(1500));

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));

        // When
        EquipmentDTO result = fleetService.getEquipment(equipmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("EXC-001");
        assertThat(result.name()).isEqualTo("Excavator 001");
        assertThat(result.type()).isEqualTo("Excavator");
        assertThat(result.status()).isEqualTo("Operational");
        assertThat(result.hourMeter()).isEqualByComparingTo(BigDecimal.valueOf(1500));

        verify(equipmentRepository).findById(equipmentId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when equipment not found")
    void testEquipmentNotFound() {
        // Given
        UUID unknownId = UUID.randomUUID();
        when(equipmentRepository.findById(unknownId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> fleetService.getEquipment(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Equipment")
                .hasMessageContaining(unknownId.toString());

        verify(equipmentRepository).findById(unknownId);
    }

    @Test
    @DisplayName("Should filter equipment by status, type, and location")
    void testGetAllEquipmentWithFilters() {
        // Given
        UUID locationId = UUID.randomUUID();

        Location location = new Location();
        location.setId(locationId);
        location.setName("Site A");

        Equipment eq1 = new Equipment();
        eq1.setId(UUID.randomUUID());
        eq1.setCode("EXC-001");
        eq1.setType("Excavator");
        eq1.setStatus("Operational");
        eq1.setLocation(location);

        Equipment eq2 = new Equipment();
        eq2.setId(UUID.randomUUID());
        eq2.setCode("DMP-001");
        eq2.setType("Dump Truck");
        eq2.setStatus("Operational");
        eq2.setLocation(location);

        Equipment eq3 = new Equipment();
        eq3.setId(UUID.randomUUID());
        eq3.setCode("EXC-002");
        eq3.setType("Excavator");
        eq3.setStatus("Under Maintenance");
        eq3.setLocation(location);

        when(equipmentRepository.findAll()).thenReturn(List.of(eq1, eq2, eq3));

        // When: filter by type "Excavator"
        List<EquipmentDTO> results = fleetService.getAllEquipment(null, "Excavator", null);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(EquipmentDTO::code)
                .containsExactlyInAnyOrder("EXC-001", "EXC-002");

        verify(equipmentRepository).findAll();
    }

    @Test
    @DisplayName("Should create work order and maintenance record")
    void testWorkOrderCreation() {
        // Given
        UUID equipmentId = UUID.randomUUID();
        UUID maintenanceId = UUID.randomUUID();

        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setCode("EXC-001");
        equipment.setName("Excavator 001");
        equipment.setStatus("Operational");

        MaintenanceRecordDTO dto = new MaintenanceRecordDTO(
                null,
                equipmentId,
                null,
                LocalDate.now().toString(),
                "PREVENTIVE",
                "Oil change and filter replacement",
                "OPEN",
                "INTERNAL",
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(200000),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(150000),
                BigDecimal.ZERO,
                null
        );

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> {
            WorkOrder wo = invocation.getArgument(0);
            wo.setId(UUID.randomUUID());
            return wo;
        });
        when(maintenanceRepository.save(any(MaintenanceRecord.class))).thenAnswer(invocation -> {
            MaintenanceRecord rec = invocation.getArgument(0);
            rec.setId(maintenanceId);
            return rec;
        });

        // When
        MaintenanceRecordDTO result = fleetService.createMaintenance(dto);

        // Then: verify WorkOrder was created with correct fields
        verify(workOrderRepository).save(workOrderCaptor.capture());
        WorkOrder savedWorkOrder = workOrderCaptor.getValue();

        assertThat(savedWorkOrder.getWoNumber()).startsWith("WO-");
        assertThat(savedWorkOrder.getStatus()).isEqualTo("OPEN");
        assertThat(savedWorkOrder.getType()).isEqualTo("PREVENTIVE");
        assertThat(savedWorkOrder.getDescription()).isEqualTo("Oil change and filter replacement");
        assertThat(savedWorkOrder.getEquipment()).isEqualTo(equipment);

        // Then: verify MaintenanceRecord was created with correct fields
        verify(maintenanceRepository).save(maintRecordCaptor.capture());
        MaintenanceRecord savedRecord = maintRecordCaptor.getValue();

        assertThat(savedRecord.getWorkOrder()).isEqualTo(savedWorkOrder);
        assertThat(savedRecord.getEquipment()).isEqualTo(equipment);
        assertThat(savedRecord.getDescription()).isEqualTo("Oil change and filter replacement");
        assertThat(savedRecord.getPartsCost()).isEqualByComparingTo(BigDecimal.valueOf(500000));

        // Then: verify result DTO
        assertThat(result).isNotNull();
        assertThat(result.woNumber()).startsWith("WO-");
        assertThat(result.type()).isEqualTo("PREVENTIVE");
        assertThat(result.status()).isEqualTo("OPEN");
        assertThat(result.partsCost()).isEqualByComparingTo(BigDecimal.valueOf(500000));
    }

    @Test
    @DisplayName("Should create equipment and return DTO")
    void testCreateEquipment() {
        // Given
        UUID equipmentId = UUID.randomUUID();
        EquipmentDTO inputDto = new EquipmentDTO(
                null,
                "DOZ-001",
                "Dozer 001",
                "D85",
                "Dozer",
                "Komatsu",
                "SN-12345",
                "EN-67890",
                "CN-11223",
                "B 1234 XX",
                2023,
                "Operational",
                null,
                "Unknown",
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(10000),
                "JPM"
        );

        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> {
            Equipment eq = invocation.getArgument(0);
            eq.setId(equipmentId);
            return eq;
        });

        // When
        EquipmentDTO result = fleetService.createEquipment(inputDto);

        // Then
        verify(equipmentRepository).save(equipmentCaptor.capture());
        Equipment saved = equipmentCaptor.getValue();

        assertThat(saved.getCode()).isEqualTo("DOZ-001");
        assertThat(saved.getName()).isEqualTo("Dozer 001");
        assertThat(saved.getModel()).isEqualTo("D85");
        assertThat(saved.getType()).isEqualTo("Dozer");
        assertThat(saved.getStatus()).isEqualTo("Operational");
        assertThat(saved.getHourMeter()).isEqualByComparingTo(BigDecimal.valueOf(500));

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(equipmentId);
        assertThat(result.code()).isEqualTo("DOZ-001");
    }

    @Test
    @DisplayName("Should update equipment status")
    void testUpdateEquipmentStatus() {
        // Given
        UUID equipmentId = UUID.randomUUID();
        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setCode("EXC-001");
        equipment.setStatus("Operational");

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EquipmentDTO result = fleetService.updateEquipmentStatus(equipmentId, "Under Maintenance");

        // Then
        assertThat(result.status()).isEqualTo("Under Maintenance");
        verify(equipmentRepository).save(equipment);
        assertThat(equipment.getStatus()).isEqualTo("Under Maintenance");
    }

    @Test
    @DisplayName("Should update equipment hour meter")
    void testUpdateEquipmentHourMeter() {
        // Given
        UUID equipmentId = UUID.randomUUID();
        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setCode("EXC-001");
        equipment.setHourMeter(BigDecimal.valueOf(1500));

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EquipmentDTO result = fleetService.updateEquipmentHourMeter(equipmentId, BigDecimal.valueOf(1600));

        // Then
        assertThat(result.hourMeter()).isEqualByComparingTo(BigDecimal.valueOf(1600));
        verify(equipmentRepository).save(equipment);
        assertThat(equipment.getHourMeter()).isEqualByComparingTo(BigDecimal.valueOf(1600));
    }

    @Test
    @DisplayName("Should delete equipment when it exists")
    void testDeleteEquipment() {
        // Given
        UUID equipmentId = UUID.randomUUID();
        when(equipmentRepository.existsById(equipmentId)).thenReturn(true);

        // When
        fleetService.deleteEquipment(equipmentId);

        // Then
        verify(equipmentRepository).deleteById(equipmentId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent equipment")
    void testDeleteNonExistentEquipment() {
        // Given
        UUID equipmentId = UUID.randomUUID();
        when(equipmentRepository.existsById(equipmentId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> fleetService.deleteEquipment(equipmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Equipment");

        verify(equipmentRepository, never()).deleteById(any());
    }
}