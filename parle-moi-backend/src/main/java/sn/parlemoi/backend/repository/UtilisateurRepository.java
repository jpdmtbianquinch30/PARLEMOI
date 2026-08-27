package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
}