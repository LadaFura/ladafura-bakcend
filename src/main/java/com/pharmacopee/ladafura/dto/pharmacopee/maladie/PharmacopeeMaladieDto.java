package com.pharmacopee.ladafura.dto.pharmacopee.maladie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacopeeMaladieDto {
    private Long id;
    private String nom;
    private String description;
}

