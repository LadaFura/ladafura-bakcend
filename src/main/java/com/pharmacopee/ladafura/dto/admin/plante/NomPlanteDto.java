package com.pharmacopee.ladafura.dto.admin.plante;

import jakarta.validation.constraints.NotBlank;
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
public class NomPlanteDto {

    private Long id;

    @NotBlank(message = "Le nom vernaculaire est obligatoire")
    private String nom;

    private String langue;

    @Builder.Default
    private String pays = "Mali";
}
