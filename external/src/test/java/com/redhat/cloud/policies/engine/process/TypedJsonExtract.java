package com.redhat.cloud.policies.engine.process;

import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypedJsonExtract {

    @Test
    public void testJsonParsing() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);
        JsonObject json = new JsonObject(inputJson);

        JsonObject systemProfile = json.getJsonObject("system_profile");
        Map<String, Object> facts = Receiver.parseSystemProfile(systemProfile);

        Map<String, Object> yum_repos = (Map<String, Object>) facts.get("yum_repos");
        Map<String, Object> string = (Map<String, Object>) yum_repos.get("string");
        assertEquals(true, string.get("enabled"));

        Map<String, Object> networkInterfaces = (Map<String, Object>) facts.get("network_interfaces");
        Map<String, Object> eth0 = (Map<String, Object>) networkInterfaces.get("eth0");
        assertTrue(eth0.containsKey("ipv4_addresses"));
        Iterable<String> ipv4Addresses = (Iterable<String>) eth0.get("ipv4_addresses");
        assertTrue(ipv4Addresses.iterator().hasNext());
        assertEquals("198.51.100.42", ipv4Addresses.iterator().next());
    }

}
