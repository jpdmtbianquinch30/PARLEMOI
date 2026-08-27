package sn.parlemoi.backend.exception;

public class ReservationConflitException extends RuntimeException {
    public ReservationConflitException(String message) {
        super(message);
    }
}