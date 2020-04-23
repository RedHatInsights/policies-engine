package com.redhat.cloud.policies.engine.handlers;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.hawkular.alerts.AlertsStandalone;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class ReadinessHandler implements HealthCheck {

    private static final MsgLogger log = MsgLogging.getMsgLogger(ReadinessHandler.class);

    @Inject
    AlertsStandalone alerts;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder response = HealthCheckResponse.named("Policies Engine readiness check");
        if(alerts.isReindexing()) {
            return response
                    .down()
                    .withData("reindex", "In Progress")
                    .build();
        }
        return response.up().build();
    }
}
