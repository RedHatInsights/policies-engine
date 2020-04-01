package com.redhat.cloud.policies.engine.metrics;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class QuarkusRegistryConfig {

    public static MsgLogger LOGGER = MsgLogging.getMsgLogger(QuarkusRegistryConfig.class);
    @Inject
    Vertx vertx;

    void init(@Observes StartupEvent startupEvent) {
        LOGGER.infof("=============> VERTX-METRICS: %s", vertx.isMetricsEnabled());
    }


}
