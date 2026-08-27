package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Ecoutant;

import java.util.Optional;

public interface EcoutantRepository extends JpaRepository<Ecoutant, String> {
    Optional<Ecoutant> findByEmail(String email);
}