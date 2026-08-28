package sn.parlemoi.backend.dto.conversation;

import jakarta.validation.constraints.Size;

public record DemarrerConversationRequest(
        @Size(max = 500, message = "Le sujet ne peut pas depasser 500 caracteres")
        String sujetOptionnel
) {}