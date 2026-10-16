-- =============================================================================
-- Migration SQL : Système de Notifications Complet LADAFURA (MySQL 8)
-- =============================================================================
-- Ce script crée ou met à jour la table `notifications` avec les colonnes et
-- index requis pour les espaces Agent de collecte, Administration,
-- Gestionnaires de pharmacopée et Citoyens/Population.
-- =============================================================================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'COMMANDE_NOUVELLE, COMMANDE_STATUT, REFERENCEMENT, AVIS_NOUVEAU, STOCK_ALERTE, INFO_IMPORTANTE, COLLECTE_VALIDEE, COLLECTE_REJETEE, COLLECTE_CORRECTION, COLLECTE_STATUT, COLLECTE_SOUMISE, COLLECTE_ATTRIBUEE',
    niveau VARCHAR(30) NOT NULL DEFAULT 'INFO' COMMENT 'INFO, SUCCES, ATTENTION, URGENT',
    lue TINYINT(1) NOT NULL DEFAULT 0,
    date_notification DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    date_lecture DATETIME(6) NULL,
    type_entite_liee VARCHAR(60) NULL COMMENT 'COLLECTE, COMMANDE, PHARMACOPEE, AVIS, STOCK',
    reference_id VARCHAR(100) NULL COMMENT 'Identifiant de la ressource métier cible',
    lien VARCHAR(255) NULL COMMENT 'Route ou lien applicatif vers la ressource cible',
    pharmacopee_id BIGINT NULL,
    utilisateur_id BIGINT NULL,
    CONSTRAINT fk_notifications_pharmacopee
        FOREIGN KEY (pharmacopee_id) REFERENCES pharmacopees(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ajout idempotent de la colonne type_entite_liee si la table existait déjà
SET @dbname = DATABASE();
SET @tablename = 'notifications';
SET @columnname = 'type_entite_liee';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  'ALTER TABLE notifications ADD COLUMN type_entite_liee VARCHAR(60) NULL AFTER date_lecture'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Mise à jour de la colonne `type` en VARCHAR(50) pour supporter tous les types (COLLECTE_SOUMISE, COLLECTE_ATTRIBUEE, etc.)
ALTER TABLE notifications MODIFY COLUMN type VARCHAR(50) NOT NULL;

-- Index d'optimisation pour les compteurs de cloche (non lues) et la pagination
CREATE INDEX idx_notifications_utilisateur_lue_date ON notifications (utilisateur_id, lue, date_notification DESC);
CREATE INDEX idx_notifications_pharmacopee_lue_date ON notifications (pharmacopee_id, lue, date_notification DESC);
CREATE INDEX idx_notifications_dedup ON notifications (utilisateur_id, type, reference_id, date_notification);

