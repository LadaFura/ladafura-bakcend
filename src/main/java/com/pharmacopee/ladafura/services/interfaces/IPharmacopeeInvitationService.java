package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.pharmacopee.membre.InvitationResponse;

/** Invitations reçues par l'utilisateur connecté ; il n'est pas encore membre de ces pharmacopées. */
public interface IPharmacopeeInvitationService {

    List<InvitationResponse> getMesInvitations();

    /**
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si l'invitation a expiré
     * @throws com.pharmacopee.ladafura.exceptions.QuotaExceededException si la pharmacopée a atteint son quota
     */
    void accepter(Long id);

    void refuser(Long id);
}
