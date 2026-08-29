package sn.parlemoi.backend.exception;

public class CompteDesactiveException extends RuntimeException {
    public CompteDesactiveException() {
        super("Ce compte a ete desactive. Contactez un administrateur.");
    }
}