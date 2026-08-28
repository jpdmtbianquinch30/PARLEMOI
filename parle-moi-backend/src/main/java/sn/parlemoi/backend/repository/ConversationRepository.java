package sn.parlemoi.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.enums.StatutConversation;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByCode(String code);

    boolean existsByEcoutantIdAndStatut(String ecoutantId, StatutConversation statut);

    long countByEcoutantIdAndStatut(String ecoutantId, StatutConversation statut);

    // Verifie l'assignation sans jamais charger la relation LAZY ecoutant -
    // evite de reproduire le bug de LazyInitializationException deja rencontre en Phase 0
    boolean existsByCodeAndEcoutantId(String code, String ecoutantId);

    // Verrou pessimiste : serialise les envois concurrents sur UNE MEME conversation
    // pour garantir que le compteur de messages gratuits ne peut jamais etre contourne
    // par une race condition (deux messages envoyes au meme instant)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conversation c where c.code = :code")
    Optional<Conversation> findByCodeForUpdate(@Param("code") String code);
}