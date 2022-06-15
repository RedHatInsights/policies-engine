package com.redhat.cloud.policies.engine.rest;

import com.redhat.cloud.policies.engine.condition.ConditionParser;
import io.quarkus.logging.Log;
import io.vertx.core.json.JsonObject;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

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
