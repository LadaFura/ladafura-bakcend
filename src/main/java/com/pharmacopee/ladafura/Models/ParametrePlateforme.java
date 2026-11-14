package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.TypeValeurParametre;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Paramètre global modifiable par l'administrateur sans redéploiement
 * (ex. MAX_PHARMACOPEES_PAR_GERANT, TAUX_TAXE_ABONNEMENT).
 * Voir membres-roles-abonnements.puml.
 */
@Entity
@Table(name = "parametres_plateforme")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametrePlateforme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    private String valeur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeValeurParametre typeValeur;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Administrateur auteur de la dernière modification. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifie_par_id")
    private Utilisateur modifiePar;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        if (this.typeValeur == null) this.typeValeur = TypeValeurParametre.TEXTE;
        if (this.dateModification == null) this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}
