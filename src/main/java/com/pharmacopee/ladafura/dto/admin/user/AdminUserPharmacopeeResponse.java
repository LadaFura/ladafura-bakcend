package com.pharmacopee.ladafura.dto.admin.user;

import com.pharmacopee.ladafura.enums.StatutPharmacopee;

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
public class AdminUserPharmacopeeResponse {

    private Long id;
    private String nom;
    private String telephone;
    private StatutPharmacopee statut;
    private String region;
    private String cercle;
    private String commune;
    private String localite;
    private Boolean estTitulairePrincipal;
}
