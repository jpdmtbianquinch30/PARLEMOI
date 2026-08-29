package sn.parlemoi.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record ModifierEcoutantRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        boolean actif

) {}