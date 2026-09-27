package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modes_retrait")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeRetrait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeModeRetrait type;

    @Builder.Default
    @Column(nullable = false)
    private Boolean actif = true;

    @Builder.Default
    @Column(nullable = false)
    private Double frais = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    @JsonIgnore
    private Pharmacopee pharmacopee;
}
