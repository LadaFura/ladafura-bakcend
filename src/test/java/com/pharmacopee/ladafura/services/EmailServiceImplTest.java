package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.pharmacopee.ladafura.services.impl.EmailServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IEmailService.Email;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock private ObjectProvider<JavaMailSender> fournisseur;
    @Mock private JavaMailSender expedition;

    private final Email email = new Email(List.of("contact@ladafura.ml"), "Sujet", "Corps");

    @BeforeEach
    void preparer() {
        Mockito.lenient().when(fournisseur.getIfAvailable()).thenReturn(expedition);
    }

    private EmailServiceImpl serviceConfigure() {
        return new EmailServiceImpl(fournisseur, "smtp.exemple.ml", "ne-pas-repondre@ladafura.ml");
    }

    @Test
    @DisplayName("sans configuration SMTP, rien n'est envoyé")
    void nonConfigure() {
        new EmailServiceImpl(fournisseur, "", "").envoyer(email);

        verify(expedition, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("une panne d'envoi ne remonte pas à l'appelant")
    void panneSansEffet() {
        doThrow(new MailSendException("serveur indisponible")).when(expedition).send(any(SimpleMailMessage.class));

        assertThatCode(() -> serviceConfigure().envoyer(email)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("un saut de ligne dans le sujet ne peut pas ajouter d'en-tête")
    void sujetAssaini() {
        serviceConfigure().envoyer(new Email(List.of("contact@ladafura.ml"), "Officine\r\nBcc: pirate@exemple.ml", "Corps"));

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(expedition).send(message.capture());
        assertThat(message.getValue().getSubject()).doesNotContain("\r").doesNotContain("\n");
    }

    @Test
    @DisplayName("sans destinataire, rien n'est envoyé")
    void sansDestinataire() {
        when(fournisseur.getIfAvailable()).thenReturn(expedition);

        serviceConfigure().envoyer(new Email(List.of(), "Sujet", "Corps"));

        verify(expedition, never()).send(any(SimpleMailMessage.class));
    }
}
