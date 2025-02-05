package at.ac.ase.inso.group02.exceptions;

public class UnauthorizedCreationException extends RuntimeException {
    public UnauthorizedCreationException(String message) {
        super(message);
    }

    public UnauthorizedCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
