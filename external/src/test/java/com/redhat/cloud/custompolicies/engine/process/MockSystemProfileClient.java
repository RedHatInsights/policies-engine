package com.redhat.cloud.custompolicies.engine.process;

import io.quarkus.test.Mock;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Mock
public class MockSystemProfileClient extends SystemProfileClient {

    private JsonObject constructFactsMap() {
        Map<String, Object> facts = new HashMap<>();
        facts.put("arch", "string");
        return new JsonObject(facts);
    }

    @Override
    public CompletionStage<JsonObject> getSystemProfile(String accountNumber, String id) {
        return CompletableFuture.supplyAsync(this::constructFactsMap);
    }
}
