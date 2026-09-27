package com.pharmacopee.ladafura.dto.responses;
import com.pharmacopee.ladafura.Enums.StatutPlante;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class PlanteDtoResponse {
    
    private Long id;

    private String nomScientifique;

    private String description;
    
    private StatutPlante statut;
}