package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Message;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {

    // Exclut les messages deja expires selon la retention configuree par l'ecoutante
    // (le message peut encore exister en base jusqu'au job de purge de la Phase 8,
    // mais ne doit plus jamais etre restitue au client une fois expire)
    List<Message> findByConversationIdAndExpireLeAfterOrderByEnvoyeLeAsc(
            String conversationId, LocalDateTime maintenant
    );
}