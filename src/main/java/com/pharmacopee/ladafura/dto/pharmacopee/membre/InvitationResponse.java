package com.pharmacopee.ladafura.dto.pharmacopee.membre;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Invitation reçue par l'utilisateur connecté")
public class InvitationResponse {
    private Long id;
    private Long pharmacopeeId;
    private String pharmacopeeNom;
    private String poste;
    private LocalDateTime dateInvitation;
    private LocalDateTime dateExpiration;
}
