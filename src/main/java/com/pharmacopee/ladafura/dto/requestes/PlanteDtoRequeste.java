package com.pharmacopee.ladafura.dto.requestes;
import com.pharmacopee.ladafura.Enums.StatutPlante;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
@Data 
@Builder 

public class PlanteDtoRequeste {
    
    private String nomScientifique;
    @NotBlank(message= "La description est obligatoire")
    private String description;
    
    private StatutPlante statut;
}