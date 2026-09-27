package com.pharmacopee.ladafura.dto.admin.pharmacopee;

import com.pharmacopee.ladafura.enums.TypeModeRetrait;

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
public class AdminModeRetraitResponse {

    private Long id;
    private TypeModeRetrait type;
    private Boolean actif;
    private Double frais;
}
