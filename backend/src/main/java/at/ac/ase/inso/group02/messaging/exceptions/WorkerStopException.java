package at.ac.ase.inso.group02.messaging.exceptions;

/**
 * thrown when the shutdown of a worker has fails, which e.g. indicates that a RabbitMQ queue could not be deleted
 */
public class WorkerStopException extends RuntimeException {
    public WorkerStopException(String message) {
        super(message);
    }

    public WorkerStopException(String message, Throwable cause) {
        super(message, cause);
    }
}
