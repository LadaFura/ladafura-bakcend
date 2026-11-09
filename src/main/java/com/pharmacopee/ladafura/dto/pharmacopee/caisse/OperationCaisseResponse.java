package com.pharmacopee.ladafura.dto.pharmacopee.caisse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Opération de caisse")
public class OperationCaisseResponse {
    private Long id;
    private Long caisseId;
    private Long sessionId;
    private SensOperation sens;
    private NatureOperationCaisse nature;
    private BigDecimal montant;
    private String libelle;
    private LocalDateTime dateOperation;
    private Long operationLieeId;
}
