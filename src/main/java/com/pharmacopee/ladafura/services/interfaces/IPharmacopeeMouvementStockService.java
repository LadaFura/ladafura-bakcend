package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.Models.LigneAchat;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.LigneVente;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;

/**
 * Seul point d'écriture du stock pour les modules SaaS. Chaque variation est tracée par un
 * {@link MouvementStock} immuable. S'exécute obligatoirement dans la transaction de l'appelant.
 */
public interface IPharmacopeeMouvementStockService {

    /** Ligne à l'origine d'un mouvement ; au plus une est renseignée. */
    record OrigineMouvement(LigneAchat ligneAchat, LigneVente ligneVente, LigneCommande ligneCommande) {

        public static OrigineMouvement achat(LigneAchat ligne) {
            return new OrigineMouvement(ligne, null, null);
        }

        public static OrigineMouvement vente(LigneVente ligne) {
            return new OrigineMouvement(null, ligne, null);
        }

        public static OrigineMouvement commande(LigneCommande ligne) {
            return new OrigineMouvement(null, null, ligne);
        }
    }

    /**
     * Ajoute une quantité au stock. Le lot éventuel a déjà été créé par l'appelant avec sa quantité.
     *
     * @throws com.pharmacopee.ladafura.exceptions.BadRequestException si la quantité n'est pas positive
     * @throws com.pharmacopee.ladafura.exceptions.ResourceNotFoundException si la fiche n'appartient pas à la pharmacopée
     */
    MouvementStock entrer(Long pharmacopeeId, Long disponibiliteId, int quantite, TypeMouvementStock type,
                          Lot lot, OrigineMouvement origine, String motif, MembrePharmacopee auteur);

    /**
     * Retire une quantité : d'abord les lots non périmés les plus proches de la péremption, puis le stock
     * non suivi par lot. Un mouvement par lot puisé. Un lot périmé n'est jamais utilisé.
     *
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si la quantité vendable est insuffisante
     */
    List<MouvementStock> sortir(Long pharmacopeeId, Long disponibiliteId, int quantite, TypeMouvementStock type,
                                OrigineMouvement origine, String motif, MembrePharmacopee auteur);

    /**
     * Crée les mouvements inverses, vers les mêmes lots.
     *
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si le stock ou le lot a déjà été consommé
     */
    List<MouvementStock> annuler(List<MouvementStock> mouvements, TypeMouvementStock typeAnnulation,
                                 String motif, MembrePharmacopee auteur);
}
