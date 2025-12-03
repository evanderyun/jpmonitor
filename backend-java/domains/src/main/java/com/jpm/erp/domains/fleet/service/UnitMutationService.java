package com.jpm.erp.domains.fleet.service;

import com.jpm.erp.domains.fleet.dto.UnitMutationDTO;
import java.util.List;
import java.util.UUID;

public interface UnitMutationService {
    List<UnitMutationDTO> getAllMutations();

    UnitMutationDTO createMutation(UnitMutationDTO dto);

    UnitMutationDTO updateMutation(UUID id, UnitMutationDTO dto);

    void deleteMutation(UUID id);
}
