package sn.parlemoi.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.entity.Message;
import sn.parlemoi.backend.enums.StatutConversation;
import sn.parlemoi.backend.repository.ConversationRepository;
import sn.parlemoi.backend.repository.FichierRepository;
import sn.parlemoi.backend.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;

// Purge reelle des donnees selon la retention configuree par chaque ecoutante (H24/J7/J30)
// et selon la limite systeme d'1 mois par conversation (SCRUM-12). L'acces est deja bloque
// avant meme que ce job ne passe (ConversationService verifie expireLe a chaque lecture) -
// ce service s'occupe uniquement de la suppression physique des donnees, pas du controle d'acces.
@Service
public class PurgeService {

    private static final Logger log = LoggerFactory.getLogger(PurgeService.class);

    private final MessageRepository messageRepository;
    private final FichierRepository fichierRepository;
    private final ConversationRepository conversationRepository;
    private final FichierService fichierService;

    public PurgeService(
            MessageRepository messageRepository,
            FichierRepository fichierRepository,
            ConversationRepository conversationRepository,
            FichierService fichierService
    ) {
        this.messageRepository = messageRepository;
        this.fichierRepository = fichierRepository;
        this.conversationRepository = conversationRepository;
        this.fichierService = fichierService;
    }

    // Toutes les heures : largement suffisant, la retention la plus courte possible est 24h (H24)
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void purgerMessagesExpires() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<Message> expires = messageRepository.findByExpireLeBefore(maintenant);

        if (expires.isEmpty()) {
            return;
        }

        for (Message message : expires) {
            supprimerMessageEtFichierAssocie(message);
        }
        log.info("Purge retention : {} message(s) supprime(s)", expires.size());
    }

    // Limite systeme absolue d'1 mois : purge le contenu restant meme si la retention individuelle
    // (H24/J7/J30) n'a pas encore expire chaque message - le code devient invalide de toute facon
    // (SCRUM-12), donc son contenu doit disparaitre au meme moment.
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void purgerConversationsExpirees() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<Conversation> expirees = conversationRepository
                .findByExpireLeBeforeAndStatutNot(maintenant, StatutConversation.EXPIREE);

        if (expirees.isEmpty()) {
            return;
        }

        for (Conversation conversation : expirees) {
            List<Message> messagesRestants = messageRepository.findByConversationId(conversation.getId());
            for (Message message : messagesRestants) {
                supprimerMessageEtFichierAssocie(message);
            }

            // La ligne Conversation elle-meme N'EST PAS supprimee, ni les Paiement/Appel associes -
            // conserves comme trace comptable/statistique admin, anonymes par nature (aucune
            // donnee personnelle identifiante dessus). Seul le contenu de la discussion disparait.
            conversation.setStatut(StatutConversation.EXPIREE);
            conversationRepository.save(conversation);
        }
        log.info("Purge retention : {} conversation(s) marquee(s) EXPIREE et videe(s)", expirees.size());
    }

    private void supprimerMessageEtFichierAssocie(Message message) {
        fichierRepository.findByMessageId(message.getId()).ifPresent(fichier -> {
            fichierService.supprimerObjet(fichier.getCleObjet());
            fichierRepository.delete(fichier);
        });
        messageRepository.delete(message);
    }
}