package com.pharmacopee.ladafura.configs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active la planification nécessaire à l'anti-veille, indépendamment des tâches SaaS
 * ({@link TachesPlanifieesConfig}). Désactivé par défaut : inutile en développement local.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "ladafura.maintien-eveil.enabled", havingValue = "true")
public class MaintienEveilConfig {
}
