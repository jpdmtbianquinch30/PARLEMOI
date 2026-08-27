package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Appel;

import java.util.Optional;

public interface AppelRepository extends JpaRepository<Appel, String> {
    Optional<Appel> findByConversationId(String conversationId);
}