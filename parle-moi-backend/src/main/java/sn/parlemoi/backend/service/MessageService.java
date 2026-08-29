package sn.parlemoi.backend.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.dto.message.EnvoyerMessageRequest;
import sn.parlemoi.backend.dto.message.EvenementSystemeResponse;
import sn.parlemoi.backend.dto.message.MessageResponse;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.entity.Fichier;
import sn.parlemoi.backend.entity.Message;
import sn.parlemoi.backend.enums.DureeRetention;
import sn.parlemoi.backend.enums.StatutConversation;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.ConversationRepository;
import sn.parlemoi.backend.repository.FichierRepository;
import sn.parlemoi.backend.repository.MessageRepository;
import sn.parlemoi.backend.security.AnonymePrincipal;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class MessageService {

    private static final int MESSAGES_GRATUITS_MAX = 5;
    private static final int LONGUEUR_MESSAGE_MAX = 4000;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final FichierRepository fichierRepository;
    private final FichierService fichierService;
    private final SimpMessagingTemplate messagingTemplate;

    private final ConcurrentMap<String, Bucket> antiFloodParConversation = new ConcurrentHashMap<>();

    public MessageService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            FichierRepository fichierRepository,
            FichierService fichierService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.fichierRepository = fichierRepository;
        this.fichierService = fichierService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void envoyer(String code, Principal principal, EnvoyerMessageRequest request) {

        if (!antiFlood(code).tryConsume(1)) {
            envoyerEvenementSysteme(code, "ERREUR", "Trop de messages envoyes trop rapidement, ralentissez.", null);
            return;
        }

        Conversation conversation = conversationRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new RessourceNonTrouveeException("Conversation introuvable"));

        if (conversation.getStatut() == StatutConversation.TERMINEE
                || conversation.getStatut() == StatutConversation.ANNULEE
                || conversation.getStatut() == StatutConversation.EXPIREE) {
            envoyerEvenementSysteme(code, "ERREUR", "Cette conversation est cloturee.", null);
            return;
        }

        // Un fichier joint est optionnel - doit obligatoirement appartenir a CETTE conversation,
        // jamais faire confiance a un fichierId venu du client sans revalider son appartenance
        // (sinon un utilisateur pourrait rattacher a sa conversation un fichier uploade ailleurs).
        Fichier fichierJoint = null;
        if (request.fichierId() != null && !request.fichierId().isBlank()) {
            fichierJoint = fichierRepository.findByIdAndConversationId(request.fichierId(), conversation.getId())
                    .orElse(null);
            if (fichierJoint == null) {
                envoyerEvenementSysteme(code, "ERREUR", "Fichier introuvable pour cette conversation.", null);
                return;
            }
        }

        String contenu = nettoyer(request.contenu());
        if (contenu.isEmpty() && fichierJoint == null) {
            return;
        }
        if (contenu.length() > LONGUEUR_MESSAGE_MAX) {
            envoyerEvenementSysteme(code, "ERREUR", "Message trop long (4000 caracteres maximum).", null);
            return;
        }

        boolean estUtilisateur = principal instanceof AnonymePrincipal;
        String auteurType = estUtilisateur ? "UTILISATEUR" : "ECOUTANT";

        boolean forfaitActif = conversation.getForfaitExpireLe() != null
                && conversation.getForfaitExpireLe().isAfter(LocalDateTime.now());

        if (estUtilisateur && !forfaitActif && conversation.getNbMessagesGratuitsUtilises() >= MESSAGES_GRATUITS_MAX) {
            envoyerEvenementSysteme(
                    code, "PAYWALL",
                    "Vous avez atteint la limite de messages gratuits. Activez un forfait pour continuer la conversation.",
                    0
            );
            return;
        }

        Message message = Message.builder()
                .conversation(conversation)
                .auteurType(auteurType)
                .contenu(contenu)
                .expireLe(calculerExpiration(conversation))
                .build();
        Message messageSauvegarde = messageRepository.save(message);

        if (fichierJoint != null) {
            fichierJoint.setMessage(messageSauvegarde);
            fichierRepository.save(fichierJoint);
        }

        if (estUtilisateur && !forfaitActif) {
            conversation.setNbMessagesGratuitsUtilises(conversation.getNbMessagesGratuitsUtilises() + 1);
            conversationRepository.save(conversation);

            int restants = MESSAGES_GRATUITS_MAX - conversation.getNbMessagesGratuitsUtilises();
            if (restants == 0) {
                envoyerEvenementSysteme(
                        code, "PAYWALL_IMMINENT",
                        "Il ne vous reste plus de messages gratuits. Le prochain message necessitera un forfait actif.",
                        0
                );
            }
        }

        messagingTemplate.convertAndSend("/topic/conversations/" + code, versReponse(messageSauvegarde));
    }

    private Bucket antiFlood(String code) {
        return antiFloodParConversation.computeIfAbsent(code, k -> {
            Bandwidth limite = Bandwidth.classic(15, Refill.intervally(15, Duration.ofSeconds(10)));
            return Bucket.builder().addLimit(limite).build();
        });
    }

    private String nettoyer(String contenuBrut) {
        if (contenuBrut == null) {
            return "";
        }
        String nettoye = contenuBrut.replaceAll("[\\p{Cntrl}&&[^\n\r\t]]", "");
        return nettoye.trim();
    }

    private LocalDateTime calculerExpiration(Conversation conversation) {
        DureeRetention duree = conversation.getEcoutant().getDureeRetentionMessages();
        LocalDateTime maintenant = LocalDateTime.now();
        return switch (duree) {
            case H24 -> maintenant.plusHours(24);
            case J7 -> maintenant.plusDays(7);
            case J30 -> maintenant.plusDays(30);
        };
    }

    private void envoyerEvenementSysteme(String code, String type, String message, Integer restants) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + code,
                new EvenementSystemeResponse(type, message, restants)
        );
    }

    public MessageResponse versReponse(Message message) {
        Optional<Fichier> fichier = fichierRepository.findByMessageId(message.getId());

        return new MessageResponse(
                message.getId(),
                message.getAuteurType(),
                message.getContenu(),
                message.getEnvoyeLe(),
                fichier.map(Fichier::getId).orElse(null),
                fichier.map(Fichier::getNomOriginal).orElse(null),
                fichier.map(Fichier::getTypeMime).orElse(null),
                fichier.map(f -> fichierService.urlSigneePour(f.getCleObjet())).orElse(null)
        );
    }
}