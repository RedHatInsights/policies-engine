package com.redhat.cloud.policies.engine.lightweight;

import org.hawkular.alerts.api.services.LightweightEngine;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import java.util.Set;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/lightweight-engine")
public class LightweightEngineResource {

    @Inject
    LightweightEngine lightweightEngine;

    @PUT
    @Path("/init")
    public void init() {
        lightweightEngine.init();
    }

    @PUT
    @Path("/validate")
    @Consumes(TEXT_PLAIN)
    public void validateCondition(@NotNull String condition) {
        lightweightEngine.validateCondition(condition);
    }

    @PUT
    @Path("/reload")
    @Consumes(APPLICATION_JSON)
    public void reloadTriggers(@HeaderParam("Hawkular-Tenant") String accountId, @NotNull Set<UUID> triggerIds) {
        lightweightEngine.reloadTriggers(accountId, triggerIds);
    }
}
