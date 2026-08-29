package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Fichier;

import java.util.Optional;

public interface FichierRepository extends JpaRepository<Fichier, String> {
    Optional<Fichier> findByIdAndConversationId(String id, String conversationId);
    Optional<Fichier> findByMessageId(String messageId);
}