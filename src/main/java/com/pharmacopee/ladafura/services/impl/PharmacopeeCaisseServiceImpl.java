package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CloturerSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.EnregistrerCaisseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationManuelleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OuvrirSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.SessionCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.TransfertCaisseRequest;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.CaisseMapper;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.SessionCaisseRepository;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService.NouvelleOperation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeCaisseServiceImpl implements IPharmacopeeCaisseService {

    private static final String TYPE_SESSION = "SessionCaisse";

    private final IPharmacopeeContexteService contexteService;
    private final IPharmacopeeOperationCaisseService operationCaisseService;
    private final IJournalAuditService journalAuditService;
    private final CaisseRepository caisseRepository;
    private final SessionCaisseRepository sessionRepository;
    private final OperationCaisseRepository operationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CaisseResponse> getCaisses() {
        Long pharmacopeeId = pharmacopeeEnLecture();
        return caisseRepository.findByPharmacopeeIdOrderByNomAsc(pharmacopeeId).stream().map(c -> versReponse(pharmacopeeId, c)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CaisseResponse getCaisse(Long id) {
        Long pharmacopeeId = pharmacopeeEnLecture();
        return versReponse(pharmacopeeId, caisseDe(pharmacopeeId, id));
    }

    @Override
    public CaisseResponse creer(EnregistrerCaisseRequest request) {
        ContexteMembre contexte = ecriture(IPharmacopeeContexteService.CAISSE_GERER);
        Caisse caisse = caisseRepository.save(Caisse.builder()
                .pharmacopee(contexte.pharmacopee())
                .nom(request.getNom().trim())
                .type(request.getType())
                .referenceCompte(request.getReferenceCompte())
                .description(request.getDescription())
                .build());
        return CaisseMapper.caisse(caisse, BigDecimal.ZERO, null);
    }

    @Override
    public CaisseResponse modifier(Long id, EnregistrerCaisseRequest request) {
        Long pharmacopeeId = ecriture(IPharmacopeeContexteService.CAISSE_GERER).pharmacopee().getId();
        Caisse caisse = caisseDe(pharmacopeeId, id);
        caisse.setNom(request.getNom().trim());
        caisse.setReferenceCompte(request.getReferenceCompte());
        caisse.setDescription(request.getDescription());
        return versReponse(pharmacopeeId, caisse);
    }

    @Override
    public CaisseResponse changerActivation(Long id, boolean actif) {
        Long pharmacopeeId = ecriture(IPharmacopeeContexteService.CAISSE_GERER).pharmacopee().getId();
        Caisse caisse = caisseDe(pharmacopeeId, id);
        if (!actif && sessionOuverte(caisse).isPresent()) {
            throw new ConflictException("Clôturez la session ouverte avant de désactiver cette caisse.");
        }
        caisse.setActif(actif);
        return versReponse(pharmacopeeId, caisse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionCaisseResponse> getSessions(Long caisseId, Pageable pageable) {
        Caisse caisse = caisseDe(pharmacopeeEnLecture(), caisseId);
        return sessionRepository.findByCaisseIdOrderByDateOuvertureDesc(caisse.getId(), pageable).map(CaisseMapper::session);
    }

    @Override
    public SessionCaisseResponse ouvrirSession(Long caisseId, OuvrirSessionRequest request) {
        ContexteMembre contexte = ecriture(IPharmacopeeContexteService.CAISSE_OUVRIR_FERMER);
        Caisse caisse = verrouiller(contexte.pharmacopee().getId(), caisseId);
        if (!caisse.estPhysique()) {
            throw new BadRequestException("Seule une caisse physique a des sessions.");
        }
        if (!Boolean.TRUE.equals(caisse.getActif())) {
            throw new BadRequestException("Cette caisse est désactivée.");
        }
        if (sessionOuverte(caisse).isPresent()) {
            throw new ConflictException("Une session est déjà ouverte sur cette caisse.");
        }
        Optional<BigDecimal> dernierConstate = sessionRepository.findFirstByCaisseIdOrderByDateOuvertureDesc(caisse.getId())
                .map(SessionCaisse::getMontantConstate);
        BigDecimal fond = request.getFondOuverture() != null ? request.getFondOuverture() : dernierConstate.orElse(BigDecimal.ZERO);
        boolean ecartAvecCloture = dernierConstate.isPresent() && dernierConstate.get().compareTo(fond) != 0;
        if (ecartAvecCloture && estVide(request.getCommentaire())) {
            throw new BadRequestException("Le fond diffère du montant de la dernière clôture (" + dernierConstate.get() + ") : un commentaire est obligatoire.");
        }
        SessionCaisse session = sessionRepository.saveAndFlush(SessionCaisse.builder()
                .caisse(caisse)
                .ouvertePar(contexte.membre())
                .fondOuverture(fond)
                .commentaire(request.getCommentaire())
                .build());
        if (ecartAvecCloture) {
            tracer(contexte, TypeActionAudit.ECART_CAISSE, TYPE_SESSION, session.getId(), dernierConstate.get().toString(), fond.toString(),
                    "Fond d'ouverture différent de la dernière clôture : " + request.getCommentaire());
        }
        return CaisseMapper.session(session);
    }

    @Override
    public SessionCaisseResponse cloturerSession(Long caisseId, CloturerSessionRequest request) {
        ContexteMembre contexte = ecriture(IPharmacopeeContexteService.CAISSE_OUVRIR_FERMER);
        Long pharmacopeeId = contexte.pharmacopee().getId();
        Caisse caisse = verrouiller(pharmacopeeId, caisseId);
        SessionCaisse session = sessionOuverte(caisse)
                .orElseThrow(() -> new ConflictException("Aucune session n'est ouverte sur cette caisse."));
        BigDecimal theorique = session.getFondOuverture().add(operationRepository.soldeSession(session.getId()));
        BigDecimal constate = request.getMontantConstate();
        BigDecimal ecart = constate.subtract(theorique);
        if (ecart.signum() != 0) {
            if (estVide(request.getCommentaire())) {
                throw new BadRequestException("Un écart de " + ecart + " est constaté : un commentaire est obligatoire.");
            }
            ajuster(pharmacopeeId, caisse, ecart, contexte);
            tracer(contexte, TypeActionAudit.ECART_CAISSE, TYPE_SESSION, session.getId(), theorique.toString(), constate.toString(), request.getCommentaire());
        }
        session.cloturer(theorique, constate, contexte.membre());
        session.setCommentaire(fusionner(session.getCommentaire(), request.getCommentaire()));
        tracer(contexte, TypeActionAudit.SESSION_CAISSE_CLOTUREE, TYPE_SESSION, session.getId(), null, constate.toString(), "Clôture de session");
        return CaisseMapper.session(session);
    }

    private void ajuster(Long pharmacopeeId, Caisse caisse, BigDecimal ecart, ContexteMembre contexte) {
        operationCaisseService.enregistrer(pharmacopeeId, NouvelleOperation.builder()
                .caisseId(caisse.getId())
                .sens(ecart.signum() > 0 ? SensOperation.ENTREE : SensOperation.SORTIE)
                .nature(NatureOperationCaisse.AJUSTEMENT)
                .montant(ecart.abs())
                .libelle("Écart constaté à la clôture")
                .auteur(contexte.membre())
                .build());
    }

    private static String fusionner(String ouverture, String cloture) {
        if (estVide(ouverture)) {
            return cloture;
        }
        return estVide(cloture) ? ouverture : ouverture + " | " + cloture;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OperationCaisseResponse> getOperations(Long caisseId, Pageable pageable) {
        Caisse caisse = caisseDe(pharmacopeeEnLecture(), caisseId);
        return operationRepository.findByCaisseIdOrderByDateOperationDesc(caisse.getId(), pageable).map(CaisseMapper::operation);
    }

    @Override
    public OperationCaisseResponse enregistrerOperation(Long caisseId, OperationManuelleRequest request) {
        ContexteMembre contexte = ecriture(IPharmacopeeContexteService.CAISSE_GERER);
        OperationCaisse operation = operationCaisseService.enregistrer(contexte.pharmacopee().getId(), NouvelleOperation.builder()
                .caisseId(caisseId)
                .sens(sensManuel(request.getNature()))
                .nature(request.getNature())
                .montant(request.getMontant())
                .libelle(request.getLibelle())
                .auteur(contexte.membre())
                .build());
        return CaisseMapper.operation(operation);
    }

    private static SensOperation sensManuel(NatureOperationCaisse nature) {
        return switch (nature) {
            case APPORT, RECETTE_DIVERSE -> SensOperation.ENTREE;
            case RETRAIT -> SensOperation.SORTIE;
            case VENTE, PAIEMENT_COMMANDE, ACHAT, DEPENSE, TRANSFERT, AJUSTEMENT, ANNULATION ->
                    throw new BadRequestException("Cette nature d'opération ne peut pas être saisie manuellement.");
        };
    }

    @Override
    public List<OperationCaisseResponse> transferer(TransfertCaisseRequest request) {
        if (request.getCaisseSourceId().equals(request.getCaisseDestinationId())) {
            throw new BadRequestException("La caisse source et la caisse de destination doivent être différentes.");
        }
        ContexteMembre contexte = ecriture(IPharmacopeeContexteService.CAISSE_GERER);
        Long pharmacopeeId = contexte.pharmacopee().getId();
        // Toujours le même ordre de verrouillage, pour que deux transferts croisés ne s'interbloquent pas.
        verrouiller(pharmacopeeId, Math.min(request.getCaisseSourceId(), request.getCaisseDestinationId()));
        verrouiller(pharmacopeeId, Math.max(request.getCaisseSourceId(), request.getCaisseDestinationId()));

        OperationCaisse sortie = operationCaisseService.enregistrer(pharmacopeeId, transfert(request.getCaisseSourceId(), SensOperation.SORTIE, request, contexte, null));
        OperationCaisse entree = operationCaisseService.enregistrer(pharmacopeeId, transfert(request.getCaisseDestinationId(), SensOperation.ENTREE, request, contexte, sortie));
        return List.of(CaisseMapper.operation(sortie), CaisseMapper.operation(entree));
    }

    private static NouvelleOperation transfert(Long caisseId, SensOperation sens, TransfertCaisseRequest request, ContexteMembre contexte, OperationCaisse liee) {
        return NouvelleOperation.builder()
                .caisseId(caisseId)
                .sens(sens)
                .nature(NatureOperationCaisse.TRANSFERT)
                .montant(request.getMontant())
                .libelle(request.getLibelle())
                .auteur(contexte.membre())
                .operationLiee(liee)
                .build();
    }

    @Override
    public OperationCaisseResponse annulerOperation(Long operationId, String motif) {
        ContexteMembre contexte = ecriture(IPharmacopeeContexteService.CAISSE_GERER);
        Long pharmacopeeId = contexte.pharmacopee().getId();
        OperationCaisse operation = operationRepository.findByIdAndPharmacopeeId(operationId, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération de caisse", "id", operationId));
        if (operation.getPaiementVente() != null || operation.getPaiement() != null
                || operation.getReglementAchat() != null || operation.getDepense() != null) {
            throw new ConflictException("Cette opération vient d'un paiement ou d'une dépense : annulez-la depuis son origine.");
        }
        NatureOperationCaisse nature = operation.getNature();
        if (nature == NatureOperationCaisse.TRANSFERT) {
            throw new ConflictException("Un transfert se corrige par un transfert inverse.");
        }
        if (nature == NatureOperationCaisse.AJUSTEMENT || nature == NatureOperationCaisse.ANNULATION) {
            throw new ConflictException("Cette opération ne peut pas être annulée.");
        }
        OperationCaisse inverse = operationCaisseService.contrePasser(pharmacopeeId, operationId, "Annulation : " + motif, contexte.membre());
        tracer(contexte, TypeActionAudit.OPERATION_CAISSE_ANNULEE, "OperationCaisse", operationId, null, null, motif);
        return CaisseMapper.operation(inverse);
    }

    private ContexteMembre ecriture(String permission) {
        return contexteService.verifierOperationCommerciale(permission);
    }

    private Long pharmacopeeEnLecture() {
        return contexteService.verifierPermission(IPharmacopeeContexteService.CAISSE_CONSULTER).pharmacopee().getId();
    }

    private Caisse caisseDe(Long pharmacopeeId, Long id) {
        return caisseRepository.findByIdAndPharmacopeeId(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Caisse", "id", id));
    }

    private Caisse verrouiller(Long pharmacopeeId, Long id) {
        return caisseRepository.findByIdAndPharmacopeeIdForUpdate(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Caisse", "id", id));
    }

    private Optional<SessionCaisse> sessionOuverte(Caisse caisse) {
        return sessionRepository.findFirstByCaisseIdAndStatut(caisse.getId(), StatutSessionCaisse.OUVERTE);
    }

    private CaisseResponse versReponse(Long pharmacopeeId, Caisse caisse) {
        return CaisseMapper.caisse(caisse, operationCaisseService.soldeTheorique(pharmacopeeId, caisse.getId()),
                sessionOuverte(caisse).map(SessionCaisse::getId).orElse(null));
    }

    private static boolean estVide(String texte) {
        return texte == null || texte.isBlank();
    }

    private void tracer(ContexteMembre contexte, TypeActionAudit action, String typeEntite, Long entiteId, String ancienne, String nouvelle, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.PHARMACOPEE)
                .pharmacopee(contexte.pharmacopee())
                .utilisateur(contexte.membre().getUtilisateur())
                .typeEntite(typeEntite)
                .entiteId(entiteId)
                .ancienneValeur(ancienne)
                .nouvelleValeur(nouvelle)
                .description(description)
                .build());
    }
}
