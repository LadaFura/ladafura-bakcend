package com.pharmacopee.ladafura.dto.admin.collecte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCollecteStatsResponse {

    private long total;
    private long soumises;
    private long enExamen;
    private long validees;
    private long rejetees;
}
