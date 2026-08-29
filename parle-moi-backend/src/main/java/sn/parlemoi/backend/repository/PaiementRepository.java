package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Paiement;

import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, String> {
    Optional<Paiement> findByCleIdempotence(String cleIdempotence);
    Optional<Paiement> findByReferenceProvider(String referenceProvider);

    @org.springframework.data.jpa.repository.Query(
            "select coalesce(sum(p.montant), 0) from Paiement p where p.statut = sn.parlemoi.backend.enums.StatutPaiement.REUSSI"
    )
    java.math.BigDecimal sommeRevenusReussis();

    long countByStatut(sn.parlemoi.backend.enums.StatutPaiement statut);
}