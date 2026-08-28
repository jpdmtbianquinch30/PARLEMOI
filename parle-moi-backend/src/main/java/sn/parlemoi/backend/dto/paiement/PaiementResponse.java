package sn.parlemoi.backend.dto.paiement;

import sn.parlemoi.backend.enums.StatutPaiement;

import java.math.BigDecimal;

public record PaiementResponse(
        String paiementId,
        String provider,
        StatutPaiement statut,
        String urlPaiement,
        BigDecimal montant,
        String devise
) {}