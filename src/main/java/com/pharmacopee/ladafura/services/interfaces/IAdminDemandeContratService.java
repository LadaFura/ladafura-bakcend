package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.abonnement.CreerAbonnementContratRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.TraiterDemandeContratRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratResponse;
import com.pharmacopee.ladafura.enums.StatutDemandeContrat;

/** Traitement des demandes de contrat. Une demande ne crée jamais d'abonnement toute seule. */
public interface IAdminDemandeContratService {

    Page<DemandeContratResponse> getDemandes(StatutDemandeContrat statut, Pageable pageable);

    DemandeContratResponse traiter(Long id, TraiterDemandeContratRequest request);

    AbonnementResponse creerAbonnement(Long id, CreerAbonnementContratRequest request);
}
