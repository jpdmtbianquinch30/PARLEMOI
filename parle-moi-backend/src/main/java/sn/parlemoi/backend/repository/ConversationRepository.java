package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Conversation;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByCode(String code);
}