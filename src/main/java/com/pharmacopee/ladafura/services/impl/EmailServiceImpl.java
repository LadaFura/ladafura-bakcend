package com.pharmacopee.ladafura.services.impl;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pharmacopee.ladafura.services.interfaces.IEmailService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String hote;
    private final String expediteur;

    public EmailServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                            @Value("${spring.mail.host:}") String hote,
                            @Value("${ladafura.email.expediteur:}") String expediteur) {
        this.mailSender = mailSender;
        this.hote = hote;
        this.expediteur = expediteur;
    }

    @Override
    public void envoyer(Email email) {
        JavaMailSender expedition = mailSender.getIfAvailable();
        if (!StringUtils.hasText(hote) || !StringUtils.hasText(expediteur) || expedition == null) {
            log.info("Envoi d'emails non configuré : notification « {} » non envoyée.", email.sujet());
            return;
        }
        if (email.destinataires().isEmpty()) {
            log.warn("Aucun destinataire pour la notification « {} ».", email.sujet());
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(expediteur);
            message.setTo(email.destinataires().toArray(String[]::new));
            message.setSubject(sansSautDeLigne(email.sujet()));
            message.setText(email.corps());
            expedition.send(message);
        } catch (RuntimeException e) {
            log.error("Échec de l'envoi de la notification « {} » : {}", email.sujet(), e.getMessage());
        }
    }

    /** Un sujet peut contenir un nom saisi par un utilisateur : il ne doit pas pouvoir ajouter d'en-tête. */
    private static String sansSautDeLigne(String sujet) {
        return sujet.replaceAll("[\\r\\n]+", " ");
    }
}
