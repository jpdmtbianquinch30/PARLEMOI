package sn.parlemoi.backend.exception;

public class RessourceNonTrouveeException extends RuntimeException {
    public RessourceNonTrouveeException(String message) {
        super(message);
    }
}