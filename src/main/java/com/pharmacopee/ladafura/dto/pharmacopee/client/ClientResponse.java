package com.pharmacopee.ladafura.dto.pharmacopee.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Client de la pharmacopée")
public class ClientResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String observation;
    private Boolean actif;
    private LocalDateTime dateCreation;
    @Schema(description = "Reste à payer de ses ventes validées")
    private BigDecimal encours;
}
