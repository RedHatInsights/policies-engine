package org.hawkular.alerts.actions.api;

/**
 * A sender interface that allows to a plugin to send operations message to the alerts engine.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface ActionPluginSender {

    /**
     * Factory to create new ActionResponseMessage messages.
     * There could be different implementation of messages depending on the context (bus, standalone) so
     * new instances of ActionResponseMessage should be created through this factory method.
     *
     * @param operation the type of operation
     * @return a new ActionResponseMessage
     */
    ActionResponseMessage createMessage(ActionResponseMessage.Operation operation);

    /**
     * Send a message to the engine.
     * Plugin should not have access to the implementation used.
     *
     * @param msg the response message to be sent
     * @throws Exception any problem
     */
    void send(ActionResponseMessage msg) throws Exception;
}
