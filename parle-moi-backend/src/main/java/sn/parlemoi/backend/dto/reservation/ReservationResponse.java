package sn.parlemoi.backend.dto.reservation;

import sn.parlemoi.backend.enums.StatutReservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        String code,
        String formuleNom,
        LocalDate dateReservation,
        LocalTime heureReservation,
        StatutReservation statut
) {}