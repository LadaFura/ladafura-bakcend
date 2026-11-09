package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.achat.AchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerReglementAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.ReglementAchatResponse;
import com.pharmacopee.ladafura.enums.StatutAchat;

/** Achats de la pharmacopée courante. Seule la validation modifie le stock. */
public interface IPharmacopeeAchatService {

    Page<AchatResponse> getAchats(StatutAchat statut, Long fournisseurId, Pageable pageable);

    AchatResponse getAchat(Long id);

    AchatResponse creer(EnregistrerAchatRequest request);

    AchatResponse modifier(Long id, EnregistrerAchatRequest request);

    void supprimer(Long id);

    /**
     * Fait entrer la marchandise en stock.
     *
     * @throws com.pharmacopee.ladafura.exceptions.QuotaExceededException si un nouveau produit dépasse le quota
     */
    AchatResponse valider(Long id);

    /** @throws com.pharmacopee.ladafura.exceptions.ConflictException s'il reste des règlements ou si le stock a été consommé */
    AchatResponse annuler(Long id, String motif);

    List<ReglementAchatResponse> getReglements(Long achatId);

    ReglementAchatResponse enregistrerReglement(Long achatId, EnregistrerReglementAchatRequest request);

    ReglementAchatResponse annulerReglement(Long achatId, Long reglementId, String motif);
}
