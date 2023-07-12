package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.policies.engine.process.Event.HostGroup;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;


@QuarkusTest
public class EventTest {
    @Test
    void testAddHostGroupsFromJsonObjects() {
        Event event = new Event();

        var group1 = new JsonObject(Map.of("name", "group_one", "id", "00000000-0000-0000-0000-000000000001"));
        var group2 = new JsonObject(Map.of("name", "group_two", "id", "00000000-0000-0000-0000-000000000002"));
        var groups = JsonArray.of(group1, group2);

        event.addHostGroups(groups);
        var addedGroups = event.getHostGroups();
        assertEquals(2, addedGroups.size());
        assertEquals("group_one", addedGroups.get(0).name);
        assertEquals("00000000-0000-0000-0000-000000000001", addedGroups.get(0).id);
        assertEquals("group_two", addedGroups.get(1).name);
        assertEquals("00000000-0000-0000-0000-000000000002", addedGroups.get(1).id);
    }

    @Test
    void testHostGroupToJsonObject() {
        var group = new HostGroup("00000000-0000-0000-0000-000000000001", "group_one");
        var expected = JsonObject.of("id", "00000000-0000-0000-0000-000000000001",
                                     "name", "group_one");
        assertEquals(expected, group.toJsonObject());
    }
}
