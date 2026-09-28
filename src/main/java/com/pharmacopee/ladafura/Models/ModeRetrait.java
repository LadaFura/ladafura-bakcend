package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
