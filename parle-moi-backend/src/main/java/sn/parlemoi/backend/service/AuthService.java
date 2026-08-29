package sn.parlemoi.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.dto.auth.LoginRequest;
import sn.parlemoi.backend.dto.auth.LoginResponse;
import sn.parlemoi.backend.entity.Ecoutant;
import sn.parlemoi.backend.exception.CompteVerrouilleException;
import sn.parlemoi.backend.exception.IdentifiantsInvalidesException;
import sn.parlemoi.backend.repository.EcoutantRepository;
import sn.parlemoi.backend.security.JwtService;
import sn.parlemoi.backend.exception.CompteDesactiveException;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final EcoutantRepository ecoutantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TentativeConnexionService tentativeConnexionService;

    public AuthService(
            EcoutantRepository ecoutantRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TentativeConnexionService tentativeConnexionService
    ) {
        this.ecoutantRepository = ecoutantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tentativeConnexionService = tentativeConnexionService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Ecoutant ecoutant = ecoutantRepository.findByEmail(request.email())
                .orElseThrow(IdentifiantsInvalidesException::new);

        if (ecoutant.getVerrouilleJusqua() != null && ecoutant.getVerrouilleJusqua().isAfter(LocalDateTime.now())) {
            throw new CompteVerrouilleException(ecoutant.getVerrouilleJusqua());
        }

        if (!ecoutant.isActif()) {
            throw new CompteDesactiveException();
        }

        if (!passwordEncoder.matches(request.motDePasse(), ecoutant.getMotDePasseHash())) {
            tentativeConnexionService.enregistrerEchec(ecoutant.getId());
            throw new IdentifiantsInvalidesException();
        }

        ecoutant.setTentativesEchouees(0);
        ecoutant.setVerrouilleJusqua(null);
        ecoutantRepository.save(ecoutant);

        String token = jwtService.genererToken(ecoutant.getId(), ecoutant.getEmail(), ecoutant.getRole());
        return new LoginResponse(token, ecoutant.getRole().name(), ecoutant.getNom());
    }
}