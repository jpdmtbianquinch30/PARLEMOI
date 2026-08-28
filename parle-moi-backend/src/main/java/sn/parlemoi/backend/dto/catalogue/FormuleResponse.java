package sn.parlemoi.backend.dto.catalogue;

import java.math.BigDecimal;

public record FormuleResponse(
        String id,
        String nom,
        String description,
        Integer dureeMinutes,
        BigDecimal prix,
        String devise
) {}