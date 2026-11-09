package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.TypeEvenementAbonnement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Événement de l'historique d'un abonnement")
public class EvenementAbonnementResponse {
    private Long id;
    private TypeEvenementAbonnement typeEvenement;
    private String ancienneValeur;
    private String nouvelleValeur;
    private LocalDateTime dateEvenement;
    private String description;
}
