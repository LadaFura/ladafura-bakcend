package com.pharmacopee.ladafura.dto.agent.maladie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentMaladieResponse {
    private Long id;
    private String nom;
    private String description;
}

