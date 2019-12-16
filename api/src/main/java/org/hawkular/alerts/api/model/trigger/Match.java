package org.hawkular.alerts.api.model.trigger;

/**
 * The policy used for deciding whether the trigger condition-set is satisfied.  Either <code>ALL<code> conditions must
 * evaluate to true, or whether <code>ANY<code> one is enough.
 *
 * @author jay shaughnessy
 * @author lucas ponce
 */
public enum Match {
    ALL, ANY
}
