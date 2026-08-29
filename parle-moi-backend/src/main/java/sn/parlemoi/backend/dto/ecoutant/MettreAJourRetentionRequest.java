package sn.parlemoi.backend.dto.ecoutant;

import sn.parlemoi.backend.enums.DureeRetention;

public record MettreAJourRetentionRequest(DureeRetention dureeRetentionMessages) {}