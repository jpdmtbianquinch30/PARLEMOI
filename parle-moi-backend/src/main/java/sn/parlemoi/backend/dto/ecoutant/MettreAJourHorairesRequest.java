package sn.parlemoi.backend.dto.ecoutant;

import jakarta.validation.constraints.Pattern;

public record MettreAJourHorairesRequest(
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Format attendu : HH:mm")
        String horaireDebut,

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Format attendu : HH:mm")
        String horaireFin
) {}