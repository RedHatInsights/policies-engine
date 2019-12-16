package org.hawkular.alerts.api.model.trigger;

/**
 * The type of a Group Trigger.
 *
 * @author jay shaughnessy
 * @author lucas ponce
 */
public enum TriggerType {
    STANDARD, // Deployed, individually managed trigger
    GROUP, // Undeployed, template-level definition for managing a group of member triggers
    DATA_DRIVEN_GROUP, // Like group, but members are generated automatically based on incoming data
    MEMBER, // Deployed, member trigger of a group
    ORPHAN // Member trigger not being managed by the group. It maintains it's group reference and can be un-orphaned.
}
