package com.pharmacopee.ladafura.dto.requestes;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
@Data 
@Builder 

public class MaladieDtoRequeste {
    
    @NotBlank(message= "La description est obligatoire")
    private String nom;
    
    @NotBlank(message= "La description est obligatoire")
    private String description;
}
    