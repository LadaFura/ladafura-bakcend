package com.pharmacopee.ladafura.mappers;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.requestes.PlanteDtoRequeste;
import com.pharmacopee.ladafura.dto.responses.PlanteDtoResponse;

@Component 
public class PlanteMapper {
    public PlanteDtoResponse toDto(Plante plante) {
        if (plante == null) {
            return null;
        }

        PlanteDtoResponse dto = new PlanteDtoResponse();
        dto.setId(plante.getId());
        dto.setDescription(plante.getDescription());
        // Map other fields as needed

        return dto;
    }

    public Plante toEntity(PlanteDtoRequeste dto) {
        if (dto == null) {
            return null;
        }

        Plante plante = new Plante();
        plante.setDescription(dto.getDescription());
        // Map other fields as needed

        return plante;
    }
}
