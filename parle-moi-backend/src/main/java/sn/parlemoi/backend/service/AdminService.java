package sn.parlemoi.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sn.parlemoi.backend.dto.admin.ConversationAdminResponse;
import sn.parlemoi.backend.dto.admin.StatsResponse;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.enums.RoleEcoutant;
import sn.parlemoi.backend.enums.StatutConversation;
import sn.parlemoi.backend.enums.StatutPaiement;
import sn.parlemoi.backend.repository.ConversationRepository;
import sn.parlemoi.backend.repository.EcoutantRepository;
import sn.parlemoi.backend.repository.PaiementRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AdminService {

    private final ConversationRepository conversationRepository;
    private final PaiementRepository paiementRepository;
    private final EcoutantRepository ecoutantRepository;

    public AdminService(
            ConversationRepository conversationRepository,
            PaiementRepository paiementRepository,
            EcoutantRepository ecoutantRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.paiementRepository = paiementRepository;
        this.ecoutantRepository = ecoutantRepository;
    }

    @Transactional
    public StatsResponse consulterStats() {
        LocalDateTime debutAujourdhui = LocalDate.now().atStartOfDay();
        LocalDateTime debutSemaine = LocalDate.now().minusDays(7).atStartOfDay();

        long enLigne = ecoutantRepository.findAll().stream()
                .filter(e -> e.getRole() == RoleEcoutant.ECOUTANT && e.isEnLigne())
                .count();

        return new StatsResponse(
                conversationRepository.countByCreeLeAfter(debutAujourdhui),
                conversationRepository.countByCreeLeAfter(debutSemaine),
                conversationRepository.countByStatut(StatutConversation.EN_ATTENTE),
                conversationRepository.countByStatut(StatutConversation.TERMINEE),
                enLigne,
                paiementRepository.countByStatut(StatutPaiement.REUSSI),
                paiementRepository.sommeRevenusReussis()
        );
    }

    @Transactional
    public List<ConversationAdminResponse> listerToutesConversations() {
        return conversationRepository.findAllByOrderByCreeLeDesc()
                .stream()
                .map(this::versReponse)
                .toList();
    }

    private ConversationAdminResponse versReponse(Conversation c) {
        boolean forfaitActif = c.getForfaitExpireLe() != null && c.getForfaitExpireLe().isAfter(LocalDateTime.now());
        return new ConversationAdminResponse(
                c.getCode(),
                c.getStatut(),
                c.getEcoutant().getNom(),
                c.getFormule() != null ? c.getFormule().getNom() : null,
                forfaitActif,
                c.getDateProgrammee(),
                c.getHeureProgrammee(),
                c.getCreeLe()
        );
    }
}