package sn.parlemoi.backend.exception;

public class ConversationClotureeException extends RuntimeException {
    public ConversationClotureeException() {
        super("Cette conversation est cloturee et ne peut plus etre modifiee");
    }
}