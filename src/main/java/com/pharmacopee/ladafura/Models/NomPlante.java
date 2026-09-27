package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "noms_plantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NomPlante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String langue;

    @Builder.Default
    private String pays = "Mali";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plante_id", nullable = false)
    @JsonIgnore
    private Plante plante;
}
