package com.redhat.cloud.policies.engine.rest;

import com.redhat.cloud.policies.api.model.condition.expression.ExprParser;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(LightweightEngineResource.class);

    @PUT
    @Path("/validate")
    @Consumes(TEXT_PLAIN)
    public Response validateCondition(@NotNull String condition) {
        try {
            ExprParser.validate(condition);
            return Response.ok().build();
        } catch (Exception e) {
            LOGGER.debugf(e, "Validation failed for condition %s", condition);
            JsonObject errorMessage = new JsonObject(Map.of("errorMsg", e.getMessage()));
            return Response.status(BAD_REQUEST).entity(errorMessage).build();
        }
    }
}
