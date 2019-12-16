package org.hawkular.alerts.api;

import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.event.Event;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressionParserTest {

    @Test
    public void testParens2() {
        String expr = "true AND (\"cores\" == 1  OR \"rhelversion\" > \"8\")";

        EventCondition cond = new EventCondition();
        cond.setExpr(expr);

        Event event1 = new Event();
        Map<String,Object> facts = new HashMap<>();
        facts.put("cores",1);
        facts.put("rhelversion","8.1");

        event1.setFacts(facts);
        assertTrue(cond.match(event1));
    }

}
