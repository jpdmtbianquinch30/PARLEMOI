package sn.parlemoi.backend.dto.appel;

public record SignalAppelRequest(
        String type,   // OFFRE, REPONSE, CANDIDAT, DEMARRER, ACCEPTER, REFUSER, RACCROCHER
        String contenu // SDP ou candidat ICE serialise en JSON, null pour DEMARRER/ACCEPTER/REFUSER/RACCROCHER
) {}