package com.pharmacopee.ladafura.dto.agent.media;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'association directe des URLs de médias (photo, audio) à une fiche de collecte")
public class AgentMediaUpdateRequest {

    @Size(max = 500, message = "L'URL de la photo ne peut pas dépasser 500 caractères")
    @Schema(description = "URL publique ou cloud de la photo de l'échantillon/plante", example = "https://storage.ladafura.ml/collectes/photos/img_12.jpg")
    private String photoUrl;

    @Size(max = 500, message = "L'URL de l'enregistrement audio ne peut pas dépasser 500 caractères")
    @Schema(description = "URL publique ou cloud de l'enregistrement audio du témoignage", example = "https://storage.ladafura.ml/collectes/audios/rec_12.mp3")
    private String audioUrl;
}
