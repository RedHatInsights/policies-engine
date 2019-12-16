package org.hawkular.alerts.api.services;

import java.util.Map;

/**
 * Interface that allows to check main status of Hawkular Alerting system
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface StatusService {

    /**
     * @return true if system is initialized
     *         false otherwise
     */
    boolean isStarted();

    /**
     * @return true if system is running on a distributed scenario.
     *         false if system is running on a standalone scenario.
     */
    boolean isDistributed();

    /**
     * Show additional information about distributed status.
     * In distributed scenarios
     *  - getDistributedStatus().get("currentNode") returns a string with the identifier of the current node
     *  - getDistributedStatus().get("members") returns a string with a list comma identifiers of the topology nodes
     *    at the moment of the call
     * In standalone scenarios getDistributedStatus() returns an empty map.
     *
     * @return Map with currentNode and members information for distributed scenarios
     */
    Map<String, String> getDistributedStatus();
}
