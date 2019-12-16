package org.hawkular.alerts.api.exception;


/**
 * Indicates a query for expected data did not return any results.
 */
public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }
}