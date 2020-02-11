package org.hawkular.alerts.api.services;

import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.paging.Page;
import org.hawkular.alerts.api.model.paging.Pager;
import org.hawkular.alerts.api.model.trigger.Trigger;

/**
 * A interface used to send actions.
 *
 * Actions will be created inside of the alerts engine and will be delivered using a chain of ActionListener listeners.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface ActionsService {

    /**
     * Generate and send an action to be processed by the plugins architecture.
     *
     * @param trigger Trigger generating the action
     * @param event Event payload of the action
     */
    void send(final Trigger trigger, final Event event);

    /**
     * Update the result of an action.
     *
     * @param action Action
     */
    void updateResult(Action action);

    /**
     * Notify the action plugin that the results can be sent forward, fire cycle is done
     */
    void flush();

    /**
     * @param tenantId Tenant where actions are stored
     * @param criteria If null returns all actions for the tenant (not recommended)
     * @param pager Paging requirement for fetching actions. Optional. Return all if null.
     * @return NotNull, can be empty.
     * @throws Exception on any problem
     */
    Page<Action> getActions(String tenantId, ActionsCriteria criteria, Pager pager) throws Exception;

    /**
     * Delete the requested Actions from the history, as described by the provided criteria.
     *
     * @param tenantId Tenant where actions are stored
     * @param criteria If null deletes all actions (not recommended)
     * @return number of actions deleted
     * @throws Exception on any problem
     */
    int deleteActions(String tenantId, ActionsCriteria criteria) throws Exception;

    /**
     * Register a listener that will process actions.
     * ActionListeners are responsible to connect Actions with plugins.
     *
     * @param listener the listener
     */
    void addListener(ActionListener listener);

}
