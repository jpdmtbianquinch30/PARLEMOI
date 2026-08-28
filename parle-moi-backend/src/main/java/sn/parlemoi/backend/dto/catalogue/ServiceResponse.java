package sn.parlemoi.backend.dto.catalogue;

import java.util.List;

public record ServiceResponse(
        String id,
        String nom,
        String description,
        List<FormuleResponse> formules
) {}