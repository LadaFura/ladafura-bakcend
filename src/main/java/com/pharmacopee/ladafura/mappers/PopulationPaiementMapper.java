package com.pharmacopee.ladafura.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationMethodePaiementInfoDto;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationPaiementResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

@Component
public class PopulationPaiementMapper {

    public List<PopulationMethodePaiementInfoDto> getAvailableMethodes() {
        return List.of(
                PopulationMethodePaiementInfoDto.builder()
                        .code(MethodePaiement.MOBILE_MONEY)
                        .libelle("Mobile Money (Orange Money, Moov Money, Wave)")
                        .description("Règlement instantané et sécurisé par portefeuille mobile au Mali.")
                        .disponible(true)
                        .operateurs(List.of("Orange Money Mali", "Moov Africa Malitel", "Wave Mali"))
                        .instructions("Saisissez votre numéro de téléphone et validez le prélèvement via le code USSD (#144# / *166#) ou l'application de votre opérateur.")
                        .build(),
                PopulationMethodePaiementInfoDto.builder()
                        .code(MethodePaiement.CASH)
                        .libelle("Paiement en espèces (à la livraison ou au comptoir)")
                        .description("Règlement en espèces directement lors de la livraison ou au comptoir de la pharmacopée.")
                        .disponible(true)
                        .operateurs(List.of("Espèces (FCFA)"))
                        .instructions("Prévoyez l'appoint si possible lors de la réception de votre commande ou lors du retrait en officine.")
                        .build(),
                PopulationMethodePaiementInfoDto.builder()
                        .code(MethodePaiement.CARTE_BANCAIRE)
                        .libelle("Carte bancaire (Visa, Mastercard, GIM-UEMOA)")
                        .description("Paiement sécurisé par carte bancaire nationale ou internationale.")
                        .disponible(true)
                        .operateurs(List.of("Visa", "Mastercard", "GIM-UEMOA"))
                        .instructions("Paiement sécurisé par authentification 3D-Secure.")
                        .build()
        );
    }

    public String formatLibelleMethode(MethodePaiement methode, String operateur) {
        if (methode == null) {
            return "Non défini";
        }
        return switch (methode) {
            case MOBILE_MONEY -> (operateur != null && !operateur.isBlank())
                    ? "Mobile Money (" + operateur + ")"
                    : "Mobile Money (Orange/Moov/Wave)";
            case CASH -> "Paiement en espèces (Cash)";
            case CARTE_BANCAIRE -> "Carte bancaire (Visa/Mastercard)";
        };
    }

    public PopulationPaiementResponse toResponse(Paiement p, Commande c, String message) {
        if (p == null) {
            return null;
        }

        Commande commande = (c != null) ? c : p.getCommande();
        boolean isSuccess = p.getStatut() == StatutPaiement.REUSSI;

        return PopulationPaiementResponse.builder()
                .id(p.getId())
                .reference(p.getReference())
                .commandeId(commande != null ? commande.getId() : null)
                .numeroCommande(commande != null ? commande.getNumero() : null)
                .montant(p.getMontant())
                .methode(p.getMethode())
                .libelleMethode(formatLibelleMethode(p.getMethode(), null))
                .statut(p.getStatut())
                .datePaiement(p.getDatePaiement())
                .succes(isSuccess)
                .message(message)
                .statutCommande(commande != null ? commande.getStatut() : null)
                .build();
    }
}
