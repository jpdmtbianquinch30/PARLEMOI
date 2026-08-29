package sn.parlemoi.backend.dto.admin;

import sn.parlemoi.backend.enums.DureeRetention;

import java.time.LocalTime;

public record EcoutantAdminResponse(
        String id,
        String email,
        String nom,
        boolean actif,
        boolean enLigne,
        LocalTime horaireDebut,
        LocalTime horaireFin,
        DureeRetention dureeRetentionMessages
) {}