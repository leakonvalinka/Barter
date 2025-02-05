package at.ac.ase.inso.group02.images.exceptions;

/**
 * thrown when image data could not be loaded
 */
public class ImageDataRetrievalError extends RuntimeException {
    public ImageDataRetrievalError(String message) {
        super(message);
    }

    public ImageDataRetrievalError(String message, Throwable cause) {
        super(message, cause);
    }
}
