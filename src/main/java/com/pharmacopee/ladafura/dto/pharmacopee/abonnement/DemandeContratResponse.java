package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutDemandeContrat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Demande de contrat personnalisé")
public class DemandeContratResponse {
    private Long id;
    private Long pharmacopeeId;
    private Integer dureeSouhaiteeMois;
    private LocalDateTime dateDemande;
    private String nomContact;
    private String telephone;
    private String email;
    private String descriptionBesoin;
    private StatutDemandeContrat statut;
    private String observation;
    private LocalDateTime dateTraitement;
}
