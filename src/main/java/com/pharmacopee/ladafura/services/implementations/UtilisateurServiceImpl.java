package com.pharmacopee.ladafura.services.implementations;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Override
    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable"));
    }

    @Override
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur updateUtilisateur(
            Long id,
            Utilisateur utilisateur) {

        Utilisateur utilisateurExistant =
                utilisateurRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur introuvable"));

        utilisateurExistant.setNom(utilisateur.getNom());
        utilisateurExistant.setPrenom(utilisateur.getPrenom());
        utilisateurExistant.setEmail(utilisateur.getEmail());

        return utilisateurRepository.save(utilisateurExistant);
    }

    @Override
    public void deleteUtilisateur(Long id) {

        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }

        utilisateurRepository.deleteById(id);
    }
}