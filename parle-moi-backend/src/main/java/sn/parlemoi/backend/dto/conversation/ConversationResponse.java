package sn.parlemoi.backend.dto.conversation;

import sn.parlemoi.backend.enums.StatutConversation;

import java.time.LocalDateTime;

public record ConversationResponse(
        String code,
        StatutConversation statut,
        Integer positionFileAttente,
        int nbMessagesGratuitsUtilises,
        int nbMessagesGratuitsRestants,
        LocalDateTime expireLe
) {}