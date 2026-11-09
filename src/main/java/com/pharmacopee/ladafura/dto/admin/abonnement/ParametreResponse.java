package com.pharmacopee.ladafura.dto.admin.abonnement;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.TypeValeurParametre;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paramètre de plateforme")
public class ParametreResponse {
    private String code;
    private String valeur;
    private TypeValeurParametre typeValeur;
    private String description;
    private LocalDateTime dateModification;
}
