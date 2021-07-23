package org.hawkular.alerts.api.services;

import org.hawkular.alerts.api.model.event.Alert;

public interface PoliciesHistoryService {

    /**
     * Adds an entry into the PostgreSQL policies history table for the given alert.
     */
    void put(Alert alert);
}
