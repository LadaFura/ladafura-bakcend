package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.Utilisateur;
import java.util.List;

public interface UtilisateurService {
    List<Utilisateur> getAllUtilisateurs();

    Utilisateur getUtilisateurById(Long id);

    Utilisateur createUtilisateur(Utilisateur utilisateur);

    Utilisateur updateUtilisateur(Long id, Utilisateur utilisateur);

    void deleteUtilisateur(Long id);

}
