package com.pharmacopee.ladafura.dto.admin.etude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AdminEtudeRequest {

    @NotBlank(message = "Le titre de l'étude est obligatoire")
    private String titre;

    private String auteurs;

    private Integer annee;

    private String reference;

    private String resume;

    private String documentUrl;

    @NotNull(message = "L'identifiant de la plante associée est obligatoire")
    private Long planteId;
}
