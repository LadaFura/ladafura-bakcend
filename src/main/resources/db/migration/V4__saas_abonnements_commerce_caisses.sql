-- =============================================================================
-- V4 : Schéma SaaS (abonnements, fournisseurs, achats, stock, ventes, caisses, audit)
-- =============================================================================
-- Objet     : créer les 27 tables des entités SaaS et compléter deux tables existantes.
--             Modèle : docs/architecture/plantuml/*.puml.
-- Tables modifiées : disponibilites_produits (contrainte UNIQUE),
--                    membres_pharmacopee (3 colonnes, unicité du propriétaire).
-- Données   : seules les pharmacopées à plusieurs propriétaires sont modifiées
--             (les propriétaires en trop deviennent de simples membres ; aucune suppression).
-- DDL généré par Hibernate 7 à partir des entités, FK et index regroupés par table.
--
-- Colonnes générées : pharmacopee_en_vigueur, pharmacopee_programme, caisse_ouverte,
-- pharmacopee_proprietaire. Inconnues des entités, elles portent les règles d'unicité
-- qu'un index UNIQUE MySQL ne sait pas exprimer autrement (index partiels).
--
-- PRÉREQUIS sur une base existante : aucun doublon (pharmacopée, produit).
--   SELECT pharmacopee_id, produit_id, COUNT(*) FROM disponibilites_produits
--   GROUP BY pharmacopee_id, produit_id HAVING COUNT(*) > 1;
-- S'il y en a, les corriger à la main (décider du stock et du prix à conserver) : la
-- contrainte de la section 1 échoue sinon, sans qu'aucun autre changement n'ait eu lieu.
--
-- Réversibilité : DROP des 27 tables créées ici, puis
--   ALTER TABLE disponibilites_produits DROP INDEX uk_disponibilite_pharmacopee_produit;
--   ALTER TABLE membres_pharmacopee DROP INDEX uk_membre_proprietaire,
--     DROP COLUMN pharmacopee_proprietaire, DROP COLUMN motif_suspension,
--     DROP COLUMN date_depart, DROP COLUMN date_expiration_invitation;
-- La normalisation des propriétaires (section 2) n'est pas réversible automatiquement.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Unicité (pharmacopée, produit). En premier : MySQL ne fait pas de DDL
--    transactionnel, un échec ici ne laisse aucun changement partiel.
-- -----------------------------------------------------------------------------
ALTER TABLE disponibilites_produits
  ADD CONSTRAINT uk_disponibilite_pharmacopee_produit UNIQUE (pharmacopee_id, produit_id);

-- -----------------------------------------------------------------------------
-- 2. Un seul propriétaire par pharmacopée : on garde celui déclaré sur la
--    pharmacopée, sinon le plus ancien membre. Le rôle est ajusté dans V5.
-- -----------------------------------------------------------------------------
UPDATE membres_pharmacopee m
JOIN (
    SELECT m2.pharmacopee_id,
           COALESCE(MIN(CASE WHEN m2.utilisateur_id = ph.utilisateur_id THEN m2.id END), MIN(m2.id)) AS membre_conserve
    FROM membres_pharmacopee m2
    JOIN pharmacopees ph ON ph.id = m2.pharmacopee_id
    WHERE m2.est_proprietaire = 1
    GROUP BY m2.pharmacopee_id
) g ON g.pharmacopee_id = m.pharmacopee_id
SET m.est_proprietaire = 0,
    m.poste = 'Collaborateur'
WHERE m.est_proprietaire = 1
  AND m.id <> g.membre_conserve;

-- -----------------------------------------------------------------------------
-- 3. Membres : invitation, départ, motif de suspension, propriétaire unique
-- -----------------------------------------------------------------------------
ALTER TABLE membres_pharmacopee
  ADD COLUMN date_expiration_invitation datetime(6) DEFAULT NULL,
  ADD COLUMN date_depart datetime(6) DEFAULT NULL,
  ADD COLUMN motif_suspension enum('DECISION_ADMIN','DECISION_GERANT','QUOTA_ABONNEMENT') DEFAULT NULL,
  ADD COLUMN pharmacopee_proprietaire bigint GENERATED ALWAYS AS (CASE WHEN est_proprietaire = 1 THEN pharmacopee_id END) VIRTUAL,
  ADD UNIQUE KEY uk_membre_proprietaire (pharmacopee_proprietaire);

-- -----------------------------------------------------------------------------
-- 4. Plateforme et catalogue des offres
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parametres_plateforme (
  date_modification datetime(6) not null,
  id bigint not null auto_increment,
  modifie_par_id bigint,
  code varchar(100) not null,
  description TEXT,
  valeur varchar(255) not null,
  type_valeur enum ('BOOLEEN','DECIMAL','ENTIER','TEXTE') not null,
  PRIMARY KEY (id),
  UNIQUE KEY UK8t2uic0xxepn7buwq2y8dutb4 (code),
  CONSTRAINT FKhb2des9avpiff9j6nsx8fac6y FOREIGN KEY (modifie_par_id) REFERENCES utilisateurs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS offres_abonnement (
  actif bit(1) not null,
  devise varchar(3) not null,
  duree_mois integer,
  jours_grace integer not null,
  prix decimal(15,2) not null,
  date_creation datetime(6) not null,
  date_modification datetime(6),
  id bigint not null auto_increment,
  code varchar(50) not null,
  description TEXT,
  nom varchar(255) not null,
  type_offre enum ('ANNUELLE','CONTRAT_PERSONNALISE','GRATUITE','MENSUELLE','SEMESTRIELLE') not null,
  PRIMARY KEY (id),
  UNIQUE KEY UKpfh77a5lu9d3faoltb855hgej (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS limites_offre (
  id bigint not null auto_increment,
  code_limite varchar(50) not null,
  description TEXT,
  nom varchar(255) not null,
  mode_calcul enum ('COMPTAGE','COMPTEUR') not null,
  periodicite enum ('AUCUNE','MENSUELLE') not null,
  PRIMARY KEY (id),
  UNIQUE KEY UKlnawootqphd0gg5p9t3s49p2l (code_limite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS configurations_limites_offre (
  illimite bit(1) not null,
  valeur_maximale integer,
  id bigint not null auto_increment,
  limite_offre_id bigint not null,
  offre_abonnement_id bigint not null,
  PRIMARY KEY (id),
  UNIQUE KEY uk_configuration_offre_limite (offre_abonnement_id, limite_offre_id),
  CONSTRAINT FKmwb7rn5ws9bt9ldpcgpadpycn FOREIGN KEY (limite_offre_id) REFERENCES limites_offre (id),
  CONSTRAINT FKn3qafgcncol0nl6tnmnjqdvnf FOREIGN KEY (offre_abonnement_id) REFERENCES offres_abonnement (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -----------------------------------------------------------------------------
-- 5. Souscriptions et facturation
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS demandes_contrat (
  duree_souhaitee_mois integer,
  date_demande datetime(6) not null,
  date_traitement datetime(6),
  demandeur_id bigint,
  id bigint not null auto_increment,
  pharmacopee_id bigint,
  traite_par_id bigint,
  telephone varchar(30) not null,
  description_besoin TEXT not null,
  email varchar(255),
  nom_contact varchar(255) not null,
  observation TEXT,
  statut enum ('ACCEPTEE','CLOTUREE','EN_COURS','NOUVELLE','REFUSEE') not null,
  PRIMARY KEY (id),
  KEY idx_demandes_contrat_statut (statut),
  CONSTRAINT FKb8ccxyw6dddtml28dqf7mv4up FOREIGN KEY (demandeur_id) REFERENCES utilisateurs (id),
  CONSTRAINT FKjtyxi1m0cx2hdfrlqg10imxjh FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id),
  CONSTRAINT FKac5go87lapj6itk83u2twelww FOREIGN KEY (traite_par_id) REFERENCES utilisateurs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS abonnements (
  devise_souscrite varchar(3) not null,
  duree_mois_souscrite integer,
  jours_grace_souscrits integer not null,
  prix_souscrit decimal(15,2) not null,
  renouvellement_automatique bit(1) not null,
  abonnement_precedent_id bigint,
  cree_par_admin_id bigint,
  date_annulation datetime(6),
  date_consentement_renouvellement datetime(6),
  date_creation datetime(6) not null,
  date_debut datetime(6),
  date_fin datetime(6),
  date_fin_grace datetime(6),
  date_miseajour datetime(6),
  date_renouvellement datetime(6),
  date_souscription datetime(6) not null,
  demande_contrat_id bigint,
  id bigint not null auto_increment,
  offre_abonnement_id bigint not null,
  pharmacopee_id bigint not null,
  souscrit_par_id bigint,
  code_offre_souscrite varchar(50) not null,
  motif_annulation varchar(500),
  nom_offre_souscrite varchar(255) not null,
  statut enum ('ACTIF','ANNULE','EN_ATTENTE_PAIEMENT','EN_GRACE','EXPIRE','PROGRAMME','SUSPENDU') not null,
  pharmacopee_en_vigueur bigint GENERATED ALWAYS AS (CASE WHEN statut IN ('ACTIF','EN_GRACE') THEN pharmacopee_id END) VIRTUAL,
  pharmacopee_programme bigint GENERATED ALWAYS AS (CASE WHEN statut = 'PROGRAMME' THEN pharmacopee_id END) VIRTUAL,
  PRIMARY KEY (id),
  UNIQUE KEY UKm2ude6s046jjc6rkiqfposwdg (demande_contrat_id),
  UNIQUE KEY uk_abonnement_en_vigueur (pharmacopee_en_vigueur),
  UNIQUE KEY uk_abonnement_programme (pharmacopee_programme),
  KEY idx_abonnements_pharmacopee_statut (pharmacopee_id, statut),
  KEY idx_abonnements_statut_date_fin (statut, date_fin),
  CONSTRAINT FKnb3hfiluqghe7f3gyurc2tqi7 FOREIGN KEY (abonnement_precedent_id) REFERENCES abonnements (id),
  CONSTRAINT FKctix95pt0d7y671my4fupq4yl FOREIGN KEY (cree_par_admin_id) REFERENCES utilisateurs (id),
  CONSTRAINT FKfdtws7duwfmt2euj93pj3esje FOREIGN KEY (demande_contrat_id) REFERENCES demandes_contrat (id),
  CONSTRAINT FKn363xkd98ltkoa35dxureadfq FOREIGN KEY (offre_abonnement_id) REFERENCES offres_abonnement (id),
  CONSTRAINT FK71mtlakc9pdqesu6y3w5xjiyf FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id),
  CONSTRAINT FK1xe27vjd2wpn9h3i6ww8pj083 FOREIGN KEY (souscrit_par_id) REFERENCES membres_pharmacopee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS limites_abonnement (
  illimite bit(1) not null,
  valeur_maximale integer,
  abonnement_id bigint not null,
  id bigint not null auto_increment,
  limite_offre_id bigint not null,
  code_limite varchar(50) not null,
  PRIMARY KEY (id),
  UNIQUE KEY uk_limite_abonnement (abonnement_id, limite_offre_id),
  CONSTRAINT FK38co2g9ajnvh1aa767f6m0rhi FOREIGN KEY (abonnement_id) REFERENCES abonnements (id),
  CONSTRAINT FKlfo8o66cptye1v1smdj2mq5tf FOREIGN KEY (limite_offre_id) REFERENCES limites_offre (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS utilisations_limite (
  periode_debut date not null,
  periode_fin date not null,
  valeur_utilisee integer not null,
  date_derniere_miseajour datetime(6) not null,
  id bigint not null auto_increment,
  limite_abonnement_id bigint not null,
  PRIMARY KEY (id),
  UNIQUE KEY uk_utilisation_limite_periode (limite_abonnement_id, periode_debut),
  CONSTRAINT FKo69h6vrli4f1qoionimsmoa21 FOREIGN KEY (limite_abonnement_id) REFERENCES limites_abonnement (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS historiques_abonnement (
  abonnement_id bigint not null,
  acteur_id bigint,
  date_evenement datetime(6) not null,
  id bigint not null auto_increment,
  ancienne_valeur varchar(255),
  description TEXT,
  nouvelle_valeur varchar(255),
  type_evenement enum ('ACTIVATION','ANNULATION','CHANGEMENT_OFFRE','EXPIRATION','FACTURE_EMISE','PAIEMENT_CONFIRME','PAIEMENT_DECLARE','PAIEMENT_REJETE','PASSAGE_GRACE','PROGRAMMATION','REACTIVATION','RENOUVELLEMENT','RETOUR_GRATUIT','SOUSCRIPTION','SUSPENSION') not null,
  PRIMARY KEY (id),
  KEY idx_historiques_abonnement_date (abonnement_id, date_evenement),
  CONSTRAINT FKi0d65i1kg00u0w5fha0ra8mev FOREIGN KEY (abonnement_id) REFERENCES abonnements (id),
  CONSTRAINT FK5qr5f7xc2mwg5kusfato6wxmb FOREIGN KEY (acteur_id) REFERENCES utilisateurs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS paiements_abonnement (
  devise varchar(3) not null,
  montant decimal(15,2) not null,
  abonnement_id bigint not null,
  date_creation datetime(6) not null,
  date_paiement datetime(6) not null,
  date_validation datetime(6),
  declare_par_id bigint,
  id bigint not null auto_increment,
  valide_par_id bigint,
  reference varchar(50) not null,
  reference_transaction varchar(100),
  contact_nom varchar(150),
  contact_telephone varchar(30),
  motif_rejet varchar(500),
  observation TEXT,
  mode_paiement enum ('AUTRE','ESPECES','ORANGE_MONEY','VIREMENT','WAVE') not null,
  statut enum ('ANNULE','ECHOUE','EN_ATTENTE','REUSSI') not null,
  PRIMARY KEY (id),
  UNIQUE KEY UKkj8iyqeqtj284kqlv6vkqfyxp (reference),
  KEY idx_paiements_abonnement_statut (statut),
  KEY idx_paiements_abonnement_transaction (mode_paiement, reference_transaction),
  KEY idx_paiements_abonnement_abonnement (abonnement_id),
  CONSTRAINT FKrixswrxme9g8ypr2wihiqvfnp FOREIGN KEY (abonnement_id) REFERENCES abonnements (id),
  CONSTRAINT FKda4hkyo9qb3pwl798ishrtqbq FOREIGN KEY (declare_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKpk96c3ubaogr2t6820g4k5no3 FOREIGN KEY (valide_par_id) REFERENCES utilisateurs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS factures_abonnement (
  devise varchar(3) not null,
  montant_ht decimal(15,2) not null,
  montant_taxe decimal(15,2) not null,
  montant_ttc decimal(15,2) not null,
  taux_taxe decimal(5,2) not null,
  abonnement_id bigint not null,
  date_emission datetime(6) not null,
  id bigint not null auto_increment,
  paiement_abonnement_id bigint,
  numero_facture varchar(30) not null,
  statut enum ('ANNULEE','EMISE','PAYEE') not null,
  PRIMARY KEY (id),
  UNIQUE KEY UKjx2av8i3kstm3auoo0pq48wha (paiement_abonnement_id),
  UNIQUE KEY UKpn7ahef6tjoc8sqgvb4ijok4j (numero_facture),
  CONSTRAINT FKoln6fkjltyd9s21u0byqidrrr FOREIGN KEY (abonnement_id) REFERENCES abonnements (id),
  CONSTRAINT FKhjf6dpet4c23r9yreeknnmams FOREIGN KEY (paiement_abonnement_id) REFERENCES paiements_abonnement (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -----------------------------------------------------------------------------
-- 6. Fournisseurs et achats
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fournisseurs (
  date_creation datetime(6) not null,
  date_modification datetime(6),
  id bigint not null auto_increment,
  pharmacopee_id bigint not null,
  telephone varchar(30),
  adresse varchar(255),
  description TEXT,
  email varchar(255),
  nom varchar(255) not null,
  statut enum ('ACTIF','INACTIF') not null,
  type_fournisseur enum ('AUTRE','COOPERATIVE','CUEILLEUR','GROSSISTE','PRODUCTEUR','TRADIPRATICIEN') not null,
  PRIMARY KEY (id),
  KEY idx_fournisseurs_pharmacopee_statut (pharmacopee_id, statut),
  CONSTRAINT FKqr9hfdhd3lv0d2h795ksibr4k FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fournisseurs_produits (
  delai_approvisionnement integer,
  disponible bit(1) not null,
  prix_indicatif decimal(15,2),
  fournisseur_id bigint not null,
  id bigint not null auto_increment,
  produit_id bigint not null,
  reference_fournisseur varchar(100),
  observation TEXT,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fournisseur_produit (fournisseur_id, produit_id),
  CONSTRAINT FK6i8cg8qrjnpefxcf1wiy987by FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs (id),
  CONSTRAINT FK75u4f6i4b3k0p0p2pbjkd5dhh FOREIGN KEY (produit_id) REFERENCES produits (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS achats (
  montant_remise decimal(15,2) not null,
  montant_total decimal(15,2) not null,
  annule_par_id bigint,
  date_achat datetime(6) not null,
  date_annulation datetime(6),
  date_creation datetime(6) not null,
  date_modification datetime(6),
  date_validation datetime(6),
  enregistre_par_id bigint not null,
  fournisseur_id bigint,
  id bigint not null auto_increment,
  pharmacopee_id bigint not null,
  valide_par_id bigint,
  numero_achat varchar(30) not null,
  motif_annulation varchar(500),
  observation TEXT,
  statut enum ('ANNULE','BROUILLON','VALIDE') not null,
  PRIMARY KEY (id),
  UNIQUE KEY uk_achat_pharmacopee_numero (pharmacopee_id, numero_achat),
  KEY idx_achats_pharmacopee_statut_date (pharmacopee_id, statut, date_achat),
  KEY idx_achats_fournisseur (fournisseur_id),
  CONSTRAINT FKjgvwqaq2qcny845pfdt8b9p5t FOREIGN KEY (annule_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKpef6j5ywioqiafdmgqipb9fsw FOREIGN KEY (enregistre_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKjphgx9cvysvktboojy9ta7tu6 FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs (id),
  CONSTRAINT FKop5k81b9kg4sv8ppk6oh87x75 FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id),
  CONSTRAINT FK5cm2x3d6hlca54me9v6i6dplh FOREIGN KEY (valide_par_id) REFERENCES membres_pharmacopee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lignes_achat (
  date_peremption date,
  prix_unitaire decimal(15,2) not null,
  quantite integer not null,
  remise decimal(15,2) not null,
  sous_total decimal(15,2) not null,
  achat_id bigint not null,
  id bigint not null auto_increment,
  produit_id bigint not null,
  numero_lot varchar(50),
  PRIMARY KEY (id),
  KEY idx_lignes_achat_achat (achat_id),
  KEY idx_lignes_achat_produit (produit_id),
  CONSTRAINT FKrwmlps9nc9jnayy046ua0ur1w FOREIGN KEY (achat_id) REFERENCES achats (id),
  CONSTRAINT FKrn6syx807j5uj9f6dwsv2w3in FOREIGN KEY (produit_id) REFERENCES produits (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reglements_achat (
  montant decimal(15,2) not null,
  achat_id bigint not null,
  date_annulation datetime(6),
  date_reglement datetime(6) not null,
  enregistre_par_id bigint not null,
  id bigint not null auto_increment,
  reference varchar(50),
  motif_annulation varchar(500),
  observation TEXT,
  methode enum ('CARTE_BANCAIRE','CASH','MOBILE_MONEY') not null,
  statut enum ('ANNULE','VALIDE') not null,
  PRIMARY KEY (id),
  KEY idx_reglements_achat_achat (achat_id),
  CONSTRAINT FK1wyn62phdxqucm6fwoabmdabe FOREIGN KEY (achat_id) REFERENCES achats (id),
  CONSTRAINT FK6mrf3ktb9wr7ums90i26h8mtt FOREIGN KEY (enregistre_par_id) REFERENCES membres_pharmacopee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -----------------------------------------------------------------------------
-- 7. Stock, ventes et lots
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lots (
  date_peremption date,
  quantite_initiale integer not null,
  quantite_restante integer not null,
  date_reception datetime(6) not null,
  disponibilite_produit_id bigint not null,
  id bigint not null auto_increment,
  ligne_achat_id bigint,
  pharmacopee_id bigint not null,
  numero_lot varchar(50),
  PRIMARY KEY (id),
  UNIQUE KEY UKlnbfyeaqa509etpmc2e6h1kjk (ligne_achat_id),
  KEY idx_lots_disponibilite_peremption (disponibilite_produit_id, date_peremption),
  KEY idx_lots_pharmacopee (pharmacopee_id),
  CONSTRAINT FKrwesbd3rioma65dhovgs10hc2 FOREIGN KEY (disponibilite_produit_id) REFERENCES disponibilites_produits (id),
  CONSTRAINT FK5lo0yb5vap4jo1s1yps2evwig FOREIGN KEY (ligne_achat_id) REFERENCES lignes_achat (id),
  CONSTRAINT FKj0chfjh6i00omuhxrlhcd87m5 FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS clients (
  actif bit(1) not null,
  date_creation datetime(6) not null,
  date_modification datetime(6),
  id bigint not null auto_increment,
  pharmacopee_id bigint not null,
  utilisateur_id bigint,
  telephone varchar(30),
  adresse varchar(255),
  nom varchar(255) not null,
  observation TEXT,
  prenom varchar(255),
  PRIMARY KEY (id),
  UNIQUE KEY uk_client_pharmacopee_telephone (pharmacopee_id, telephone),
  CONSTRAINT FK2ilfhu8oa6j0s1my37fcu0b67 FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id),
  CONSTRAINT FKfviksevb0acjakcmjidesj8jb FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ventes (
  montant_brut decimal(15,2) not null,
  montant_remise decimal(15,2) not null,
  montant_total decimal(15,2) not null,
  annule_par_id bigint,
  client_id bigint,
  date_annulation datetime(6),
  date_creation datetime(6) not null,
  date_modification datetime(6),
  date_validation datetime(6),
  date_vente datetime(6) not null,
  id bigint not null auto_increment,
  pharmacopee_id bigint not null,
  valide_par_id bigint,
  vendeur_id bigint not null,
  numero_vente varchar(30) not null,
  motif_annulation varchar(500),
  observation TEXT,
  statut enum ('ABANDONNEE','ANNULEE','EN_ATTENTE','VALIDEE') not null,
  PRIMARY KEY (id),
  UNIQUE KEY uk_vente_pharmacopee_numero (pharmacopee_id, numero_vente),
  KEY idx_ventes_pharmacopee_statut_date (pharmacopee_id, statut, date_vente),
  KEY idx_ventes_client (client_id),
  CONSTRAINT FKss09ljo7go7qruu8vnux244o5 FOREIGN KEY (annule_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKbvx23i6p7wmclguf5x4wbls6e FOREIGN KEY (client_id) REFERENCES clients (id),
  CONSTRAINT FK4dm2nwefy80mbwiud2p15sv9x FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id),
  CONSTRAINT FKil3ggm2jdqelaa29ccy36j3nh FOREIGN KEY (valide_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKn0rrsjv68ms3vi5m3ekihvkyq FOREIGN KEY (vendeur_id) REFERENCES membres_pharmacopee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lignes_vente (
  prix_unitaire decimal(15,2) not null,
  quantite integer not null,
  remise decimal(15,2) not null,
  sous_total decimal(15,2) not null,
  disponibilite_produit_id bigint not null,
  id bigint not null auto_increment,
  vente_id bigint not null,
  PRIMARY KEY (id),
  KEY idx_lignes_vente_vente (vente_id),
  KEY idx_lignes_vente_disponibilite (disponibilite_produit_id),
  CONSTRAINT FKfi8xvh41jun48opx45l1u5cqg FOREIGN KEY (disponibilite_produit_id) REFERENCES disponibilites_produits (id),
  CONSTRAINT FK5u3waoonthmakyx51opesgjr6 FOREIGN KEY (vente_id) REFERENCES ventes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS paiements_vente (
  montant decimal(15,2) not null,
  date_annulation datetime(6),
  date_paiement datetime(6) not null,
  enregistre_par_id bigint not null,
  id bigint not null auto_increment,
  vente_id bigint not null,
  reference varchar(50) not null,
  reference_transaction varchar(100),
  motif_annulation varchar(500),
  observation TEXT,
  methode enum ('CARTE_BANCAIRE','CASH','MOBILE_MONEY') not null,
  statut enum ('ANNULE','VALIDE') not null,
  PRIMARY KEY (id),
  UNIQUE KEY UK3dhpnourjrjj4yaqoguuajv28 (reference),
  KEY idx_paiements_vente_vente (vente_id),
  CONSTRAINT FKko4bk2l5mrjvgiugn4dlkhjoq FOREIGN KEY (enregistre_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKtoewhu9n20os7308djhsd327l FOREIGN KEY (vente_id) REFERENCES ventes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS mouvements_stock (
  quantite integer not null,
  stock_apres integer not null,
  stock_avant integer not null,
  date_mouvement datetime(6) not null,
  disponibilite_produit_id bigint not null,
  effectue_par_id bigint,
  id bigint not null auto_increment,
  ligne_achat_id bigint,
  ligne_commande_id bigint,
  ligne_vente_id bigint,
  lot_id bigint,
  pharmacopee_id bigint not null,
  motif varchar(255),
  type enum ('AJUSTEMENT_INVENTAIRE','ANNULATION_ACHAT','ANNULATION_COMMANDE','ANNULATION_VENTE','ENTREE_ACHAT','SORTIE_COMMANDE','SORTIE_VENTE') not null,
  PRIMARY KEY (id),
  KEY idx_mouvements_pharmacopee_date (pharmacopee_id, date_mouvement),
  KEY idx_mouvements_disponibilite (disponibilite_produit_id),
  CONSTRAINT FKoj7a7ckalf89b2nkpst4xxna9 FOREIGN KEY (disponibilite_produit_id) REFERENCES disponibilites_produits (id),
  CONSTRAINT FKtcyqp6d6heyebl5a3x9uupgvg FOREIGN KEY (effectue_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKgptgdm6j4u3jhwnl45qmwie8y FOREIGN KEY (ligne_achat_id) REFERENCES lignes_achat (id),
  CONSTRAINT FK9tcuyuw183kpodxuo0aodww6 FOREIGN KEY (ligne_commande_id) REFERENCES lignes_commande (id),
  CONSTRAINT FK1qhwfq5k0snn6a80axs7engf3 FOREIGN KEY (ligne_vente_id) REFERENCES lignes_vente (id),
  CONSTRAINT FK2696gdcowmm0kl6wxd4drng1b FOREIGN KEY (lot_id) REFERENCES lots (id),
  CONSTRAINT FKiybkmc77eqormq8j2hipxw1a7 FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -----------------------------------------------------------------------------
-- 8. Caisses et trésorerie
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS caisses (
  actif bit(1) not null,
  date_creation datetime(6) not null,
  id bigint not null auto_increment,
  pharmacopee_id bigint not null,
  nom varchar(100) not null,
  reference_compte varchar(100),
  description varchar(255),
  type enum ('BANQUE','MOBILE_MONEY','PHYSIQUE') not null,
  PRIMARY KEY (id),
  KEY idx_caisses_pharmacopee_type (pharmacopee_id, type),
  CONSTRAINT FK51qncro9rupom5a8942p2gcy9 FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sessions_caisse (
  ecart decimal(15,2),
  fond_ouverture decimal(15,2) not null,
  montant_constate decimal(15,2),
  montant_theorique decimal(15,2),
  caisse_id bigint not null,
  cloture_par_id bigint,
  date_cloture datetime(6),
  date_ouverture datetime(6) not null,
  id bigint not null auto_increment,
  ouverte_par_id bigint not null,
  commentaire TEXT,
  statut enum ('CLOTUREE','OUVERTE') not null,
  caisse_ouverte bigint GENERATED ALWAYS AS (CASE WHEN statut = 'OUVERTE' THEN caisse_id END) VIRTUAL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_session_caisse_ouverte (caisse_ouverte),
  KEY idx_sessions_caisse_statut (caisse_id, statut),
  CONSTRAINT FK9f8o4v4sb51ndr32bpv5hnjs8 FOREIGN KEY (caisse_id) REFERENCES caisses (id),
  CONSTRAINT FK8jp4jfe2iksb8q6w1kary3pcp FOREIGN KEY (cloture_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKo69yv9ofcdwyrjhcb01p981yu FOREIGN KEY (ouverte_par_id) REFERENCES membres_pharmacopee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS depenses (
  montant decimal(15,2) not null,
  date_annulation datetime(6),
  date_creation datetime(6) not null,
  date_depense datetime(6) not null,
  enregistre_par_id bigint not null,
  id bigint not null auto_increment,
  pharmacopee_id bigint not null,
  motif_annulation varchar(500),
  beneficiaire varchar(255),
  justificatif_url varchar(255),
  libelle varchar(255) not null,
  observation TEXT,
  categorie enum ('AUTRE','COMMUNICATION','ELECTRICITE_EAU','ENTRETIEN','FOURNITURES','LOYER','SALAIRE','TAXE_IMPOT','TRANSPORT') not null,
  statut enum ('ANNULE','VALIDE') not null,
  PRIMARY KEY (id),
  KEY idx_depenses_pharmacopee_date (pharmacopee_id, date_depense),
  CONSTRAINT FKbi0664h9rjd8ikdiv4xgy8xs7 FOREIGN KEY (enregistre_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FK6otlya4okjx7jajixv6df14pb FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operations_caisse (
  montant decimal(15,2) not null,
  caisse_id bigint not null,
  date_operation datetime(6) not null,
  depense_id bigint,
  effectue_par_id bigint,
  id bigint not null auto_increment,
  operation_liee_id bigint,
  paiement_commande_id bigint,
  paiement_vente_id bigint,
  pharmacopee_id bigint not null,
  reglement_achat_id bigint,
  session_caisse_id bigint,
  libelle varchar(255),
  nature enum ('ACHAT','AJUSTEMENT','ANNULATION','APPORT','DEPENSE','PAIEMENT_COMMANDE','RECETTE_DIVERSE','RETRAIT','TRANSFERT','VENTE') not null,
  sens enum ('ENTREE','SORTIE') not null,
  PRIMARY KEY (id),
  KEY idx_operations_caisse_date (caisse_id, date_operation),
  KEY idx_operations_pharmacopee_date (pharmacopee_id, date_operation),
  KEY idx_operations_session (session_caisse_id),
  CONSTRAINT FKna9bn36ernv5k4nv68b8wsj15 FOREIGN KEY (caisse_id) REFERENCES caisses (id),
  CONSTRAINT FKjsdrhum952rtwdoltyqrclrwt FOREIGN KEY (depense_id) REFERENCES depenses (id),
  CONSTRAINT FKfpr4jn53ygu6bsjv43opk5orp FOREIGN KEY (effectue_par_id) REFERENCES membres_pharmacopee (id),
  CONSTRAINT FKb557ytsxyv5mlxfy88idqde2v FOREIGN KEY (operation_liee_id) REFERENCES operations_caisse (id),
  CONSTRAINT FK8ium1e1vkduynuu4syg2qdkqw FOREIGN KEY (paiement_commande_id) REFERENCES paiements (id),
  CONSTRAINT FK43ukeqwrwihftovcg94xyompv FOREIGN KEY (paiement_vente_id) REFERENCES paiements_vente (id),
  CONSTRAINT FKt5kwqx4tdn086g6iwhxbbi70v FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id),
  CONSTRAINT FKg6ylnx285iejbo8x55rby9q7c FOREIGN KEY (reglement_achat_id) REFERENCES reglements_achat (id),
  CONSTRAINT FKkh62iht101q32r5sei234qwal FOREIGN KEY (session_caisse_id) REFERENCES sessions_caisse (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -----------------------------------------------------------------------------
-- 9. Traçabilité
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS journal_audit (
  date_action datetime(6) not null,
  entite_id bigint,
  id bigint not null auto_increment,
  pharmacopee_id bigint,
  utilisateur_id bigint,
  adresse_ip varchar(45),
  type_entite varchar(100) not null,
  description varchar(500),
  ancienne_valeur TEXT,
  nouvelle_valeur TEXT,
  action enum ('ABONNEMENT_ANNULE','ABONNEMENT_SOUSCRIT','ABONNEMENT_SUSPENDU','ACCES_ADMIN_PHARMACOPEE','ACHAT_ANNULE','DEPENSE_ANNULEE','ECART_CAISSE','MEMBRE_ACTIVE','MEMBRE_INVITE','MEMBRE_RETIRE','MEMBRE_SUSPENDU','OFFRE_MODIFIEE','OPERATION_CAISSE_ANNULEE','PAIEMENT_ABONNEMENT_CONFIRME','PAIEMENT_ABONNEMENT_REJETE','PAIEMENT_VENTE_ANNULE','PARAMETRE_MODIFIE','PERMISSIONS_ROLE_MODIFIEES','PHARMACOPEE_STATUT_MODIFIE','PROPRIETE_TRANSFEREE','REGLEMENT_ACHAT_ANNULE','ROLE_ATTRIBUE','ROLE_CREE','ROLE_DESACTIVE','ROLE_MODIFIE','ROLE_RETIRE','SESSION_CAISSE_CLOTUREE','STOCK_AJUSTE','VENTE_ANNULEE') not null,
  origine enum ('ADMINISTRATION','PHARMACOPEE','SYSTEME') not null,
  PRIMARY KEY (id),
  KEY idx_journal_audit_pharmacopee_date (pharmacopee_id, date_action),
  KEY idx_journal_audit_entite (type_entite, entite_id),
  CONSTRAINT FKnj1dxhkxq9uiasiurtdfxpwfo FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees (id),
  CONSTRAINT FKkcddxplh974rqqu8hjq5xhpoa FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
