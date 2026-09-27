package com.pharmacopee.ladafura.dto.admin.etude;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEtudeResponse {

    private Long id;
    private String titre;
    private String auteurs;
    private Integer annee;
    private String reference;
    private String resume;
    private String documentUrl;

    private Long planteId;
    private String planteNomScientifique;
}
