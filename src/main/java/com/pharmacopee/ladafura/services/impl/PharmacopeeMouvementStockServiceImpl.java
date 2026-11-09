package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LotRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PharmacopeeMouvementStockServiceImpl implements IPharmacopeeMouvementStockService {

    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final LotRepository lotRepository;
    private final MouvementStockRepository mouvementStockRepository;

    @Override
    public MouvementStock entrer(Long pharmacopeeId, Long disponibiliteId, int quantite, TypeMouvementStock type,
                                 Lot lot, OrigineMouvement origine, String motif, MembrePharmacopee auteur) {
        exigerQuantitePositive(quantite);
        DisponibiliteProduit fiche = verrouillerFiche(pharmacopeeId, disponibiliteId);
        int stockAvant = stockDe(fiche);
        fiche.setQuantiteStock(stockAvant + quantite);
        return tracer(fiche, lot, type, quantite, stockAvant, origine, motif, auteur);
    }

    @Override
    public List<MouvementStock> sortir(Long pharmacopeeId, Long disponibiliteId, int quantite, TypeMouvementStock type,
                                       OrigineMouvement origine, String motif, MembrePharmacopee auteur) {
        exigerQuantitePositive(quantite);
        DisponibiliteProduit fiche = verrouillerFiche(pharmacopeeId, disponibiliteId);
        int stock = stockDe(fiche);
        List<Lot> lotsVendables = lotRepository.findLotsVendablesForUpdate(disponibiliteId, LocalDate.now());
        // Les lots périmés restent comptés ici : leurs unités ne doivent pas passer pour du stock sans lot.
        int stockSansLot = Math.max(0, stock - lotRepository.sumQuantiteRestante(disponibiliteId));
        int vendable = stockSansLot + lotsVendables.stream().mapToInt(Lot::getQuantiteRestante).sum();
        if (quantite > vendable) {
            throw new ConflictException("Stock insuffisant : " + vendable + " unité(s) vendable(s) pour " + quantite + " demandée(s).");
        }

        List<MouvementStock> mouvements = new ArrayList<>();
        int reste = quantite;
        int stockCourant = stock;
        for (Lot lot : lotsVendables) {
            if (reste == 0) {
                break;
            }
            int preleve = Math.min(reste, lot.getQuantiteRestante());
            lot.setQuantiteRestante(lot.getQuantiteRestante() - preleve);
            mouvements.add(tracer(fiche, lot, type, -preleve, stockCourant, origine, motif, auteur));
            stockCourant -= preleve;
            reste -= preleve;
        }
        if (reste > 0) {
            mouvements.add(tracer(fiche, null, type, -reste, stockCourant, origine, motif, auteur));
            stockCourant -= reste;
        }
        fiche.setQuantiteStock(stockCourant);
        return mouvements;
    }

    @Override
    public List<MouvementStock> annuler(List<MouvementStock> mouvements, TypeMouvementStock typeAnnulation,
                                        String motif, MembrePharmacopee auteur) {
        List<MouvementStock> inverses = new ArrayList<>();
        for (MouvementStock mouvement : mouvements) {
            inverses.add(inverser(mouvement, typeAnnulation, motif, auteur));
        }
        return inverses;
    }

    private MouvementStock inverser(MouvementStock mouvement, TypeMouvementStock type, String motif, MembrePharmacopee auteur) {
        DisponibiliteProduit fiche = verrouillerFiche(mouvement.getPharmacopee().getId(), mouvement.getDisponibiliteProduit().getId());
        int variation = -mouvement.getQuantite();
        int stockAvant = stockDe(fiche);
        if (stockAvant + variation < 0) {
            throw new ConflictException("Annulation impossible : ce stock a déjà été consommé.");
        }
        Lot lot = mouvement.getLot();
        if (lot != null) {
            int restante = lot.getQuantiteRestante() + variation;
            if (restante < 0) {
                throw new ConflictException("Annulation impossible : ce lot a déjà été consommé.");
            }
            lot.setQuantiteRestante(restante);
        }
        fiche.setQuantiteStock(stockAvant + variation);
        OrigineMouvement origine = new OrigineMouvement(mouvement.getLigneAchat(), mouvement.getLigneVente(), mouvement.getLigneCommande());
        return tracer(fiche, lot, type, variation, stockAvant, origine, motif, auteur);
    }

    private DisponibiliteProduit verrouillerFiche(Long pharmacopeeId, Long disponibiliteId) {
        return disponibiliteProduitRepository.findByIdAndPharmacopeeIdForUpdate(disponibiliteId, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "id", disponibiliteId));
    }

    private MouvementStock tracer(DisponibiliteProduit fiche, Lot lot, TypeMouvementStock type, int quantite, int stockAvant,
                                  OrigineMouvement origine, String motif, MembrePharmacopee auteur) {
        return mouvementStockRepository.save(MouvementStock.builder()
                .pharmacopee(fiche.getPharmacopee())
                .disponibiliteProduit(fiche)
                .lot(lot)
                .ligneAchat(origine != null ? origine.ligneAchat() : null)
                .ligneVente(origine != null ? origine.ligneVente() : null)
                .ligneCommande(origine != null ? origine.ligneCommande() : null)
                .type(type)
                .quantite(quantite)
                .stockAvant(stockAvant)
                .stockApres(stockAvant + quantite)
                .motif(motif)
                .effectuePar(auteur)
                .build());
    }

    private static int stockDe(DisponibiliteProduit fiche) {
        return fiche.getQuantiteStock() != null ? fiche.getQuantiteStock() : 0;
    }

    private static void exigerQuantitePositive(int quantite) {
        if (quantite <= 0) {
            throw new BadRequestException("La quantité doit être strictement positive.");
        }
    }
}
