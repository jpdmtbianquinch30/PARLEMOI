package sn.parlemoi.backend.dto.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreerReservationRequest(

        @NotBlank(message = "L'identifiant de la formule est obligatoire")
        String formuleId,

        @NotNull(message = "La date est obligatoire")
        @Future(message = "La date doit etre dans le futur")
        LocalDate dateReservation,

        @NotNull(message = "L'heure est obligatoire")
        LocalTime heureReservation,

        @Size(max = 500, message = "Le sujet ne peut pas depasser 500 caracteres")
        String sujetOptionnel

) {}