package at.ac.ase.inso.group02.exceptions;

/**
 * thrown when the system detects unauthenticated users manually, e.g.
 * when given a JWT for a user that just deleted their account
 */
public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException(String message) {
        super(message);
    }

    public UnauthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
