package com.pharmacopee.ladafura.dto.admin.pharmacopee;

import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPharmacopeeSummaryResponse {

    private Long id;
    private String nom;
    private String telephone;
    private StatutPharmacopee statut;
    private String region;
    private String cercle;
    private String commune;
    private String localite;
    private String nomProprietaire;
    private Integer nbProduits;
}
