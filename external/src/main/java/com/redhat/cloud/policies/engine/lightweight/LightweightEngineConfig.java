package com.redhat.cloud.policies.engine.lightweight;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

// TODO POL-649 Remove this class after we're done migrating to the lightweight engine.
@ApplicationScoped
public class LightweightEngineConfig {

    private static final Logger LOGGER = Logger.getLogger(LightweightEngineConfig.class);

    @ConfigProperty(name = "lightweight-engine.kafka-processing.enabled", defaultValue = "false")
    boolean kafkaProcessingEnabled;

    @ConfigProperty(name = "lightweight-engine.db-loading.enabled", defaultValue = "false")
    boolean dbLoadingEnabled;

    public void runAtStartup(@Observes StartupEvent event) {
        if (!kafkaProcessingEnabled && dbLoadingEnabled) {
            throw new IllegalStateException("The lightweight engine DB loading should not be enabled when the Kafka processing is disabled");
        }
        LOGGER.infof("The lightweight engine Kafka processing is %s", kafkaProcessingEnabled ? "enabled" : "disabled");
        LOGGER.infof("The lightweight engine DB loading is %s", dbLoadingEnabled ? "enabled" : "disabled");
    }

    public boolean isKafkaProcessingEnabled() {
        return kafkaProcessingEnabled;
    }

    public boolean isDbLoadingEnabled() {
        return dbLoadingEnabled;
    }
}
