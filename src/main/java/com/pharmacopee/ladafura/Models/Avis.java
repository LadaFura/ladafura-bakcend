package com.pharmacopee.ladafura.Models;

import com.pharmacopee.ladafura.enums.StatutAvis;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "avis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dateAvis = LocalDateTime.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAvis statut = StatutAvis.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String reponseOfficine;

    private LocalDateTime dateReponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @PrePersist
    public void onCreate() {
        if (this.dateAvis == null) {
            this.dateAvis = LocalDateTime.now();
        }
    }
}
