package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.ConfigurationLimiteOffre;
import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.HistoriqueAbonnement;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.MotifSuspensionMembre;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeEvenementAbonnement;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.ConfigurationLimiteOffreRepository;
import com.pharmacopee.ladafura.repository.HistoriqueAbonnementRepository;
import com.pharmacopee.ladafura.repository.LimiteAbonnementRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.OffreAbonnementRepository;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AbonnementCycleServiceImpl implements IAbonnementCycleService {

    private final AbonnementRepository abonnementRepository;
    private final LimiteAbonnementRepository limiteAbonnementRepository;
    private final HistoriqueAbonnementRepository historiqueRepository;
    private final OffreAbonnementRepository offreRepository;
    private final ConfigurationLimiteOffreRepository configurationRepository;
    private final MembrePharmacopeeRepository membreRepository;
    private final IJournalAuditService journalAuditService;

    @Override
    public Abonnement creerDepuisOffre(Pharmacopee pharmacopee, OffreAbonnement offre, MembrePharmacopee souscritPar) {
        Abonnement abonnement = base(pharmacopee, offre, offre.getPrix(), offre.getDureeMois(), offre.getJoursGrace());
        abonnement.setSouscritPar(souscritPar);
        for (ConfigurationLimiteOffre configuration : configurationRepository.findByOffreId(offre.getId())) {
            abonnement.ajouterLimite(LimiteAbonnement.builder()
                    .limiteOffre(configuration.getLimite())
                    .codeLimite(configuration.getLimite().getCodeLimite())
                    .valeurMaximale(configuration.getValeurMaximale())
                    .illimite(configuration.getIllimite())
                    .build());
        }
        return abonnementRepository.save(abonnement);
    }

    @Override
    public Abonnement creerNegocie(Pharmacopee pharmacopee, OffreAbonnement offre, BigDecimal prix, int dureeMois, int joursGrace,
                                   List<QuotaNegocie> quotas, DemandeContrat demande, Utilisateur administrateur) {
        Abonnement abonnement = base(pharmacopee, offre, prix, dureeMois, joursGrace);
        abonnement.setDemandeContrat(demande);
        abonnement.setCreeParAdmin(administrateur);
        for (QuotaNegocie quota : quotas) {
            abonnement.ajouterLimite(LimiteAbonnement.builder()
                    .limiteOffre(quota.limite())
                    .codeLimite(quota.limite().getCodeLimite())
                    .valeurMaximale(quota.illimite() ? null : quota.valeurMaximale())
                    .illimite(quota.illimite())
                    .build());
        }
        return abonnementRepository.save(abonnement);
    }

    private static Abonnement base(Pharmacopee pharmacopee, OffreAbonnement offre, BigDecimal prix, Integer dureeMois, Integer joursGrace) {
        return Abonnement.builder()
                .pharmacopee(pharmacopee)
                .offre(offre)
                .statut(StatutAbonnement.EN_ATTENTE_PAIEMENT)
                .codeOffreSouscrite(offre.getCode())
                .nomOffreSouscrite(offre.getNom())
                .prixSouscrit(prix)
                .deviseSouscrite(offre.getDevise())
                .dureeMoisSouscrite(dureeMois)
                .joursGraceSouscrits(joursGrace)
                .renouvellementAutomatique(false)
                .build();
    }

    @Override
    public void activerApresPaiement(Abonnement abonnement, Utilisateur acteur) {
        if (abonnement.getStatut() != StatutAbonnement.EN_ATTENTE_PAIEMENT) {
            throw new ConflictException("Cet abonnement n'est pas en attente de paiement.");
        }
        Long pharmacopeeId = abonnement.getPharmacopee().getId();
        if (abonnementRepository.existsByPharmacopeeIdAndStatut(pharmacopeeId, StatutAbonnement.SUSPENDU)) {
            throw new ConflictException("Un abonnement suspendu existe pour cette pharmacopée : réactivez-le ou annulez-le d'abord.");
        }
        LocalDateTime maintenant = LocalDateTime.now();
        Optional<Abonnement> enVigueur = abonnementRepository.findEnVigueur(pharmacopeeId);
        if (enVigueur.isEmpty()) {
            activer(abonnement, maintenant, TypeEvenementAbonnement.ACTIVATION, acteur);
            return;
        }
        Abonnement courant = enVigueur.get();
        abonnement.setAbonnementPrecedent(courant);
        if (OFFRE_GRATUITE.equals(courant.getCodeOffreSouscrite())) {
            terminer(courant, StatutAbonnement.EXPIRE, null, acteur);
            activer(abonnement, maintenant, TypeEvenementAbonnement.ACTIVATION, acteur);
        } else if (courant.getStatut() == StatutAbonnement.EN_GRACE) {
            terminer(courant, StatutAbonnement.EXPIRE, null, acteur);
            activer(abonnement, maintenant, TypeEvenementAbonnement.RENOUVELLEMENT, acteur);
        } else if (courant.getCodeOffreSouscrite().equals(abonnement.getCodeOffreSouscrite())) {
            programmer(abonnement, courant, acteur);
        } else {
            terminer(courant, StatutAbonnement.ANNULE, "Changement d'offre", acteur);
            activer(abonnement, maintenant, TypeEvenementAbonnement.CHANGEMENT_OFFRE, acteur);
        }
    }

    private void programmer(Abonnement abonnement, Abonnement courant, Utilisateur acteur) {
        Long pharmacopeeId = abonnement.getPharmacopee().getId();
        if (abonnementRepository.existsByPharmacopeeIdAndStatut(pharmacopeeId, StatutAbonnement.PROGRAMME)) {
            throw new ConflictException("Un renouvellement est déjà programmé pour cette pharmacopée.");
        }
        abonnement.setStatut(StatutAbonnement.PROGRAMME);
        abonnement.setDateDebut(courant.getDateFin());
        abonnement.setDateFin(calculerFin(abonnement, courant.getDateFin()));
        abonnement.setDateRenouvellement(LocalDateTime.now());
        abonnementRepository.saveAndFlush(abonnement);
        historiser(abonnement, TypeEvenementAbonnement.PROGRAMMATION, StatutAbonnement.EN_ATTENTE_PAIEMENT.name(),
                StatutAbonnement.PROGRAMME.name(), "Renouvellement anticipé", acteur);
    }

    /** Écrit et flushé avant toute activation : la base n'admet qu'un abonnement en vigueur. */
    private void terminer(Abonnement abonnement, StatutAbonnement statut, String motif, Utilisateur acteur) {
        String ancien = abonnement.getStatut().name();
        abonnement.setStatut(statut);
        if (abonnement.getDateFin() == null || abonnement.getDateFin().isAfter(LocalDateTime.now())) {
            abonnement.setDateFin(LocalDateTime.now());
        }
        if (statut == StatutAbonnement.ANNULE) {
            abonnement.setDateAnnulation(LocalDateTime.now());
            abonnement.setMotifAnnulation(motif);
        }
        abonnementRepository.saveAndFlush(abonnement);
        TypeEvenementAbonnement type = statut == StatutAbonnement.ANNULE ? TypeEvenementAbonnement.ANNULATION : TypeEvenementAbonnement.EXPIRATION;
        historiser(abonnement, type, ancien, statut.name(), motif, acteur);
    }

    private void activer(Abonnement abonnement, LocalDateTime debut, TypeEvenementAbonnement evenement, Utilisateur acteur) {
        String ancien = abonnement.getStatut().name();
        abonnement.setStatut(StatutAbonnement.ACTIF);
        abonnement.setDateDebut(debut);
        abonnement.setDateFin(calculerFin(abonnement, debut));
        abonnementRepository.saveAndFlush(abonnement);
        historiser(abonnement, evenement, ancien, StatutAbonnement.ACTIF.name(), null, acteur);
        reactiverMembresDansLaLimite(abonnement);
    }

    private static LocalDateTime calculerFin(Abonnement abonnement, LocalDateTime debut) {
        return abonnement.getDureeMoisSouscrite() != null ? debut.plusMonths(abonnement.getDureeMoisSouscrite()) : null;
    }

    private void reactiverMembresDansLaLimite(Abonnement abonnement) {
        Long pharmacopeeId = abonnement.getPharmacopee().getId();
        Optional<LimiteAbonnement> limite = limiteUtilisateurs(abonnement);
        if (limite.isEmpty()) {
            return;
        }
        long actifs = membreRepository.countByPharmacopeeIdAndStatut(pharmacopeeId, StatutMembre.ACTIF);
        List<MembrePharmacopee> suspendus = membreRepository.findByPharmacopeeIdAndStatutAndMotifSuspensionOrderByIdAsc(
                pharmacopeeId, StatutMembre.SUSPENDU, MotifSuspensionMembre.QUOTA_ABONNEMENT);
        for (MembrePharmacopee membre : suspendus) {
            if (!limite.get().autorise(actifs)) {
                break;
            }
            membre.setStatut(StatutMembre.ACTIF);
            membre.setMotifSuspension(null);
            actifs++;
            tracerMembre(abonnement.getPharmacopee(), membre, TypeActionAudit.MEMBRE_ACTIVE, "Réactivation après activation de l'abonnement");
        }
    }

    private Optional<LimiteAbonnement> limiteUtilisateurs(Abonnement abonnement) {
        return limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(abonnement.getId(), IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS);
    }

    @Override
    public Abonnement retournerAuGratuit(Pharmacopee pharmacopee, Abonnement precedent, Utilisateur acteur) {
        OffreAbonnement gratuite = offreRepository.findByCode(OFFRE_GRATUITE)
                .orElseThrow(() -> new ResourceNotFoundException("Offre", "code", OFFRE_GRATUITE));
        Abonnement abonnement = creerDepuisOffre(pharmacopee, gratuite, null);
        abonnement.setAbonnementPrecedent(precedent);
        activer(abonnement, LocalDateTime.now(), TypeEvenementAbonnement.RETOUR_GRATUIT, acteur);
        suspendreMembresEnTrop(abonnement);
        return abonnement;
    }

    private void suspendreMembresEnTrop(Abonnement abonnement) {
        Optional<LimiteAbonnement> limite = limiteUtilisateurs(abonnement);
        if (limite.isEmpty() || Boolean.TRUE.equals(limite.get().getIllimite()) || limite.get().getValeurMaximale() == null) {
            return;
        }
        Long pharmacopeeId = abonnement.getPharmacopee().getId();
        long actifs = membreRepository.countByPharmacopeeIdAndStatut(pharmacopeeId, StatutMembre.ACTIF);
        List<MembrePharmacopee> plusRecentsDabord = membreRepository
                .findByPharmacopeeIdAndStatutAndEstProprietaireFalseOrderByDateAdhesionDescIdDesc(pharmacopeeId, StatutMembre.ACTIF);
        for (MembrePharmacopee membre : plusRecentsDabord) {
            if (actifs <= limite.get().getValeurMaximale()) {
                break;
            }
            membre.setStatut(StatutMembre.SUSPENDU);
            membre.setMotifSuspension(MotifSuspensionMembre.QUOTA_ABONNEMENT);
            actifs--;
            tracerMembre(abonnement.getPharmacopee(), membre, TypeActionAudit.MEMBRE_SUSPENDU, "Quota de l'offre gratuite dépassé");
        }
    }

    @Override
    public int traiterEcheances(LocalDateTime maintenant) {
        int traites = 0;
        for (Abonnement abonnement : abonnementRepository.findByStatutAndDateFinBefore(StatutAbonnement.ACTIF, maintenant)) {
            traiterFinDePeriode(abonnement);
            traites++;
        }
        for (Abonnement abonnement : abonnementRepository.findByStatutAndDateFinGraceBefore(StatutAbonnement.EN_GRACE, maintenant)) {
            terminerGrace(abonnement);
            traites++;
        }
        return traites;
    }

    private void traiterFinDePeriode(Abonnement abonnement) {
        Optional<Abonnement> programme = abonnementRepository.findFirstByPharmacopeeIdAndStatut(abonnement.getPharmacopee().getId(), StatutAbonnement.PROGRAMME);
        if (programme.isPresent()) {
            terminer(abonnement, StatutAbonnement.EXPIRE, null, null);
            activer(programme.get(), programme.get().getDateDebut(), TypeEvenementAbonnement.RENOUVELLEMENT, null);
            return;
        }
        abonnement.setStatut(StatutAbonnement.EN_GRACE);
        abonnement.setDateFinGrace(abonnement.getDateFin().plusDays(abonnement.getJoursGraceSouscrits()));
        historiser(abonnement, TypeEvenementAbonnement.PASSAGE_GRACE, StatutAbonnement.ACTIF.name(), StatutAbonnement.EN_GRACE.name(), null, null);
    }

    private void terminerGrace(Abonnement abonnement) {
        terminer(abonnement, StatutAbonnement.EXPIRE, null, null);
        retournerAuGratuit(abonnement.getPharmacopee(), abonnement, null);
    }

    @Override
    public void historiser(Abonnement abonnement, TypeEvenementAbonnement type, String ancienne, String nouvelle,
                           String description, Utilisateur acteur) {
        historiqueRepository.save(HistoriqueAbonnement.builder()
                .abonnement(abonnement)
                .typeEvenement(type)
                .ancienneValeur(ancienne)
                .nouvelleValeur(nouvelle)
                .description(description)
                .acteur(acteur)
                .build());
    }

    private void tracerMembre(Pharmacopee pharmacopee, MembrePharmacopee membre, TypeActionAudit action, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.SYSTEME)
                .pharmacopee(pharmacopee)
                .typeEntite("MembrePharmacopee")
                .entiteId(membre.getId())
                .description(description)
                .build());
    }
}
