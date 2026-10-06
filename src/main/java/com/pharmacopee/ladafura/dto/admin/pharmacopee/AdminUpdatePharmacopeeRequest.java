package com.pharmacopee.ladafura.dto.admin.pharmacopee;

import com.pharmacopee.ladafura.enums.StatutPharmacopee;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUpdatePharmacopeeRequest {

    @Size(min = 2, max = 150, message = "Le nom doit comporter entre 2 et 150 caractères")
    private String nom;

    private String description;

    private String telephone;

    private StatutPharmacopee statut;

    private Long utilisateurId;

    private java.util.List<Long> praticienIds;

    // Localisation
    private String region;
    private String cercle;
    private String commune;
    private String localite;
    private Double latitude;
    private Double longitude;

    private String photoUrl;
}
