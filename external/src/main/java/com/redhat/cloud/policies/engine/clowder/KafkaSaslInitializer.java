package com.redhat.cloud.policies.engine.clowder;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import java.util.Optional;

public class KafkaSaslInitializer {

    private static final Logger LOGGER = Logger.getLogger(KafkaSaslInitializer.class);
    private static final String KAFKA_SASL_JAAS_CONFIG = "kafka.sasl.jaas.config";
    private static final String KAFKA_SASL_MECHANISM = "kafka.sasl.mechanism";
    private static final String KAFKA_SECURITY_PROTOCOL = "kafka.security.protocol";
    private static final String KAFKA_SSL_TRUSTSTORE_LOCATION = "kafka.ssl.truststore.location";
    private static final String KAFKA_SSL_TRUSTSTORE_TYPE = "kafka.ssl.truststore.type";

    public static void init() {
        Config config = ConfigProvider.getConfig();
        Optional<String> kafkaSaslJaasConfig = config.getOptionalValue(KAFKA_SASL_JAAS_CONFIG, String.class);
        Optional<String> kafkaSaslMechanism = config.getOptionalValue(KAFKA_SASL_MECHANISM, String.class);
        Optional<String> kafkaSecurityProtocol = config.getOptionalValue(KAFKA_SECURITY_PROTOCOL, String.class);
        Optional<String> kafkaSslTruststoreLocation = config.getOptionalValue(KAFKA_SSL_TRUSTSTORE_LOCATION, String.class);
        Optional<String> kafkaSslTruststoreType = config.getOptionalValue(KAFKA_SSL_TRUSTSTORE_TYPE, String.class);

        if (kafkaSaslJaasConfig.isPresent() || kafkaSaslMechanism.isPresent() || kafkaSecurityProtocol.isPresent() || kafkaSslTruststoreLocation.isPresent()) {
            LOGGER.info("Initializing Kafka SASL configuration...");
            setValue(KAFKA_SASL_JAAS_CONFIG, kafkaSaslJaasConfig);
            setValue(KAFKA_SASL_MECHANISM, kafkaSaslMechanism);
            setValue(KAFKA_SECURITY_PROTOCOL, kafkaSecurityProtocol);
            setValue(KAFKA_SSL_TRUSTSTORE_LOCATION, kafkaSslTruststoreLocation);
            setValue(KAFKA_SSL_TRUSTSTORE_TYPE, kafkaSslTruststoreType);
        }
    }

    private static void setValue(String configKey, Optional<String> configValue) {
        configValue.ifPresent(value -> {
            System.setProperty(configKey, value);
            LOGGER.info(configKey + " has been set");
        });
    }
}
