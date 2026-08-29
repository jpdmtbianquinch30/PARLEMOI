package sn.parlemoi.backend.dto.ecoutant;

import sn.parlemoi.backend.enums.DureeRetention;

import java.time.LocalTime;

public record EcoutantProfilResponse(
        String id,
        String nom,
        String email,
        boolean enLigne,
        LocalTime horaireDebut,
        LocalTime horaireFin,
        DureeRetention dureeRetentionMessages
) {}