package sn.parlemoi.backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.dto.message.EvenementForfaitResponse;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.enums.EtatNotificationForfait;
import sn.parlemoi.backend.repository.ConversationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForfaitSurveillanceService {

    private static final long DELAI_AVERTISSEMENT_MINUTES = 5;

    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ForfaitSurveillanceService(
            ConversationRepository conversationRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.conversationRepository = conversationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Toutes les 30s : assez reactif pour une fenetre d'avertissement de 5 min,
    // sans surcharger la base pour un site a faible volume en V1.
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void verifier() {
        LocalDateTime maintenant = LocalDateTime.now();
        traiterAvertissements(maintenant);
        traiterExpirations(maintenant);
    }

    private void traiterAvertissements(LocalDateTime maintenant) {
        LocalDateTime borneSuperieure = maintenant.plusMinutes(DELAI_AVERTISSEMENT_MINUTES);

        List<Conversation> aAvertir = conversationRepository
                .findByForfaitExpireLeBetweenAndEtatNotificationForfait(
                        maintenant, borneSuperieure, EtatNotificationForfait.AUCUNE
                );

        for (Conversation conversation : aAvertir) {
            conversation.setEtatNotificationForfait(EtatNotificationForfait.AVERTISSEMENT_ENVOYE);
            conversationRepository.save(conversation);

            // Diffuse sur le meme topic que MessageService - utilisateur ET ecoutante
            // (SCRUM-9 et SCRUM-17) recoivent l'evenement, ils sont deja tous les deux abonnes.
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + conversation.getCode(),
                    new EvenementForfaitResponse(
                            "AVERTISSEMENT_FIN_FORFAIT",
                            "Votre forfait se termine dans moins de 5 minutes.",
                            conversation.getForfaitExpireLe()
                    )
            );
        }
    }

    private void traiterExpirations(LocalDateTime maintenant) {
        List<Conversation> expirees = conversationRepository
                .findByForfaitExpireLeBeforeAndEtatNotificationForfaitNot(
                        maintenant, EtatNotificationForfait.EXPIRATION_NOTIFIEE
                );

        for (Conversation conversation : expirees) {
            conversation.setEtatNotificationForfait(EtatNotificationForfait.EXPIRATION_NOTIFIEE);
            conversationRepository.save(conversation);

            // La conversation N'EST PAS clôturee (statut inchange) - seul le forfait est termine.
            // MessageService.envoyer() re-declenchera deja le PAYWALL au prochain message envoye,
            // cet evenement ne fait qu'informer proactivement sans attendre cette tentative.
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + conversation.getCode(),
                    new EvenementForfaitResponse(
                            "FORFAIT_TERMINE",
                            "Votre forfait est termine. Activez un nouveau forfait pour continuer la conversation.",
                            conversation.getForfaitExpireLe()
                    )
            );
        }
    }
}