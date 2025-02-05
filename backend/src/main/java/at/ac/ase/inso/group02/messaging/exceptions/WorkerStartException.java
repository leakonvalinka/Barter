package at.ac.ase.inso.group02.messaging.exceptions;

/**
 * thrown when creation of the WebSocket-RabbitMQ worker fails
 */
public class WorkerStartException extends RuntimeException {
    public WorkerStartException(String message) {
        super(message);
    }

    public WorkerStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
