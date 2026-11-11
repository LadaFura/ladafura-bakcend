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
                        .libelle("Mobile Money")
                        .description("Payez sur le compte de la pharmacopée, puis indiquez le numéro de transaction reçu par SMS.")
                        .disponible(true)
                        .operateurs(List.of())
                        .instructions("La pharmacopée vérifie la réception de votre paiement, puis confirme votre commande.")
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
                        .description("Bientôt disponible.")
                        .disponible(false)
                        .operateurs(List.of("Visa", "Mastercard", "GIM-UEMOA"))
                        .instructions("Ce moyen de paiement n'est pas encore disponible.")
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
