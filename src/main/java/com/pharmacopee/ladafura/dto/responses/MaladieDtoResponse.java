package com.pharmacopee.ladafura.dto.responses;


import lombok.Builder;
import lombok.Data;
@Data 
@Builder 
public class MaladieDtoResponse {
    private Long id;

    private String nom;

    private String description;
}
