package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Depense;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.DepenseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.EnregistrerDepenseRequest;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutEcriture;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.CaisseMapper;
import com.pharmacopee.ladafura.repository.DepenseRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeDepenseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService.NouvelleOperation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeDepenseServiceImpl implements IPharmacopeeDepenseService {

    private final IPharmacopeeContexteService contexteService;
    private final IPharmacopeeOperationCaisseService operationCaisseService;
    private final IJournalAuditService journalAuditService;
    private final DepenseRepository depenseRepository;
    private final OperationCaisseRepository operationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DepenseResponse> getDepenses(LocalDate debut, LocalDate fin, Pageable pageable) {
        LocalDateTime depuis = debut != null ? debut.atStartOfDay() : null;
        LocalDateTime jusqua = fin != null ? fin.atTime(LocalTime.MAX) : null;
        return depenseRepository.rechercher(pharmacopeeEnLecture(), depuis, jusqua, pageable).map(CaisseMapper::depense);
    }

    @Override
    @Transactional(readOnly = true)
    public DepenseResponse getDepense(Long id) {
        return CaisseMapper.depense(depenseDe(pharmacopeeEnLecture(), id));
    }

    @Override
    public DepenseResponse creer(EnregistrerDepenseRequest request) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.DEPENSES_GERER);
        Depense depense = depenseRepository.save(Depense.builder()
                .pharmacopee(contexte.pharmacopee())
                .enregistrePar(contexte.membre())
                .libelle(request.getLibelle())
                .categorie(request.getCategorie())
                .montant(request.getMontant())
                .dateDepense(request.getDateDepense())
                .beneficiaire(request.getBeneficiaire())
                .justificatifUrl(request.getJustificatifUrl())
                .observation(request.getObservation())
                .build());
        operationCaisseService.enregistrer(contexte.pharmacopee().getId(), NouvelleOperation.builder()
                .caisseId(request.getCaisseId())
                .sens(SensOperation.SORTIE)
                .nature(NatureOperationCaisse.DEPENSE)
                .montant(request.getMontant())
                .methode(request.getMethode())
                .libelle(request.getLibelle())
                .auteur(contexte.membre())
                .depense(depense)
                .build());
        return CaisseMapper.depense(depense);
    }

    @Override
    public DepenseResponse annuler(Long id, String motif) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.DEPENSES_GERER);
        Long pharmacopeeId = contexte.pharmacopee().getId();
        Depense depense = depenseDe(pharmacopeeId, id);
        if (depense.getStatut() != StatutEcriture.VALIDE) {
            throw new ConflictException("Cette dépense est déjà annulée.");
        }
        Long operationId = operationRepository.findFirstByDepenseIdAndNatureNot(depense.getId(), NatureOperationCaisse.ANNULATION)
                .orElseThrow(() -> new ResourceNotFoundException("Opération de caisse de la dépense", "id", id))
                .getId();
        operationCaisseService.contrePasser(pharmacopeeId, operationId, "Annulation d'une dépense : " + motif, contexte.membre());
        depense.setStatut(StatutEcriture.ANNULE);
        depense.setDateAnnulation(LocalDateTime.now());
        depense.setMotifAnnulation(motif);
        journalAuditService.tracer(EvenementAudit.builder()
                .action(TypeActionAudit.DEPENSE_ANNULEE)
                .origine(OrigineAction.PHARMACOPEE)
                .pharmacopee(contexte.pharmacopee())
                .utilisateur(contexte.membre().getUtilisateur())
                .typeEntite("Depense")
                .entiteId(depense.getId())
                .description(motif)
                .build());
        return CaisseMapper.depense(depense);
    }

    private Long pharmacopeeEnLecture() {
        return contexteService.verifierPermission(IPharmacopeeContexteService.DEPENSES_CONSULTER).pharmacopee().getId();
    }

    private Depense depenseDe(Long pharmacopeeId, Long id) {
        return depenseRepository.findByIdAndPharmacopeeId(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépense", "id", id));
    }
}
