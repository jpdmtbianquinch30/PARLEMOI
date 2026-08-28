package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Ecoutant;
import sn.parlemoi.backend.enums.RoleEcoutant;

import java.util.Optional;

public interface EcoutantRepository extends JpaRepository<Ecoutant, String> {
    Optional<Ecoutant> findByEmail(String email);

    // V1 : une seule ecoutante active - on prend la plus ancienne creee pour un comportement deterministe
    Optional<Ecoutant> findFirstByRoleOrderByCreeLeAsc(RoleEcoutant role);
}