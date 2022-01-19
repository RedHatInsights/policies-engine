package com.redhat.cloud.policies.engine.clowder;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Optional;

@ApplicationScoped
public class KafkaSaslInitializer {

    private static final Logger LOGGER = Logger.getLogger(KafkaSaslInitializer.class);
    private static final String KAFKA_SASL_JAAS_CONFIG = "kafka.sasl.jaas.config";
    private static final String KAFKA_SASL_MECHANISM = "kafka.sasl.mechanism";
    private static final String KAFKA_SECURITY_PROTOCOL = "kafka.security.protocol";
    private static final String KAFKA_SSL_TRUSTSTORE_LOCATION = "kafka.ssl.truststore.location";

    @ConfigProperty(name = KAFKA_SASL_JAAS_CONFIG)
    Optional<String> kafkaSaslJaasConfig;

    @ConfigProperty(name = KAFKA_SASL_MECHANISM)
    Optional<String> kafkaSaslMechanism;

    @ConfigProperty(name = KAFKA_SECURITY_PROTOCOL)
    Optional<String> kafkaSecurityProtocol;

    @ConfigProperty(name = KAFKA_SSL_TRUSTSTORE_LOCATION)
    Optional<String> kafkaSslTruststoreLocation;

    void init(@Observes StartupEvent event) {
        if (kafkaSaslJaasConfig.isPresent() || kafkaSaslMechanism.isPresent() || kafkaSecurityProtocol.isPresent() || kafkaSslTruststoreLocation.isPresent()) {
            LOGGER.info("Initializing Kafka SASL configuration...");
            setValue(KAFKA_SASL_JAAS_CONFIG, kafkaSaslJaasConfig);
            setValue(KAFKA_SASL_MECHANISM, kafkaSaslMechanism);
            setValue(KAFKA_SECURITY_PROTOCOL, kafkaSecurityProtocol);
            setValue(KAFKA_SSL_TRUSTSTORE_LOCATION, kafkaSslTruststoreLocation);
        }
    }

    private void setValue(String configKey, Optional<String> configValue) {
        configValue.ifPresent(value -> {
            System.setProperty(configKey, value);
            LOGGER.info(configKey + " has been set");
        });
    }
}
