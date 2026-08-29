package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Ecoutant;
import sn.parlemoi.backend.enums.RoleEcoutant;

import java.util.Optional;
import java.util.List;

public interface EcoutantRepository extends JpaRepository<Ecoutant, String> {

    List<Ecoutant> findByRoleOrderByCreeLeAsc(RoleEcoutant role);

    boolean existsByEmailAndIdNot(String email, String id);

    Optional<Ecoutant> findByEmail(String email);

    Optional<Ecoutant> findFirstByRoleAndActifTrueOrderByCreeLeAsc(RoleEcoutant role);
}