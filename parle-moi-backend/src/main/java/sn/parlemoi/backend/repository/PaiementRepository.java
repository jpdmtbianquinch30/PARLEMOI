package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Paiement;

import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, String> {
    Optional<Paiement> findByCleIdempotence(String cleIdempotence);
    Optional<Paiement> findByReferenceProvider(String referenceProvider);
}