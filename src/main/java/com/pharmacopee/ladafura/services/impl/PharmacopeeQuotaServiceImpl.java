package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Comparator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutVente;
import com.pharmacopee.ladafura.exceptions.QuotaExceededException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LimiteAbonnementRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PharmacopeeQuotaServiceImpl implements IPharmacopeeQuotaService {

    private static final String PARAMETRE_MAX_PHARMACOPEES = "MAX_PHARMACOPEES_PAR_GERANT";
    private static final int MAX_PHARMACOPEES_PAR_DEFAUT = 2;

    private final AbonnementRepository abonnementRepository;
    private final LimiteAbonnementRepository limiteAbonnementRepository;
    private final MembrePharmacopeeRepository membrePharmacopeeRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final VenteRepository venteRepository;
    private final com.pharmacopee.ladafura.repository.ParametrePlateformeRepository parametreRepository;

    @Override
    public void verifierQuota(Long pharmacopeeId, String codeLimite) {
        List<Long> abonnements = abonnementsEnVigueur(pharmacopeesCouvertes(pharmacopeeId));
        if (abonnements.isEmpty()) {
            throw new QuotaExceededException("Cette pharmacopée n'a pas d'abonnement en vigueur.");
        }
        Plafond plafond = plusFavorable(abonnements, codeLimite)
                .orElseThrow(() -> new QuotaExceededException("Cet abonnement ne définit pas la limite " + codeLimite + "."));

        long consommation = consommationActuelle(pharmacopeeId, codeLimite);
        if (!plafond.autorise(consommation)) {
            log.warn("Quota {} atteint pour la pharmacopée ID {} ({}/{})",
                    codeLimite, pharmacopeeId, consommation, plafond.valeurMaximale());
            throw new QuotaExceededException("La limite de la formule est atteinte (" + plafond.valeurMaximale()
                    + "). Le gérant peut choisir une formule supérieure.");
        }
    }

    @Override
    public Optional<Plafond> plafondApplicable(Long pharmacopeeId, String codeLimite) {
        return plusFavorable(abonnementsEnVigueur(pharmacopeesCouvertes(pharmacopeeId)), codeLimite);
    }

    @Override
    public int maxPharmacopeesParGerant(Long utilisateurId) {
        Optional<Plafond> plafond = plusFavorable(abonnementsEnVigueur(pharmacopeesDuGerant(utilisateurId)), MAX_PHARMACOPEES);
        if (plafond.isEmpty() || (!plafond.get().illimite() && plafond.get().valeurMaximale() == null)) {
            return maxPharmacopeesPlateforme();
        }
        return plafond.get().illimite() ? PHARMACOPEES_ILLIMITEES : plafond.get().valeurMaximale();
    }

    /** La pharmacopée elle-même et toutes celles de son gérant. */
    private List<Long> pharmacopeesCouvertes(Long pharmacopeeId) {
        Set<Long> ids = new LinkedHashSet<>();
        ids.add(pharmacopeeId);
        membrePharmacopeeRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(pharmacopeeId)
                .ifPresent(gerant -> ids.addAll(pharmacopeesDuGerant(gerant.getUtilisateur().getId())));
        return List.copyOf(ids);
    }

    private List<Long> pharmacopeesDuGerant(Long utilisateurId) {
        return membrePharmacopeeRepository.findByUtilisateurIdAndStatut(utilisateurId, StatutMembre.ACTIF).stream()
                .filter(membre -> Boolean.TRUE.equals(membre.getEstProprietaire()))
                .map(membre -> membre.getPharmacopee().getId())
                .toList();
    }

    private List<Long> abonnementsEnVigueur(List<Long> pharmacopeesIds) {
        return pharmacopeesIds.stream()
                .map(abonnementRepository::findEnVigueur)
                .flatMap(Optional::stream)
                .map(Abonnement::getId)
                .toList();
    }

    /** Un plafond illimité l'emporte, sinon la valeur la plus haute. */
    private Optional<Plafond> plusFavorable(List<Long> abonnementsIds, String codeLimite) {
        return abonnementsIds.stream()
                .map(id -> limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(id, codeLimite)
                        .map(l -> new Plafond(l.getValeurMaximale(), Boolean.TRUE.equals(l.getIllimite()), id)))
                .flatMap(Optional::stream)
                .max(Comparator.comparing(Plafond::illimite)
                        .thenComparing(Plafond::valeurMaximale, Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    private int maxPharmacopeesPlateforme() {
        return parametreRepository.findByCode(PARAMETRE_MAX_PHARMACOPEES)
                .map(p -> Integer.parseInt(p.getValeur().trim()))
                .orElse(MAX_PHARMACOPEES_PAR_DEFAUT);
    }

    @Override
    public long pharmacopeesPossedees(Long utilisateurId) {
        return membrePharmacopeeRepository.countByUtilisateurIdAndEstProprietaireTrueAndStatut(utilisateurId, StatutMembre.ACTIF);
    }

    @Override
    public long consommationActuelle(Long pharmacopeeId, String codeLimite) {
        return switch (codeLimite) {
            case MAX_UTILISATEURS_ACTIFS ->
                    membrePharmacopeeRepository.countByPharmacopeeIdAndStatut(pharmacopeeId, StatutMembre.ACTIF);
            case MAX_PRODUITS -> disponibiliteProduitRepository.countByPharmacopeeId(pharmacopeeId);
            case MAX_VENTES_MENSUELLES -> ventesValideesDuMois(pharmacopeeId);
            case MAX_PHARMACOPEES -> membrePharmacopeeRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(pharmacopeeId)
                    .map(gerant -> pharmacopeesPossedees(gerant.getUtilisateur().getId()))
                    .orElse(1L);
            default -> throw new IllegalArgumentException("Limite inconnue : " + codeLimite);
        };
    }

    private long ventesValideesDuMois(Long pharmacopeeId) {
        YearMonth mois = YearMonth.now();
        LocalDateTime debut = mois.atDay(1).atStartOfDay();
        LocalDateTime fin = mois.atEndOfMonth().atTime(LocalTime.MAX);
        return venteRepository.countByPharmacopeeIdAndStatutAndDateVenteBetween(pharmacopeeId, StatutVente.VALIDEE, debut, fin);
    }
}
