package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.pharmacopee.ladafura.exceptions.QuotaExceededException;
import com.pharmacopee.ladafura.handlerException.ErrorResponse;
import com.pharmacopee.ladafura.handlerException.GlobalExceptionHandler;

class GlobalExceptionHandlerQuotaTest {

    @Test
    @DisplayName("un quota atteint renvoie 429 et le message métier, pas une erreur interne")
    void quotaAtteint() {
        MockHttpServletRequest requete = new MockHttpServletRequest("POST", "/api/v1/pharmacopee/membres");
        requete.setRequestURI("/api/v1/pharmacopee/membres");

        ResponseEntity<ErrorResponse> reponse = new GlobalExceptionHandler()
                .handleQuotaExceededException(new QuotaExceededException("Vous avez atteint la limite de votre abonnement (1)."), requete);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(reponse.getBody().getMessage()).isEqualTo("Vous avez atteint la limite de votre abonnement (1).");
    }
}
