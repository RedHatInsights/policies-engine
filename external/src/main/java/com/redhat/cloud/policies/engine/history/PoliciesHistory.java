package com.redhat.cloud.policies.engine.history;

import org.hawkular.alerts.api.model.event.Alert;
import org.hawkular.alerts.api.services.PoliciesHistoryService;
import org.hibernate.Session;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.Optional;

@ApplicationScoped
public class PoliciesHistory implements PoliciesHistoryService {

    private static final Logger LOGGER = Logger.getLogger(PoliciesHistory.class);
    private static final String INVENTORY_ID_TAG_KEY = "inventory_id";
    private static final String DISPLAY_NAME_TAG_KEY = "display_name";

    @Inject
    Session session;

    @Override
    @Transactional
    @ActivateRequestContext
    public void put(Alert alert) {
        try {
            LOGGER.debugf("Creating %s from %s", PoliciesHistoryEntry.class.getSimpleName(), alert);
            PoliciesHistoryEntry historyEntry = new PoliciesHistoryEntry();
            historyEntry.setTenantId(alert.getTenantId());
            historyEntry.setPolicyId(alert.getTriggerId());
            historyEntry.setCtime(alert.getCtime());
            getSingleTagValue(alert, INVENTORY_ID_TAG_KEY).ifPresent(historyEntry::setHostId);
            getSingleTagValue(alert, DISPLAY_NAME_TAG_KEY).ifPresent(historyEntry::setHostName);
            session.persist(historyEntry);
            LOGGER.debugf("Persisted %s", historyEntry);
        } catch (Exception e) {
            LOGGER.error("Policies history entry persist failed", e);
        }
    }

    private static Optional<String> getSingleTagValue(Alert alert, String tagKey) {
        Iterator<String> iterator = alert.getTags().get(tagKey).iterator(); // .iterator() won't cause a NPE if tagKey is not found.
        if (iterator.hasNext()) {
            return Optional.ofNullable(iterator.next());
        } else {
            return Optional.empty();
        }
    }
}
