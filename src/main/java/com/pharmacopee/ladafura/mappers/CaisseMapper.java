package com.pharmacopee.ladafura.mappers;

import java.math.BigDecimal;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.Depense;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.SessionCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.DepenseResponse;

/** Conversions entité vers DTO des caisses et des dépenses, sans accès à la base. */
public final class CaisseMapper {

    private CaisseMapper() {
    }

    public static CaisseResponse caisse(Caisse c, BigDecimal solde, Long sessionOuverteId) {
        return CaisseResponse.builder().id(c.getId()).nom(c.getNom()).type(c.getType()).referenceCompte(c.getReferenceCompte())
                .description(c.getDescription()).actif(c.getActif()).dateCreation(c.getDateCreation())
                .soldeTheorique(solde).sessionOuverteId(sessionOuverteId).build();
    }

    public static SessionCaisseResponse session(SessionCaisse s) {
        return SessionCaisseResponse.builder().id(s.getId()).caisseId(s.getCaisse().getId()).statut(s.getStatut())
                .dateOuverture(s.getDateOuverture()).fondOuverture(s.getFondOuverture()).dateCloture(s.getDateCloture())
                .montantTheorique(s.getMontantTheorique()).montantConstate(s.getMontantConstate()).ecart(s.getEcart())
                .commentaire(s.getCommentaire()).build();
    }

    public static OperationCaisseResponse operation(OperationCaisse o) {
        return OperationCaisseResponse.builder().id(o.getId()).caisseId(o.getCaisse().getId())
                .sessionId(o.getSession() != null ? o.getSession().getId() : null).sens(o.getSens()).nature(o.getNature())
                .montant(o.getMontant()).libelle(o.getLibelle()).dateOperation(o.getDateOperation())
                .operationLieeId(o.getOperationLiee() != null ? o.getOperationLiee().getId() : null).build();
    }

    public static DepenseResponse depense(Depense d) {
        return DepenseResponse.builder().id(d.getId()).libelle(d.getLibelle()).categorie(d.getCategorie()).montant(d.getMontant())
                .dateDepense(d.getDateDepense()).beneficiaire(d.getBeneficiaire()).justificatifUrl(d.getJustificatifUrl())
                .observation(d.getObservation()).statut(d.getStatut()).dateCreation(d.getDateCreation())
                .dateAnnulation(d.getDateAnnulation()).motifAnnulation(d.getMotifAnnulation()).build();
    }
}
