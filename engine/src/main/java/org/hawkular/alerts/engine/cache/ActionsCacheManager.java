package org.hawkular.alerts.engine.cache;

import static org.hawkular.alerts.api.services.DefinitionsEvent.Type.ACTION_DEFINITION_CREATE;
import static org.hawkular.alerts.api.services.DefinitionsEvent.Type.ACTION_DEFINITION_REMOVE;
import static org.hawkular.alerts.api.services.DefinitionsEvent.Type.ACTION_DEFINITION_UPDATE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.services.DefinitionsService;

import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.infinispan.Cache;

/**
 * It manages the cache of global actions.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class ActionsCacheManager {
    private final AlertingLogger log = MsgLogging.getMsgLogger(AlertingLogger.class, ActionsCacheManager.class);

    DefinitionsService definitions;

    private Cache<ActionKey, ActionDefinition> globalActionsCache;

    public void setDefinitions(DefinitionsService definitions) {
        this.definitions = definitions;
    }

    public void setGlobalActionsCache(Cache<ActionKey, ActionDefinition> globalActionsCache) {
        this.globalActionsCache = globalActionsCache;
    }

    public void init() {
        log.infoInitActionsCache();

        globalActionsCache.clear();

        initialCacheUpdate();

        definitions.registerListener(events -> {
            events.stream().forEach(event -> {
                ActionKey key = new ActionKey(event.getTargetTenantId(), event.getActionPlugin(), event.getTargetId());
                switch (event.getType()) {
                    case ACTION_DEFINITION_CREATE:
                    case ACTION_DEFINITION_UPDATE:
                        ActionDefinition actionDefinition = event.getActionDefinition();
                        if (actionDefinition.isGlobal()) {
                            globalActionsCache.put(key, actionDefinition);
                        }
                        break;
                    case ACTION_DEFINITION_REMOVE:
                        globalActionsCache.remove(key);
                }
            });
        }, ACTION_DEFINITION_CREATE, ACTION_DEFINITION_REMOVE, ACTION_DEFINITION_UPDATE);
    }

    public boolean hasGlobalActions() {
        return !globalActionsCache.isEmpty();
    }

    public Collection<ActionDefinition> getGlobalActions(String tenantId) {
        List<ActionDefinition> globalActions = new ArrayList<>();
        for (ActionKey key : globalActionsCache.keySet()) {
            if (key.getTenantId().equals(tenantId)) {
                globalActions.add(globalActionsCache.get(key));
            }
        }
        return globalActions;
    }

    private void initialCacheUpdate() {
        try {
            log.debug("Initial ActionsCacheManager update in progress..");

            globalActionsCache.startBatch();
            Collection<ActionDefinition> actionDefinitions = definitions.getAllActionDefinitions();
            for (ActionDefinition actionDefinition : actionDefinitions) {
                if (actionDefinition.isGlobal()) {
                    ActionKey key = new ActionKey(actionDefinition.getTenantId(),
                            actionDefinition.getActionPlugin(),
                            actionDefinition.getActionId());
                    globalActionsCache.put(key, actionDefinition);
                }
            }
            globalActionsCache.endBatch(true);
        } catch (Exception e) {
            log.error("Failed to load global actions", e);
            globalActionsCache.endBatch(false);
            return;
        }
    }

    public static class ActionKey implements Serializable {
        private String tenantId;
        private String actionPlugin;
        private String actionId;

        public ActionKey(String tenantId, String actionPlugin, String actionId) {
            this.tenantId = tenantId;
            this.actionPlugin = actionPlugin;
            this.actionId = actionId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getActionPlugin() {
            return actionPlugin;
        }

        public void setActionPlugin(String actionPlugin) {
            this.actionPlugin = actionPlugin;
        }

        public String getActionId() {
            return actionId;
        }

        public void setActionId(String actionId) {
            this.actionId = actionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ActionKey actionKey = (ActionKey) o;

            if (tenantId != null ? !tenantId.equals(actionKey.tenantId) : actionKey.tenantId != null) return false;
            if (actionPlugin != null ? !actionPlugin.equals(actionKey.actionPlugin) : actionKey.actionPlugin != null)
                return false;
            return actionId != null ? actionId.equals(actionKey.actionId) : actionKey.actionId == null;
        }

        @Override
        public int hashCode() {
            int result = tenantId != null ? tenantId.hashCode() : 0;
            result = 31 * result + (actionPlugin != null ? actionPlugin.hashCode() : 0);
            result = 31 * result + (actionId != null ? actionId.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ActionKey{" +
                    "tenantId='" + tenantId + '\'' +
                    ", actionPlugin='" + actionPlugin + '\'' +
                    ", actionId='" + actionId + '\'' +
                    '}';
        }
    }
}
