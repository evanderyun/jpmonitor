package com.jpm.erp.api;

import com.jpm.erp.domains.core.entity.Location;
import com.jpm.erp.domains.core.entity.Project;
import com.jpm.erp.domains.core.repository.LocationRepository;
import com.jpm.erp.domains.core.repository.ProjectRepository;
import com.jpm.erp.domains.fleet.dto.DailyLogDTO;
import com.jpm.erp.domains.fleet.dto.UnitMutationDTO;
import com.jpm.erp.domains.fleet.entity.Equipment;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import com.jpm.erp.domains.fleet.service.DailyLogService;
import com.jpm.erp.domains.fleet.service.UnitMutationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
class IntegrationTest {

    @Autowired
    private DailyLogService dailyLogService;

    @Autowired
    private UnitMutationService unitMutationService;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void testFleetLifecycleFlow() {
        // 1. Setup Data
        Project project = new Project();
        project.setName("Test Project");
        project.setCode("PRJ-001");
        project.setStatus("ACTIVE");
        project = projectRepository.save(project);

        Location locA = new Location();
        locA.setName("Location A");
        locA.setCode("LOC-A");
        locA.setType("SITE");
        locA.setProject(project);
        locA = locationRepository.save(locA);

        Location locB = new Location();
        locB.setName("Location B");
        locB.setCode("LOC-B");
        locB.setType("WORKSHOP");
        locB.setProject(project);
        locB = locationRepository.save(locB);

        Equipment equipment = new Equipment();
        equipment.setCode("EQ-001");
        equipment.setName("Excavator 001");
        equipment.setHourMeter(new BigDecimal("1000"));
        equipment.setLocation(locA);
        equipment.setProject(project);
        equipment.setStatus("READY");
        equipment = equipmentRepository.save(equipment);

        // 2. Create Daily Log (Update HM)
        // Use DTO
        DailyLogDTO logDTO = new DailyLogDTO(
                null,
                LocalDate.now().toString(),
                equipment.getId(),
                equipment.getCode(),
                null,
                "Test Operator",
                locA.getId(),
                locA.getName(),
                project.getId(),
                project.getName(),
                new BigDecimal("1000"),
                new BigDecimal("1010"),
                new BigDecimal("10"),
                "WORKING",
                "Test Log"
        );

        dailyLogService.createLog(logDTO);

        // Verify HM updated
        Equipment updatedEq = equipmentRepository.findById(equipment.getId()).orElseThrow();
        assertEquals(new BigDecimal("1010.00"), updatedEq.getHourMeter());

        // 3. Create Unit Mutation (Move to Loc B)
        // Use DTO
        UnitMutationDTO mutationDTO = new UnitMutationDTO(
                null,
                "MUT-001",
                "TRANSFER",
                equipment.getId(),
                equipment.getCode(),
                locA.getId(),
                locA.getName(),
                locB.getId(),
                locB.getName(),
                LocalDate.now().toString(),
                null,
                new BigDecimal("1010"),
                "Test Mutation",
                "Driver A",
                "Trailer",
                "B 1234 XX",
                "JPM",
                "Sender",
                "JPM",
                "Receiver"
        );

        unitMutationService.createMutation(mutationDTO);

        // Verify Location updated
        updatedEq = equipmentRepository.findById(equipment.getId()).orElseThrow();
        assertEquals(locB.getId(), updatedEq.getLocation().getId());
    }
}