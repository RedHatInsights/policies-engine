package com.redhat.cloud.policies.engine;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static com.redhat.cloud.policies.engine.process.NotificationSender.WEBHOOK_CHANNEL;
import static com.redhat.cloud.policies.engine.process.Receiver.EVENTS_CHANNEL;
import static io.smallrye.reactive.messaging.memory.InMemoryConnector.switchIncomingChannelsToInMemory;
import static io.smallrye.reactive.messaging.memory.InMemoryConnector.switchOutgoingChannelsToInMemory;

public class TestLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(
        DockerImageName.parse("docker.io/postgres").asCompatibleSubstituteFor("postgres")
    );

    @Override
    public Map<String, String> start() {
        Map<String, String> properties = new HashMap<>();
        setupInMemoryChannels(properties);
        setupPostgres(properties);
        return properties;
    }

    private void setupInMemoryChannels(Map<String, String> properties) {
        /*
         * We'll use in-memory Reactive Messaging connectors to receive and send payloads.
         * See https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/2/testing/testing.html
         */
        properties.putAll(switchIncomingChannelsToInMemory(EVENTS_CHANNEL));
        properties.putAll(switchOutgoingChannelsToInMemory(WEBHOOK_CHANNEL));
    }

    private void setupPostgres(Map<String, String> properties) {
        POSTGRESQL_CONTAINER.start();
        properties.put("quarkus.datasource.jdbc.url", POSTGRESQL_CONTAINER.getJdbcUrl());
        properties.put("quarkus.datasource.username", "test");
        properties.put("quarkus.datasource.password", "test");
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
        POSTGRESQL_CONTAINER.stop();
    }
}
