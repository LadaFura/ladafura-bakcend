-- =============================================================================
-- V1 : Schéma initial LADAFURA (MySQL 8)
-- =============================================================================
-- Consolidation de tout l'historique antérieur à Flyway :
--   * schéma créé par Hibernate (ddl-auto=update) depuis les entités Models/ ;
--   * ancien script migrations/V2__notifications_complete.sql (index ajoutés en fin) ;
--   * élargissements de colonnes de configs/SchemaColumnFixer (déjà déclarés TEXT
--     dans les entités VertuDeLaPlante et Collecte).
-- DDL généré par Hibernate 7 sur une base vierge, puis extrait par mysqldump :
-- les noms de contraintes (FK..., UK...) sont ceux qu'Hibernate a créés sur la base
-- existante.
--
-- Exécution :
--   * base VIERGE : exécuté intégralement par Flyway ;
--   * base EXISTANTE (Aiven) : NON exécuté — marqué comme baseline
--     (spring.flyway.baseline-on-migrate=true, baseline-version=1).
--
-- Règle de développement : au plus 5 versions (V1 à V5). Tant que le projet est en
-- développement, une nouvelle évolution se range dans la version existante la plus
-- adaptée (V4/V5 réservées) ; modifier une version déjà appliquée impose
-- `flyway repair` (ou une base recréée) sur les environnements concernés.
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `agents_collecte` (
  `id` bigint NOT NULL,
  `matricule` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `zone_couverture` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKdlxy0gmthb381l3iihqvjnfta` FOREIGN KEY (`id`) REFERENCES `utilisateurs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `avis` (
  `note` int NOT NULL,
  `date_avis` datetime(6) NOT NULL,
  `date_reponse` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pharmacopee_id` bigint NOT NULL,
  `utilisateur_id` bigint NOT NULL,
  `commentaire` text COLLATE utf8mb4_unicode_ci,
  `reponse_officine` text COLLATE utf8mb4_unicode_ci,
  `statut` enum('EN_ATTENTE','MASQUE','PUBLIE','REJETE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgoon54s8lfqer8cfcvuomcmx7` (`pharmacopee_id`),
  KEY `FK5t211yop0n2gf99wtsh7u9c6u` (`utilisateur_id`),
  CONSTRAINT `FK5t211yop0n2gf99wtsh7u9c6u` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FKgoon54s8lfqer8cfcvuomcmx7` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`),
  CONSTRAINT `avis_chk_1` CHECK (((`note` <= 5) and (`note` >= 1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `categories_produits` (
  `statut` bit(1) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5uii1018ambq0k5dqn2ripqe8` (`nom`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `collectes` (
  `agent_collecte_id` bigint DEFAULT NULL,
  `date_collecte` datetime(6) NOT NULL,
  `date_soumission` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `localisation_id` bigint DEFAULT NULL,
  `source_id` bigint DEFAULT NULL,
  `audio_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `motif_rejet` text COLLATE utf8mb4_unicode_ci,
  `photo_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `statut` enum('BROUILLON','EN_EXAMEN','REJETEE','SOUMISE','VALIDEE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpel4rvlth9crai9uekhrppctq` (`agent_collecte_id`),
  KEY `FKp9riw3qo739yspguqrtdjio6k` (`localisation_id`),
  KEY `FKghjvcl8jv1wvcg810svc3bdw0` (`source_id`),
  CONSTRAINT `FKghjvcl8jv1wvcg810svc3bdw0` FOREIGN KEY (`source_id`) REFERENCES `sources_connaissances` (`id`),
  CONSTRAINT `FKp9riw3qo739yspguqrtdjio6k` FOREIGN KEY (`localisation_id`) REFERENCES `localisations` (`id`),
  CONSTRAINT `FKpel4rvlth9crai9uekhrppctq` FOREIGN KEY (`agent_collecte_id`) REFERENCES `agents_collecte` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `commandes` (
  `montant_livraison` double NOT NULL,
  `montant_total` double NOT NULL,
  `total_produit` double NOT NULL,
  `date_commande` datetime(6) NOT NULL,
  `date_miseajour` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mode_retrait_id` bigint DEFAULT NULL,
  `pharmacopee_id` bigint DEFAULT NULL,
  `utilisateur_id` bigint NOT NULL,
  `adresse_livraison` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numero` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `statut` enum('ANNULEE','CONFIRMEE','DISPONIBLE_PICKUP','EN_ATTENTE','EN_LIVRAISON','LIVREE','PREPAREE','RETIREE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmyfnokgjpejv0m7nbajy90cg1` (`numero`),
  KEY `FKl7re42fvfy67rn5any05e6kge` (`mode_retrait_id`),
  KEY `FK7c5oljnfb7ml03ehbcd272b80` (`pharmacopee_id`),
  KEY `FK394iuksyq22ka7rwo7pynmwam` (`utilisateur_id`),
  CONSTRAINT `FK394iuksyq22ka7rwo7pynmwam` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FK7c5oljnfb7ml03ehbcd272b80` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`),
  CONSTRAINT `FKl7re42fvfy67rn5any05e6kge` FOREIGN KEY (`mode_retrait_id`) REFERENCES `modes_retrait` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `compositions_produits` (
  `quantite` double DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plante_id` bigint NOT NULL,
  `produit_id` bigint NOT NULL,
  `unite` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrkkugb20t0ctgqlc7nv24t7n7` (`plante_id`),
  KEY `FK5tdqk49svo70eiaare9b0mije` (`produit_id`),
  CONSTRAINT `FK5tdqk49svo70eiaare9b0mije` FOREIGN KEY (`produit_id`) REFERENCES `produits` (`id`),
  CONSTRAINT `FKrkkugb20t0ctgqlc7nv24t7n7` FOREIGN KEY (`plante_id`) REFERENCES `plantes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `disponibilites_produits` (
  `disponible` bit(1) NOT NULL,
  `prix` double DEFAULT NULL,
  `quantite_stock` int DEFAULT NULL,
  `date_miseajour` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pharmacopee_id` bigint NOT NULL,
  `produit_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8a1tuy3pxlnue2u3oopqlp7ef` (`pharmacopee_id`),
  KEY `FKd7btousqo1fd79ovbemw817jh` (`produit_id`),
  CONSTRAINT `FK8a1tuy3pxlnue2u3oopqlp7ef` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`),
  CONSTRAINT `FKd7btousqo1fd79ovbemw817jh` FOREIGN KEY (`produit_id`) REFERENCES `produits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `etudes_scientifiques` (
  `annee` int DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plante_id` bigint DEFAULT NULL,
  `auteurs` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `document_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reference` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `resume` text COLLATE utf8mb4_unicode_ci,
  `titre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7p6foinmaqo5enucymx0xdmom` (`plante_id`),
  CONSTRAINT `FK7p6foinmaqo5enucymx0xdmom` FOREIGN KEY (`plante_id`) REFERENCES `plantes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `favoris` (
  `date_ajout` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pharmacopee_id` bigint DEFAULT NULL,
  `plante_id` bigint DEFAULT NULL,
  `produit_id` bigint DEFAULT NULL,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk5iaov1isyvn2013sscn58mdv` (`pharmacopee_id`),
  KEY `FKmsvv7lm3ts885krvisaa50gvn` (`plante_id`),
  KEY `FK94p2cu1myar4jxmbips66pf9v` (`produit_id`),
  KEY `FK66vy5l51mxyx0hrgqo54q40xm` (`utilisateur_id`),
  CONSTRAINT `FK66vy5l51mxyx0hrgqo54q40xm` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FK94p2cu1myar4jxmbips66pf9v` FOREIGN KEY (`produit_id`) REFERENCES `produits` (`id`),
  CONSTRAINT `FKk5iaov1isyvn2013sscn58mdv` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`),
  CONSTRAINT `FKmsvv7lm3ts885krvisaa50gvn` FOREIGN KEY (`plante_id`) REFERENCES `plantes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `fichiers_medias_stockes` (
  `date_creation` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `taille` bigint NOT NULL,
  `content_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `chemin` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nom_original` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `donnees` longblob NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_fichier_media_chemin` (`chemin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lignes_commande` (
  `prix_unitaire` double NOT NULL,
  `quantite` int NOT NULL,
  `sous_total` double NOT NULL,
  `commande_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `produit_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtry44xh8jbos217m4nk3wyyem` (`commande_id`),
  KEY `FK97125vvuxcin9rytjad4u9bm4` (`produit_id`),
  CONSTRAINT `FK97125vvuxcin9rytjad4u9bm4` FOREIGN KEY (`produit_id`) REFERENCES `produits` (`id`),
  CONSTRAINT `FKtry44xh8jbos217m4nk3wyyem` FOREIGN KEY (`commande_id`) REFERENCES `commandes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lignes_panier` (
  `prix_unitaire` double NOT NULL,
  `quantite` int NOT NULL,
  `sous_total` double NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `panier_id` bigint NOT NULL,
  `produit_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5urbu5yw99sn79ntt7n64osyx` (`panier_id`),
  KEY `FKfmpx2g06on6xvqw7m3n5h3rll` (`produit_id`),
  CONSTRAINT `FK5urbu5yw99sn79ntt7n64osyx` FOREIGN KEY (`panier_id`) REFERENCES `paniers` (`id`),
  CONSTRAINT `FKfmpx2g06on6xvqw7m3n5h3rll` FOREIGN KEY (`produit_id`) REFERENCES `produits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `localisations` (
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cercle` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `commune` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `localite` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `region` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `maladies` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` text COLLATE utf8mb4_unicode_ci,
  `nom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrbd5vef78a0biupyo15lapnep` (`nom`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `modes_retrait` (
  `actif` bit(1) NOT NULL,
  `frais` double NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pharmacopee_id` bigint NOT NULL,
  `type` enum('LIVRAISON','PICKUP') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkkodyo28g8jcubd554p0ac797` (`pharmacopee_id`),
  CONSTRAINT `FKkkodyo28g8jcubd554p0ac797` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `noms_plantes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plante_id` bigint NOT NULL,
  `langue` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pays` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcdfoee34jdeuroe2wia41r3h7` (`plante_id`),
  CONSTRAINT `FKcdfoee34jdeuroe2wia41r3h7` FOREIGN KEY (`plante_id`) REFERENCES `plantes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notifications` (
  `lue` bit(1) NOT NULL,
  `date_lecture` datetime(6) DEFAULT NULL,
  `date_notification` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pharmacopee_id` bigint DEFAULT NULL,
  `utilisateur_id` bigint DEFAULT NULL,
  `type_entite_liee` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lien` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `reference_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `titre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `niveau` enum('ATTENTION','INFO','SUCCES','URGENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('AVIS_NOUVEAU','COLLECTE_ATTRIBUEE','COLLECTE_CORRECTION','COLLECTE_REJETEE','COLLECTE_SOUMISE','COLLECTE_STATUT','COLLECTE_VALIDEE','COMMANDE_NOUVELLE','COMMANDE_STATUT','INFO_IMPORTANTE','REFERENCEMENT','STOCK_ALERTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK61dpi935tkwqnm0wlqkqljnh7` (`pharmacopee_id`),
  KEY `FKsoq6jchv8p6wnydvsnf6ubw4y` (`utilisateur_id`),
  CONSTRAINT `FK61dpi935tkwqnm0wlqkqljnh7` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`),
  CONSTRAINT `FKsoq6jchv8p6wnydvsnf6ubw4y` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `paiements` (
  `montant` double NOT NULL,
  `commande_id` bigint NOT NULL,
  `date_paiement` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reference` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `methode` enum('CARTE_BANCAIRE','CASH','MOBILE_MONEY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `statut` enum('ANNULE','ECHOUE','EN_ATTENTE','REUSSI') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKtkx0ahv3fe1vnlgl0fk8v7x99` (`commande_id`),
  CONSTRAINT `FK9nk9m8et7flj8ilc0692d8xmp` FOREIGN KEY (`commande_id`) REFERENCES `commandes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `paniers` (
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfuwnphllc7qsy1h9wmhdqbok0` (`utilisateur_id`),
  CONSTRAINT `FKjy2tpcxy4ew6hh6aqpa8uyeix` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `pharmacopees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `localisation_id` bigint DEFAULT NULL,
  `utilisateur_id` bigint DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `nom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `photo_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telephone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `statut` enum('EN_ATTENTE','REJETEE','SUSPENDUE','VALIDEE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKu491t5dgrrbue60uqbsytxq3` (`localisation_id`),
  KEY `FKma26kmr8876kmgo5vp7acjvgd` (`utilisateur_id`),
  CONSTRAINT `FKma26kmr8876kmgo5vp7acjvgd` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FKu491t5dgrrbue60uqbsytxq3` FOREIGN KEY (`localisation_id`) REFERENCES `localisations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `plante_maladies` (
  `maladie_id` bigint NOT NULL,
  `plante_id` bigint NOT NULL,
  PRIMARY KEY (`maladie_id`,`plante_id`),
  KEY `FKlby1o9udv3spsfd3s3qvj2g9p` (`plante_id`),
  CONSTRAINT `FKlby1o9udv3spsfd3s3qvj2g9p` FOREIGN KEY (`plante_id`) REFERENCES `plantes` (`id`),
  CONSTRAINT `FKt3dduatass3alj9r98nbaca48` FOREIGN KEY (`maladie_id`) REFERENCES `maladies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `plantes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre_consultations` bigint NOT NULL DEFAULT '0',
  `description` text COLLATE utf8mb4_unicode_ci,
  `nom_scientifique` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `photo_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `statut` enum('ARCHIVE','BROUILLON','EN_ATTENTE','REJETE','VALIDE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK449r8y26dyxo59hilj1xrj3ek` (`nom_scientifique`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `praticiens` (
  `est_praticien_principal` bit(1) NOT NULL,
  `quota_max_structures` int NOT NULL,
  `id` bigint NOT NULL,
  `numero_agrement` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `plan_abonnement` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `specialite` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK96dt2jofovh1mm67ubolsdt53` FOREIGN KEY (`id`) REFERENCES `utilisateurs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `praticiens_pharmacopees` (
  `pharmacopee_id` bigint NOT NULL,
  `praticien_id` bigint NOT NULL,
  PRIMARY KEY (`pharmacopee_id`,`praticien_id`),
  KEY `FK60bb81ouavlxtlcce7m4ptl6s` (`praticien_id`),
  CONSTRAINT `FK60bb81ouavlxtlcce7m4ptl6s` FOREIGN KEY (`praticien_id`) REFERENCES `praticiens` (`id`),
  CONSTRAINT `FKooykjrp5fbjocj6922vnpa25r` FOREIGN KEY (`pharmacopee_id`) REFERENCES `pharmacopees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `produit_maladies` (
  `maladie_id` bigint NOT NULL,
  `produit_id` bigint NOT NULL,
  PRIMARY KEY (`maladie_id`,`produit_id`),
  KEY `FK1omj7oxnyfbr9osq2rxhsnqln` (`produit_id`),
  CONSTRAINT `FK1omj7oxnyfbr9osq2rxhsnqln` FOREIGN KEY (`produit_id`) REFERENCES `produits` (`id`),
  CONSTRAINT `FKqb451res5fl5mlbe2wr1b99ht` FOREIGN KEY (`maladie_id`) REFERENCES `maladies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `produits` (
  `prix` double NOT NULL,
  `categorie_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `composition` text COLLATE utf8mb4_unicode_ci,
  `description` text COLLATE utf8mb4_unicode_ci,
  `forme` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `photo_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `posologie` text COLLATE utf8mb4_unicode_ci,
  `statut` enum('ARCHIVE','BROUILLON','EN_ATTENTE','REJETE','VALIDE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9xa6cyqy5gvw0d1ab5hsqvl2m` (`categorie_id`),
  CONSTRAINT `FK9xa6cyqy5gvw0d1ab5hsqvl2m` FOREIGN KEY (`categorie_id`) REFERENCES `categories_produits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sources_connaissances` (
  `annees_experience` int DEFAULT NULL,
  `id` bigint NOT NULL,
  `adresse` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `specialite` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKnjh2j4tmdqplqmis5g533b4gn` FOREIGN KEY (`id`) REFERENCES `utilisateurs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `utilisateurs` (
  `date_creation` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `firebase_uid` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mot_de_passe` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `prenom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telephone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMINISTRATEUR','AGENT_COLLECTE','HERBORISTE','PHARMACOPEE','POPULATION','THERAPEUTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `statut` enum('ACTIF','INACTIF','SUSPENDU') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6ldvumu3hqvnmmxy1b6lsxwqy` (`email`),
  UNIQUE KEY `UK32tmj6t911egldxm7tur15735` (`firebase_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `vertus_plantes` (
  `collecte_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plante_id` bigint DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `partie_utilisee` text COLLATE utf8mb4_unicode_ci,
  `precaution` text COLLATE utf8mb4_unicode_ci,
  `preparation` text COLLATE utf8mb4_unicode_ci,
  `usage_rapporte` text COLLATE utf8mb4_unicode_ci,
  `statut` enum('ARCHIVE','BROUILLON','EN_ATTENTE','REJETE','VALIDE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsoa8lpva6ys22g4m9eroj20iw` (`collecte_id`),
  KEY `FKhiaq24uwb1ec2owvvllj2u08n` (`plante_id`),
  CONSTRAINT `FKhiaq24uwb1ec2owvvllj2u08n` FOREIGN KEY (`plante_id`) REFERENCES `plantes` (`id`),
  CONSTRAINT `FKsoa8lpva6ys22g4m9eroj20iw` FOREIGN KEY (`collecte_id`) REFERENCES `collectes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -----------------------------------------------------------------------------
-- Index de performance des notifications (reprise de l'ancien V2)
-- -----------------------------------------------------------------------------
CREATE INDEX idx_notifications_utilisateur_lue_date ON notifications (utilisateur_id, lue, date_notification DESC);
CREATE INDEX idx_notifications_pharmacopee_lue_date ON notifications (pharmacopee_id, lue, date_notification DESC);
CREATE INDEX idx_notifications_dedup ON notifications (utilisateur_id, type, reference_id, date_notification);

SET FOREIGN_KEY_CHECKS = 1;
