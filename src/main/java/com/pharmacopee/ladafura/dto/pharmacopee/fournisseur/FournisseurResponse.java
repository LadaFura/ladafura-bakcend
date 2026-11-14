package com.pharmacopee.ladafura.dto.pharmacopee.fournisseur;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutFournisseur;
import com.pharmacopee.ladafura.enums.TypeFournisseur;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fournisseur de la pharmacopée")
public class FournisseurResponse {
    private Long id;
    private String nom;
    private TypeFournisseur typeFournisseur;
    private String telephone;
    private String email;
    private String adresse;
    private String description;
    private StatutFournisseur statut;
    private LocalDateTime dateCreation;
}
