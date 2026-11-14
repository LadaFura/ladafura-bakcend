package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.AttributionRolePharmacopee;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.CreerMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InviterMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.MembreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.ModifierRolesMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.TransfertProprieteRequest;
import com.pharmacopee.ladafura.enums.MotifSuspensionMembre;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.QuotaExceededException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMembreServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeMembreServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Long ROLE_ID = 20L;

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private IPharmacopeeQuotaService quotaService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private IPharmacopeeAuthService pharmacopeeAuthService;
    @Mock private IFirebaseAuthService firebaseAuthService;
    @Mock private MembrePharmacopeeRepository membreRepository;
    @Mock private AttributionRolePharmacopeeRepository attributionRepository;
    @Mock private RolePharmacopeeRepository roleRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private PharmacopeeRepository pharmacopeeRepository;

    @InjectMocks private PharmacopeeMembreServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine de Bamako").build();
    private final Utilisateur gerant = utilisateur(1L, "gerant@test.ml");
    private final MembrePharmacopee membreGerant = MembrePharmacopee.builder().id(100L).utilisateur(gerant)
            .pharmacopee(pharmacopee).statut(StatutMembre.ACTIF).estProprietaire(true).build();
    private final RolePharmacopee vendeur = RolePharmacopee.builder().id(ROLE_ID).code("VENDEUR_CAISSIER").libelle("Vendeur-caissier").systeme(true).actif(true).build();
    private final ContexteMembre contexte = new ContexteMembre(membreGerant, pharmacopee, Set.of());

    @BeforeEach
    void preparer() {
        lenient().when(contexteService.verifierPermission(anyString())).thenReturn(contexte);
        lenient().when(membreRepository.save(any(MembrePharmacopee.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(membreRepository.saveAndFlush(any(MembrePharmacopee.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(attributionRepository.findByMembrePharmacopeeId(any())).thenReturn(List.of());
        lenient().when(roleRepository.findDisponibleByIdPourPharmacopee(ROLE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(vendeur));
    }

    private static Utilisateur utilisateur(Long id, String email) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setNom("Traoré");
        u.setPrenom("Awa");
        u.setEmail(email);
        u.setTelephone("+223 70 00 00 00");
        return u;
    }

    private MembrePharmacopee membre(Long id, StatutMembre statut) {
        MembrePharmacopee m = MembrePharmacopee.builder().id(id).utilisateur(utilisateur(id, "membre" + id + "@test.ml"))
                .pharmacopee(pharmacopee).statut(statut).estProprietaire(false).build();
        lenient().when(membreRepository.findByIdAndPharmacopeeId(id, PHARMACOPEE_ID)).thenReturn(Optional.of(m));
        return m;
    }

    private static InviterMembreRequest invitationParEmail(String email) {
        return InviterMembreRequest.builder().email(email).roleIds(List.of(ROLE_ID)).build();
    }

    @Test
    @DisplayName("invitation d'un compte existant : INVITE, 7 jours, identité masquée")
    void invitation() {
        Utilisateur invite = utilisateur(2L, "awa@gmail.com");
        when(utilisateurRepository.findByEmail("awa@gmail.com")).thenReturn(Optional.of(invite));
        when(membreRepository.findByUtilisateurIdAndPharmacopeeId(2L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        MembreResponse reponse = service.inviter(invitationParEmail("awa@gmail.com"));

        assertThat(reponse.getStatut()).isEqualTo(StatutMembre.INVITE);
        assertThat(reponse.getDateExpirationInvitation()).isAfter(reponse.getDateInvitation().plusDays(6));
        assertThat(reponse.getContactMasque()).isEqualTo("a***@gmail.com");
        assertThat(reponse.getNom()).isNull();
        assertThat(reponse.getEmail()).isNull();
        assertThat(reponse.getTelephone()).isNull();
        verify(attributionRepository).save(any(AttributionRolePharmacopee.class));
    }

    @Test
    @DisplayName("sans compte correspondant, 404 propose la création")
    void invitationSansCompte() {
        when(utilisateurRepository.findByEmail("inconnu@test.ml")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.inviter(invitationParEmail("inconnu@test.ml")))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("créer un compte");
    }

    @Test
    @DisplayName("il faut l'email ou le téléphone, pas les deux")
    void invitationDeuxContacts() {
        InviterMembreRequest request = InviterMembreRequest.builder().email("a@test.ml").telephone("70").roleIds(List.of(ROLE_ID)).build();

        assertThatThrownBy(() -> service.inviter(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un téléphone partagé par plusieurs comptes donne 409")
    void telephonePartage() {
        when(utilisateurRepository.findByTelephone("70")).thenReturn(List.of(utilisateur(2L, "a@t.ml"), utilisateur(3L, "b@t.ml")));
        InviterMembreRequest request = InviterMembreRequest.builder().telephone("70").roleIds(List.of(ROLE_ID)).build();

        assertThatThrownBy(() -> service.inviter(request)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un membre déjà actif ne peut pas être réinvité")
    void dejaMembre() {
        Utilisateur invite = utilisateur(2L, "awa@gmail.com");
        when(utilisateurRepository.findByEmail("awa@gmail.com")).thenReturn(Optional.of(invite));
        when(membreRepository.findByUtilisateurIdAndPharmacopeeId(2L, PHARMACOPEE_ID))
                .thenReturn(Optional.of(MembrePharmacopee.builder().id(9L).statut(StatutMembre.ACTIF).build()));

        assertThatThrownBy(() -> service.inviter(invitationParEmail("awa@gmail.com"))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un ancien membre retiré retrouve sa ligne, sans ses anciens rôles")
    void ancienMembreReinvite() {
        Utilisateur invite = utilisateur(2L, "awa@gmail.com");
        MembrePharmacopee ancien = MembrePharmacopee.builder().id(9L).utilisateur(invite).pharmacopee(pharmacopee)
                .statut(StatutMembre.RETIRE).estProprietaire(false).dateDepart(java.time.LocalDateTime.now()).build();
        when(utilisateurRepository.findByEmail("awa@gmail.com")).thenReturn(Optional.of(invite));
        when(membreRepository.findByUtilisateurIdAndPharmacopeeId(2L, PHARMACOPEE_ID)).thenReturn(Optional.of(ancien));

        service.inviter(invitationParEmail("awa@gmail.com"));

        assertThat(ancien.getStatut()).isEqualTo(StatutMembre.INVITE);
        assertThat(ancien.getDateDepart()).isNull();
        verify(attributionRepository).flush();
    }

    @Test
    @DisplayName("le rôle GERANT n'est jamais attribuable")
    void gerantNonAttribuable() {
        RolePharmacopee roleGerant = RolePharmacopee.builder().id(21L).code("GERANT").systeme(true).actif(true).build();
        when(roleRepository.findDisponibleByIdPourPharmacopee(21L, PHARMACOPEE_ID)).thenReturn(Optional.of(roleGerant));

        assertThatThrownBy(() -> service.inviter(InviterMembreRequest.builder().email("a@t.ml").roleIds(List.of(21L)).build()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("un rôle d'une autre pharmacopée n'est pas trouvé")
    void roleAutrePharmacopee() {
        when(roleRepository.findDisponibleByIdPourPharmacopee(99L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.inviter(InviterMembreRequest.builder().email("a@t.ml").roleIds(List.of(99L)).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static CreerMembreRequest creation() {
        return CreerMembreRequest.builder().nom("Sissoko").prenom("Awa").email("awa@gmail.com").telephone("70")
                .motDePasse("secret12").roleIds(List.of(ROLE_ID)).build();
    }

    @Test
    @DisplayName("création directe : compte Firebase, membre ACTIF, aucun mot de passe en base")
    void creationDirecte() {
        when(utilisateurRepository.existsByEmail("awa@gmail.com")).thenReturn(false);
        when(firebaseAuthService.createUser("awa@gmail.com", "secret12", "Awa Sissoko")).thenReturn("uid-1");
        when(utilisateurRepository.saveAndFlush(any(Utilisateur.class))).thenAnswer(i -> i.getArgument(0));

        MembreResponse reponse = service.creer(creation());

        assertThat(reponse.getStatut()).isEqualTo(StatutMembre.ACTIF);
        verify(utilisateurRepository).saveAndFlush(Mockito.argThat(u -> u.getMotDePasse() == null && "uid-1".equals(u.getFirebaseUid())));
    }

    @Test
    @DisplayName("offre gratuite : le quota bloque la création avant Firebase")
    void quotaGratuit() {
        when(utilisateurRepository.existsByEmail("awa@gmail.com")).thenReturn(false);
        doThrow(new QuotaExceededException("limite")).when(quotaService)
                .verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS);

        assertThatThrownBy(() -> service.creer(creation())).isInstanceOf(QuotaExceededException.class);
        verify(firebaseAuthService, never()).createUser(any(), any(), any());
    }

    @Test
    @DisplayName("si l'enregistrement échoue, le compte Firebase est supprimé")
    void compensationFirebase() {
        when(utilisateurRepository.existsByEmail("awa@gmail.com")).thenReturn(false);
        when(firebaseAuthService.createUser(any(), any(), any())).thenReturn("uid-1");
        when(utilisateurRepository.saveAndFlush(any(Utilisateur.class))).thenThrow(new IllegalStateException("base"));

        assertThatThrownBy(() -> service.creer(creation())).isInstanceOf(IllegalStateException.class);
        verify(firebaseAuthService).deleteUser("uid-1");
    }

    @Test
    @DisplayName("un membre d'une autre pharmacopée n'est pas trouvé")
    void membreAutrePharmacopee() {
        when(membreRepository.findByIdAndPharmacopeeId(77L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMembre(77L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("une permission manquante arrête l'opération")
    void permissionManquante() {
        when(contexteService.verifierPermission(IPharmacopeeContexteService.MEMBRES_GERER)).thenThrow(new ForbiddenException("non"));

        assertThatThrownBy(() -> service.suspendre(10L)).isInstanceOf(ForbiddenException.class);
        verify(journalAuditService, never()).tracer(any());
    }

    @Test
    @DisplayName("un abonnement non actif interdit les écritures")
    void abonnementLectureSeule() {
        doThrow(new ForbiddenException("grâce")).when(contexteService).verifierEcritureAutorisee(PHARMACOPEE_ID);

        assertThatThrownBy(() -> service.suspendre(10L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("suspension : motif décision du gérant, tracée")
    void suspension() {
        MembrePharmacopee m = membre(10L, StatutMembre.ACTIF);

        service.suspendre(10L);

        assertThat(m.getStatut()).isEqualTo(StatutMembre.SUSPENDU);
        assertThat(m.getMotifSuspension()).isEqualTo(MotifSuspensionMembre.DECISION_GERANT);
        verify(journalAuditService).tracer(Mockito.argThat(e -> e.action() == TypeActionAudit.MEMBRE_SUSPENDU));
    }

    @Test
    @DisplayName("le propriétaire ne peut être ni suspendu, ni retiré, ni changer de rôles")
    void proprietaireProtege() {
        when(membreRepository.findByIdAndPharmacopeeId(100L, PHARMACOPEE_ID)).thenReturn(Optional.of(membreGerant));

        assertThatThrownBy(() -> service.suspendre(100L)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.retirer(100L)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.modifierRoles(100L, new ModifierRolesMembreRequest(List.of(ROLE_ID)))).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("réactivation : soumise au quota")
    void reactivationQuota() {
        membre(10L, StatutMembre.SUSPENDU);
        doThrow(new QuotaExceededException("limite")).when(quotaService)
                .verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS);

        assertThatThrownBy(() -> service.reactiver(10L)).isInstanceOf(QuotaExceededException.class);
    }

    @Test
    @DisplayName("réactivation : efface le motif")
    void reactivation() {
        MembrePharmacopee m = membre(10L, StatutMembre.SUSPENDU);
        m.setMotifSuspension(MotifSuspensionMembre.QUOTA_ABONNEMENT);

        service.reactiver(10L);

        assertThat(m.getStatut()).isEqualTo(StatutMembre.ACTIF);
        assertThat(m.getMotifSuspension()).isNull();
    }

    @Test
    @DisplayName("un membre suspendu par l'administrateur ne peut pas être réactivé par le gérant")
    void suspensionAdmin() {
        membre(10L, StatutMembre.SUSPENDU).setMotifSuspension(MotifSuspensionMembre.DECISION_ADMIN);

        assertThatThrownBy(() -> service.reactiver(10L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("retrait : statut RETIRE et date de départ")
    void retrait() {
        MembrePharmacopee m = membre(10L, StatutMembre.ACTIF);

        service.retirer(10L);

        assertThat(m.getStatut()).isEqualTo(StatutMembre.RETIRE);
        assertThat(m.getDateDepart()).isNotNull();
    }

    @Test
    @DisplayName("remplacement des rôles : seule la différence est écrite")
    void remplacementRoles() {
        MembrePharmacopee m = membre(10L, StatutMembre.ACTIF);
        RolePharmacopee comptable = RolePharmacopee.builder().id(22L).code("COMPTABLE").systeme(true).actif(true).build();
        AttributionRolePharmacopee ancienne = AttributionRolePharmacopee.builder().membrePharmacopee(m).rolePharmacopee(comptable).build();
        when(attributionRepository.findByMembrePharmacopeeId(10L)).thenReturn(List.of(ancienne));

        service.modifierRoles(10L, new ModifierRolesMembreRequest(List.of(ROLE_ID)));

        verify(attributionRepository).deleteAll(List.of(ancienne));
        verify(attributionRepository).save(Mockito.argThat(a -> a.getRolePharmacopee() == vendeur));
    }

    private void rolesDeTransfert() {
        when(roleRepository.findByCodeAndPharmacopeeIsNull("GERANT"))
                .thenReturn(Optional.of(RolePharmacopee.builder().id(1L).code("GERANT").build()));
        when(roleRepository.findByCodeAndPharmacopeeIsNull("MANAGER"))
                .thenReturn(Optional.of(RolePharmacopee.builder().id(2L).code("MANAGER").build()));
    }

    @Test
    @DisplayName("transfert : l'ancien propriétaire est écrit avant le nouveau")
    void transfert() {
        MembrePharmacopee cible = membre(10L, StatutMembre.ACTIF);
        rolesDeTransfert();
        when(membreRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(PHARMACOPEE_ID)).thenReturn(Optional.of(membreGerant));

        service.transfererPropriete(new TransfertProprieteRequest(10L));

        InOrder ordre = Mockito.inOrder(membreRepository);
        ordre.verify(membreRepository).saveAndFlush(membreGerant);
        ordre.verify(membreRepository).saveAndFlush(cible);
        assertThat(membreGerant.getEstProprietaire()).isFalse();
        assertThat(cible.getEstProprietaire()).isTrue();
        assertThat(pharmacopee.getUtilisateur()).isSameAs(cible.getUtilisateur());
        verify(journalAuditService).tracer(Mockito.argThat(e -> e.action() == TypeActionAudit.PROPRIETE_TRANSFEREE));
    }

    @Test
    @DisplayName("seul le propriétaire peut transférer")
    void transfertParNonProprietaire() {
        MembrePharmacopee manager = MembrePharmacopee.builder().id(11L).estProprietaire(false).pharmacopee(pharmacopee).build();
        when(contexteService.verifierPermission(IPharmacopeeContexteService.MEMBRES_GERER))
                .thenReturn(new ContexteMembre(manager, pharmacopee, Set.of()));

        assertThatThrownBy(() -> service.transfererPropriete(new TransfertProprieteRequest(10L))).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("le nouveau gérant doit être actif")
    void transfertVersMembreInactif() {
        membre(10L, StatutMembre.SUSPENDU);

        assertThatThrownBy(() -> service.transfererPropriete(new TransfertProprieteRequest(10L))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("transfert par l'administrateur : tracé avec l'origine ADMINISTRATION")
    void transfertAdmin() {
        membre(10L, StatutMembre.ACTIF);
        rolesDeTransfert();
        when(pharmacopeeRepository.findById(PHARMACOPEE_ID)).thenReturn(Optional.of(pharmacopee));
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(utilisateur(50L, "admin@ladafura.ml"));
        when(membreRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(PHARMACOPEE_ID)).thenReturn(Optional.empty());

        service.transfererProprieteParAdmin(PHARMACOPEE_ID, new TransfertProprieteRequest(10L));

        verify(journalAuditService).tracer(Mockito.argThat(e -> e.origine() == com.pharmacopee.ladafura.enums.OrigineAction.ADMINISTRATION));
    }
}
