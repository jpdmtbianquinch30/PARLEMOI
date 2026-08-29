package sn.parlemoi.backend.dto.admin;

import java.math.BigDecimal;

public record StatsResponse(
        long conversationsAujourdhui,
        long conversationsSemaine,
        long conversationsEnAttente,
        long conversationsTerminees,
        long ecoutantsEnLigne,
        long paiementsReussis,
        BigDecimal revenuTotal
) {}