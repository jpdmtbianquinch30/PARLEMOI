package sn.parlemoi.backend.dto.message;

public record EvenementSystemeResponse(
        String type,
        String message,
        Integer nbMessagesGratuitsRestants
) {}