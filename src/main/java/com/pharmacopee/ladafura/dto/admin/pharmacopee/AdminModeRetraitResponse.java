package com.pharmacopee.ladafura.dto.admin.pharmacopee;

import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import lombok.*;

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
