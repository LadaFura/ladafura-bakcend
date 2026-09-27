package com.pharmacopee.ladafura.dto.requestes;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class SourceDtoRequeste {
  private Long id;

    @NotBlank(message= "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message= "Le prénom est obligatoire")
    private String prenom;  
    
}
