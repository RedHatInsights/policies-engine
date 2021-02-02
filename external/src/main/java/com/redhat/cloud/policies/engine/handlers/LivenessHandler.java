package com.redhat.cloud.policies.engine.handlers;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;

@Liveness
@ApplicationScoped
public class LivenessHandler implements HealthCheck {

    private static boolean isUp = true;

    public static void markAsDown() {
        LivenessHandler.isUp = false;
    }

    @Override
    public HealthCheckResponse call() {
        if (isUp) {
            return HealthCheckResponse.up("Policies Engine has started");
        } else {
            return HealthCheckResponse.down("Policies Engine was marked as down");
        }
    }
}
