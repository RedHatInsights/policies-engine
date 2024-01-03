package com.redhat.cloud.policies.engine.rest;

import com.redhat.cloud.policies.engine.condition.ConditionParser;
import io.quarkus.logging.Log;
import io.vertx.core.json.JsonObject;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/lightweight-engine")
public class LightweightEngineResource {

    @PUT
    @Path("/validate")
    @Consumes(TEXT_PLAIN)
    public Response validateCondition(@NotNull String condition) {
        try {
            ConditionParser.validate(condition);
            return Response.ok().build();
        } catch (Exception e) {
            Log.debugf(e, "Validation failed for condition %s", condition);
            JsonObject errorMessage = new JsonObject(Map.of("errorMsg", e.getMessage()));
            return Response.status(BAD_REQUEST).entity(errorMessage).build();
        }
    }
}
