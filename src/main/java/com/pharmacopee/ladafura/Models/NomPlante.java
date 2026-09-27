package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
