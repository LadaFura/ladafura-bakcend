package com.pharmacopee.ladafura.services.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.pharmacopee.ladafura.services.interfaces.IEmailService;
import com.pharmacopee.ladafura.services.interfaces.IEmailService.Email;

import lombok.RequiredArgsConstructor;

/** L'email ne part qu'une fois l'opération validée, et sans faire attendre l'utilisateur. */
@Component
@RequiredArgsConstructor
public class EmailApresValidationListener {

    private final IEmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void envoyer(Email email) {
        emailService.envoyer(email);
    }
}
