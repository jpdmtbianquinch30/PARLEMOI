package sn.parlemoi.backend.dto.message;

import java.time.LocalDateTime;

public record EvenementForfaitResponse(
        String type,
        String message,
        LocalDateTime forfaitExpireLe
) {}