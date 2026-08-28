package sn.parlemoi.backend.security;

import java.security.Principal;

public record AnonymePrincipal(String code) implements Principal {
    @Override
    public String getName() {
        return "anonyme:" + code;
    }
}