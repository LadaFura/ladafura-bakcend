package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.ParametrePlateforme;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.abonnement.ParametreResponse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeValeurParametre;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.ParametrePlateformeRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminParametreService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminParametreServiceImpl implements IAdminParametreService {

    private final ParametrePlateformeRepository parametreRepository;
    private final IJournalAuditService journalAuditService;
    private final IPharmacopeeAuthService pharmacopeeAuthService;

    @Override
    @Transactional(readOnly = true)
    public List<ParametreResponse> getParametres() {
        return parametreRepository.findAll().stream().map(AdminParametreServiceImpl::versReponse).toList();
    }

    @Override
    public ParametreResponse modifier(String code, String valeur) {
        ParametrePlateforme parametre = parametreRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Paramètre", "code", code));
        String normalisee = valeur.trim();
        exigerValeurValide(parametre.getTypeValeur(), normalisee);
        String ancienne = parametre.getValeur();
        Utilisateur administrateur = pharmacopeeAuthService.getCurrentUtilisateur();
        parametre.setValeur(normalisee);
        parametre.setModifiePar(administrateur);
        parametre.setDateModification(LocalDateTime.now());
        journalAuditService.tracer(EvenementAudit.builder()
                .action(TypeActionAudit.PARAMETRE_MODIFIE)
                .origine(OrigineAction.ADMINISTRATION)
                .utilisateur(administrateur)
                .typeEntite("ParametrePlateforme")
                .entiteId(parametre.getId())
                .ancienneValeur(ancienne)
                .nouvelleValeur(normalisee)
                .description(code)
                .build());
        return versReponse(parametre);
    }

    private static void exigerValeurValide(TypeValeurParametre type, String valeur) {
        boolean valide = switch (type) {
            case ENTIER -> valeur.matches("\\d+");
            case DECIMAL -> estDecimalPositif(valeur);
            case BOOLEEN -> valeur.equals("true") || valeur.equals("false");
            case TEXTE -> true;
        };
        if (!valide) {
            throw new BadRequestException("La valeur « " + valeur + " » n'est pas valide pour un paramètre de type " + type + ".");
        }
    }

    private static boolean estDecimalPositif(String valeur) {
        try {
            return new BigDecimal(valeur).signum() >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static ParametreResponse versReponse(ParametrePlateforme p) {
        return ParametreResponse.builder().code(p.getCode()).valeur(p.getValeur()).typeValeur(p.getTypeValeur())
                .description(p.getDescription()).dateModification(p.getDateModification()).build();
    }
}
