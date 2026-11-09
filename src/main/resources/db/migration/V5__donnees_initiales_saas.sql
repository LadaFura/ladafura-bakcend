-- =============================================================================
-- V5 : Données initiales SaaS (rôles, permissions, offres, abonnements, caisses)
-- =============================================================================
-- Objet     : charger la matrice des rôles système, le catalogue des offres et des
--             limites, les paramètres de plateforme, puis donner à chaque pharmacopée
--             existante un abonnement GRATUITE et ses caisses par défaut.
-- Tables    : roles_pharmacopee, roles_pharmacopee_permissions, permissions,
--             attributions_roles_pharmacopee, roles_plateforme_permissions,
--             limites_offre, offres_abonnement, configurations_limites_offre,
--             parametres_plateforme, abonnements, limites_abonnement,
--             historiques_abonnement, caisses.
-- Idempotent: oui (INSERT IGNORE, NOT EXISTS) ; seule la matrice des 6 rôles système
--             est recréée à l'identique.
--
-- Rattrapage (section 1) : si une version antérieure de V2 a déjà été appliquée sur la
-- base, elle contient les anciens codes de rôles. Cette section les renomme ; elle est
-- sans effet ailleurs. Sur une telle base, lancer d'abord `flyway repair` : V2 et V3 ont
-- changé depuis (somme de contrôle différente).
--
-- Offres payantes : créées INACTIVES, au prix de 0 et sans quotas, tant que
-- l'administrateur n'a pas fixé les vrais prix et limites.
-- Pharmacopées existantes : leurs membres actifs ne sont pas suspendus, même au-delà du
-- quota gratuit ; le dépassement bloque seulement les nouvelles activations.
--
-- Réversibilité : suppression des lignes ajoutées ; le renommage de la section 1 n'est
-- pas annulé automatiquement.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Rattrapage des anciens codes de rôles (marqueur : rôle système PROPRIETAIRE)
--    L'ordre compte : GERANT doit devenir MANAGER avant que PROPRIETAIRE devienne GERANT.
-- -----------------------------------------------------------------------------
SET @ancienne_v2 = (SELECT COUNT(*) FROM roles_pharmacopee WHERE code = 'PROPRIETAIRE' AND pharmacopee_id IS NULL);

UPDATE roles_pharmacopee
SET code = 'MANAGER', libelle = 'Manager',
    description = 'Supervision des activités et des employés, hors membres, rôles et abonnement'
WHERE code = 'GERANT' AND pharmacopee_id IS NULL AND @ancienne_v2 > 0;

UPDATE roles_pharmacopee
SET code = 'GERANT', libelle = 'Gérant',
    description = 'Responsable principal : tous les droits sur la pharmacopée'
WHERE code = 'PROPRIETAIRE' AND pharmacopee_id IS NULL AND @ancienne_v2 > 0;

UPDATE roles_pharmacopee
SET code = 'VENDEUR_CAISSIER', libelle = 'Vendeur-caissier',
    description = 'Ventes au comptoir, clients, encaissements et sessions de caisse'
WHERE code = 'VENDEUR' AND pharmacopee_id IS NULL AND @ancienne_v2 > 0;

UPDATE roles_pharmacopee
SET code = 'GESTIONNAIRE_STOCK', libelle = 'Gestionnaire de stock',
    description = 'Produits et mouvements de stock'
WHERE code = 'MAGASINIER' AND pharmacopee_id IS NULL AND @ancienne_v2 > 0;

-- Report des attributions des deux rôles qui disparaissent
INSERT IGNORE INTO attributions_roles_pharmacopee (membre_pharmacopee_id, role_pharmacopee_id, attribue_par_id, date_attribution)
SELECT a.membre_pharmacopee_id, cible.id, a.attribue_par_id, a.date_attribution
FROM attributions_roles_pharmacopee a
JOIN roles_pharmacopee ancien ON ancien.id = a.role_pharmacopee_id AND ancien.pharmacopee_id IS NULL
JOIN roles_pharmacopee cible ON cible.pharmacopee_id IS NULL
 AND cible.code = CASE ancien.code WHEN 'CAISSIER' THEN 'VENDEUR_CAISSIER' WHEN 'COLLABORATEUR' THEN 'MANAGER' END
WHERE ancien.code IN ('CAISSIER', 'COLLABORATEUR') AND @ancienne_v2 > 0;

DELETE a FROM attributions_roles_pharmacopee a
JOIN roles_pharmacopee r ON r.id = a.role_pharmacopee_id
WHERE r.code IN ('CAISSIER', 'COLLABORATEUR') AND r.pharmacopee_id IS NULL AND @ancienne_v2 > 0;

DELETE rp FROM roles_pharmacopee_permissions rp
JOIN roles_pharmacopee r ON r.id = rp.role_pharmacopee_id
WHERE r.code IN ('CAISSIER', 'COLLABORATEUR') AND r.pharmacopee_id IS NULL AND @ancienne_v2 > 0;

DELETE FROM roles_pharmacopee
WHERE code IN ('CAISSIER', 'COLLABORATEUR') AND pharmacopee_id IS NULL AND @ancienne_v2 > 0;

-- -----------------------------------------------------------------------------
-- 2. Rôle absent de l'ancienne V2
-- -----------------------------------------------------------------------------
INSERT INTO roles_pharmacopee (code, libelle, description, pharmacopee_id, systeme, actif, date_creation)
SELECT 'GESTIONNAIRE_ACHATS', 'Gestionnaire des achats', 'Fournisseurs, achats et approvisionnements', NULL, 1, 1, NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM roles_pharmacopee WHERE code = 'GESTIONNAIRE_ACHATS' AND pharmacopee_id IS NULL);

-- -----------------------------------------------------------------------------
-- 3. Le rôle GERANT suit la propriété : les propriétaires retirés par V4 deviennent MANAGER
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO attributions_roles_pharmacopee (membre_pharmacopee_id, role_pharmacopee_id, attribue_par_id, date_attribution)
SELECT m.id, manager.id, NULL, NOW(6)
FROM membres_pharmacopee m
JOIN attributions_roles_pharmacopee a ON a.membre_pharmacopee_id = m.id
JOIN roles_pharmacopee gerant ON gerant.id = a.role_pharmacopee_id AND gerant.code = 'GERANT' AND gerant.pharmacopee_id IS NULL
JOIN roles_pharmacopee manager ON manager.code = 'MANAGER' AND manager.pharmacopee_id IS NULL
WHERE m.est_proprietaire = 0;

DELETE a FROM attributions_roles_pharmacopee a
JOIN membres_pharmacopee m ON m.id = a.membre_pharmacopee_id
JOIN roles_pharmacopee gerant ON gerant.id = a.role_pharmacopee_id
WHERE gerant.code = 'GERANT' AND gerant.pharmacopee_id IS NULL AND m.est_proprietaire = 0;

-- -----------------------------------------------------------------------------
-- 4. Permissions ajoutées au catalogue
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO permissions (code, libelle, module, portee, date_creation) VALUES
('CLIENTS_CONSULTER',           'Consulter les clients',                     'CLIENTS',     'PHARMACOPEE', NOW(6)),
('CLIENTS_GERER',               'Créer et modifier les clients',             'CLIENTS',     'PHARMACOPEE', NOW(6)),
('ABONNEMENT_CONSULTER',        'Consulter l''abonnement de la pharmacopée', 'ABONNEMENTS', 'PHARMACOPEE', NOW(6)),
('AUDIT_CONSULTER',             'Consulter le journal d''audit',             'AUDIT',       'PHARMACOPEE', NOW(6)),
('PLATEFORME_PARAMETRES_GERER', 'Gérer les paramètres de la plateforme',     'PARAMETRES',  'PLATEFORME',  NOW(6)),
('PLATEFORME_AUDIT_CONSULTER',  'Consulter tout le journal d''audit',        'AUDIT',       'PLATEFORME',  NOW(6));

INSERT IGNORE INTO roles_plateforme_permissions (role_plateforme_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_plateforme r JOIN permissions p
WHERE r.code = 'ADMINISTRATEUR' AND p.code IN ('PLATEFORME_PARAMETRES_GERER', 'PLATEFORME_AUDIT_CONSULTER');

-- -----------------------------------------------------------------------------
-- 5. Matrice des permissions des 6 rôles système (définition unique)
--    Les rôles personnalisés (pharmacopee_id non NULL) ne sont pas concernés.
-- -----------------------------------------------------------------------------
DELETE rp FROM roles_pharmacopee_permissions rp
JOIN roles_pharmacopee r ON r.id = rp.role_pharmacopee_id
WHERE r.pharmacopee_id IS NULL AND r.systeme = 1;

INSERT IGNORE INTO roles_pharmacopee_permissions (role_pharmacopee_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_pharmacopee r JOIN permissions p
WHERE r.code = 'GERANT' AND r.pharmacopee_id IS NULL AND p.portee = 'PHARMACOPEE';

INSERT IGNORE INTO roles_pharmacopee_permissions (role_pharmacopee_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_pharmacopee r JOIN permissions p
WHERE r.code = 'MANAGER' AND r.pharmacopee_id IS NULL AND p.portee = 'PHARMACOPEE'
  AND p.code NOT IN ('ROLES_GERER', 'ABONNEMENT_GERER', 'MEMBRES_INVITER', 'MEMBRES_GERER', 'PROFIL_PHARMACOPEE_GERER');

INSERT IGNORE INTO roles_pharmacopee_permissions (role_pharmacopee_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_pharmacopee r JOIN permissions p
WHERE r.code = 'VENDEUR_CAISSIER' AND r.pharmacopee_id IS NULL
  AND p.code IN ('PRODUITS_CONSULTER', 'STOCK_CONSULTER', 'VENTES_CONSULTER', 'VENTES_CREER',
                 'CLIENTS_CONSULTER', 'CLIENTS_GERER', 'COMMANDES_CONSULTER', 'COMMANDES_TRAITER',
                 'PAIEMENTS_CONSULTER', 'PAIEMENTS_ENCAISSER', 'CAISSE_CONSULTER', 'CAISSE_OUVRIR_FERMER');

INSERT IGNORE INTO roles_pharmacopee_permissions (role_pharmacopee_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_pharmacopee r JOIN permissions p
WHERE r.code = 'COMPTABLE' AND r.pharmacopee_id IS NULL
  AND p.code IN ('VENTES_CONSULTER', 'ACHATS_CONSULTER', 'FOURNISSEURS_CONSULTER', 'PAIEMENTS_CONSULTER',
                 'CAISSE_CONSULTER', 'DEPENSES_CONSULTER', 'DEPENSES_GERER', 'COMPTABILITE_CONSULTER',
                 'COMPTABILITE_EXPORTER', 'STATISTIQUES_CONSULTER', 'ABONNEMENT_CONSULTER');

INSERT IGNORE INTO roles_pharmacopee_permissions (role_pharmacopee_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_pharmacopee r JOIN permissions p
WHERE r.code = 'GESTIONNAIRE_STOCK' AND r.pharmacopee_id IS NULL
  AND p.code IN ('PRODUITS_CONSULTER', 'PRODUITS_GERER', 'STOCK_CONSULTER', 'STOCK_ENTREE',
                 'STOCK_SORTIE', 'STOCK_AJUSTER', 'ACHATS_CONSULTER');

INSERT IGNORE INTO roles_pharmacopee_permissions (role_pharmacopee_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_pharmacopee r JOIN permissions p
WHERE r.code = 'GESTIONNAIRE_ACHATS' AND r.pharmacopee_id IS NULL
  AND p.code IN ('PRODUITS_CONSULTER', 'STOCK_CONSULTER', 'FOURNISSEURS_CONSULTER', 'FOURNISSEURS_GERER',
                 'ACHATS_CONSULTER', 'ACHATS_GERER');

-- -----------------------------------------------------------------------------
-- 6. Catalogue : limites, offres, quotas de l'offre gratuite, paramètres
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO limites_offre (code_limite, nom, description, periodicite, mode_calcul) VALUES
('MAX_UTILISATEURS_ACTIFS', 'Utilisateurs actifs',  'Nombre de membres actifs, gérant compris',            'AUCUNE',    'COMPTAGE'),
('MAX_PRODUITS',            'Produits référencés',  'Nombre de produits référencés par la pharmacopée',    'AUCUNE',    'COMPTAGE'),
('MAX_VENTES_MENSUELLES',   'Ventes mensuelles',    'Nombre de ventes validées par mois calendaire',       'MENSUELLE', 'COMPTAGE');

INSERT IGNORE INTO offres_abonnement (code, nom, description, type_offre, duree_mois, prix, devise, jours_grace, actif, date_creation) VALUES
('GRATUITE',             'Offre gratuite',       'Pour démarrer : un seul utilisateur actif, sans échéance',  'GRATUITE',             NULL, 0, 'XOF', 0, 1, NOW(6)),
('MENSUELLE',            'Offre mensuelle',      'Abonnement d''un mois',                                     'MENSUELLE',            1,    0, 'XOF', 7, 0, NOW(6)),
('SEMESTRIELLE',         'Offre semestrielle',   'Abonnement de six mois',                                    'SEMESTRIELLE',         6,    0, 'XOF', 7, 0, NOW(6)),
('ANNUELLE',             'Offre annuelle',       'Abonnement de douze mois',                                  'ANNUELLE',             12,   0, 'XOF', 7, 0, NOW(6)),
('CONTRAT_PERSONNALISE', 'Contrat personnalisé', 'Conditions négociées, abonnement créé par l''administrateur', 'CONTRAT_PERSONNALISE', NULL, 0, 'XOF', 7, 1, NOW(6));

INSERT IGNORE INTO configurations_limites_offre (offre_abonnement_id, limite_offre_id, valeur_maximale, illimite)
SELECT o.id, l.id, v.valeur, 0
FROM (
    SELECT 'MAX_UTILISATEURS_ACTIFS' AS code_limite, 1 AS valeur
    UNION ALL SELECT 'MAX_PRODUITS', 20
    UNION ALL SELECT 'MAX_VENTES_MENSUELLES', 50
) v
JOIN limites_offre l ON l.code_limite = v.code_limite
JOIN offres_abonnement o ON o.code = 'GRATUITE';

INSERT IGNORE INTO parametres_plateforme (code, valeur, type_valeur, description, date_modification) VALUES
('MAX_PHARMACOPEES_PAR_GERANT', '2', 'ENTIER',  'Nombre maximal de pharmacopées pour un même gérant', NOW(6)),
('TAUX_TAXE_ABONNEMENT',        '0', 'DECIMAL', 'Taux de taxe (en %) appliqué aux factures d''abonnement', NOW(6));

-- -----------------------------------------------------------------------------
-- 7. Pharmacopées existantes : abonnement GRATUITE, instantané des limites, caisses
-- -----------------------------------------------------------------------------
INSERT INTO abonnements
    (pharmacopee_id, offre_abonnement_id, statut, date_debut, date_souscription, renouvellement_automatique,
     code_offre_souscrite, nom_offre_souscrite, prix_souscrit, devise_souscrite, duree_mois_souscrite,
     jours_grace_souscrits, date_creation)
SELECT ph.id, o.id, 'ACTIF', NOW(6), NOW(6), 0, o.code, o.nom, o.prix, o.devise, o.duree_mois, o.jours_grace, NOW(6)
FROM pharmacopees ph
JOIN offres_abonnement o ON o.code = 'GRATUITE'
WHERE NOT EXISTS (
    SELECT 1 FROM abonnements a WHERE a.pharmacopee_id = ph.id AND a.statut IN ('ACTIF', 'EN_GRACE')
);

INSERT IGNORE INTO limites_abonnement (abonnement_id, limite_offre_id, code_limite, valeur_maximale, illimite)
SELECT a.id, c.limite_offre_id, l.code_limite, c.valeur_maximale, c.illimite
FROM abonnements a
JOIN configurations_limites_offre c ON c.offre_abonnement_id = a.offre_abonnement_id
JOIN limites_offre l ON l.id = c.limite_offre_id
WHERE a.code_offre_souscrite = 'GRATUITE';

INSERT INTO historiques_abonnement (abonnement_id, type_evenement, nouvelle_valeur, date_evenement, description)
SELECT a.id, 'ACTIVATION', 'ACTIF', NOW(6), 'Offre gratuite attribuée lors de la mise en place des abonnements'
FROM abonnements a
WHERE a.code_offre_souscrite = 'GRATUITE'
  AND NOT EXISTS (SELECT 1 FROM historiques_abonnement h WHERE h.abonnement_id = a.id);

INSERT INTO caisses (pharmacopee_id, nom, type, description, actif, date_creation)
SELECT ph.id, 'Caisse principale', 'PHYSIQUE', 'Caisse créée automatiquement', 1, NOW(6)
FROM pharmacopees ph
WHERE NOT EXISTS (SELECT 1 FROM caisses c WHERE c.pharmacopee_id = ph.id AND c.type = 'PHYSIQUE');

INSERT INTO caisses (pharmacopee_id, nom, type, description, actif, date_creation)
SELECT ph.id, 'Mobile Money', 'MOBILE_MONEY', 'Compte créé automatiquement', 1, NOW(6)
FROM pharmacopees ph
WHERE NOT EXISTS (SELECT 1 FROM caisses c WHERE c.pharmacopee_id = ph.id AND c.type = 'MOBILE_MONEY');
