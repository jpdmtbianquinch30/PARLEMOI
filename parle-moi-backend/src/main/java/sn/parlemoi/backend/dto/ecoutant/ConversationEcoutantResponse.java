package sn.parlemoi.backend.dto.ecoutant;

import sn.parlemoi.backend.enums.StatutConversation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ConversationEcoutantResponse(
        String code,
        StatutConversation statut,
        Integer positionFileAttente,
        String sujetOptionnel,
        boolean forfaitActif,
        String formuleNom,
        LocalDateTime forfaitExpireLe,
        LocalDate dateProgrammee,
        LocalTime heureProgrammee,
        LocalDateTime creeLe
) {}