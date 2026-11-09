package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.admin.abonnement.ParametreResponse;

/** Paramètres de plateforme, modifiables sans redéploiement. */
public interface IAdminParametreService {

    List<ParametreResponse> getParametres();

    /** @throws com.pharmacopee.ladafura.exceptions.BadRequestException si la valeur ne respecte pas le type du paramètre */
    ParametreResponse modifier(String code, String valeur);
}
