package com.pharmacopee.ladafura.dto.admin.plante;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NomPlanteDto {

    private Long id;

    @NotBlank(message = "Le nom vernaculaire est obligatoire")
    private String nom;

    private String langue;

    @Builder.Default
    private String pays = "Mali";
}
