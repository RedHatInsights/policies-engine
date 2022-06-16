package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.policies.engine.db.StatelessSessionFactory;
import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * This is the main process for Policies. It ingests data from Kafka, enriches it with information from
 * insights-host-inventory and then sends it for event processing in the engine.
 */
@ApplicationScoped
public class Receiver {

    public static final String EVENTS_CHANNEL = "events";

    @Inject
    PayloadParser payloadParser;

    @Inject
    StatelessSessionFactory statelessSessionFactory;

    @Inject
    EventProcessor eventProcessor;

    @Incoming(EVENTS_CHANNEL)
    @Blocking
    public void process(String payload) {
        Log.tracef("Received payload: %s", payload);
        payloadParser.parse(payload).ifPresent(event -> {
            statelessSessionFactory.withSession(statelessSession -> {
                eventProcessor.process(event);
            });
        });
    }
}
