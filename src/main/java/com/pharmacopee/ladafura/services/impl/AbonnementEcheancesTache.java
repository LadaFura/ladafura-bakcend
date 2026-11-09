package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Passage quotidien des échéances d'abonnements. */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "ladafura.saas.taches-planifiees.enabled", havingValue = "true")
public class AbonnementEcheancesTache {

    private final IAbonnementCycleService cycleService;

    @Scheduled(cron = "0 30 1 * * *", zone = "Africa/Bamako")
    public void executer() {
        int traites = cycleService.traiterEcheances(LocalDateTime.now());
        log.info("Échéances d'abonnements traitées : {}", traites);
    }
}
