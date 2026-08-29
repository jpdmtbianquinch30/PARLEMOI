package sn.parlemoi.backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.dto.appel.EvenementAppelResponse;
import sn.parlemoi.backend.dto.appel.SignalAppelRequest;
import sn.parlemoi.backend.entity.Appel;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.enums.StatutAppel;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.AppelRepository;
import sn.parlemoi.backend.repository.ConversationRepository;
import sn.parlemoi.backend.security.AnonymePrincipal;

import java.security.Principal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AppelService {

    private final ConversationRepository conversationRepository;
    private final AppelRepository appelRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(AppelService.class);

    public AppelService(
            ConversationRepository conversationRepository,
            AppelRepository appelRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.conversationRepository = conversationRepository;
        this.appelRepository = appelRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void traiterSignal(String code, Principal principal, SignalAppelRequest request) {
        log.info("[Appel] Signal recu - code={}, type={}, principal={}", code, request.type(), principal);

        Conversation conversation = conversationRepository.findByCode(code)
                .orElseThrow(() -> new RessourceNonTrouveeException("Conversation introuvable"));

        String emetteur = (principal instanceof AnonymePrincipal) ? "UTILISATEUR" : "ECOUTANT";

        switch (request.type()) {
            case "DEMARRER" -> demarrer(conversation);
            case "ACCEPTER" -> accepter(conversation);
            case "REFUSER" -> terminer(conversation, StatutAppel.MANQUE);
            case "RACCROCHER" -> terminer(conversation, StatutAppel.TERMINE);
            default -> { }
        }

        String destination = "/topic/conversations/" + code + "/appel";
        log.info("[Appel] Envoi du signal vers destination={}", destination);

        messagingTemplate.convertAndSend(destination, new EvenementAppelResponse(request.type(), request.contenu(), emetteur));

        log.info("[Appel] Signal envoye avec succes");
    }

    private void demarrer(Conversation conversation) {
        // Un seul appel actif par conversation (contrainte unique deja posee en base sur conversation_id) -
        // s'il existe deja un appel non termine, on le reutilise plutot que d'en creer un second en doublon.
        Appel appel = appelRepository.findByConversationId(conversation.getId())
                .orElseGet(() -> Appel.builder().conversation(conversation).build());

        appel.setStatut(StatutAppel.EN_ATTENTE);
        appelRepository.save(appel);
    }

    private void accepter(Conversation conversation) {
        Appel appel = trouverAppel(conversation);
        appel.setStatut(StatutAppel.EN_COURS);
        appel.setStartedAt(LocalDateTime.now());
        appelRepository.save(appel);
    }

    private void terminer(Conversation conversation, StatutAppel statutFinal) {
        Appel appel = trouverAppel(conversation);

        appel.setStatut(statutFinal);
        appel.setEndedAt(LocalDateTime.now());

        if (appel.getStartedAt() != null) {
            long secondes = java.time.Duration.between(appel.getStartedAt(), appel.getEndedAt()).getSeconds();
            appel.setDureeSecondes((int) secondes);
        }

        appelRepository.save(appel);
    }

    private Appel trouverAppel(Conversation conversation) {
        return appelRepository.findByConversationId(conversation.getId())
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucun appel en cours pour cette conversation"));
    }
}