package org.hawkular.alerts.api.services;

import org.hawkular.alerts.api.model.action.Action;

/**
 * A listener that will process a action sent to the ActionsService.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface ActionListener {

    /**
     * Process a action sent to {@link ActionListener}.
     *
     * @param action Action to be processed.
     */
    void process(Action action);
}
