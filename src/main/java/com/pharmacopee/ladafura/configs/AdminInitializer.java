package com.pharmacopee.ladafura.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final IFirebaseAuthService firebaseAuthService;
    private final UtilisateurRepository utilisateurRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // 0. Migration / Réparation du schéma de base MySQL sur Render / Aiven
        try {
            log.info("Vérification et alignement du schéma MySQL pour la table notifications...");
            jdbcTemplate.execute("ALTER TABLE notifications MODIFY COLUMN type VARCHAR(60) NOT NULL");
            jdbcTemplate.execute("ALTER TABLE notifications MODIFY COLUMN niveau VARCHAR(30) NOT NULL");
            jdbcTemplate.execute("ALTER TABLE notifications MODIFY COLUMN type_entite_liee VARCHAR(60) NULL");
            log.info("Colonnes de la table 'notifications' alignées avec succès (VARCHAR 60/30).");
        } catch (Exception e) {
            log.info("Note : ajustement direct ALTER TABLE ignoré ou déjà appliqué ({})", e.getMessage());
        }

        String adminUid = "8G8iXLoEDqZkl2DiImkSIehkdMC2";
        String adminEmail = "admin@ladafura.ml";

        log.info("Vérification et initialisation du compte administrateur racine...");

        // 1. Positionnement du claim rôle ADMINISTRATEUR dans Firebase
        try {
            firebaseAuthService.setRole(adminUid, "ADMINISTRATEUR");
            log.info("Rôle Firebase 'ADMINISTRATEUR' synchronisé avec succès pour UID: {}", adminUid);
        } catch (Exception e) {
            log.warn("Impossible de synchroniser le rôle avec Firebase (mode hors-ligne ou clé non configurée) : {}", e.getMessage());
        }

        // 2. Synchronisation dans la base MySQL
        Utilisateur admin = utilisateurRepository.findByEmail(adminEmail)
                .orElseGet(() -> {
                    Utilisateur u = new Utilisateur();
                    u.setEmail(adminEmail);
                    u.setNom("LADAFURA");
                    u.setPrenom("Admin");
                    return u;
                });

        admin.setFirebaseUid(adminUid);
        admin.setRole(Role.ADMINISTRATEUR);
        admin.setStatut(StatutUtilisateur.ACTIF);

        Utilisateur saved = utilisateurRepository.save(admin);
        log.info("Compte administrateur prêt en base MySQL (ID: {}, Role: {})", saved.getId(), saved.getRole());
    }
}
