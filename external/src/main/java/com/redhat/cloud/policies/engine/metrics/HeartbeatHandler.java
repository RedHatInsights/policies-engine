package com.redhat.cloud.policies.engine.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Log a heart beat message with some stats
 */
@ApplicationScoped
public class HeartbeatHandler {

    private final Logger log = Logger.getLogger(this.getClass().getSimpleName());

    Counter incomingMessagesCount;
    Counter rejectedCount;
    Counter rejectedCountType;
    Counter rejectedCountHost;
    Counter rejectedCountReporter;
    Counter rejectedCountId;
    Counter processingErrors;

    public HeartbeatHandler(MeterRegistry registry) {
        incomingMessagesCount = Counter.builder("engine.input.processed").tags("queue=host-egress").register(registry);
        rejectedCount = Counter.builder("engine.input.rejected").tags("queue=host-egress").register(registry);
        rejectedCountType = Counter.builder("engine.input.rejected.detail").tags("queue=host-egress","reason=type").register(registry);

        rejectedCountHost = Counter.builder("engine.input.rejected.detail").tags("queue=host-egress","reason=noHost").register(registry);
        rejectedCountReporter = Counter.builder("engine.input.rejected.detail").tags("queue=host-egress","reason=reporter").register(registry);
        processingErrors = Counter.builder("engine.input.processed.errors").tags("queue=host-egress").register(registry);
        rejectedCountId = Counter.builder("engine.input.rejected.detail").tags("queue=host-egress","reason=insightsId").register(registry);
    }

    @Scheduled(every = "1h")
    void printHeartbeat() {
        String msg = String.format("Heartbeat: processed %d, rejected %d (t=%d, h=%d, r=%d, i=%d), process errors %d",
                incomingMessagesCount.count(),
                rejectedCount.count(),
                rejectedCountType.count(),
                rejectedCountHost.count(),
                rejectedCountReporter.count(),
                rejectedCountId.count(),
                processingErrors.count());
        log.info(msg);
    }
}
