package com.redhat.cloud.policies.engine.db.repositories;

import com.redhat.cloud.policies.engine.db.StatelessSessionFactory;
import com.redhat.cloud.policies.engine.db.entities.PoliciesHistoryEntry;
import com.redhat.cloud.policies.engine.process.Event;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import static com.redhat.cloud.policies.engine.process.PayloadParser.DISPLAY_NAME_FIELD;
import static com.redhat.cloud.policies.engine.process.PayloadParser.INVENTORY_ID_FIELD;

@ApplicationScoped
public class PoliciesHistoryRepository {

    @Inject
    StatelessSessionFactory statelessSessionFactory;

    public void create(UUID policyId, Event event) {
        try {
            PoliciesHistoryEntry historyEntry = new PoliciesHistoryEntry();
            historyEntry.setTenantId(event.getAccountId());
            historyEntry.setOrgId(event.getOrgId());
            historyEntry.setPolicyId(policyId.toString());
            historyEntry.setCtime(event.getCtime());
            getSingleTagValue(event, INVENTORY_ID_FIELD).ifPresent(historyEntry::setHostId);
            getSingleTagValue(event, DISPLAY_NAME_FIELD).ifPresent(historyEntry::setHostName);
            statelessSessionFactory.getCurrentSession().insert(historyEntry);
            Log.debugf("Created %s", historyEntry);
        } catch (Exception e) {
            Log.error("Policies history entry creation failed", e);
        }
    }

    private static Optional<String> getSingleTagValue(Event event, String tagKey) {
        Iterator<String> iterator = event.getTags().get(tagKey).iterator(); // .iterator() won't cause a NPE if tagKey is not found.
        if (iterator.hasNext()) {
            return Optional.ofNullable(iterator.next());
        } else {
            return Optional.empty();
        }
    }
}
