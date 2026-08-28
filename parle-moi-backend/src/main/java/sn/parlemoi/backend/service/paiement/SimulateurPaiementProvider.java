package sn.parlemoi.backend.service.paiement;

import org.springframework.stereotype.Component;
import sn.parlemoi.backend.dto.paiement.WebhookEvenement;
import sn.parlemoi.backend.entity.Paiement;
import sn.parlemoi.backend.enums.StatutPaiement;

import java.util.Map;
import java.util.UUID;

// Utilise tant qu'aucun compte marchand reel (Wave/Orange Money) n'est valide.
// Reproduit le meme contrat que les vrais providers pour que le reste du systeme
// (PaiementService, webhook, activation du forfait) soit teste avec la logique definitive.
@Component
public class SimulateurPaiementProvider implements PaiementProvider {

    @Override
    public String nom() {
        return "SIMULATEUR";
    }

    @Override
    public ResultatInitiation initier(Paiement paiement, String urlSucces, String urlEchec) {
        String referenceSimulee = "SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        // Pas de vraie page de paiement - le frontend affichera un bouton "Simuler le paiement"
        // qui appelle POST /api/paiements/{id}/simulateur/confirmer
        return new ResultatInitiation(referenceSimulee, null);
    }

    @Override
    public boolean verifierWebhook(String payloadBrut, Map<String, String> entetes) {
        return true; // pas de webhook externe en mode simulateur, confirmation manuelle uniquement
    }

    @Override
    public WebhookEvenement parserWebhook(String payloadBrut) {
        throw new UnsupportedOperationException("Le simulateur ne recoit jamais de webhook externe");
    }
}