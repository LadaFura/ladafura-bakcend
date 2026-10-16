package com.pharmacopee.ladafura.configs;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Élargit au démarrage les colonnes texte trop courtes déjà créées en base.
 * <p>
 * {@code spring.jpa.hibernate.ddl-auto=update} n'agrandit jamais une colonne existante
 * (ex. VARCHAR(255) -> TEXT) ; sans ce correctif, les saisies longues provoquent
 * « Data too long for column ». Les ALTER sont idempotents et sans danger.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaColumnFixer {

    private final JdbcTemplate jdbcTemplate;

    private static final List<String> STATEMENTS = List.of(
            "ALTER TABLE vertus_plantes MODIFY COLUMN preparation TEXT NULL",
            "ALTER TABLE vertus_plantes MODIFY COLUMN partie_utilisee TEXT NULL",
            "ALTER TABLE vertus_plantes MODIFY COLUMN usage_rapporte TEXT NULL",
            "ALTER TABLE vertus_plantes MODIFY COLUMN precaution TEXT NULL",
            "ALTER TABLE vertus_plantes MODIFY COLUMN description TEXT NULL",
            "ALTER TABLE collectes MODIFY COLUMN description TEXT NULL",
            "ALTER TABLE collectes MODIFY COLUMN motif_rejet TEXT NULL"
    );

    @EventListener(ApplicationReadyEvent.class)
    public void fixColumns() {
        for (String sql : STATEMENTS) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                log.debug("Correctif de schéma ignoré [{}] : {}", sql, e.getMessage());
            }
        }
        log.info("Vérification des largeurs de colonnes texte terminée.");
    }
}

