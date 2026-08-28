package sn.parlemoi.backend.service.paiement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.parlemoi.backend.dto.paiement.WebhookEvenement;
import sn.parlemoi.backend.entity.Paiement;
import sn.parlemoi.backend.enums.StatutPaiement;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Component
public class WavePaiementProvider implements PaiementProvider {

    @Value("${paiement.wave.secret-key:}")
    private String secretKey;

    @Value("${paiement.wave.webhook-secret:}")
    private String webhookSecret;

    @Override
    public String nom() {
        return "WAVE";
    }

    @Override
    public ResultatInitiation initier(Paiement paiement, String urlSucces, String urlEchec) {
        if (secretKey.isBlank()) {
            throw new IllegalStateException(
                    "Wave non configure - definir paiement.wave.secret-key (voir docs.wave.com/checkout). " +
                            "En dev, active paiement.mode-simulation=true pour eviter cet appel."
            );
        }
        // POST https://api.wave.com/v1/checkout/sessions
        // Corps : { amount, currency, client_reference: paiement.getId(), success_url, error_url }
        // Reponse : { id, wave_launch_url }
        // TODO cablage reel une fois le compte Wave Business valide (48-72h de validation KYC)
        throw new UnsupportedOperationException("Integration Wave reelle pas encore cablee");
    }

    @Override
    public boolean verifierWebhook(String payloadBrut, Map<String, String> entetes) {
        // Format Wave-Signature: "t=<timestamp>,v1=<hmac_sha256_hex>"
        String enTete = entetes.get("wave-signature");
        if (enTete == null || webhookSecret.isBlank()) {
            return false;
        }
        try {
            String[] parties = enTete.split(",");
            String timestamp = parties[0].substring(2);
            String signatureRecue = parties[1].substring(3);

            String donneesSignees = timestamp + payloadBrut;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signatureCalculee = HexFormat.of().formatHex(mac.doFinal(donneesSignees.getBytes(StandardCharsets.UTF_8)));

            return MessageDigest.isEqual(
                    signatureCalculee.getBytes(StandardCharsets.UTF_8),
                    signatureRecue.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public WebhookEvenement parserWebhook(String payloadBrut) {
        // A implementer avec Jackson une fois le format reel teste en sandbox :
        // { "type": "checkout.session.completed", "data": { "id": "...", "client_reference": "..." } }
        throw new UnsupportedOperationException("Parsing webhook Wave pas encore cable");
    }
}