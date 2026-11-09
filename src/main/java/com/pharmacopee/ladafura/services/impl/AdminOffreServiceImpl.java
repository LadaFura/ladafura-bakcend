package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.ConfigurationLimiteOffre;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.dto.admin.abonnement.DefinirQuotasOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.EnregistrerOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.LigneQuotaRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.LimiteOffreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeOffre;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AbonnementMapper;
import com.pharmacopee.ladafura.repository.ConfigurationLimiteOffreRepository;
import com.pharmacopee.ladafura.repository.LimiteOffreRepository;
import com.pharmacopee.ladafura.repository.OffreAbonnementRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminOffreService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOffreServiceImpl implements IAdminOffreService {

    private final OffreAbonnementRepository offreRepository;
    private final LimiteOffreRepository limiteRepository;
    private final ConfigurationLimiteOffreRepository configurationRepository;
    private final IJournalAuditService journalAuditService;
    private final IPharmacopeeAuthService pharmacopeeAuthService;

    @Override
    @Transactional(readOnly = true)
    public List<OffreResponse> getOffres() {
        return offreRepository.findAll().stream().map(this::versReponse).toList();
    }

    @Override
    public OffreResponse creer(EnregistrerOffreRequest request) {
        if (offreRepository.findByCode(request.getCode()).isPresent()) {
            throw new ConflictException("Offre", "code", request.getCode());
        }
        exigerPrixCoherent(request.getTypeOffre(), request.getPrix());
        OffreAbonnement offre = offreRepository.save(OffreAbonnement.builder()
                .code(request.getCode())
                .nom(request.getNom())
                .description(request.getDescription())
                .typeOffre(request.getTypeOffre())
                .dureeMois(dureeDu(request.getTypeOffre()))
                .prix(request.getPrix())
                .joursGrace(request.getJoursGrace())
                .actif(false)
                .build());
        tracer(offre, "Création de l'offre");
        return versReponse(offre);
    }

    @Override
    public OffreResponse modifier(Long id, EnregistrerOffreRequest request) {
        OffreAbonnement offre = offre(id);
        exigerPrixCoherent(offre.getTypeOffre(), request.getPrix());
        offre.setNom(request.getNom());
        offre.setDescription(request.getDescription());
        offre.setPrix(request.getPrix());
        if (request.getJoursGrace() != null) {
            offre.setJoursGrace(request.getJoursGrace());
        }
        if (Boolean.TRUE.equals(offre.getActif())) {
            exigerVendable(offre);
        }
        tracer(offre, "Modification de l'offre");
        return versReponse(offre);
    }

    @Override
    public OffreResponse activer(Long id) {
        OffreAbonnement offre = offre(id);
        exigerVendable(offre);
        offre.setActif(true);
        tracer(offre, "Activation de l'offre");
        return versReponse(offre);
    }

    @Override
    public OffreResponse desactiver(Long id) {
        OffreAbonnement offre = offre(id);
        if (offre.estGratuite()) {
            throw new ConflictException("L'offre gratuite doit rester active : toute pharmacopée peut y revenir.");
        }
        offre.setActif(false);
        tracer(offre, "Désactivation de l'offre");
        return versReponse(offre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LimiteOffreResponse> getLimites() {
        return limiteRepository.findAll().stream()
                .map(l -> LimiteOffreResponse.builder().id(l.getId()).codeLimite(l.getCodeLimite()).nom(l.getNom())
                        .description(l.getDescription()).periodicite(l.getPeriodicite()).modeCalcul(l.getModeCalcul()).build())
                .toList();
    }

    @Override
    public OffreResponse definirQuotas(Long offreId, DefinirQuotasOffreRequest request) {
        OffreAbonnement offre = offre(offreId);
        Map<Long, ConfigurationLimiteOffre> existantes = configurationRepository.findByOffreId(offreId).stream()
                .collect(Collectors.toMap(c -> c.getLimite().getId(), Function.identity()));
        Set<Long> demandees = request.getQuotas().stream().map(LigneQuotaRequest::getLimiteId).collect(Collectors.toSet());
        if (demandees.size() != request.getQuotas().size()) {
            throw new BadRequestException("Une même limite ne peut pas être renseignée deux fois.");
        }
        existantes.values().stream().filter(c -> !demandees.contains(c.getLimite().getId())).forEach(configurationRepository::delete);
        for (LigneQuotaRequest ligne : request.getQuotas()) {
            appliquer(offre, ligne, existantes.get(ligne.getLimiteId()));
        }
        configurationRepository.flush();
        if (Boolean.TRUE.equals(offre.getActif())) {
            exigerVendable(offre);
        }
        tracer(offre, "Modification des quotas de l'offre");
        return versReponse(offre);
    }

    private void appliquer(OffreAbonnement offre, LigneQuotaRequest ligne, ConfigurationLimiteOffre existante) {
        boolean illimite = Boolean.TRUE.equals(ligne.getIllimite());
        if (!illimite && ligne.getValeurMaximale() == null) {
            throw new BadRequestException("Renseignez une valeur maximale, ou indiquez que la limite est illimitée.");
        }
        Integer valeur = illimite ? null : ligne.getValeurMaximale();
        if (existante != null) {
            existante.setValeurMaximale(valeur);
            existante.setIllimite(illimite);
            return;
        }
        LimiteOffre limite = limiteRepository.findById(ligne.getLimiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Limite", "id", ligne.getLimiteId()));
        configurationRepository.save(ConfigurationLimiteOffre.builder().offre(offre).limite(limite).valeurMaximale(valeur).illimite(illimite).build());
    }

    private OffreAbonnement offre(Long id) {
        return offreRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Offre", "id", id));
    }

    /** Une offre payante n'est vendable qu'avec un vrai prix et tous ses quotas. */
    private void exigerVendable(OffreAbonnement offre) {
        if (offre.getTypeOffre() == TypeOffre.GRATUITE || offre.getTypeOffre() == TypeOffre.CONTRAT_PERSONNALISE) {
            return;
        }
        if (offre.getPrix().signum() <= 0) {
            throw new BadRequestException("Une offre payante doit avoir un prix supérieur à zéro.");
        }
        if (configurationRepository.findByOffreId(offre.getId()).size() < limiteRepository.count()) {
            throw new BadRequestException("Tous les quotas de l'offre doivent être renseignés.");
        }
    }

    private static void exigerPrixCoherent(TypeOffre type, BigDecimal prix) {
        if (type == TypeOffre.GRATUITE && prix.signum() != 0) {
            throw new BadRequestException("Le prix de l'offre gratuite doit être nul.");
        }
    }

    /** La durée découle du type : le catalogue ne peut pas proposer plus de 12 mois. */
    private static Integer dureeDu(TypeOffre type) {
        return switch (type) {
            case GRATUITE, CONTRAT_PERSONNALISE -> null;
            case MENSUELLE -> 1;
            case SEMESTRIELLE -> 6;
            case ANNUELLE -> 12;
        };
    }

    private OffreResponse versReponse(OffreAbonnement offre) {
        return AbonnementMapper.offre(offre, configurationRepository.findByOffreId(offre.getId()));
    }

    private void tracer(OffreAbonnement offre, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(TypeActionAudit.OFFRE_MODIFIEE)
                .origine(OrigineAction.ADMINISTRATION)
                .utilisateur(pharmacopeeAuthService.getCurrentUtilisateur())
                .typeEntite("OffreAbonnement")
                .entiteId(offre.getId())
                .description(description + " " + offre.getCode())
                .build());
    }
}
