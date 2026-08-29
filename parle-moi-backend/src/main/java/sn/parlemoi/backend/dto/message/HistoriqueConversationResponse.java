package sn.parlemoi.backend.dto.message;

import sn.parlemoi.backend.dto.conversation.ConversationResponse;

import java.util.List;

public record HistoriqueConversationResponse(
        ConversationResponse conversation,
        List<MessageResponse> messages
) {}