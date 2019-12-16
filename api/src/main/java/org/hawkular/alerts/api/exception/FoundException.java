package org.hawkular.alerts.api.exception;

/**
 * Indicates in an insert operation that the element to add exists on backend.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class FoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FoundException(){
    }

    public FoundException(String message) {
        super(message);
    }
}
