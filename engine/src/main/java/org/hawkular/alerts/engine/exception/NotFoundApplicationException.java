package org.hawkular.alerts.engine.exception;

import org.hawkular.alerts.api.exception.NotFoundException;

/**
 * Indicates a query for expected data did not return any results.
 */
public class NotFoundApplicationException extends NotFoundException {
    private static final long serialVersionUID = 1L;

    // Default no-arg constructor required by JAXB
    public NotFoundApplicationException() {
    }

    /**
     * Create an exception indicating the resource with the specified id was not found.
     */
    public NotFoundApplicationException(String type, String tenantId, String id) {
        super("Failed to fetch [" + type + "] with tenant/id [" + tenantId + "/" + id + "]");
    }

    public NotFoundApplicationException(String message) {
        super(message);
    }
}