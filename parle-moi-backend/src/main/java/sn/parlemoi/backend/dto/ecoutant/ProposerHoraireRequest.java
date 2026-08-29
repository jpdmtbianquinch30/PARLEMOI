package sn.parlemoi.backend.dto.ecoutant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ProposerHoraireRequest(
        @NotNull @Future(message = "La date doit etre dans le futur")
        LocalDate dateProgrammee,

        @NotNull
        LocalTime heureProgrammee
) {}