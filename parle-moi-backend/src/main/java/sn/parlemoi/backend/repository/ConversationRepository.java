package sn.parlemoi.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.enums.EtatNotificationForfait;
import sn.parlemoi.backend.enums.StatutConversation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByCode(String code);

    boolean existsByEcoutantIdAndStatut(String ecoutantId, StatutConversation statut);

    long countByEcoutantIdAndStatut(String ecoutantId, StatutConversation statut);

    boolean existsByCodeAndEcoutantId(String code, String ecoutantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conversation c where c.code = :code")
    Optional<Conversation> findByCodeForUpdate(@Param("code") String code);

    // SCRUM-9 : forfaits dont l'expiration tombe dans les 5 prochaines minutes, pas encore avertis
    List<Conversation> findByForfaitExpireLeBetweenAndEtatNotificationForfait(
            LocalDateTime debut, LocalDateTime fin, EtatNotificationForfait etat
    );

    // SCRUM-18 : forfaits deja expires, pas encore notifies comme tels
    List<Conversation> findByForfaitExpireLeBeforeAndEtatNotificationForfaitNot(
            LocalDateTime maintenant, EtatNotificationForfait etat
    );
}