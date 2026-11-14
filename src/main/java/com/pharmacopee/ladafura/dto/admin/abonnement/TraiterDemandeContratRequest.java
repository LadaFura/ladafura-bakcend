package com.pharmacopee.ladafura.dto.admin.abonnement;

import com.pharmacopee.ladafura.enums.StatutDemandeContrat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Changement de statut d'une demande de contrat")
public class TraiterDemandeContratRequest {
    @NotNull(message = "Le statut est obligatoire")
    private StatutDemandeContrat statut;
    private String observation;
}
