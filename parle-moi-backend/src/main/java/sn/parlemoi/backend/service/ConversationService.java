package sn.parlemoi.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.dto.conversation.ConversationResponse;
import sn.parlemoi.backend.dto.conversation.DemarrerConversationRequest;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.entity.Ecoutant;
import sn.parlemoi.backend.entity.Utilisateur;
import sn.parlemoi.backend.enums.RoleEcoutant;
import sn.parlemoi.backend.enums.StatutConversation;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.ConversationRepository;
import sn.parlemoi.backend.repository.EcoutantRepository;
import sn.parlemoi.backend.repository.UtilisateurRepository;
import sn.parlemoi.backend.dto.message.HistoriqueConversationResponse;
import sn.parlemoi.backend.dto.message.MessageResponse;
import sn.parlemoi.backend.entity.Message;
import sn.parlemoi.backend.repository.MessageRepository;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ConversationService {

    private static final int MAX_TENTATIVES_GENERATION_CODE = 5;
    private static final int MESSAGES_GRATUITS_MAX = 5;

    private final ConversationRepository conversationRepository;
    private final EcoutantRepository ecoutantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final MessageRepository messageRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            EcoutantRepository ecoutantRepository,
            UtilisateurRepository utilisateurRepository,
            CodeGeneratorService codeGeneratorService,
            MessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.ecoutantRepository = ecoutantRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.codeGeneratorService = codeGeneratorService;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ConversationResponse demarrer(DemarrerConversationRequest request) {

        Ecoutant ecoutant = ecoutantRepository.findFirstByRoleAndActifTrueOrderByCreeLeAsc(RoleEcoutant.ECOUTANT)
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucune ecoutante n'est configuree pour le moment"));

        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder().build());

        boolean ecoutanteOccupee = conversationRepository.existsByEcoutantIdAndStatut(
                ecoutant.getId(), StatutConversation.EN_COURS
        );
        boolean disponibleMaintenant = ecoutant.isEnLigne() && !ecoutanteOccupee;

        StatutConversation statutInitial = disponibleMaintenant
                ? StatutConversation.EN_COURS
                : StatutConversation.EN_ATTENTE;

        Integer position = disponibleMaintenant
                ? null
                : (int) conversationRepository.countByEcoutantIdAndStatut(
                ecoutant.getId(), StatutConversation.EN_ATTENTE
        ) + 1;

        Conversation conversation = Conversation.builder()
                .code(genererCodeUnique())
                .utilisateur(utilisateur)
                .ecoutant(ecoutant)
                .statut(statutInitial)
                .positionFileAttente(position)
                .sujetOptionnel(request.sujetOptionnel())
                // SCRUM-12 : consultable via le code pendant 1 mois
                .expireLe(LocalDateTime.now().plusMonths(1))
                .build();

        Conversation sauvegardee = conversationRepository.save(conversation);
        return versReponse(sauvegardee);
    }

    @Transactional
    public HistoriqueConversationResponse consulterHistorique(String code) {
        Conversation conversation = conversationRepository.findByCode(code)
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucune conversation trouvee avec ce code"));

        if (conversation.getExpireLe() != null && conversation.getExpireLe().isBefore(LocalDateTime.now())) {
            throw new RessourceNonTrouveeException("Cette conversation a expire et n'est plus consultable");
        }

        List<Message> messages = messageRepository.findByConversationIdAndExpireLeAfterOrderByEnvoyeLeAsc(
                conversation.getId(), LocalDateTime.now()
        );

        List<MessageResponse> messagesReponse = messages.stream()
                .map(m -> new MessageResponse(m.getId(), m.getAuteurType(), m.getContenu(), m.getEnvoyeLe()))
                .toList();

        return new HistoriqueConversationResponse(versReponse(conversation), messagesReponse);
    }

    @Transactional
    public ConversationResponse trouverParCode(String code) {
        Conversation conversation = conversationRepository.findByCode(code)
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucune conversation trouvee avec ce code"));
        return versReponse(conversation);
    }

    private String genererCodeUnique() {
        for (int i = 0; i < MAX_TENTATIVES_GENERATION_CODE; i++) {
            String code = codeGeneratorService.genererCode();
            if (conversationRepository.findByCode(code).isEmpty()) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de generer un code de conversation unique apres plusieurs tentatives");
    }

    private ConversationResponse versReponse(Conversation conversation) {
        int restants = Math.max(0, MESSAGES_GRATUITS_MAX - conversation.getNbMessagesGratuitsUtilises());

        boolean forfaitActif = conversation.getForfaitExpireLe() != null
                && conversation.getForfaitExpireLe().isAfter(LocalDateTime.now());

        return new ConversationResponse(
                conversation.getCode(),
                conversation.getStatut(),
                conversation.getPositionFileAttente(),
                conversation.getNbMessagesGratuitsUtilises(),
                restants,
                forfaitActif,
                forfaitActif ? conversation.getFormule().getNom() : null,
                conversation.getForfaitExpireLe(),
                conversation.getExpireLe()
        );
    }
}