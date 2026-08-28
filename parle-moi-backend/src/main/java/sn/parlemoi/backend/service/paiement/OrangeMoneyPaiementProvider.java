package sn.parlemoi.backend.service.paiement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.parlemoi.backend.dto.paiement.WebhookEvenement;
import sn.parlemoi.backend.entity.Paiement;

import java.util.Map;

@Component
public class OrangeMoneyPaiementProvider implements PaiementProvider {

    @Value("${paiement.orange-money.merchant-key:}")
    private String merchantKey;

    @Override
    public String nom() {
        return "ORANGE_MONEY";
    }

    @Override
    public ResultatInitiation initier(Paiement paiement, String urlSucces, String urlEchec) {
        if (merchantKey.isBlank()) {
            throw new IllegalStateException(
                    "Orange Money non configure - le statut marchand direct chez Orange demande 3-6 semaines " +
                            "de validation ; en pratique on passera probablement par un agregateur (PayDunya/CinetPay). " +
                            "En dev, active paiement.mode-simulation=true pour eviter cet appel."
            );
        }
        // 1. POST /oauth/v3/token (Basic Auth) -> access_token
        // 2. POST /orange-money-webpay/{pays}/v1/webpayment -> pay_token + payment_url
        //    avec notif_token genere ici et stocke, compare au retour webhook (pas de HMAC chez Orange)
        // TODO cablage reel une fois le choix agregateur tranche
        throw new UnsupportedOperationException("Integration Orange Money reelle pas encore cablee");
    }

    @Override
    public boolean verifierWebhook(String payloadBrut, Map<String, String> entetes) {
        // Orange Money ne signe pas ses webhooks (contrairement a Wave) : la seule protection
        // est de comparer le notif_token recu a celui genere et stocke lors de l'initiation.
        // Implementation exacte a faire une fois le format de payload confirme en sandbox.
        throw new UnsupportedOperationException("Verification webhook Orange Money pas encore cablee");
    }

    @Override
    public WebhookEvenement parserWebhook(String payloadBrut) {
        throw new UnsupportedOperationException("Parsing webhook Orange Money pas encore cable");
    }
}