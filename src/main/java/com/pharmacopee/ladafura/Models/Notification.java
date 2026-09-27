package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauNotification niveau = NiveauNotification.INFO;

    @Builder.Default
    @Column(nullable = false)
    private Boolean lue = false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dateNotification = LocalDateTime.now();

    private LocalDateTime dateLecture;

    private String referenceId;

    private String lien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id")
    @JsonIgnore
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnore
    private Utilisateur utilisateur;

    @PrePersist
    public void onCreate() {
        if (this.dateNotification == null) {
            this.dateNotification = LocalDateTime.now();
        }
        if (this.lue == null) {
            this.lue = false;
        }
        if (this.niveau == null) {
            this.niveau = NiveauNotification.INFO;
        }
    }
}
