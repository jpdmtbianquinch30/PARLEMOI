package sn.parlemoi.backend.dto.paiement;

import sn.parlemoi.backend.enums.StatutPaiement;

// Format interne neutre - chaque provider traduit son propre format vers celui-ci
public record WebhookEvenement(
        String referenceProvider,
        StatutPaiement statut
) {}