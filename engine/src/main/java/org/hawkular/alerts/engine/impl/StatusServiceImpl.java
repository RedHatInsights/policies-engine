package org.hawkular.alerts.engine.impl;

import java.util.Map;

import org.hawkular.alerts.api.services.StatusService;
import org.hawkular.alerts.engine.service.PartitionManager;

/**
 * An implementation of {@link org.hawkular.alerts.api.services.StatusService}.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class StatusServiceImpl implements StatusService {

    PartitionManager partitionManager;

    public void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    @Override
    public boolean isStarted() {
        // TODO [lponce] this test is quite simple and with a different backend perhaps it doesnt give enough info
        // TODO Perhaps on this call is better to call backend and check which is working correctly
        // TODO i.e. SELECT 1 FROM Test or similar kind of test probe
        return true;
    }

    @Override
    public boolean isDistributed() {
        return partitionManager.isDistributed();
    }

    @Override
    public Map<String, String> getDistributedStatus() {
        return partitionManager.getStatus();
    }
}
