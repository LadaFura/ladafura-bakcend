package com.pharmacopee.ladafura.services.interfaces;

import java.util.Collection;
import java.util.List;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;

/**
 * Met en place ce dont toute pharmacopée a besoin pour fonctionner : gérant, abonnement gratuit et comptes de trésorerie.
 * S'exécute obligatoirement dans la transaction de l'appelant.
 */
public interface IPharmacopeeInitialisationService {

    /**
     * @param proprietaire gérant de la pharmacopée, nul si l'administrateur la crée sans titulaire
     */
    void initialiser(Pharmacopee pharmacopee, Utilisateur proprietaire);

    /** Fait de cet utilisateur le gérant ; l'ancien gérant éventuel reste membre avec le rôle MANAGER. */
    void definirProprietaire(Pharmacopee pharmacopee, Utilisateur proprietaire);

    /** Aligne les adhésions sur les praticiens rattachés par l'administrateur, sans toucher aux autres membres. */
    void synchroniserPraticiens(Pharmacopee pharmacopee, List<Utilisateur> rattaches, Collection<Long> detaches);
}
