package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.SessionCaisseRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PharmacopeeOperationCaisseServiceImpl implements IPharmacopeeOperationCaisseService {

    private final CaisseRepository caisseRepository;
    private final SessionCaisseRepository sessionCaisseRepository;
    private final OperationCaisseRepository operationCaisseRepository;

    @Override
    public OperationCaisse enregistrer(Long pharmacopeeId, NouvelleOperation operation) {
        exigerAuPlusUneOrigine(operation);
        if (operation.montant() == null || operation.montant().signum() <= 0) {
            throw new BadRequestException("Le montant doit être strictement positif.");
        }
        Caisse caisse = verrouillerCaisse(pharmacopeeId, operation.caisseId());
        if (!Boolean.TRUE.equals(caisse.getActif())) {
            throw new BadRequestException("Cette caisse est désactivée.");
        }
        if (operation.methode() != null && typeRequis(operation.methode()) != caisse.getType()) {
            throw new BadRequestException("Le mode de paiement " + operation.methode()
                    + " n'est pas compatible avec une caisse de type " + caisse.getType() + ".");
        }
        SessionCaisse session = sessionPourEcriture(caisse);
        exigerSoldeSuffisant(session, operation.sens(), operation.montant());

        return operationCaisseRepository.save(OperationCaisse.builder()
                .pharmacopee(caisse.getPharmacopee())
                .caisse(caisse)
                .session(session)
                .effectuePar(operation.auteur())
                .paiementVente(operation.paiementVente())
                .paiement(operation.paiement())
                .reglementAchat(operation.reglementAchat())
                .depense(operation.depense())
                .sens(operation.sens())
                .nature(operation.nature())
                .montant(operation.montant())
                .libelle(operation.libelle())
                .build());
    }

    @Override
    public OperationCaisse contrePasser(Long pharmacopeeId, Long operationId, String libelle, MembrePharmacopee auteur) {
        OperationCaisse originale = operationCaisseRepository.findByIdAndPharmacopeeId(operationId, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("OperationCaisse", "id", operationId));
        if (originale.getNature() == NatureOperationCaisse.ANNULATION) {
            throw new BadRequestException("Une contre-passation ne peut pas être contre-passée.");
        }
        if (operationCaisseRepository.existsByOperationLieeIdAndNature(operationId, NatureOperationCaisse.ANNULATION)) {
            throw new ConflictException("Cette opération a déjà été contre-passée.");
        }
        Caisse caisse = verrouillerCaisse(pharmacopeeId, originale.getCaisse().getId());
        SessionCaisse session = sessionPourEcriture(caisse);
        SensOperation sensInverse = originale.getSens() == SensOperation.ENTREE ? SensOperation.SORTIE : SensOperation.ENTREE;
        exigerSoldeSuffisant(session, sensInverse, originale.getMontant());

        return operationCaisseRepository.save(OperationCaisse.builder()
                .pharmacopee(caisse.getPharmacopee())
                .caisse(caisse)
                .session(session)
                .effectuePar(auteur)
                .paiementVente(originale.getPaiementVente())
                .paiement(originale.getPaiement())
                .reglementAchat(originale.getReglementAchat())
                .depense(originale.getDepense())
                .operationLiee(originale)
                .sens(sensInverse)
                .nature(NatureOperationCaisse.ANNULATION)
                .montant(originale.getMontant())
                .libelle(libelle)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal soldeTheorique(Long pharmacopeeId, Long caisseId) {
        Caisse caisse = caisseRepository.findByIdAndPharmacopeeId(caisseId, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Caisse", "id", caisseId));
        if (!caisse.estPhysique()) {
            return operationCaisseRepository.soldeTheorique(caisseId);
        }
        return sessionCaisseRepository.findFirstByCaisseIdOrderByDateOuvertureDesc(caisseId)
                .map(session -> session.estOuverte() ? disponibleEnSession(session) : session.getMontantConstate())
                .orElse(BigDecimal.ZERO);
    }

    private Caisse verrouillerCaisse(Long pharmacopeeId, Long caisseId) {
        return caisseRepository.findByIdAndPharmacopeeIdForUpdate(caisseId, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Caisse", "id", caisseId));
    }

    private SessionCaisse sessionPourEcriture(Caisse caisse) {
        if (!caisse.estPhysique()) {
            return null;
        }
        return sessionCaisseRepository.findFirstByCaisseIdAndStatut(caisse.getId(), StatutSessionCaisse.OUVERTE)
                .orElseThrow(() -> new ConflictException("Aucune session n'est ouverte sur cette caisse."));
    }

    /** Le fond d'ouverture n'est pas une opération : il entre dans le disponible d'une session. */
    private void exigerSoldeSuffisant(SessionCaisse session, SensOperation sens, BigDecimal montant) {
        if (session == null || sens != SensOperation.SORTIE) {
            return;
        }
        if (disponibleEnSession(session).compareTo(montant) < 0) {
            throw new ConflictException("Solde insuffisant dans cette caisse.");
        }
    }

    private BigDecimal disponibleEnSession(SessionCaisse session) {
        return session.getFondOuverture().add(operationCaisseRepository.soldeSession(session.getId()));
    }

    private static TypeCaisse typeRequis(MethodePaiement methode) {
        return switch (methode) {
            case CASH -> TypeCaisse.PHYSIQUE;
            case MOBILE_MONEY -> TypeCaisse.MOBILE_MONEY;
            case CARTE_BANCAIRE -> TypeCaisse.BANQUE;
        };
    }

    private static void exigerAuPlusUneOrigine(NouvelleOperation operation) {
        long origines = Stream.of(operation.paiementVente(), operation.paiement(), operation.reglementAchat(), operation.depense())
                .filter(Objects::nonNull)
                .count();
        if (origines > 1) {
            throw new BadRequestException("Une opération de caisse ne peut avoir qu'une seule origine.");
        }
    }
}
