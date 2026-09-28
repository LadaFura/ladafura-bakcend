package com.pharmacopee.ladafura.dto.population.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nombre de notifications non lues (pour badge UI)")
public class PopulationNotificationCountResponse {

    @Schema(description = "Nombre de notifications non lues", example = "3")
    private long count;
}
