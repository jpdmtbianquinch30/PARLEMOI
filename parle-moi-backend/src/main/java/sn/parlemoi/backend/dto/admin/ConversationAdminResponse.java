package sn.parlemoi.backend.dto.admin;

import sn.parlemoi.backend.enums.StatutConversation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ConversationAdminResponse(
        String code,
        StatutConversation statut,
        String ecoutantNom,
        String formuleNom,
        boolean forfaitActif,
        LocalDate dateProgrammee,
        LocalTime heureProgrammee,
        LocalDateTime creeLe
) {}