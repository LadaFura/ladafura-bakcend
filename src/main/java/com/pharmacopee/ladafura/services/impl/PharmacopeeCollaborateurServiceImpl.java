package com.pharmacopee.ladafura.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Praticien;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.CreateCollaborateurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.PharmacopeeCollaborateurResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.UpdateCollaborateurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.CreerMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.MembreResponse;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PraticienRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCollaborateurService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMembreService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeRoleService;

import lombok.RequiredArgsConstructor;

/**
 * Adaptateur provisoire : conserve le contrat de /pharmacopee/collaborateurs pour les écrans Angular actuels,
 * au-dessus des membres. À retirer avec ces écrans au lot 7. L'identifiant exposé reste celui de l'utilisateur.
 */
@Service
@RequiredArgsConstructor
public class PharmacopeeCollaborateurServiceImpl implements IPharmacopeeCollaborateurService {

    private final MembrePharmacopeeRepository membreRepository;
    private final PraticienRepository praticienRepository;
    private final RolePharmacopeeRepository roleRepository;
    private final IPharmacopeeContexteService contexteService;
    private final IPharmacopeeMembreService membreService;

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacopeeCollaborateurResponse> getCollaborateurs(Pageable pageable) {
        Long pharmacopeeId = contexteService.verifierPharmacopeeValidee(IPharmacopeeContexteService.MEMBRES_CONSULTER).pharmacopee().getId();
        return membreRepository.findByPharmacopeeIdAndEstProprietaireFalseAndStatutNot(pharmacopeeId, StatutMembre.RETIRE, pageable)
                .map(this::versReponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeCollaborateurResponse getCollaborateurById(Long id) {
        ContexteMembre contexte = contexteService.verifierPharmacopeeValidee(IPharmacopeeContexteService.MEMBRES_CONSULTER);
        return versReponse(collaborateurDe(contexte, id));
    }

    @Override
    @Transactional
    public PharmacopeeCollaborateurResponse addCollaborateur(CreateCollaborateurRequest request) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.MEMBRES_INVITER);
        Long roleManagerId = roleRepository.findByCodeAndPharmacopeeIsNull(IPharmacopeeRoleService.ROLE_MANAGER)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "code", IPharmacopeeRoleService.ROLE_MANAGER))
                .getId();
        CreerMembreRequest creation = new CreerMembreRequest();
        creation.setNom(request.getNom());
        creation.setPrenom(request.getPrenom());
        creation.setEmail(request.getEmail());
        creation.setTelephone(request.getTelephone());
        creation.setMotDePasse(request.getMotDePasse());
        creation.setPoste(request.getSpecialite());
        creation.setRoleIds(List.of(roleManagerId));
        MembreResponse cree = membreService.creer(creation);
        return versReponse(membreRepository.findByIdAndPharmacopeeId(cree.getId(), contexte.pharmacopee().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Membre", "id", cree.getId())));
    }

    @Override
    @Transactional
    public PharmacopeeCollaborateurResponse updateCollaborateur(Long id, UpdateCollaborateurRequest request) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.MEMBRES_GERER);
        MembrePharmacopee membre = collaborateurDe(contexte, id);
        refuserModificationIdentite(membre.getUtilisateur(), request);
        membre.setPoste(request.getSpecialite());
        appliquerStatut(membre, request.getStatut());
        return versReponse(membre);
    }

    @Override
    @Transactional
    public void deleteCollaborateur(Long id) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.MEMBRES_GERER);
        membreService.retirer(collaborateurDe(contexte, id).getId());
    }

    private MembrePharmacopee collaborateurDe(ContexteMembre contexte, Long utilisateurId) {
        return membreRepository.findByUtilisateurIdAndPharmacopeeId(utilisateurId, contexte.pharmacopee().getId())
                .filter(membre -> !Boolean.TRUE.equals(membre.getEstProprietaire()) && membre.getStatut() != StatutMembre.RETIRE)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborateur non trouvé ou non autorisé"));
    }

    /** Le compte peut servir dans d'autres pharmacopées, et l'email identifie la connexion : un gérant ne les modifie pas. */
    private static void refuserModificationIdentite(Utilisateur compte, UpdateCollaborateurRequest request) {
        boolean identiteInchangee = java.util.Objects.equals(compte.getEmail(), request.getEmail())
                && java.util.Objects.equals(compte.getNom(), request.getNom())
                && java.util.Objects.equals(compte.getPrenom(), request.getPrenom())
                && java.util.Objects.equals(compte.getTelephone(), request.getTelephone());
        if (!identiteInchangee) {
            throw new BadRequestException("Le nom, l'email et le téléphone appartiennent au compte de la personne et ne peuvent pas être modifiés ici.");
        }
    }

    private void appliquerStatut(MembrePharmacopee membre, StatutUtilisateur statut) {
        if (statut == StatutUtilisateur.ACTIF && membre.getStatut() == StatutMembre.SUSPENDU) {
            membreService.reactiver(membre.getId());
        } else if (statut != null && statut != StatutUtilisateur.ACTIF && membre.getStatut() == StatutMembre.ACTIF) {
            membreService.suspendre(membre.getId());
        }
    }

    private PharmacopeeCollaborateurResponse versReponse(MembrePharmacopee membre) {
        Utilisateur compte = membre.getUtilisateur();
        String specialite = membre.getPoste() != null ? membre.getPoste()
                : praticienRepository.findById(compte.getId()).map(Praticien::getSpecialite).orElse(null);
        return PharmacopeeCollaborateurResponse.builder()
                .id(compte.getId())
                .nom(compte.getNom())
                .prenom(compte.getPrenom())
                .email(compte.getEmail())
                .telephone(compte.getTelephone())
                .specialite(specialite)
                .statut(membre.getStatut() == StatutMembre.ACTIF ? StatutUtilisateur.ACTIF : compte.getStatut() == StatutUtilisateur.ACTIF ? StatutUtilisateur.SUSPENDU : compte.getStatut())
                .dateCreation(compte.getDateCreation())
                .build();
    }
}
