package at.ac.ase.inso.group02.authentication.exception;

/**
 * thrown when a user tries to login with an incorrect password or username/email that does not exist
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
