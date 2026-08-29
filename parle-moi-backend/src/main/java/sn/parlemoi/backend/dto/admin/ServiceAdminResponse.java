package sn.parlemoi.backend.dto.admin;

import java.util.List;

public record ServiceAdminResponse(
        String id,
        String nom,
        String description,
        boolean actif,
        List<FormuleAdminResponse> formules
) {}