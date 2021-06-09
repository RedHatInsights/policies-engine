package com.redhat.cloud.policies.engine.process;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

import static io.smallrye.reactive.messaging.connectors.InMemoryConnector.switchIncomingChannelsToInMemory;
import static io.smallrye.reactive.messaging.connectors.InMemoryConnector.switchOutgoingChannelsToInMemory;

public class ReactiveMessagingLifecycleManager implements QuarkusTestResourceLifecycleManager {

    public static final String EVENTS_CHANNEL = "events";
    public static final String WEBHOOK_CHANNEL = "webhook";

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        /*
         * We'll use in-memory Reactive Messaging connectors to receive and send payloads.
         * See https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/2/testing/testing.html
         */
        env.putAll(switchIncomingChannelsToInMemory(EVENTS_CHANNEL));
        env.putAll(switchOutgoingChannelsToInMemory(WEBHOOK_CHANNEL));
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
