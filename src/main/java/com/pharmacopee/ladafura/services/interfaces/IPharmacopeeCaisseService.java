package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CloturerSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.EnregistrerCaisseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationManuelleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OuvrirSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.SessionCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.TransfertCaisseRequest;

/** Caisses, sessions et opérations manuelles de la pharmacopée courante. */
public interface IPharmacopeeCaisseService {

    List<CaisseResponse> getCaisses();

    CaisseResponse getCaisse(Long id);

    CaisseResponse creer(EnregistrerCaisseRequest request);

    CaisseResponse modifier(Long id, EnregistrerCaisseRequest request);

    CaisseResponse changerActivation(Long id, boolean actif);

    Page<SessionCaisseResponse> getSessions(Long caisseId, Pageable pageable);

    /** Le fond reprend le montant de la dernière clôture ; une autre valeur doit être justifiée. */
    SessionCaisseResponse ouvrirSession(Long caisseId, OuvrirSessionRequest request);

    /** Un écart doit être justifié ; il est réaligné par une opération d'ajustement. */
    SessionCaisseResponse cloturerSession(Long caisseId, CloturerSessionRequest request);

    Page<OperationCaisseResponse> getOperations(Long caisseId, Pageable pageable);

    OperationCaisseResponse enregistrerOperation(Long caisseId, OperationManuelleRequest request);

    List<OperationCaisseResponse> transferer(TransfertCaisseRequest request);

    OperationCaisseResponse annulerOperation(Long operationId, String motif);
}
