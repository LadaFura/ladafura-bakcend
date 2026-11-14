package com.pharmacopee.ladafura.services.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationCompteMobileMoneyResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPaiementDeclareService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementDeclareServiceImpl implements IPaiementDeclareService {

    private final CaisseRepository caisseRepository;
    private final PaiementRepository paiementRepository;
    private final PharmacopeeRepository pharmacopeeRepository;

    @Override
    public void exigerMethodeDisponible(MethodePaiement methode) {
        if (methode == MethodePaiement.CARTE_BANCAIRE) {
            throw new BadRequestException("Le paiement par carte bancaire n'est pas encore disponible. Choisissez Mobile Money ou les espèces.");
        }
    }

    @Override
    public void declarerMobileMoney(Paiement paiement, Long pharmacopeeId, Long compteId, String referenceTransaction, String telephone) {
        exigerRenseigne(compteId, "Choisissez le compte Mobile Money sur lequel vous avez payé.");
        exigerRenseigne(telephone, "Le numéro de téléphone Mobile Money est obligatoire.");
        exigerRenseigne(referenceTransaction, "Le numéro de transaction reçu par SMS est obligatoire.");

        // Le compte vient du client : il doit appartenir à la pharmacopée de la commande.
        Caisse compte = caisseRepository.findByIdAndPharmacopeeId(compteId, pharmacopeeId)
                .filter(c -> Boolean.TRUE.equals(c.getActif()) && c.getType() == TypeCaisse.MOBILE_MONEY)
                .orElseThrow(() -> new BadRequestException("Ce compte Mobile Money n'est pas valable pour cette pharmacopée."));

        String reference = normaliser(referenceTransaction);
        if (paiementRepository.referenceDejaDeclaree(compte.getId(), reference, StatutPaiement.ECHOUE, paiement.getId())) {
            throw new ConflictException("Ce numéro de transaction a déjà été déclaré. Vérifiez le SMS de confirmation.");
        }

        paiement.setCaisse(compte);
        paiement.setReference(reference);
        paiement.setTelephoneEmetteur(telephone.trim());
        paiement.setMotifRejet(null);
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopulationCompteMobileMoneyResponse> getComptesMobileMoney(Long pharmacopeeId) {
        boolean validee = pharmacopeeRepository.findById(pharmacopeeId)
                .filter(p -> p.getStatut() == StatutPharmacopee.VALIDEE)
                .isPresent();
        if (!validee) {
            return List.of();
        }
        return caisseRepository.findByPharmacopeeIdAndActifTrue(pharmacopeeId).stream()
                .filter(c -> c.getType() == TypeCaisse.MOBILE_MONEY && c.getReferenceCompte() != null && !c.getReferenceCompte().isBlank())
                .map(c -> PopulationCompteMobileMoneyResponse.builder()
                        .id(c.getId())
                        .nom(c.getNom())
                        .numero(c.getReferenceCompte())
                        .build())
                .toList();
    }

    private static void exigerRenseigne(Long valeur, String message) {
        if (valeur == null) {
            throw new BadRequestException(message);
        }
    }

    private static void exigerRenseigne(String valeur, String message) {
        if (valeur == null || valeur.isBlank()) {
            throw new BadRequestException(message);
        }
    }

    /** Les mêmes chiffres saisis différemment doivent être reconnus comme un doublon. */
    private static String normaliser(String reference) {
        return reference.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
    }
}
