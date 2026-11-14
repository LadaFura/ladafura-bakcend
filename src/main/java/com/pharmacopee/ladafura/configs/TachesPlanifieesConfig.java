package com.pharmacopee.ladafura.configs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active les tâches planifiées SaaS. Désactivé par défaut : sur une base où V4 et V5 ne sont pas
 * appliquées, ces tâches interrogeraient des tables absentes.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "ladafura.saas.taches-planifiees.enabled", havingValue = "true")
public class TachesPlanifieesConfig {
}
