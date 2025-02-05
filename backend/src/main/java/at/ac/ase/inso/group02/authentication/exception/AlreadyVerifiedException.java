package at.ac.ase.inso.group02.authentication.exception;

/**
 * thrown when a user who is already verified tries to verify himself again
 */
public class AlreadyVerifiedException extends RuntimeException {
    public AlreadyVerifiedException(String message) {
        super(message);
    }

    public AlreadyVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
