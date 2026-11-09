-- =============================================================================
-- V2 : Rôles, permissions et membres de pharmacopée (socle SaaS multi-tenant)
-- =============================================================================
-- Tables : permissions, roles_plateforme, roles_plateforme_permissions,
--          attributions_roles_plateforme, membres_pharmacopee, roles_pharmacopee,
--          roles_pharmacopee_permissions, attributions_roles_pharmacopee
-- puis catalogue initial : permissions (portées PLATEFORME / PHARMACOPEE),
-- rôles de plateforme et rôles système de pharmacopée.
--
-- Nature    : ADDITIVE. Aucune table existante n'est modifiée.
-- Idempotent: CREATE TABLE IF NOT EXISTS (les tables existent déjà sur la base
--             Aiven, créées par Hibernate) ; INSERT IGNORE / NOT EXISTS.
-- DDL des tables généré par Hibernate 7 (identique à celui déjà créé sur Aiven).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Tables
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `permissions` (
  `date_creation` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `module` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `libelle` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `portee` enum('PHARMACOPEE','PLATEFORME') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7lcb6glmvwlro3p2w2cewxtvd` (`code`),
  KEY `idx_permissions_portee` (`portee`),
  KEY `idx_permissions_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `roles_plateforme` (
  `actif` bit(1) NOT NULL,
  `systeme` bit(1) NOT NULL,
  `date_creation` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `libelle` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg3lajy8wy2r8ooxxmx0ousxkb` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `roles_plateforme_permissions` (
  `date_attribution` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `permission_id` bigint NOT NULL,
  `role_plateforme_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_plateforme_permission` (`role_plateforme_id`,`permission_id`),
  KEY `FKh0a1c33056fixixsvj1lawucv` (`permission_id`),
  CONSTRAINT `FKh0a1c33056fixixsvj1lawucv` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `FKoi1e3rp4hapcn18t920bf3bat` FOREIGN KEY (`role_plateforme_id`) REFERENCES `roles_plateforme` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `attributions_roles_plateforme` (
  `attribue_par_id` bigint DEFAULT NULL,
  `date_attribution` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_plateforme_id` bigint NOT NULL,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attribution_role_plateforme` (`utilisateur_id`,`role_plateforme_id`),
  KEY `FK3u4blcxx3h2akqoc006pk6mni` (`attribue_par_id`),
  KEY `FK7kc6dtmv25h3u4vusqndqcaeh` (`role_plateforme_id`),
  CONSTRAINT `FK3u4blcxx3h2akqoc006pk6mni` FOREIGN KEY (`attribue_par_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FK7kc6dtmv25h3u4vusqndqcaeh` FOREIGN KEY (`role_plateforme_id`) REFERENCES `roles_plateforme` (`id`),
  CONSTRAINT `FKfntk8qkjef2r2r17y2hfelaki` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `membres_pharmacopee` (
  `est_proprietaire` bit(1) NOT NULL,
  `date_adhesion` datetime(6) DEFAULT NULL,
  `date_creation` datetime(6) NOT NULL,
  `date_invitation` datetime(6) DEFAULT NULL,
  `date_mise_a_jour` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `invite_par_id` bigint DEFAULT NULL,
  `pharmacopee_id` bigint NOT NULL,
  `utilisateur_id` bigint NOT NULL,
  `poste` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numero_agrement` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `specialite` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `statut` enum('ACTIF','INVITE','RETIRE','SUSPENDU') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_membre_utilisateur_pharmacopee` (`utilisateur_id`,`pharmacopee_id`),
  KEY `idx_membres_pharmacopee_statut` (`pharmacopee_id`,`statut`),
  KEY `idx_membres_utilisateur_statut` (`utilisateur_id`,`statut`),
  KEY `FKmji34axw7qleot9lhhll506xu` (`invite_par_id`),
  CONSTRAINT `FKj6b02t77mk7esoybdjc7m05lc` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FKmji34axw7qleot9lhhll506xu` FOREIGN KEY (`invite_par_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FKqpky6ngt8wtljl4dx735w0wo` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `roles_pharmacopee` (
  `actif` bit(1) NOT NULL,
  `systeme` bit(1) NOT NULL,
  `cree_par_id` bigint DEFAULT NULL,
  `date_creation` datetime(6) NOT NULL,
  `date_mise_a_jour` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pharmacopee_id` bigint DEFAULT NULL,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `libelle` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_pharmacopee_code` (`pharmacopee_id`,`code`),
  KEY `idx_roles_pharmacopee_systeme` (`systeme`),
  KEY `FK938veju6uda60bpj7trghh05d` (`cree_par_id`),
  CONSTRAINT `FK938veju6uda60bpj7trghh05d` FOREIGN KEY (`cree_par_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FKtnojohdv213rm1ye4c7v6f8ft` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `roles_pharmacopee_permissions` (
  `date_attribution` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `permission_id` bigint NOT NULL,
  `role_pharmacopee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_pharmacopee_permission` (`role_pharmacopee_id`,`permission_id`),
  KEY `FKfelboe4pk02bl3xuy0q5dopon` (`permission_id`),
  CONSTRAINT `FK6l1xunr5d4c2hxb3agnii45am` FOREIGN KEY (`role_pharmacopee_id`) REFERENCES `roles_pharmacopee` (`id`),
  CONSTRAINT `FKfelboe4pk02bl3xuy0q5dopon` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `attributions_roles_pharmacopee` (
  `attribue_par_id` bigint DEFAULT NULL,
  `date_attribution` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `membre_pharmacopee_id` bigint NOT NULL,
  `role_pharmacopee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attribution_role_pharmacopee` (`membre_pharmacopee_id`,`role_pharmacopee_id`),
  KEY `FKk6j833gp7kkuukjb8q6s2sao7` (`attribue_par_id`),
  KEY `FKhiw6na7x7klm8ikhrftukt3g` (`role_pharmacopee_id`),
  CONSTRAINT `FK90ham6pe5ip8m0oqxq7xgg7cc` FOREIGN KEY (`membre_pharmacopee_id`) REFERENCES `membres_pharmacopee` (`id`),
  CONSTRAINT `FKhiw6na7x7klm8ikhrftukt3g` FOREIGN KEY (`role_pharmacopee_id`) REFERENCES `roles_pharmacopee` (`id`),
  CONSTRAINT `FKk6j833gp7kkuukjb8q6s2sao7` FOREIGN KEY (`attribue_par_id`) REFERENCES `utilisateurs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. Catalogue des permissions
-- -----------------------------------------------------------------------------

-- 2.a Portée PLATEFORME (administration LADAFURA)
INSERT IGNORE INTO permissions (code, libelle, module, portee, date_creation) VALUES
('PLATEFORME_UTILISATEURS_CONSULTER', 'Consulter les utilisateurs de la plateforme',        'UTILISATEURS', 'PLATEFORME', NOW(6)),
('PLATEFORME_UTILISATEURS_GERER',     'Créer, modifier, suspendre les utilisateurs',        'UTILISATEURS', 'PLATEFORME', NOW(6)),
('PLATEFORME_ROLES_GERER',            'Gérer les rôles et permissions de la plateforme',    'ROLES',        'PLATEFORME', NOW(6)),
('PLATEFORME_PHARMACOPEES_VALIDER',   'Valider, suspendre ou rejeter les pharmacopées',     'PHARMACOPEES', 'PLATEFORME', NOW(6)),
('PLATEFORME_CATALOGUE_GERER',        'Gérer plantes, maladies, produits et catégories',    'CATALOGUE',    'PLATEFORME', NOW(6)),
('PLATEFORME_COLLECTES_MODERER',      'Modérer les collectes terrain',                      'COLLECTES',    'PLATEFORME', NOW(6)),
('PLATEFORME_AVIS_MODERER',           'Modérer les avis clients',                           'AVIS',         'PLATEFORME', NOW(6)),
('PLATEFORME_ABONNEMENTS_GERER',      'Gérer les plans et abonnements SaaS',                'ABONNEMENTS',  'PLATEFORME', NOW(6)),
('PLATEFORME_STATISTIQUES_CONSULTER', 'Consulter les statistiques globales',                'STATISTIQUES', 'PLATEFORME', NOW(6)),
('PLATEFORME_COLLECTE_TERRAIN',       'Réaliser des collectes terrain (agent)',             'COLLECTES',    'PLATEFORME', NOW(6)),
('PLATEFORME_ESPACE_CITOYEN',         'Accéder à l''espace citoyen (recherche, commandes)', 'CITOYEN',      'PLATEFORME', NOW(6));

-- 2.b Portée PHARMACOPEE (gestion interne d'une officine)
INSERT IGNORE INTO permissions (code, libelle, module, portee, date_creation) VALUES
('MEMBRES_CONSULTER',        'Consulter les membres',                                 'MEMBRES',      'PHARMACOPEE', NOW(6)),
('MEMBRES_INVITER',          'Ajouter ou inviter un membre (collaborateur, vendeur)', 'MEMBRES',      'PHARMACOPEE', NOW(6)),
('MEMBRES_GERER',            'Modifier, suspendre ou retirer un membre',              'MEMBRES',      'PHARMACOPEE', NOW(6)),
('ROLES_GERER',              'Créer des rôles et configurer leurs permissions',       'ROLES',        'PHARMACOPEE', NOW(6)),
('PROFIL_PHARMACOPEE_GERER', 'Modifier la fiche et les paramètres de la pharmacopée', 'PARAMETRES',   'PHARMACOPEE', NOW(6)),
('PRODUITS_CONSULTER',       'Consulter les produits',                                'PRODUITS',     'PHARMACOPEE', NOW(6)),
('PRODUITS_GERER',           'Référencer, modifier, retirer des produits et prix',    'PRODUITS',     'PHARMACOPEE', NOW(6)),
('STOCK_CONSULTER',          'Consulter les stocks',                                  'STOCK',        'PHARMACOPEE', NOW(6)),
('STOCK_ENTREE',             'Enregistrer des entrées de stock',                      'STOCK',        'PHARMACOPEE', NOW(6)),
('STOCK_SORTIE',             'Enregistrer des sorties de stock (perte, péremption)',  'STOCK',        'PHARMACOPEE', NOW(6)),
('STOCK_AJUSTER',            'Ajuster un stock (inventaire)',                         'STOCK',        'PHARMACOPEE', NOW(6)),
('FOURNISSEURS_CONSULTER',   'Consulter les fournisseurs',                            'ACHATS',       'PHARMACOPEE', NOW(6)),
('FOURNISSEURS_GERER',       'Gérer les fournisseurs',                                'ACHATS',       'PHARMACOPEE', NOW(6)),
('ACHATS_CONSULTER',         'Consulter les achats',                                  'ACHATS',       'PHARMACOPEE', NOW(6)),
('ACHATS_GERER',             'Enregistrer et valider des achats',                     'ACHATS',       'PHARMACOPEE', NOW(6)),
('VENTES_CONSULTER',         'Consulter les ventes',                                  'VENTES',       'PHARMACOPEE', NOW(6)),
('VENTES_CREER',             'Vendre au comptoir',                                    'VENTES',       'PHARMACOPEE', NOW(6)),
('VENTES_ANNULER',           'Annuler ou rembourser une vente',                       'VENTES',       'PHARMACOPEE', NOW(6)),
('VENTES_REMISE',            'Accorder une remise',                                   'VENTES',       'PHARMACOPEE', NOW(6)),
('COMMANDES_CONSULTER',      'Consulter les commandes en ligne',                      'COMMANDES',    'PHARMACOPEE', NOW(6)),
('COMMANDES_TRAITER',        'Traiter les commandes en ligne (préparer, livrer)',     'COMMANDES',    'PHARMACOPEE', NOW(6)),
('RETRAIT_GERER',            'Configurer pickup et livraison',                        'COMMANDES',    'PHARMACOPEE', NOW(6)),
('PAIEMENTS_CONSULTER',      'Consulter les paiements',                               'PAIEMENTS',    'PHARMACOPEE', NOW(6)),
('PAIEMENTS_ENCAISSER',      'Encaisser un paiement',                                 'PAIEMENTS',    'PHARMACOPEE', NOW(6)),
('CAISSE_CONSULTER',         'Consulter les caisses et sessions',                     'CAISSE',       'PHARMACOPEE', NOW(6)),
('CAISSE_OUVRIR_FERMER',     'Ouvrir et clôturer une session de caisse',              'CAISSE',       'PHARMACOPEE', NOW(6)),
('CAISSE_GERER',             'Créer et configurer les caisses',                       'CAISSE',       'PHARMACOPEE', NOW(6)),
('DEPENSES_CONSULTER',       'Consulter les dépenses',                                'COMPTABILITE', 'PHARMACOPEE', NOW(6)),
('DEPENSES_GERER',           'Enregistrer des dépenses',                              'COMPTABILITE', 'PHARMACOPEE', NOW(6)),
('COMPTABILITE_CONSULTER',   'Consulter la comptabilité (journal, entrées/sorties)', 'COMPTABILITE', 'PHARMACOPEE', NOW(6)),
('COMPTABILITE_EXPORTER',    'Exporter les données comptables',                       'COMPTABILITE', 'PHARMACOPEE', NOW(6)),
('AVIS_CONSULTER',           'Consulter les avis clients',                            'AVIS',         'PHARMACOPEE', NOW(6)),
('AVIS_REPONDRE',            'Répondre aux avis clients',                             'AVIS',         'PHARMACOPEE', NOW(6)),
('STATISTIQUES_CONSULTER',   'Consulter le tableau de bord et les statistiques',      'STATISTIQUES', 'PHARMACOPEE', NOW(6)),
('ABONNEMENT_GERER',         'Gérer l''abonnement SaaS de la pharmacopée',            'ABONNEMENTS',  'PHARMACOPEE', NOW(6));

-- -----------------------------------------------------------------------------
-- 3. Rôles de plateforme (codes alignés sur l'enum Role existant)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO roles_plateforme (code, libelle, description, systeme, actif, date_creation) VALUES
('ADMINISTRATEUR', 'Administrateur LADAFURA', 'Administration globale de la plateforme', 1, 1, NOW(6)),
('AGENT_COLLECTE', 'Agent de collecte',       'Collecte terrain des savoirs traditionnels', 1, 1, NOW(6)),
('POPULATION',     'Citoyen',                 'Utilisateur grand public (recherche, achats)', 1, 1, NOW(6));

INSERT IGNORE INTO roles_plateforme_permissions (role_plateforme_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_plateforme r JOIN permissions p
WHERE r.code = 'ADMINISTRATEUR' AND p.portee = 'PLATEFORME'
  AND p.code NOT IN ('PLATEFORME_COLLECTE_TERRAIN', 'PLATEFORME_ESPACE_CITOYEN');

INSERT IGNORE INTO roles_plateforme_permissions (role_plateforme_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_plateforme r JOIN permissions p
WHERE r.code = 'AGENT_COLLECTE' AND p.code = 'PLATEFORME_COLLECTE_TERRAIN';

INSERT IGNORE INTO roles_plateforme_permissions (role_plateforme_id, permission_id, date_attribution)
SELECT r.id, p.id, NOW(6) FROM roles_plateforme r JOIN permissions p
WHERE r.code = 'POPULATION' AND p.code = 'PLATEFORME_ESPACE_CITOYEN';

-- -----------------------------------------------------------------------------
-- 4. Rôles système de pharmacopée (pharmacopee_id NULL)
--    Leurs permissions sont définies une seule fois, dans V5 (section 5).
-- -----------------------------------------------------------------------------
INSERT INTO roles_pharmacopee (code, libelle, description, pharmacopee_id, systeme, actif, date_creation)
SELECT v.code, v.libelle, v.description, NULL, 1, 1, NOW(6)
FROM (
    SELECT 'GERANT' AS code, 'Gérant' AS libelle, 'Responsable principal : tous les droits sur la pharmacopée' AS description
    UNION ALL SELECT 'MANAGER',             'Manager',                 'Supervision des activités et des employés, hors membres, rôles et abonnement'
    UNION ALL SELECT 'VENDEUR_CAISSIER',    'Vendeur-caissier',        'Ventes au comptoir, clients, encaissements et sessions de caisse'
    UNION ALL SELECT 'COMPTABLE',           'Comptable',               'Recettes, dépenses et rapports financiers'
    UNION ALL SELECT 'GESTIONNAIRE_STOCK',  'Gestionnaire de stock',   'Produits et mouvements de stock'
    UNION ALL SELECT 'GESTIONNAIRE_ACHATS', 'Gestionnaire des achats', 'Fournisseurs, achats et approvisionnements'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM roles_pharmacopee r WHERE r.code = v.code AND r.pharmacopee_id IS NULL
);
