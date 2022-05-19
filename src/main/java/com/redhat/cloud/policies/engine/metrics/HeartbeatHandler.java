package com.redhat.cloud.policies.engine.metrics;

import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Log a heart beat message with some stats
 */
@ApplicationScoped
public class HeartbeatHandler {

    private final Logger log = Logger.getLogger(this.getClass().getSimpleName());


    // The following metrics are defined in process.Receiver
    @Inject
    @Metric(absolute = true, name = "engine.input.processed", tags = {"queue=host-egress"})
    Counter incomingMessagesCount;

    @Inject
    @Metric(absolute = true, name = "engine.input.rejected", tags = {"queue=host-egress"})
    Counter rejectedCount;

    @Inject
    @Metric(absolute = true, name = "engine.input.rejected.detail", tags = {"queue=host-egress","reason=type"})
    Counter rejectedCountType;

    @Inject
    @Metric(absolute = true, name = "engine.input.rejected.detail", tags = {"queue=host-egress","reason=noHost"})
    Counter rejectedCountHost;

    @Inject
    @Metric(absolute = true, name = "engine.input.rejected.detail", tags = {"queue=host-egress","reason=reporter"})
    Counter rejectedCountReporter;

    @Inject
    @Metric(absolute = true, name = "engine.input.rejected.detail", tags = {"queue=host-egress","reason=insightsId"})
    Counter rejectedCountId;


    @Inject
    @Metric(absolute = true, name = "engine.input.processed.errors", tags = {"queue=host-egress"})
    Counter processingErrors;


    @Scheduled(every = "1h")
    void printHeartbeat() {

        String msg = String.format("Heartbeat: processed %d, rejected %d (t=%d, h=%d, r=%d, i=%d), process errors %d",
                incomingMessagesCount.getCount(),
                rejectedCount.getCount(),
                rejectedCountType.getCount(),
                rejectedCountHost.getCount(),
                rejectedCountReporter.getCount(),
                rejectedCountId.getCount(),
                processingErrors.getCount());

        log.info(msg);


    }
}
