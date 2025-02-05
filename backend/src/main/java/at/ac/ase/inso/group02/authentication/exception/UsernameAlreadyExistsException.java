package at.ac.ase.inso.group02.authentication.exception;

/**
 * thrown when a user tries to register with a username that already exists
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
