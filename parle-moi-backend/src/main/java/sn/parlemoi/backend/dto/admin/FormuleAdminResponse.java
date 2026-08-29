package sn.parlemoi.backend.dto.admin;

import java.math.BigDecimal;

public record FormuleAdminResponse(
        String id,
        String nom,
        String description,
        Integer dureeMinutes,
        BigDecimal prix,
        String devise,
        boolean actif
) {}