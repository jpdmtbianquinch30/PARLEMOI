package sn.parlemoi.backend.service.paiement;

import sn.parlemoi.backend.dto.paiement.WebhookEvenement;
import sn.parlemoi.backend.entity.Paiement;

import java.util.Map;

public interface PaiementProvider {

    String nom();

    // Initie la transaction cote provider, retourne l'URL vers laquelle rediriger l'utilisateur
    // et la reference technique du provider a stocker sur le Paiement
    ResultatInitiation initier(Paiement paiement, String urlSucces, String urlEchec);

    // Verifie l'authenticite du webhook AVANT tout traitement - jamais faire confiance
    // a un payload non verifie, quel que soit le provider
    boolean verifierWebhook(String payloadBrut, Map<String, String> entetes);

    WebhookEvenement parserWebhook(String payloadBrut);

    record ResultatInitiation(String referenceProvider, String urlPaiement) {}
}