package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

/** Envoi d'emails de notification. Sans configuration SMTP, l'envoi est simplement désactivé. */
public interface IEmailService {

    record Email(List<String> destinataires, String sujet, String corps) {
    }

    /** N'échoue jamais : une panne d'envoi est journalisée et ne doit pas affecter l'opération métier. */
    void envoyer(Email email);
}
