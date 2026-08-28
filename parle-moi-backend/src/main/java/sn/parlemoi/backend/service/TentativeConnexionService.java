package sn.parlemoi.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.entity.Ecoutant;
import sn.parlemoi.backend.exception.IdentifiantsInvalidesException;
import sn.parlemoi.backend.repository.EcoutantRepository;

import java.time.LocalDateTime;

@Service
public class TentativeConnexionService {

    private static final int MAX_TENTATIVES = 5;
    private static final long DUREE_VERROUILLAGE_MINUTES = 15;

    private final EcoutantRepository ecoutantRepository;

    public TentativeConnexionService(EcoutantRepository ecoutantRepository) {
        this.ecoutantRepository = ecoutantRepository;
    }

    // REQUIRES_NEW dans un bean SEPARE : garantit que cette transaction est bien commitee
    // independamment, meme si l'appelant (AuthService.login) leve une exception juste apres
    // et provoque le rollback de SA propre transaction.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerEchec(String ecoutantId) {
        Ecoutant ecoutant = ecoutantRepository.findById(ecoutantId)
                .orElseThrow(IdentifiantsInvalidesException::new);

        int tentatives = ecoutant.getTentativesEchouees() + 1;
        ecoutant.setTentativesEchouees(tentatives);
        if (tentatives >= MAX_TENTATIVES) {
            ecoutant.setVerrouilleJusqua(LocalDateTime.now().plusMinutes(DUREE_VERROUILLAGE_MINUTES));
        }
        ecoutantRepository.save(ecoutant);
    }
}