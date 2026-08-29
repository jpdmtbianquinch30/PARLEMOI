package sn.parlemoi.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sn.parlemoi.backend.dto.appel.TurnCredentialsResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class TurnCredentialsService {

    private static final long DUREE_VALIDITE_SECONDES = 3600; // 1h - largement suffisant pour la duree d'un appel

    private final String secret;
    private final String realm;
    private final String host;

    public TurnCredentialsService(
            @Value("${turn.secret}") String secret,
            @Value("${turn.realm}") String realm,
            @Value("${turn.host}") String host
    ) {
        this.secret = secret;
        this.realm = realm;
        this.host = host;
    }

    // Identifiants ephemeres (norme TURN REST API, utilisee par Twilio/Xirsys) :
    // username = "<timestamp_expiration>:<label>", credential = HMAC-SHA1(secret, username) en base64.
    // Jamais d'identifiants statiques exposes au client - ils expirent et ne sont valables
    // que pour la fenetre de temps encodee dans le username lui-meme.
    public TurnCredentialsResponse genererPour(String labelConversation) {
        long expiration = Instant.now().getEpochSecond() + DUREE_VALIDITE_SECONDES;
        String username = expiration + ":" + labelConversation;
        String credential = hmacSha1Base64(username);

        List<String> urls = List.of(
                "stun:" + host + ":3478",
                "turn:" + host + ":3478?transport=udp",
                "turn:" + host + ":3478?transport=tcp"
        );

        return new TurnCredentialsResponse(username, credential, DUREE_VALIDITE_SECONDES, urls);
    }

    private String hmacSha1Base64(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signature = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de generer les identifiants TURN", e);
        }
    }
}