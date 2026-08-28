package sn.parlemoi.backend.security;

import sn.parlemoi.backend.enums.RoleEcoutant;

import java.security.Principal;

public record EcoutantPrincipal(String ecoutantId, RoleEcoutant role) implements Principal {
    @Override
    public String getName() {
        return "ecoutant:" + ecoutantId;
    }
}