-- =============================================================================
-- Jeu d'essai local : à exécuter uniquement via jeu-essai.sh, sur la base de
-- développement. Ce fichier n'est pas une migration et n'est jamais déployé.
-- Les prix, quotas et produits sont des exemples.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Utilisateurs
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO utilisateurs (date_creation, email, nom, prenom, telephone, role, statut) VALUES
(NOW(6), @email_gerant,    'Diarra',   'Aminata', '+223 70 10 00 01', 'PHARMACOPEE', 'ACTIF'),
(NOW(6), @email_vendeur,   'Traoré',   'Moussa',  '+223 70 10 00 02', 'PHARMACOPEE', 'ACTIF'),
(NOW(6), @email_comptable, 'Keita',    'Fatou',   '+223 70 10 00 03', 'PHARMACOPEE', 'ACTIF'),
(NOW(6), @email_stock,     'Coulibaly','Oumar',   '+223 70 10 00 04', 'PHARMACOPEE', 'ACTIF'),
(NOW(6), @email_achats,    'Sanogo',   'Mariam',  '+223 70 10 00 05', 'PHARMACOPEE', 'ACTIF');

SET @u_gerant    = (SELECT id FROM utilisateurs WHERE email = @email_gerant);
SET @u_vendeur   = (SELECT id FROM utilisateurs WHERE email = @email_vendeur);
SET @u_comptable = (SELECT id FROM utilisateurs WHERE email = @email_comptable);
SET @u_stock     = (SELECT id FROM utilisateurs WHERE email = @email_stock);
SET @u_achats    = (SELECT id FROM utilisateurs WHERE email = @email_achats);

-- Les écrans existants s'appuient encore sur Praticien.
INSERT IGNORE INTO praticiens (id, est_praticien_principal, quota_max_structures, plan_abonnement)
VALUES (@u_gerant, 1, 2, 'GRATUIT');

-- -----------------------------------------------------------------------------
-- Pharmacopées
-- -----------------------------------------------------------------------------
INSERT INTO pharmacopees (nom, description, telephone, statut, utilisateur_id)
VALUES ('Officine de démonstration', 'Jeu d''essai : offre annuelle', '+223 20 22 00 01', 'VALIDEE', @u_gerant);
SET @ph_demo = LAST_INSERT_ID();

INSERT INTO pharmacopees (nom, description, telephone, statut, utilisateur_id)
VALUES ('Officine à l''offre gratuite', 'Jeu d''essai : limites de l''offre gratuite', '+223 20 22 00 02', 'VALIDEE', @u_gerant);
SET @ph_gratuite = LAST_INSERT_ID();

INSERT IGNORE INTO praticiens_pharmacopees (praticien_id, pharmacopee_id) VALUES (@u_gerant, @ph_demo), (@u_gerant, @ph_gratuite);

-- -----------------------------------------------------------------------------
-- Membres et rôles
-- -----------------------------------------------------------------------------
INSERT INTO membres_pharmacopee (utilisateur_id, pharmacopee_id, statut, est_proprietaire, poste, date_adhesion, date_creation) VALUES
(@u_gerant,    @ph_demo,     'ACTIF', 1, 'Gérante',               NOW(6), NOW(6)),
(@u_vendeur,   @ph_demo,     'ACTIF', 0, 'Vendeur-caissier',      NOW(6), NOW(6)),
(@u_comptable, @ph_demo,     'ACTIF', 0, 'Comptable',             NOW(6), NOW(6)),
(@u_stock,     @ph_demo,     'ACTIF', 0, 'Gestionnaire de stock', NOW(6), NOW(6)),
(@u_achats,    @ph_demo,     'ACTIF', 0, 'Gestionnaire d''achats', NOW(6), NOW(6)),
(@u_gerant,    @ph_gratuite, 'ACTIF', 1, 'Gérante',               NOW(6), NOW(6));

INSERT INTO attributions_roles_pharmacopee (membre_pharmacopee_id, role_pharmacopee_id, date_attribution)
SELECT m.id, r.id, NOW(6)
FROM membres_pharmacopee m
JOIN (
    SELECT @u_gerant AS utilisateur, 'GERANT' AS code
    UNION ALL SELECT @u_vendeur, 'VENDEUR_CAISSIER'
    UNION ALL SELECT @u_comptable, 'COMPTABLE'
    UNION ALL SELECT @u_stock, 'GESTIONNAIRE_STOCK'
    UNION ALL SELECT @u_achats, 'GESTIONNAIRE_ACHATS'
) v ON v.utilisateur = m.utilisateur_id
JOIN roles_pharmacopee r ON r.code = v.code AND r.pharmacopee_id IS NULL
WHERE m.pharmacopee_id IN (@ph_demo, @ph_gratuite);

-- -----------------------------------------------------------------------------
-- Offres payantes : prix et quotas d'exemple, base locale uniquement
-- -----------------------------------------------------------------------------
UPDATE offres_abonnement SET prix = 10000, actif = 1 WHERE code = 'MENSUELLE';
UPDATE offres_abonnement SET prix = 54000, actif = 1 WHERE code = 'SEMESTRIELLE';
UPDATE offres_abonnement SET prix = 96000, actif = 1 WHERE code = 'ANNUELLE';

INSERT IGNORE INTO configurations_limites_offre (offre_abonnement_id, limite_offre_id, valeur_maximale, illimite)
SELECT o.id, l.id, q.valeur, q.valeur IS NULL
FROM (
    SELECT 'MENSUELLE' AS offre, 'MAX_UTILISATEURS_ACTIFS' AS limite, 5 AS valeur
    UNION ALL SELECT 'MENSUELLE', 'MAX_PRODUITS', 200
    UNION ALL SELECT 'MENSUELLE', 'MAX_VENTES_MENSUELLES', 1000
    UNION ALL SELECT 'SEMESTRIELLE', 'MAX_UTILISATEURS_ACTIFS', 10
    UNION ALL SELECT 'SEMESTRIELLE', 'MAX_PRODUITS', 500
    UNION ALL SELECT 'SEMESTRIELLE', 'MAX_VENTES_MENSUELLES', 5000
    UNION ALL SELECT 'ANNUELLE', 'MAX_UTILISATEURS_ACTIFS', 20
    UNION ALL SELECT 'ANNUELLE', 'MAX_PRODUITS', NULL
    UNION ALL SELECT 'ANNUELLE', 'MAX_VENTES_MENSUELLES', NULL
) q
JOIN offres_abonnement o ON o.code = q.offre
JOIN limites_offre l ON l.code_limite = q.limite;

-- -----------------------------------------------------------------------------
-- Abonnements en vigueur, avec instantané des conditions
-- -----------------------------------------------------------------------------
INSERT INTO abonnements (pharmacopee_id, offre_abonnement_id, statut, date_debut, date_fin, date_souscription, renouvellement_automatique,
                         code_offre_souscrite, nom_offre_souscrite, prix_souscrit, devise_souscrite, duree_mois_souscrite, jours_grace_souscrits, date_creation)
SELECT v.pharmacopee, o.id, 'ACTIF', NOW(6), IF(o.duree_mois IS NULL, NULL, NOW(6) + INTERVAL o.duree_mois MONTH), NOW(6), 0,
       o.code, o.nom, o.prix, o.devise, o.duree_mois, o.jours_grace, NOW(6)
FROM (
    SELECT @ph_demo AS pharmacopee, 'ANNUELLE' AS code
    UNION ALL SELECT @ph_gratuite, 'GRATUITE'
) v
JOIN offres_abonnement o ON o.code = v.code;

INSERT INTO limites_abonnement (abonnement_id, limite_offre_id, code_limite, valeur_maximale, illimite)
SELECT a.id, c.limite_offre_id, l.code_limite, c.valeur_maximale, c.illimite
FROM abonnements a
JOIN configurations_limites_offre c ON c.offre_abonnement_id = a.offre_abonnement_id
JOIN limites_offre l ON l.id = c.limite_offre_id
WHERE a.pharmacopee_id IN (@ph_demo, @ph_gratuite);

INSERT INTO historiques_abonnement (abonnement_id, type_evenement, nouvelle_valeur, date_evenement, description)
SELECT id, 'ACTIVATION', 'ACTIF', NOW(6), 'Jeu d''essai' FROM abonnements WHERE pharmacopee_id IN (@ph_demo, @ph_gratuite);

-- -----------------------------------------------------------------------------
-- Catalogue et stock (officine de démonstration)
-- -----------------------------------------------------------------------------
INSERT INTO categories_produits (nom, description, statut) VALUES ('Tisanes (démo)', 'Jeu d''essai', 1);
SET @cat_tisanes = LAST_INSERT_ID();
INSERT INTO categories_produits (nom, description, statut) VALUES ('Sirops et baumes (démo)', 'Jeu d''essai', 1);
SET @cat_sirops = LAST_INSERT_ID();

INSERT INTO produits (nom, description, forme, prix, statut, categorie_id) VALUES
('Tisane de kinkéliba (démo)',   'Produit fictif du jeu d''essai', 'Sachet',  1500, 'VALIDE', @cat_tisanes),
('Tisane de citronnelle (démo)', 'Produit fictif du jeu d''essai', 'Sachet',  1200, 'VALIDE', @cat_tisanes),
('Tisane d''hibiscus (démo)',    'Produit fictif du jeu d''essai', 'Sachet',  1000, 'VALIDE', @cat_tisanes),
('Sirop de moringa (démo)',      'Produit fictif du jeu d''essai', 'Flacon',  3500, 'VALIDE', @cat_sirops),
('Baume de karité (démo)',       'Produit fictif du jeu d''essai', 'Pot',     2500, 'VALIDE', @cat_sirops),
('Poudre de baobab (démo)',      'Produit fictif du jeu d''essai', 'Sachet',  2000, 'VALIDE', @cat_sirops);

INSERT INTO disponibilites_produits (disponible, prix, quantite_stock, date_miseajour, pharmacopee_id, produit_id)
SELECT 1, NULL, 20, NOW(6), @ph_demo, p.id FROM produits p WHERE p.nom LIKE '%(démo)';

SET @fiche_kinkeliba = (SELECT d.id FROM disponibilites_produits d JOIN produits p ON p.id = d.produit_id WHERE p.nom = 'Tisane de kinkéliba (démo)' AND d.pharmacopee_id = @ph_demo);
SET @fiche_moringa   = (SELECT d.id FROM disponibilites_produits d JOIN produits p ON p.id = d.produit_id WHERE p.nom = 'Sirop de moringa (démo)' AND d.pharmacopee_id = @ph_demo);

-- Une fiche avec un lot périmé (jamais vendu), un lot proche de la péremption et du stock sans lot.
INSERT INTO lots (pharmacopee_id, disponibilite_produit_id, numero_lot, date_peremption, quantite_initiale, quantite_restante, date_reception) VALUES
(@ph_demo, @fiche_kinkeliba, 'DEMO-PERIME', CURDATE() - INTERVAL 30 DAY, 5, 5, NOW(6)),
(@ph_demo, @fiche_kinkeliba, 'DEMO-PROCHE', CURDATE() + INTERVAL 20 DAY, 5, 5, NOW(6)),
(@ph_demo, @fiche_moringa,   'DEMO-LOT-1',  CURDATE() + INTERVAL 1 YEAR, 20, 20, NOW(6));

-- -----------------------------------------------------------------------------
-- Caisses, fournisseurs, clients
-- -----------------------------------------------------------------------------
INSERT INTO caisses (pharmacopee_id, nom, type, description, actif, date_creation) VALUES
(@ph_demo,     'Caisse principale', 'PHYSIQUE',     'Jeu d''essai', 1, NOW(6)),
(@ph_demo,     'Mobile Money',      'MOBILE_MONEY', 'Jeu d''essai', 1, NOW(6)),
(@ph_demo,     'Banque',            'BANQUE',       'Jeu d''essai', 1, NOW(6)),
(@ph_gratuite, 'Caisse principale', 'PHYSIQUE',     'Jeu d''essai', 1, NOW(6)),
(@ph_gratuite, 'Mobile Money',      'MOBILE_MONEY', 'Jeu d''essai', 1, NOW(6)),
(@ph_gratuite, 'Banque',            'BANQUE',       'Jeu d''essai', 1, NOW(6));

INSERT INTO fournisseurs (pharmacopee_id, nom, type_fournisseur, telephone, adresse, statut, date_creation) VALUES
(@ph_demo, 'Coopérative de Sikasso (démo)', 'COOPERATIVE', '+223 76 00 00 01', 'Sikasso', 'ACTIF', NOW(6)),
(@ph_demo, 'Grossiste de Bamako (démo)',    'GROSSISTE',   '+223 76 00 00 02', 'Bamako',  'ACTIF', NOW(6));

INSERT INTO fournisseurs_produits (fournisseur_id, produit_id, reference_fournisseur, prix_indicatif, delai_approvisionnement, disponible)
SELECT f.id, p.id, CONCAT('REF-', p.id), p.prix * 0.6, 5, 1
FROM fournisseurs f JOIN produits p ON p.nom LIKE 'Tisane%(démo)'
WHERE f.pharmacopee_id = @ph_demo AND f.nom LIKE 'Coopérative%';

INSERT INTO clients (pharmacopee_id, nom, prenom, telephone, actif, date_creation) VALUES
(@ph_demo, 'Sidibé', 'Oumar', '+223 66 00 00 01', 1, NOW(6)),
(@ph_demo, 'Konaté', 'Awa',   '+223 66 00 00 02', 1, NOW(6));
