package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Formule;

import java.util.List;

public interface FormuleRepository extends JpaRepository<Formule, String> {
    List<Formule> findByServiceIdAndActifTrueOrderByOrdreAffichageAsc(String serviceId);
    List<Formule> findByServiceIdOrderByOrdreAffichageAsc(String serviceId);
}