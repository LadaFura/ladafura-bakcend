package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InvitationResponse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeInvitationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeInvitationServiceImpl implements IPharmacopeeInvitationService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final IPharmacopeeContexteService contexteService;
    private final IPharmacopeeQuotaService quotaService;
    private final IJournalAuditService journalAuditService;
    private final MembrePharmacopeeRepository membreRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getMesInvitations() {
        Utilisateur utilisateur = pharmacopeeAuthService.getCurrentUtilisateur();
        return membreRepository.findByUtilisateurIdAndStatut(utilisateur.getId(), StatutMembre.INVITE).stream()
                .filter(m -> !estExpiree(m))
                .map(m -> InvitationResponse.builder()
                        .id(m.getId())
                        .pharmacopeeId(m.getPharmacopee().getId())
                        .pharmacopeeNom(m.getPharmacopee().getNom())
                        .poste(m.getPoste())
                        .dateInvitation(m.getDateInvitation())
                        .dateExpiration(m.getDateExpirationInvitation())
                        .build())
                .toList();
    }

    /** L'expiration lève une ConflictException sans annuler le passage de l'invitation à RETIRE. */
    @Override
    @Transactional(noRollbackFor = ConflictException.class)
    public void accepter(Long id) {
        Utilisateur utilisateur = pharmacopeeAuthService.getCurrentUtilisateur();
        MembrePharmacopee invitation = invitationDe(id, utilisateur);
        if (estExpiree(invitation)) {
            invitation.setStatut(StatutMembre.RETIRE);
            throw new ConflictException("Cette invitation a expiré. Demandez-en une nouvelle au gérant.");
        }
        Long pharmacopeeId = invitation.getPharmacopee().getId();
        contexteService.verifierEcritureAutorisee(pharmacopeeId);
        quotaService.verifierQuota(pharmacopeeId, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS);
        invitation.setStatut(StatutMembre.ACTIF);
        invitation.setDateAdhesion(LocalDateTime.now());
        tracer(invitation, utilisateur, TypeActionAudit.MEMBRE_ACTIVE, "Invitation acceptée");
    }

    @Override
    public void refuser(Long id) {
        Utilisateur utilisateur = pharmacopeeAuthService.getCurrentUtilisateur();
        MembrePharmacopee invitation = invitationDe(id, utilisateur);
        invitation.setStatut(StatutMembre.RETIRE);
        tracer(invitation, utilisateur, TypeActionAudit.MEMBRE_RETIRE, "Invitation refusée");
    }

    private MembrePharmacopee invitationDe(Long id, Utilisateur utilisateur) {
        return membreRepository.findByIdAndUtilisateurIdAndStatut(id, utilisateur.getId(), StatutMembre.INVITE)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));
    }

    private static boolean estExpiree(MembrePharmacopee invitation) {
        return invitation.getDateExpirationInvitation() != null
                && invitation.getDateExpirationInvitation().isBefore(LocalDateTime.now());
    }

    private void tracer(MembrePharmacopee invitation, Utilisateur utilisateur, TypeActionAudit action, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.PHARMACOPEE)
                .pharmacopee(invitation.getPharmacopee())
                .utilisateur(utilisateur)
                .typeEntite("MembrePharmacopee")
                .entiteId(invitation.getId())
                .description(description)
                .build());
    }
}
