package com.redhat.cloud.custompolicies.engine.process;

import io.vertx.axle.core.Vertx;
import io.vertx.axle.core.buffer.Buffer;
import io.vertx.axle.ext.web.client.HttpResponse;
import io.vertx.axle.ext.web.client.WebClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@ApplicationScoped
public class SystemProfileClient {
    static String SYSTEM_PROFILE = "system_profile";
    private static String AUTH_HEADER = "x-rh-identity";

    private final MsgLogger log = MsgLogging.getMsgLogger(SystemProfileClient.class);

    @Inject
    Vertx vertx;

    @ConfigProperty(name="external.insights-host-inventory.server")
    String insightsInventoryHostname;

    @ConfigProperty(name="external.insights-host-inventory.port")
    Integer insightsInventoryPort;

    private WebClient client;

    @PostConstruct
    void initialize() {
        client = WebClient.create(vertx, new WebClientOptions().setDefaultHost(insightsInventoryHostname).setDefaultPort(insightsInventoryPort));
    }

    private String getAuthHeader(String accountNumber) {
        JsonObject authHeader = new JsonObject();
        JsonObject identityObject = new JsonObject();
        identityObject.put("account_number", accountNumber);
        authHeader.put("identity", identityObject);

        return new String(Base64.getEncoder().encode(authHeader.encode().getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Return system_profile mapping for the given account_id, insights_id pair.
     * @param accountNumber
     * @param id
     * @return
     */
    public CompletionStage<JsonObject> getSystemProfile(String accountNumber, String id) {
        CompletionStage<JsonObject> stageSystemProfile = client.get(String.format("/api/inventory/v1/hosts/%s/system_profile", id))
                .putHeader(AUTH_HEADER, getAuthHeader(accountNumber))
                .send()
                .thenApply((Function<HttpResponse<Buffer>, JsonObject>) resp -> {
                    if (resp.statusCode() == 200) {
                        JsonObject body = resp.bodyAsJsonObject();
                        JsonArray results = body.getJsonArray("results");
                        if (results.size() > 0) {
                            JsonObject profileObject = results.getJsonObject(0);
                            if (profileObject.containsKey(SYSTEM_PROFILE)) {
                                return profileObject.getJsonObject(SYSTEM_PROFILE);
                            }
                        }
                        return new JsonObject();
                    } else {
                        log.errorf("Failed to get information from insights-host-inventory: " + resp.bodyAsString());
                        throw new RuntimeException("Could not fetch system_profile data from hosts inventory: " + resp.statusCode());
                    }
                });
        return stageSystemProfile;
    }
}
