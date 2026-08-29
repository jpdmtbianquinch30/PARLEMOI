package sn.parlemoi.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.parlemoi.backend.dto.admin.CreerEcoutantRequest;
import sn.parlemoi.backend.dto.admin.EcoutantAdminResponse;
import sn.parlemoi.backend.dto.admin.ModifierEcoutantRequest;
import sn.parlemoi.backend.entity.Ecoutant;
import sn.parlemoi.backend.enums.DureeRetention;
import sn.parlemoi.backend.enums.RoleEcoutant;
import sn.parlemoi.backend.exception.ReservationConflitException;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.EcoutantRepository;

import java.util.List;

@Service
public class AdminEcoutantService {

    private final EcoutantRepository ecoutantRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminEcoutantService(EcoutantRepository ecoutantRepository, PasswordEncoder passwordEncoder) {
        this.ecoutantRepository = ecoutantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public List<EcoutantAdminResponse> lister() {
        return ecoutantRepository.findByRoleOrderByCreeLeAsc(RoleEcoutant.ECOUTANT)
                .stream()
                .map(this::versReponse)
                .toList();
    }

    @Transactional
    public EcoutantAdminResponse creer(CreerEcoutantRequest request) {
        if (ecoutantRepository.findByEmail(request.email()).isPresent()) {
            throw new ReservationConflitException("Un compte existe deja avec cet email");
        }

        Ecoutant ecoutant = Ecoutant.builder()
                .email(request.email())
                .motDePasseHash(passwordEncoder.encode(request.motDePasse()))
                .nom(request.nom())
                .role(RoleEcoutant.ECOUTANT)
                .actif(true)
                .enLigne(false)
                .dureeRetentionMessages(DureeRetention.J7)
                .build();

        return versReponse(ecoutantRepository.save(ecoutant));
    }

    @Transactional
    public EcoutantAdminResponse modifier(String id, ModifierEcoutantRequest request) {
        Ecoutant ecoutant = ecoutantRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Ecoutant introuvable"));

        ecoutant.setNom(request.nom());
        ecoutant.setActif(request.actif());

        // Une ecoutante desactivee ne peut pas rester affichee comme en ligne
        if (!request.actif()) {
            ecoutant.setEnLigne(false);
        }

        return versReponse(ecoutantRepository.save(ecoutant));
    }

    private EcoutantAdminResponse versReponse(Ecoutant e) {
        return new EcoutantAdminResponse(
                e.getId(), e.getEmail(), e.getNom(), e.isActif(), e.isEnLigne(),
                e.getHoraireDebut(), e.getHoraireFin(), e.getDureeRetentionMessages()
        );
    }
}