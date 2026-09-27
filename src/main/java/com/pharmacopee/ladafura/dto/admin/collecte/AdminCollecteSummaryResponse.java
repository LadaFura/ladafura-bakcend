package com.pharmacopee.ladafura.dto.admin.collecte;

import com.pharmacopee.ladafura.enums.StatutCollecte;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCollecteSummaryResponse {

    private Long id;
    private LocalDateTime dateCollecte;
    private LocalDateTime dateSoumission;
    private StatutCollecte statut;
    private String nomCompletAgent;
    private String nomCompletSource;
    private String region;
    private String cercle;
    private String localite;
    private Integer nbVertus;
}
