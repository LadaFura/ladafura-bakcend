package com.pharmacopee.ladafura.mappers;

import java.math.BigDecimal;
import java.util.List;

import com.pharmacopee.ladafura.Models.Client;
import com.pharmacopee.ladafura.Models.LigneVente;
import com.pharmacopee.ladafura.Models.PaiementVente;
import com.pharmacopee.ladafura.Models.Vente;
import com.pharmacopee.ladafura.dto.pharmacopee.client.ClientResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.LigneVenteResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.VenteResponse;

/** Conversions entité vers DTO des clients et des ventes, sans accès à la base. */
public final class VenteMapper {

    private VenteMapper() {
    }

    public static ClientResponse client(Client c, BigDecimal encours) {
        return ClientResponse.builder().id(c.getId()).nom(c.getNom()).prenom(c.getPrenom()).telephone(c.getTelephone())
                .adresse(c.getAdresse()).observation(c.getObservation()).actif(c.getActif()).dateCreation(c.getDateCreation())
                .encours(encours).build();
    }

    public static VenteResponse vente(Vente v, BigDecimal montantPaye, List<LigneVenteResponse> lignes) {
        Client client = v.getClient();
        return VenteResponse.builder()
                .id(v.getId())
                .numeroVente(v.getNumeroVente())
                .dateVente(v.getDateVente())
                .statut(v.getStatut())
                .clientId(client != null ? client.getId() : null)
                .clientNom(client != null ? nomComplet(client) : null)
                .montantBrut(v.getMontantBrut())
                .montantRemise(v.getMontantRemise())
                .montantTotal(v.getMontantTotal())
                .montantPaye(montantPaye)
                .resteAPayer(v.getMontantTotal().subtract(montantPaye))
                .observation(v.getObservation())
                .dateValidation(v.getDateValidation())
                .dateAnnulation(v.getDateAnnulation())
                .motifAnnulation(v.getMotifAnnulation())
                .lignes(lignes)
                .build();
    }

    private static String nomComplet(Client client) {
        return client.getPrenom() != null ? client.getPrenom() + " " + client.getNom() : client.getNom();
    }

    public static LigneVenteResponse ligne(LigneVente l) {
        return LigneVenteResponse.builder().id(l.getId()).disponibiliteProduitId(l.getDisponibiliteProduit().getId())
                .produitNom(l.getDisponibiliteProduit().getProduit().getNom()).quantite(l.getQuantite())
                .prixUnitaire(l.getPrixUnitaire()).remise(l.getRemise()).sousTotal(l.getSousTotal()).build();
    }

    public static PaiementVenteResponse paiement(PaiementVente p) {
        return PaiementVenteResponse.builder().id(p.getId()).venteId(p.getVente().getId()).reference(p.getReference()).montant(p.getMontant())
                .methode(p.getMethode()).referenceTransaction(p.getReferenceTransaction()).datePaiement(p.getDatePaiement())
                .observation(p.getObservation()).statut(p.getStatut()).dateAnnulation(p.getDateAnnulation()).motifAnnulation(p.getMotifAnnulation()).build();
    }
}
