package com.pharmacopee.ladafura.dto.pharmacopee.caisse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutSessionCaisse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Session d'une caisse physique")
public class SessionCaisseResponse {
    private Long id;
    private Long caisseId;
    private StatutSessionCaisse statut;
    private LocalDateTime dateOuverture;
    private BigDecimal fondOuverture;
    private LocalDateTime dateCloture;
    private BigDecimal montantTheorique;
    private BigDecimal montantConstate;
    private BigDecimal ecart;
    private String commentaire;
}
