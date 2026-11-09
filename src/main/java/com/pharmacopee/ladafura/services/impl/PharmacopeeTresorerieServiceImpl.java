package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.dto.pharmacopee.tresorerie.DepenseCategorieResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.tresorerie.FluxCompteResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.tresorerie.SyntheseTresorerieResponse;
import com.pharmacopee.ladafura.enums.CategorieDepense;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.DepenseRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeTresorerieService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PharmacopeeTresorerieServiceImpl implements IPharmacopeeTresorerieService {

    private static final List<StatutCommande> COMMANDES_REALISEES = List.of(StatutCommande.LIVREE, StatutCommande.RETIREE);

    private final IPharmacopeeContexteService contexteService;
    private final VenteRepository venteRepository;
    private final CommandeRepository commandeRepository;
    private final OperationCaisseRepository operationRepository;
    private final DepenseRepository depenseRepository;

    @Override
    public SyntheseTresorerieResponse getSynthese(LocalDate debut, LocalDate fin) {
        Long pharmacopeeId = contexteService.verifierPermission(IPharmacopeeContexteService.COMPTABILITE_CONSULTER).pharmacopee().getId();
        if (debut.isAfter(fin)) {
            throw new BadRequestException("La date de début doit précéder la date de fin.");
        }
        LocalDateTime depuis = debut.atStartOfDay();
        LocalDateTime jusqua = fin.atTime(LocalTime.MAX);
        return SyntheseTresorerieResponse.builder()
                .debut(debut)
                .fin(fin)
                .chiffreAffairesComptoir(venteRepository.chiffreAffaires(pharmacopeeId, depuis, jusqua))
                .chiffreAffairesCommandes(BigDecimal.valueOf(commandeRepository.sumMontantTotalParPeriode(pharmacopeeId, COMMANDES_REALISEES, depuis, jusqua)))
                .comptes(comptes(operationRepository.fluxParCompte(pharmacopeeId, depuis, jusqua)))
                .depenses(depenseRepository.totauxParCategorie(pharmacopeeId, depuis, jusqua).stream()
                        .map(l -> DepenseCategorieResponse.builder().categorie((CategorieDepense) l[0]).montant((BigDecimal) l[1]).build())
                        .toList())
                .build();
    }

    /** Regroupe sur une ligne par compte les totaux reçus par sens. */
    private static List<FluxCompteResponse> comptes(List<Object[]> lignes) {
        Map<Long, FluxCompteResponse> parCompte = new LinkedHashMap<>();
        for (Object[] ligne : lignes) {
            FluxCompteResponse compte = parCompte.computeIfAbsent((Long) ligne[0], id -> FluxCompteResponse.builder()
                    .caisseId(id).caisseNom((String) ligne[1]).type((TypeCaisse) ligne[2])
                    .entrees(BigDecimal.ZERO).sorties(BigDecimal.ZERO).build());
            if (ligne[3] == SensOperation.ENTREE) {
                compte.setEntrees((BigDecimal) ligne[4]);
            } else {
                compte.setSorties((BigDecimal) ligne[4]);
            }
        }
        return new ArrayList<>(parCompte.values());
    }
}
