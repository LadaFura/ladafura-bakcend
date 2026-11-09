package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutAbonnement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Abonnement avec les conditions souscrites")
public class AbonnementResponse {
    private Long id;
    private Long pharmacopeeId;
    private String pharmacopeeNom;
    private String codeOffre;
    private String nomOffre;
    private BigDecimal prix;
    private String devise;
    private Integer dureeMois;
    private Integer joursGrace;
    private StatutAbonnement statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateFinGrace;
    private LocalDateTime dateSouscription;
    private Boolean renouvellementAutomatique;
    private String motifAnnulation;
    private List<LimiteConsommationResponse> limites;
}
