package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutCollecte;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "collectes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collecte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateCollecte;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCollecte statut = StatutCollecte.BROUILLON;

    private String photoUrl;

    private String audioUrl;

    private LocalDateTime dateSoumission;

    @Column(columnDefinition = "TEXT")
    private String motifRejet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_collecte_id")
    private AgentCollecte agentCollecte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localisation_id")
    private Localisation localisation;

    @Builder.Default
    @OneToMany(mappedBy = "collecte", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VertuDeLaPlante> vertus = new ArrayList<>();
}
