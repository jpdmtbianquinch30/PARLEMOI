package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Service;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, String> {
    List<sn.parlemoi.backend.entity.Service> findByActifTrueOrderByOrdreAffichageAsc();
}