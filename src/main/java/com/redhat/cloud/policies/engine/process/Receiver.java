package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.policies.engine.db.StatelessSessionFactory;
import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.kafka.Record;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

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
    @ActivateRequestContext
    public void process(Record<String, String> record) {
        String payload = record.value();
        String key = record.key();

        Log.tracef("Message %s: Received payload: %s", key, payload);
        try {
            payloadParser.parse(key, payload).ifPresent(event -> {
                statelessSessionFactory.withSession(statelessSession -> {
                    eventProcessor.process(event);
                });
            });
        } catch (Exception e) {
            if (Log.isDebugEnabled()) {
                /*
                 * When the DEBUG log level is enabled, the log entry will include the payload.
                 * The entry is still logged at ERROR level because the dev team needs to be alerted through Sentry that an issue happened.
                 */
                Log.errorf(e, "Message %s: Payload processing failed: %s", key, payload);
            } else {
                Log.errorf(e, "Message %s: Payload processing failed. Set the log level to DEBUG to print the payload in the logs.", key);
            }
        }
    }
}
