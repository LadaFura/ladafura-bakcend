package com.pharmacopee.ladafura.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitRequest;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitResponse;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationPharmacopeeRetraitOptionsResponse;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationRetraitMapper;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationRetraitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PopulationRetraitServiceImpl implements IPopulationRetraitService {

    private final PharmacopeeRepository pharmacopeeRepository;
    private final ModeRetraitRepository modeRetraitRepository;
    private final PopulationRetraitMapper mapper;

    @Override
    public PopulationPharmacopeeRetraitOptionsResponse getOptionsRetraitPharmacopee(Long pharmacopeeId) {
        log.info("Consultation des options de retrait (Livraison/Pickup) pour la pharmacopée ID: {}", pharmacopeeId);

        Pharmacopee pharmacopee = pharmacopeeRepository.findById(pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", pharmacopeeId));

        if (pharmacopee.getStatut() != StatutPharmacopee.VALIDEE) {
            throw new BadRequestException("Cette pharmacopée n'est pas agréée pour proposer des services de commande et retrait.");
        }

        List<ModeRetrait> modes = modeRetraitRepository.findByPharmacopeeId(pharmacopeeId);
        return mapper.toPharmacopeeOptionsResponse(pharmacopee, modes);
    }

    @Override
    public PopulationEstimationRetraitResponse estimerOptionRetrait(PopulationEstimationRetraitRequest request) {
        log.info("Estimation du mode de retrait ({}) pour la pharmacopée ID: {}", request.getType(), request.getPharmacopeeId());

        Pharmacopee pharmacopee = pharmacopeeRepository.findById(request.getPharmacopeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", request.getPharmacopeeId()));

        if (pharmacopee.getStatut() != StatutPharmacopee.VALIDEE) {
            throw new BadRequestException("Cette pharmacopée n'est pas agréée pour proposer des services de commande et retrait.");
        }

        Optional<ModeRetrait> modeOpt = modeRetraitRepository.findByPharmacopeeIdAndType(pharmacopee.getId(), request.getType());

        return mapper.toEstimationResponse(pharmacopee, modeOpt.orElse(null), request.getType());
    }
}
