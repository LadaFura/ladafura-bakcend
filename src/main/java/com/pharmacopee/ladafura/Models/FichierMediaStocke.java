package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "fichiers_medias_stockes",
    indexes = {
        @Index(name = "idx_fichier_media_chemin", columnList = "chemin", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichierMediaStocke {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chemin", nullable = false, unique = true, length = 500)
    private String chemin;

    @Column(name = "nom_original", length = 255)
    private String nomOriginal;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "taille", nullable = false)
    private Long taille;

    @Lob
    @Column(name = "donnees", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] donnees;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }
}
