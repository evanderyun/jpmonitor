package com.jpm.erp.domains.production.service.impl;

import com.jpm.erp.domains.core.repository.LocationRepository;
import com.jpm.erp.domains.core.repository.ProjectRepository;
import com.jpm.erp.domains.hr.repository.EmployeeRepository;
import com.jpm.erp.domains.production.dto.PitDTO;
import com.jpm.erp.domains.production.dto.ProductionRecordDTO;
import com.jpm.erp.domains.production.dto.StockpileDTO;
import com.jpm.erp.domains.production.entity.Pit;
import com.jpm.erp.domains.production.entity.ProductionRecord;
import com.jpm.erp.domains.production.entity.Stockpile;
import com.jpm.erp.domains.production.repository.PitRepository;
import com.jpm.erp.domains.production.repository.ProductionRecordRepository;
import com.jpm.erp.domains.production.repository.StockpileRepository;
import com.jpm.erp.domains.production.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductionServiceImpl implements ProductionService {

    private final ProductionRecordRepository recordRepository;
    private final PitRepository pitRepository;
    private final StockpileRepository stockpileRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final LocationRepository locationRepository;

    // --- RECORDS ---

    @Override
    @Transactional(readOnly = true)
    public List<ProductionRecordDTO> getAllRecords() {
        return recordRepository.findAllWithDetails().stream()
                .map(this::mapRecordToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductionRecordDTO createRecord(ProductionRecordDTO dto) {
        ProductionRecord record = new ProductionRecord();
        updateRecordEntity(record, dto);
        return mapRecordToDTO(recordRepository.save(record));
    }

    @Override
    public ProductionRecordDTO updateRecord(UUID id, ProductionRecordDTO dto) {
        ProductionRecord record = recordRepository.findById(id).orElseThrow();
        updateRecordEntity(record, dto);
        return mapRecordToDTO(recordRepository.save(record));
    }

    @Override
    public void deleteRecord(UUID id) {
        recordRepository.deleteById(id);
    }

    // --- PITS ---

    @Override
    @Transactional(readOnly = true)
    public List<PitDTO> getAllPits() {
        return pitRepository.findAll().stream()
                .map(this::mapPitToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PitDTO createPit(PitDTO dto) {
        Pit pit = new Pit();
        updatePitEntity(pit, dto);
        return mapPitToDTO(pitRepository.save(pit));
    }

    @Override
    public PitDTO updatePit(UUID id, PitDTO dto) {
        Pit pit = pitRepository.findById(id).orElseThrow();
        updatePitEntity(pit, dto);
        return mapPitToDTO(pitRepository.save(pit));
    }

    @Override
    public void deletePit(UUID id) {
        pitRepository.deleteById(id);
    }

    // --- STOCKPILES ---

    @Override
    @Transactional(readOnly = true)
    public List<StockpileDTO> getAllStockpiles() {
        return stockpileRepository.findAll().stream()
                .map(this::mapStockpileToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StockpileDTO createStockpile(StockpileDTO dto) {
        Stockpile stockpile = new Stockpile();
        updateStockpileEntity(stockpile, dto);
        return mapStockpileToDTO(stockpileRepository.save(stockpile));
    }

    @Override
    public StockpileDTO updateStockpile(UUID id, StockpileDTO dto) {
        Stockpile stockpile = stockpileRepository.findById(id).orElseThrow();
        updateStockpileEntity(stockpile, dto);
        return mapStockpileToDTO(stockpileRepository.save(stockpile));
    }

    @Override
    public void deleteStockpile(UUID id) {
        stockpileRepository.deleteById(id);
    }

    // --- MAPPERS & HELPERS ---

    private void updateRecordEntity(ProductionRecord record, ProductionRecordDTO dto) {
        record.setDate(LocalDate.parse(dto.date()));
        record.setShift(dto.shift());
        record.setPit(pitRepository.findById(dto.pitId()).orElseThrow());

        if (dto.supervisorId() != null) {
            record.setSupervisor(employeeRepository.findById(dto.supervisorId()).orElse(null));
        }

        record.setOverburdenBcm(dto.overburdenBcm());
        record.setCoalMt(dto.coalMt());
        record.setStatus(dto.status());
        record.setNotes(dto.notes());
    }

    private ProductionRecordDTO mapRecordToDTO(ProductionRecord r) {
        return new ProductionRecordDTO(
                r.getId(),
                r.getDate().toString(),
                r.getShift(),
                r.getPit().getId(),
                r.getPit().getName(),
                r.getSupervisor() != null ? r.getSupervisor().getId() : null,
                r.getSupervisor() != null ? r.getSupervisor().getName() : null,
                r.getOverburdenBcm(),
                r.getCoalMt(),
                r.getStrippingRatio(),
                r.getStatus(),
                r.getNotes());
    }

    private void updatePitEntity(Pit pit, PitDTO dto) {
        // Note: PitDTO doesn't have projectId field, removed
        pit.setCode(dto.code());
        pit.setName(dto.name());
        pit.setBlock(dto.block());
        pit.setStripRatioPlan(dto.stripRatioPlan());
        // Note: PitDTO doesn't have status field, removed
    }

    private PitDTO mapPitToDTO(Pit p) {
        return new PitDTO(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getBlock(),
                p.getStripRatioPlan());
    }

    private void updateStockpileEntity(Stockpile s, StockpileDTO dto) {
        if (dto.projectId() != null) {
            s.setProject(projectRepository.findById(dto.projectId()).orElse(null));
        }
        s.setCode(dto.code());
        s.setName(dto.name());

        if (dto.locationId() != null) {
            s.setLocation(locationRepository.findById(dto.locationId()).orElse(null));
        }

        s.setCapacityMt(dto.capacityMt());
        s.setCurrentVolumeMt(dto.currentVolumeMt());
    }

    private StockpileDTO mapStockpileToDTO(Stockpile s) {
        return new StockpileDTO(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getLocation() != null ? s.getLocation().getId() : null,
                s.getLocation() != null ? s.getLocation().getName() : null,
                s.getProject() != null ? s.getProject().getId() : null,
                s.getProject() != null ? s.getProject().getName() : null,
                s.getCapacityMt(),
                s.getCurrentVolumeMt());
    }
}
