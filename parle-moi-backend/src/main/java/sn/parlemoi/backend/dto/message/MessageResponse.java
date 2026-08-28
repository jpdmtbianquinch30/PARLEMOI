package sn.parlemoi.backend.dto.message;

import java.time.LocalDateTime;

public record MessageResponse(
        String id,
        String auteurType,
        String contenu,
        LocalDateTime envoyeLe
) {}