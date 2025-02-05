package at.ac.ase.inso.group02.exceptions;

/**
 * thrown when a user is authenticated but tries to do something they are not allowed to
 * e.g. editing a skill that they did not create themselves
 */
public class UnauthorizedModificationException extends RuntimeException {
    public UnauthorizedModificationException(String message) {
        super(message);
    }

    public UnauthorizedModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
