-- =============================================================================
-- V3 : Reprise des données existantes vers le modèle rôles/membres
-- =============================================================================
-- Prérequis : V2__roles_permissions_membres.sql exécuté (garanti par Flyway).
-- Objet     : recopier l'existant dans les nouvelles tables :
--               utilisateurs.role                 -> attributions_roles_plateforme
--               praticiens + praticiens_pharmacopees -> membres_pharmacopee
--               pharmacopees.utilisateur_id       -> membre propriétaire
--               estPraticienPrincipal             -> rôle GERANT / MANAGER
-- Nature    : LECTURE SEULE sur les anciennes tables (utilisateurs, praticiens,
--             praticiens_pharmacopees, pharmacopees ne sont pas modifiées).
--             Insertions uniquement dans les nouvelles tables.
-- Idempotent: oui (INSERT IGNORE sur clés uniques, NOT EXISTS).
-- Exécution : par Flyway, après sauvegarde. Sur une base vierge, le script ne
--             trouve aucune donnée et n'insère rien. Requêtes de diagnostic
--             (section 0) et de contrôle (section 5) à lancer à la main.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0. Diagnostic préalable (lecture seule)
-- -----------------------------------------------------------------------------
-- SELECT role, COUNT(*) FROM utilisateurs GROUP BY role;
-- SELECT COUNT(*) AS praticiens FROM praticiens;
-- SELECT COUNT(*) AS liens_praticien_pharmacopee FROM praticiens_pharmacopees;
-- SELECT COUNT(*) AS pharmacopees, SUM(utilisateur_id IS NULL) AS sans_proprietaire FROM pharmacopees;

-- -----------------------------------------------------------------------------
-- 1. Rôles de plateforme depuis utilisateurs.role
--    PHARMACOPEE n'est pas un rôle de plateforme : il devient une appartenance.
--    THERAPEUTE / HERBORISTE désignent des sources terrain : non repris.
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO attributions_roles_plateforme (utilisateur_id, role_plateforme_id, attribue_par_id, date_attribution)
SELECT u.id, rp.id, NULL, COALESCE(u.date_creation, NOW(6))
FROM utilisateurs u
JOIN roles_plateforme rp ON rp.code = u.role
WHERE u.role IN ('ADMINISTRATEUR', 'AGENT_COLLECTE', 'POPULATION');

-- -----------------------------------------------------------------------------
-- 2. Membres depuis la relation praticiens <-> pharmacopées
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO membres_pharmacopee
    (utilisateur_id, pharmacopee_id, statut, est_proprietaire, poste, numero_agrement, specialite,
     invite_par_id, date_invitation, date_adhesion, date_creation)
SELECT pp.praticien_id,
       pp.pharmacopee_id,
       CASE WHEN u.statut = 'ACTIF' THEN 'ACTIF' ELSE 'SUSPENDU' END,
       COALESCE(pr.est_praticien_principal, 1),
       CASE WHEN COALESCE(pr.est_praticien_principal, 1) = 1 THEN 'Gérant propriétaire' ELSE 'Collaborateur' END,
       pr.numero_agrement,
       pr.specialite,
       NULL,
       NULL,
       COALESCE(u.date_creation, NOW(6)),
       NOW(6)
FROM praticiens_pharmacopees pp
JOIN praticiens   pr ON pr.id = pp.praticien_id
JOIN utilisateurs u  ON u.id  = pp.praticien_id;

-- -----------------------------------------------------------------------------
-- 3. Propriétaire déclaré sur la pharmacopée (pharmacopees.utilisateur_id)
-- -----------------------------------------------------------------------------
-- 3.a Créer le membre s'il n'existe pas encore
INSERT IGNORE INTO membres_pharmacopee
    (utilisateur_id, pharmacopee_id, statut, est_proprietaire, poste, date_adhesion, date_creation)
SELECT ph.utilisateur_id, ph.id, 'ACTIF', 1, 'Gérant propriétaire',
       COALESCE(u.date_creation, NOW(6)), NOW(6)
FROM pharmacopees ph
JOIN utilisateurs u ON u.id = ph.utilisateur_id
WHERE ph.utilisateur_id IS NOT NULL;

-- 3.b S'il existait déjà comme collaborateur, le marquer propriétaire
UPDATE membres_pharmacopee m
JOIN pharmacopees ph ON ph.id = m.pharmacopee_id AND ph.utilisateur_id = m.utilisateur_id
SET m.est_proprietaire = 1,
    m.poste = 'Gérant propriétaire'
WHERE m.est_proprietaire = 0;

-- -----------------------------------------------------------------------------
-- 4. Rôles de pharmacopée des membres repris
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO attributions_roles_pharmacopee (membre_pharmacopee_id, role_pharmacopee_id, attribue_par_id, date_attribution)
SELECT m.id, r.id, NULL, NOW(6)
FROM membres_pharmacopee m
JOIN roles_pharmacopee r ON r.code = 'GERANT' AND r.pharmacopee_id IS NULL
WHERE m.est_proprietaire = 1;

INSERT IGNORE INTO attributions_roles_pharmacopee (membre_pharmacopee_id, role_pharmacopee_id, attribue_par_id, date_attribution)
SELECT m.id, r.id, NULL, NOW(6)
FROM membres_pharmacopee m
JOIN roles_pharmacopee r ON r.code = 'MANAGER' AND r.pharmacopee_id IS NULL
WHERE m.est_proprietaire = 0
  AND NOT EXISTS (SELECT 1 FROM attributions_roles_pharmacopee a WHERE a.membre_pharmacopee_id = m.id);

-- -----------------------------------------------------------------------------
-- 5. Contrôles après reprise (lecture seule) — à traiter manuellement si non vides
-- -----------------------------------------------------------------------------
-- 5.a Comptes PHARMACOPEE sans aucune appartenance
-- SELECT u.id, u.email FROM utilisateurs u
-- WHERE u.role = 'PHARMACOPEE'
--   AND NOT EXISTS (SELECT 1 FROM membres_pharmacopee m WHERE m.utilisateur_id = u.id);
--
-- 5.b Pharmacopées sans propriétaire actif
-- SELECT ph.id, ph.nom FROM pharmacopees ph
-- WHERE NOT EXISTS (SELECT 1 FROM membres_pharmacopee m
--                   WHERE m.pharmacopee_id = ph.id AND m.est_proprietaire = 1 AND m.statut = 'ACTIF');
--
-- 5.c Utilisateurs (hors sources terrain) sans rôle de plateforme ni appartenance
-- SELECT u.id, u.email, u.role FROM utilisateurs u
-- WHERE u.role NOT IN ('THERAPEUTE', 'HERBORISTE')
--   AND NOT EXISTS (SELECT 1 FROM attributions_roles_plateforme a WHERE a.utilisateur_id = u.id)
--   AND NOT EXISTS (SELECT 1 FROM membres_pharmacopee m WHERE m.utilisateur_id = u.id);
--
-- 5.d Volumes
-- SELECT statut, est_proprietaire, COUNT(*) FROM membres_pharmacopee GROUP BY statut, est_proprietaire;
-- SELECT r.code, COUNT(*) FROM attributions_roles_plateforme a JOIN roles_plateforme r ON r.id = a.role_plateforme_id GROUP BY r.code;

-- =============================================================================
-- ROLLBACK de la reprise (n'affecte pas les anciennes tables) :
--   DELETE FROM attributions_roles_pharmacopee WHERE attribue_par_id IS NULL;
--   DELETE FROM membres_pharmacopee WHERE invite_par_id IS NULL;
--   DELETE FROM attributions_roles_plateforme WHERE attribue_par_id IS NULL;
-- (à n'utiliser qu'avant toute saisie réelle dans le nouveau modèle)
-- =============================================================================
