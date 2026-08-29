package sn.parlemoi.backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.dto.ecoutant.EcoutantProfilResponse;
import sn.parlemoi.backend.dto.ecoutant.MettreAJourHorairesRequest;
import sn.parlemoi.backend.dto.ecoutant.MettreAJourRetentionRequest;
import sn.parlemoi.backend.dto.message.EvenementConversationResponse;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.entity.Ecoutant;
import sn.parlemoi.backend.enums.StatutConversation;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.ConversationRepository;
import sn.parlemoi.backend.repository.EcoutantRepository;
import sn.parlemoi.backend.dto.ecoutant.ConversationEcoutantResponse;
import sn.parlemoi.backend.dto.ecoutant.ProposerHoraireRequest;
import sn.parlemoi.backend.exception.AccesRefuseException;
import sn.parlemoi.backend.exception.ConversationClotureeException;


import java.time.LocalTime;
import java.util.List;

@Service
public class EcoutantService {

    private final EcoutantRepository ecoutantRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public EcoutantService(
            EcoutantRepository ecoutantRepository,
            ConversationRepository conversationRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.ecoutantRepository = ecoutantRepository;
        this.conversationRepository = conversationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public EcoutantProfilResponse consulterProfil(String ecoutantId) {
        return versReponse(trouver(ecoutantId));
    }

    @Transactional
    public EcoutantProfilResponse mettreAJourStatutEnLigne(String ecoutantId, boolean enLigne) {
        Ecoutant ecoutant = trouver(ecoutantId);
        ecoutant.setEnLigne(enLigne);
        ecoutantRepository.save(ecoutant);

        if (enLigne) {
            promouvoirProchaineConversationSiDisponible(ecoutant);
        }

        return versReponse(ecoutant);
    }

    @Transactional
    public List<ConversationEcoutantResponse> listerDemandes(String ecoutantId) {
        List<Conversation> conversations = conversationRepository.findByEcoutantIdAndStatutInOrderByCreeLeAsc(
                ecoutantId,
                List.of(StatutConversation.EN_ATTENTE, StatutConversation.EN_COURS, StatutConversation.PROGRAMMEE)
        );
        return conversations.stream().map(this::versConversationReponse).toList();
    }

    @Transactional
    public ConversationEcoutantResponse confirmer(String ecoutantId, String code) {
        Conversation conversation = trouverConversationAssignee(ecoutantId, code);

        boolean dejaOccupee = conversationRepository.existsByEcoutantIdAndStatut(ecoutantId, StatutConversation.EN_COURS)
                && !conversation.getStatut().equals(StatutConversation.EN_COURS);

        conversation.setStatut(dejaOccupee ? StatutConversation.EN_ATTENTE : StatutConversation.EN_COURS);
        if (!dejaOccupee) {
            conversation.setPositionFileAttente(null);
        }
        conversationRepository.save(conversation);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getCode(),
                new EvenementConversationResponse(
                        dejaOccupee ? "DEMANDE_EN_ATTENTE" : "DEMANDE_CONFIRMEE",
                        dejaOccupee
                                ? "Votre demande est en file d'attente, l'ecoutante est actuellement occupee."
                                : "Votre demande a ete confirmee, la conversation est en cours."
                )
        );

        return versConversationReponse(conversation);
    }

    @Transactional
    public ConversationEcoutantResponse mettreEnAttente(String ecoutantId, String code) {
        Conversation conversation = trouverConversationAssignee(ecoutantId, code);

        conversation.setStatut(StatutConversation.EN_ATTENTE);
        long nbEnAttente = conversationRepository.countByEcoutantIdAndStatut(ecoutantId, StatutConversation.EN_ATTENTE);
        conversation.setPositionFileAttente((int) nbEnAttente + 1);
        conversationRepository.save(conversation);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getCode(),
                new EvenementConversationResponse(
                        "DEMANDE_EN_ATTENTE",
                        "Votre demande a ete remise en file d'attente."
                )
        );

        return versConversationReponse(conversation);
    }

    @Transactional
    public ConversationEcoutantResponse proposerHoraire(String ecoutantId, String code, ProposerHoraireRequest request) {
        Conversation conversation = trouverConversationAssignee(ecoutantId, code);

        conversation.setStatut(StatutConversation.PROGRAMMEE);
        conversation.setDateProgrammee(request.dateProgrammee());
        conversation.setHeureProgrammee(request.heureProgrammee());
        conversation.setPositionFileAttente(null);
        conversationRepository.save(conversation);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getCode(),
                new EvenementConversationResponse(
                        "HORAIRE_PROPOSE",
                        "L'ecoutante propose un rendez-vous le " + request.dateProgrammee() + " a " + request.heureProgrammee() + "."
                )
        );

        return versConversationReponse(conversation);
    }

    private Conversation trouverConversationAssignee(String ecoutantId, String code) {
        Conversation conversation = conversationRepository.findByCodeAndEcoutantId(code, ecoutantId)
                .orElseThrow(() -> new AccesRefuseException("Cette conversation ne vous est pas assignee"));

        // Empeche toute action (confirmer, mettre en attente, proposer un horaire) sur une
        // conversation deja definitivement close - sans cette garde, une action pouvait "ressusciter"
        // une conversation TERMINEE/ANNULEE/EXPIREE dans un statut actif, ce qui n'a aucun sens metier.
        if (conversation.getStatut() == StatutConversation.TERMINEE
                || conversation.getStatut() == StatutConversation.ANNULEE
                || conversation.getStatut() == StatutConversation.EXPIREE) {
            throw new ConversationClotureeException();
        }

        return conversation;
    }

    private ConversationEcoutantResponse versConversationReponse(Conversation conversation) {
        boolean forfaitActif = conversation.getForfaitExpireLe() != null
                && conversation.getForfaitExpireLe().isAfter(java.time.LocalDateTime.now());

        return new ConversationEcoutantResponse(
                conversation.getCode(),
                conversation.getStatut(),
                conversation.getPositionFileAttente(),
                conversation.getSujetOptionnel(),
                forfaitActif,
                forfaitActif ? conversation.getFormule().getNom() : null,
                conversation.getForfaitExpireLe(),
                conversation.getDateProgrammee(),
                conversation.getHeureProgrammee(),
                conversation.getCreeLe()
        );
    }

    @Transactional
    public EcoutantProfilResponse mettreAJourHoraires(String ecoutantId, MettreAJourHorairesRequest request) {
        Ecoutant ecoutant = trouver(ecoutantId);
        ecoutant.setHoraireDebut(LocalTime.parse(request.horaireDebut()));
        ecoutant.setHoraireFin(LocalTime.parse(request.horaireFin()));
        ecoutantRepository.save(ecoutant);
        return versReponse(ecoutant);
    }

    @Transactional
    public EcoutantProfilResponse mettreAJourRetention(String ecoutantId, MettreAJourRetentionRequest request) {
        Ecoutant ecoutant = trouver(ecoutantId);
        ecoutant.setDureeRetentionMessages(request.dureeRetentionMessages());
        ecoutantRepository.save(ecoutant);
        return versReponse(ecoutant);
    }

    // SCRUM-23 (partie promotion automatique) : des qu'une ecoutante redevient disponible
    // et n'a aucune conversation EN_COURS, la premiere personne en file d'attente est promue.
    private void promouvoirProchaineConversationSiDisponible(Ecoutant ecoutant) {
        boolean dejaOccupee = conversationRepository.existsByEcoutantIdAndStatut(
                ecoutant.getId(), StatutConversation.EN_COURS
        );
        if (dejaOccupee) {
            return;
        }

        conversationRepository.findFirstByEcoutantIdAndStatutOrderByPositionFileAttenteAsc(
                ecoutant.getId(), StatutConversation.EN_ATTENTE
        ).ifPresent(this::promouvoir);
    }

    private void promouvoir(Conversation conversation) {
        conversation.setStatut(StatutConversation.EN_COURS);
        conversation.setPositionFileAttente(null);
        conversationRepository.save(conversation);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getCode(),
                new EvenementConversationResponse(
                        "CONVERSATION_PRISE_EN_CHARGE",
                        "Une ecoutante est disponible, vous pouvez continuer votre conversation."
                )
        );

        renumeroterFileAttente(conversation.getEcoutant().getId());
    }

    private void renumeroterFileAttente(String ecoutantId) {
        List<Conversation> enAttente = conversationRepository.findByEcoutantIdAndStatutOrderByPositionFileAttenteAsc(
                ecoutantId, StatutConversation.EN_ATTENTE
        );

        int position = 1;
        for (Conversation conversation : enAttente) {
            conversation.setPositionFileAttente(position);
            conversationRepository.save(conversation);

            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + conversation.getCode(),
                    new EvenementConversationResponse(
                            "POSITION_FILE_MISE_A_JOUR",
                            "Votre position en file d'attente est maintenant " + position + "."
                    )
            );
            position++;
        }
    }

    private Ecoutant trouver(String ecoutantId) {
        return ecoutantRepository.findById(ecoutantId)
                .orElseThrow(() -> new RessourceNonTrouveeException("Ecoutant introuvable"));
    }

    private EcoutantProfilResponse versReponse(Ecoutant ecoutant) {
        return new EcoutantProfilResponse(
                ecoutant.getId(),
                ecoutant.getNom(),
                ecoutant.getEmail(),
                ecoutant.isEnLigne(),
                ecoutant.getHoraireDebut(),
                ecoutant.getHoraireFin(),
                ecoutant.getDureeRetentionMessages()
        );
    }
}