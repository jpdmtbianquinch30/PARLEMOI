package sn.parlemoi.backend.dto.appel;

import java.util.List;

public record TurnCredentialsResponse(
        String username,
        String credential,
        long ttlSecondes,
        List<String> urls
) {}