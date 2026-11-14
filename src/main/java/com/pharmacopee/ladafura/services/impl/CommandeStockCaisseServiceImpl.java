package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.SessionCaisseRepository;
import com.pharmacopee.ladafura.services.interfaces.ICommandeStockCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService.OrigineMouvement;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService.NouvelleOperation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class CommandeStockCaisseServiceImpl implements ICommandeStockCaisseService {

    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final OperationCaisseRepository operationCaisseRepository;
    private final CaisseRepository caisseRepository;
    private final SessionCaisseRepository sessionCaisseRepository;
    private final IPharmacopeeMouvementStockService mouvementStockService;
    private final IPharmacopeeOperationCaisseService operationCaisseService;

    @Override
    public void sortirStock(Commande commande) {
        Long pharmacopeeId = commande.getPharmacopee().getId();
        for (LigneCommande ligne : commande.getLignes()) {
            DisponibiliteProduit fiche = ficheDe(pharmacopeeId, ligne);
            mouvementStockService.sortir(pharmacopeeId, fiche.getId(), ligne.getQuantite(), TypeMouvementStock.SORTIE_COMMANDE,
                    OrigineMouvement.commande(ligne), motif(commande), null);
            if (fiche.getQuantiteStock() <= 0) {
                fiche.setDisponible(false);
            }
        }
    }

    @Override
    public void reintegrerStock(Commande commande, MembrePharmacopee auteur) {
        Long pharmacopeeId = commande.getPharmacopee().getId();
        for (LigneCommande ligne : commande.getLignes()) {
            List<MouvementStock> mouvements = mouvementStockRepository.findByLigneCommandeId(ligne.getId());
            DisponibiliteProduit fiche = ficheDe(pharmacopeeId, ligne);
            if (mouvements.isEmpty()) {
                mouvementStockService.entrer(pharmacopeeId, fiche.getId(), ligne.getQuantite(), TypeMouvementStock.ANNULATION_COMMANDE,
                        null, OrigineMouvement.commande(ligne), motif(commande), auteur);
            } else {
                mouvementStockService.annuler(mouvements, TypeMouvementStock.ANNULATION_COMMANDE, motif(commande), auteur);
            }
            fiche.setDisponible(true);
        }
    }

    @Override
    public void encaisser(Paiement paiement, Long caisseId, MembrePharmacopee auteur) {
        Commande commande = paiement.getCommande();
        Long pharmacopeeId = commande.getPharmacopee().getId();
        if (operationCaisseRepository.findFirstByPaiementIdAndNature(paiement.getId(), NatureOperationCaisse.PAIEMENT_COMMANDE).isPresent()) {
            throw new ConflictException("Ce paiement a déjà été encaissé.");
        }
        operationCaisseService.enregistrer(pharmacopeeId, NouvelleOperation.builder()
                .caisseId(resoudreCaisse(pharmacopeeId, paiement.getMethode(), caisseId))
                .sens(SensOperation.ENTREE)
                .nature(NatureOperationCaisse.PAIEMENT_COMMANDE)
                .montant(BigDecimal.valueOf(paiement.getMontant()))
                .methode(paiement.getMethode())
                .libelle("Paiement de la commande " + commande.getNumero())
                .auteur(auteur)
                .paiement(paiement)
                .build());
    }

    @Override
    public void annulerEncaissement(Commande commande, MembrePharmacopee auteur) {
        Paiement paiement = commande.getPaiement();
        if (paiement == null) {
            return;
        }
        operationCaisseRepository.findFirstByPaiementIdAndNature(paiement.getId(), NatureOperationCaisse.PAIEMENT_COMMANDE)
                .ifPresent(operation -> operationCaisseService.contrePasser(commande.getPharmacopee().getId(), operation.getId(),
                        "Annulation de la commande " + commande.getNumero(), auteur));
        paiement.setStatut(StatutPaiement.ANNULE);
    }

    private DisponibiliteProduit ficheDe(Long pharmacopeeId, LigneCommande ligne) {
        Long produitId = ligne.getProduit().getId();
        return disponibiliteProduitRepository.findByPharmacopeeIdAndProduitId(pharmacopeeId, produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));
    }

    private static String motif(Commande commande) {
        return "Commande " + commande.getNumero();
    }

    private Long resoudreCaisse(Long pharmacopeeId, MethodePaiement methode, Long caisseId) {
        return switch (methode) {
            case CASH -> caisseId != null ? caisseId : uniqueCaisseOuverte(pharmacopeeId);
            case MOBILE_MONEY -> caisseId != null ? caisseId : compteActif(pharmacopeeId, TypeCaisse.MOBILE_MONEY);
            case CARTE_BANCAIRE -> compteActif(pharmacopeeId, TypeCaisse.BANQUE);
        };
    }

    private Long compteActif(Long pharmacopeeId, TypeCaisse type) {
        return caisseRepository.findFirstByPharmacopeeIdAndTypeAndActifTrue(pharmacopeeId, type)
                .map(Caisse::getId)
                .orElseThrow(() -> new ConflictException("Cette pharmacopée n'accepte pas ce mode de paiement pour le moment."));
    }

    private Long uniqueCaisseOuverte(Long pharmacopeeId) {
        List<Long> ouvertes = caisseRepository.findByPharmacopeeIdAndActifTrue(pharmacopeeId).stream()
                .filter(caisse -> caisse.getType() == TypeCaisse.PHYSIQUE)
                .map(Caisse::getId)
                .filter(id -> sessionCaisseRepository.findFirstByCaisseIdAndStatut(id, StatutSessionCaisse.OUVERTE).isPresent())
                .toList();
        if (ouvertes.isEmpty()) {
            throw new ConflictException("Ouvrez une session de caisse avant d'encaisser des espèces.");
        }
        if (ouvertes.size() > 1) {
            throw new BadRequestException("Plusieurs caisses sont ouvertes : indiquez la caisse d'encaissement.");
        }
        return ouvertes.get(0);
    }
}
