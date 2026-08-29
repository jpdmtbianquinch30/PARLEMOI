package sn.parlemoi.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.parlemoi.backend.dto.paiement.InitierPaiementRequest;
import sn.parlemoi.backend.dto.paiement.PaiementResponse;
import sn.parlemoi.backend.dto.paiement.WebhookEvenement;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.entity.Formule;
import sn.parlemoi.backend.entity.Paiement;
import sn.parlemoi.backend.enums.StatutPaiement;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.ConversationRepository;
import sn.parlemoi.backend.repository.FormuleRepository;
import sn.parlemoi.backend.repository.PaiementRepository;
import sn.parlemoi.backend.service.paiement.PaiementProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaiementService {

    @Value("${paiement.mode-simulation:true}")
    private boolean modeSimulation;

    @Value("${paiement.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    private final PaiementRepository paiementRepository;
    private final ConversationRepository conversationRepository;
    private final FormuleRepository formuleRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, PaiementProvider> providersParNom;

    public PaiementService(
            PaiementRepository paiementRepository,
            ConversationRepository conversationRepository,
            FormuleRepository formuleRepository,
            SimpMessagingTemplate messagingTemplate,
            List<PaiementProvider> providers
    ) {
        this.paiementRepository = paiementRepository;
        this.conversationRepository = conversationRepository;
        this.formuleRepository = formuleRepository;
        this.messagingTemplate = messagingTemplate;
        this.providersParNom = providers.stream()
                .collect(java.util.stream.Collectors.toMap(PaiementProvider::nom, p -> p));
    }

    @Transactional
    public PaiementResponse initier(String codeConversation, InitierPaiementRequest request) {
        Conversation conversation = conversationRepository.findByCode(codeConversation)
                .orElseThrow(() -> new RessourceNonTrouveeException("Conversation introuvable"));

        Formule formule = formuleRepository.findById(request.formuleId())
                .filter(Formule::isActif)
                .orElseThrow(() -> new RessourceNonTrouveeException("Formule introuvable ou inactive"));

        // En mode simulation, on ignore le choix du client et on force le simulateur -
        // impossible d'atteindre accidentellement une vraie API de paiement depuis le dev
        String nomProvider = modeSimulation ? "SIMULATEUR" : request.provider();
        PaiementProvider provider = providersParNom.get(nomProvider);
        if (provider == null) {
            throw new IllegalStateException("Provider de paiement inconnu: " + nomProvider);
        }

        Paiement paiement = Paiement.builder()
                .conversation(conversation)
                .formule(formule)
                .montant(formule.getPrix())
                .devise(formule.getDevise())
                .statut(StatutPaiement.EN_ATTENTE)
                .provider(nomProvider)
                .cleIdempotence(UUID.randomUUID().toString())
                .build();
        paiement = paiementRepository.save(paiement);

        String urlSucces = frontendBaseUrl + "/chat/" + codeConversation + "?paiement=succes";
        String urlEchec = frontendBaseUrl + "/chat/" + codeConversation + "?paiement=echec";

        PaiementProvider.ResultatInitiation resultat = provider.initier(paiement, urlSucces, urlEchec);
        paiement.setReferenceProvider(resultat.referenceProvider());
        paiementRepository.save(paiement);

        return versReponse(paiement, resultat.urlPaiement());
    }

    @Transactional
    public void traiterWebhook(String nomProvider, String payloadBrut, Map<String, String> entetes) {
        PaiementProvider provider = providersParNom.get(nomProvider);
        if (provider == null || !provider.verifierWebhook(payloadBrut, entetes)) {
            throw new SecurityException("Webhook non authentifie pour le provider " + nomProvider);
        }

        WebhookEvenement evenement = provider.parserWebhook(payloadBrut);
        confirmerPaiement(evenement.referenceProvider(), evenement.statut());
    }

    // Utilise a la fois par le vrai webhook et par l'endpoint de simulation dev -
    // une seule logique d'activation du forfait, jamais dupliquee
    @Transactional
    public void confirmerPaiementSimule(String paiementId) {
        if (!modeSimulation) {
            throw new IllegalStateException("Confirmation manuelle desactivee hors mode simulation");
        }
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RessourceNonTrouveeException("Paiement introuvable"));
        confirmerPaiement(paiement.getReferenceProvider(), StatutPaiement.REUSSI);
    }

    private void confirmerPaiement(String referenceProvider, StatutPaiement nouveauStatut) {
        Paiement paiement = paiementRepository.findByReferenceProvider(referenceProvider)
                .orElseThrow(() -> new RessourceNonTrouveeException("Paiement introuvable pour cette reference"));

        // Idempotence : si deja traite (retry webhook, double notification), on ne credite jamais deux fois
        if (paiement.getStatut() == StatutPaiement.REUSSI) {
            return;
        }

        paiement.setStatut(nouveauStatut);
        paiement.setConfirmeLe(LocalDateTime.now());
        paiementRepository.save(paiement);

        if (nouveauStatut != StatutPaiement.REUSSI) {
            return;
        }

        // Verrou pessimiste : la conversation peut recevoir des messages en parallele au meme instant
        Conversation conversation = conversationRepository.findByCodeForUpdate(paiement.getConversation().getCode())
                .orElseThrow(() -> new RessourceNonTrouveeException("Conversation introuvable"));

        Formule formule = paiement.getFormule();
        LocalDateTime nouvelleExpiration = LocalDateTime.now().plusMinutes(formule.getDureeMinutes());

        conversation.setFormule(formule);
        conversation.setForfaitExpireLe(nouvelleExpiration);
// Reinitialise le cycle de notification pour ce nouveau forfait -
// sans ca, un forfait renouvele apres expiration ne redeclencherait jamais
// l'avertissement 5 min ni la notification de fin (deja marques "envoyes" precedemment).
        conversation.setEtatNotificationForfait(sn.parlemoi.backend.enums.EtatNotificationForfait.AUCUNE);
        conversationRepository.save(conversation);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getCode(),
                new EvenementForfaitActive("FORFAIT_ACTIVE", formule.getNom(), nouvelleExpiration)
        );
    }

    private PaiementResponse versReponse(Paiement paiement, String urlPaiement) {
        return new PaiementResponse(
                paiement.getId(),
                paiement.getProvider(),
                paiement.getStatut(),
                urlPaiement,
                paiement.getMontant(),
                paiement.getDevise()
        );
    }

    private record EvenementForfaitActive(String type, String formuleNom, LocalDateTime forfaitExpireLe) {}
}