package at.ac.ase.inso.group02.authentication.exception;

/**
 * thrown when a user tries to register with an email that already exists
 */
public class EMailInUseException extends RuntimeException {
    public EMailInUseException(String message) {
        super(message);
    }

    public EMailInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
