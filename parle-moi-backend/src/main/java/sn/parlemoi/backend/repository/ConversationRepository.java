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
    Optional<Conversation> findByCodeAndEcoutantId(String code, String ecoutantId);

    boolean existsByEcoutantIdAndStatut(String ecoutantId, StatutConversation statut);

    long countByEcoutantIdAndStatut(String ecoutantId, StatutConversation statut);

    boolean existsByCodeAndEcoutantId(String code, String ecoutantId);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conversation c where c.code = :code")
    Optional<Conversation> findByCodeForUpdate(@Param("code") String code);

    List<Conversation> findByForfaitExpireLeBetweenAndEtatNotificationForfait(
            LocalDateTime debut, LocalDateTime fin, EtatNotificationForfait etat
    );

    List<Conversation> findByForfaitExpireLeBeforeAndEtatNotificationForfaitNot(
            LocalDateTime maintenant, EtatNotificationForfait etat
    );

    // File d'attente : prochaine conversation a promouvoir en priorite (position la plus basse)
    Optional<Conversation> findFirstByEcoutantIdAndStatutOrderByPositionFileAttenteAsc(
            String ecoutantId, StatutConversation statut
    );

    // Utilise pour renumeroter la file apres une promotion ou un depart de la file
    List<Conversation> findByEcoutantIdAndStatutOrderByPositionFileAttenteAsc(
            String ecoutantId, StatutConversation statut
    );

    // Liste des demandes visibles par l'ecoutante (SCRUM-13, 19)
    List<Conversation> findByEcoutantIdAndStatutInOrderByCreeLeAsc(
            String ecoutantId, List<StatutConversation> statuts
    );

}