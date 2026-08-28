package sn.parlemoi.backend.exception;

import java.time.LocalDateTime;

public class CompteVerrouilleException extends RuntimeException {
    public CompteVerrouilleException(LocalDateTime verrouilleJusqua) {
        super("Compte temporairement verrouille suite a trop de tentatives. Reessayez apres " + verrouilleJusqua);
    }
}