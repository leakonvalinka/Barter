package at.ac.ase.inso.group02.messaging.exceptions;

/**
 * thrown when the publishing of a new chat-message failed
 */
public class ChatMessagePublishException extends RuntimeException {
    public ChatMessagePublishException(String message) {
        super(message);
    }

    public ChatMessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
