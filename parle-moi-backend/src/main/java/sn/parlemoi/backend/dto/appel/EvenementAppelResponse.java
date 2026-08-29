package sn.parlemoi.backend.dto.appel;

public record EvenementAppelResponse(
        String type,
        String contenu,
        String emetteur // "UTILISATEUR" ou "ECOUTANT" - pour que le destinataire ignore ses propres echos
) {}