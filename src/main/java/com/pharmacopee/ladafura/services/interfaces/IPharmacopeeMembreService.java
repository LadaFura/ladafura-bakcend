package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.membre.CreerMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InviterMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.MembreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.ModifierRolesMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.TransfertProprieteRequest;
import com.pharmacopee.ladafura.enums.StatutMembre;

/** Membres de la pharmacopée courante : la pharmacopée est toujours celle du contexte serveur. */
public interface IPharmacopeeMembreService {

    Page<MembreResponse> getMembres(StatutMembre statut, Pageable pageable);

    MembreResponse getMembre(Long id);

    /** Invite un compte existant, trouvé par email ou téléphone exact. */
    MembreResponse inviter(InviterMembreRequest request);

    /** Crée un compte Firebase et un membre ACTIF ; aucun mot de passe n'est stocké en base. */
    MembreResponse creer(CreerMembreRequest request);

    MembreResponse modifierRoles(Long id, ModifierRolesMembreRequest request);

    MembreResponse suspendre(Long id);

    MembreResponse reactiver(Long id);

    MembreResponse retirer(Long id);

    /** Réservé au propriétaire actuel de la pharmacopée. */
    MembreResponse transfererPropriete(TransfertProprieteRequest request);

    /** Transfert décidé par l'administrateur LADAFURA (litige, gérant indisponible). */
    MembreResponse transfererProprieteParAdmin(Long pharmacopeeId, TransfertProprieteRequest request);
}
