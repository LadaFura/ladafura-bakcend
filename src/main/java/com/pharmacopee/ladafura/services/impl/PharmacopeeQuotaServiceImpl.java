package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

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

    private final AbonnementRepository abonnementRepository;
    private final LimiteAbonnementRepository limiteAbonnementRepository;
    private final MembrePharmacopeeRepository membrePharmacopeeRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final VenteRepository venteRepository;

    @Override
    public void verifierQuota(Long pharmacopeeId, String codeLimite) {
        Abonnement abonnement = abonnementRepository.findEnVigueur(pharmacopeeId)
                .orElseThrow(() -> new QuotaExceededException("Cette pharmacopée n'a pas d'abonnement en vigueur."));
        LimiteAbonnement limite = limiteAbonnementRepository
                .findByAbonnementIdAndCodeLimite(abonnement.getId(), codeLimite)
                .orElseThrow(() -> new QuotaExceededException("Cet abonnement ne définit pas la limite " + codeLimite + "."));

        long consommation = consommationActuelle(pharmacopeeId, codeLimite);
        if (!limite.autorise(consommation)) {
            log.warn("Quota {} atteint pour la pharmacopée ID {} ({}/{})",
                    codeLimite, pharmacopeeId, consommation, limite.getValeurMaximale());
            throw new QuotaExceededException("Vous avez atteint la limite de votre abonnement ("
                    + limite.getValeurMaximale() + "). Veuillez souscrire à une offre supérieure.");
        }
    }

    private long consommationActuelle(Long pharmacopeeId, String codeLimite) {
        return switch (codeLimite) {
            case MAX_UTILISATEURS_ACTIFS ->
                    membrePharmacopeeRepository.countByPharmacopeeIdAndStatut(pharmacopeeId, StatutMembre.ACTIF);
            case MAX_PRODUITS -> disponibiliteProduitRepository.countByPharmacopeeId(pharmacopeeId);
            case MAX_VENTES_MENSUELLES -> ventesValideesDuMois(pharmacopeeId);
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
