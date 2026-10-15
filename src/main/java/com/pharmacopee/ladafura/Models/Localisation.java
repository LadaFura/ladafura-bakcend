package com.pharmacopee.ladafura.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "localisations")
@Getter
@Setter
public class Localisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String cercle;

    @Column(nullable = false)
    private String commune;

    @Column(nullable = false)
    private String localite;

    private Double latitude;

    private Double longitude;

    public Localisation() {
    }

    @Builder
    public Localisation(Long id, String region, String cercle, String commune, String localite, Double latitude, Double longitude) {
        this.id = id;
        this.region = region;
        this.cercle = cercle;
        this.commune = commune;
        this.localite = localite;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
