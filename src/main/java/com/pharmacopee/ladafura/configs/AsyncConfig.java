package com.pharmacopee.ladafura.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Active {@code @Async}, utilisé pour l'envoi des emails hors du fil de la requête. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
