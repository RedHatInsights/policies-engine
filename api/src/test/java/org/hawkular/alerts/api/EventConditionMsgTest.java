package org.hawkular.alerts.api;

import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.junit.Test;

import java.util.Collection;

import static org.hawkular.alerts.api.json.JsonUtil.collectionFromJson;
import static org.hawkular.alerts.api.json.JsonUtil.fromJson;
import static org.junit.Assert.assertEquals;

public class EventConditionMsgTest {

    @Test
    public void testErrorMessage() {
        String json = "{\n" +
                "  \"trigger\": {\n" +
                "    \"tenantId\": \"tutorial\",\n" +
                "    \"id\": \"detect-floating\",\n" +
                "    \"name\": \"Node with no infra\",\n" +
                "    \"description\": \"These hosts are not allocated to any known infrastructure provider\",\n" +
                "    \"enabled\": true,\n" +
                "    \"eventType\": \"ALERT\",\n" +
                "    \"firingMatch\": \"ALL\",\n" +
                "    \"tags\": {\n" +
                "      \"demo\": \"old\"\n" +
                "    },\n" +
                "    \"actions\": [\n" +
                "      {\n" +
                "        \"actionPlugin\": \"webhook\",\n" +
                "        \"actionId\": \"notify-slack\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"triggerMode\": \"FIRING\",\n" +
                "      \"type\": \"EVENT\",\n" +
                "      \"dataId\": \"insight_report\",\n" +
                "      \"expression\": \"facts.infrastructure_vendor == 'string'\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        try {
            FullTrigger fullTrigger = fromJson(json, FullTrigger.class);
        } catch (Exception e) {
            assertEquals("Invalid expression: extraneous input '=' expecting {NUMBER, STRING} at line 1 position 29", e.getMessage());
        }
    }

    @Test
    public void testCollectionErrorMsg() {
        String json = "[\n" +
                "{\n" +
                "          \"triggerMode\": \"FIRING\",\n" +
                "          \"type\": \"event\",\n" +
                "          \"dataId\": \"dataId-event-expr\",\n" +
                "          \"expression\": \"NOT (valid\"\n" +
                "}\n" +
                "]\n";

        try {
            Collection<Condition> conditions = collectionFromJson(json, Condition.class);
        } catch (Exception e) {
            assertEquals("Invalid expression: missing ')' at '<EOF>' at line 1 position 10", e.getMessage());
        }
    }
}