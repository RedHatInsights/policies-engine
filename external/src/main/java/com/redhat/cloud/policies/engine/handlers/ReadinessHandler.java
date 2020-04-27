package com.redhat.cloud.policies.engine.handlers;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.hawkular.alerts.api.services.StatusService;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@Readiness
@ApplicationScoped
public class ReadinessHandler implements HealthCheck {

    private static final MsgLogger log = MsgLogging.getMsgLogger(ReadinessHandler.class);

    @Inject
    StatusService statusService;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder response = HealthCheckResponse.named("Policies Engine readiness check")
                .state(statusService.isHealthy());

        if(!statusService.isStarted()) {
            response.withData("starting", "true");
        }

        for (Map.Entry<String, String> addE : statusService.getAdditionalStatus().entrySet()) {
            response.withData(addE.getKey(), addE.getValue());
        }

        if(statusService.isDistributed()) {
            for (Map.Entry<String, String> de : statusService.getDistributedStatus().entrySet()) {
                response.withData(de.getKey(), de.getValue());
            }
        }

        return response.build();
    }
}
