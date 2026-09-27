package com.pharmacopee.ladafura.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "localisations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
