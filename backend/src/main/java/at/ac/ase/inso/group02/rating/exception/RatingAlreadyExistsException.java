package at.ac.ase.inso.group02.rating.exception;

/**
 * thrown when a specific rating of an exchange already exists and should be modified (instead of another one created)
 */
public class RatingAlreadyExistsException extends RuntimeException {
    public RatingAlreadyExistsException(String message) {
        super(message);
    }

    public RatingAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
