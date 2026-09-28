package com.pharmacopee.ladafura.dto.responses;
import com.pharmacopee.ladafura.Enums.StatutPlante;

import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@Builder
@NoArgsConstructor
public class PlanteDtoResponse {
    
    private Long id;

    private String nomScientifique;

    private String description;
    
    private StatutPlante statut;
}