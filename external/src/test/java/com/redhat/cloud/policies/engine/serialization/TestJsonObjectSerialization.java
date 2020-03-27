package com.redhat.cloud.policies.engine.serialization;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonObjectSerialization {
    @Test
    public void testNoNullSerializer() {
        JsonObjectNoNullSerializer ser = new JsonObjectNoNullSerializer();
        JsonObject json = new JsonObject();
        json.put("test", (String) null);
        byte[] jsonBytes = ser.serialize("", json);

        assertEquals("{\"test\":\"\"}", new String(jsonBytes));
    }
}
