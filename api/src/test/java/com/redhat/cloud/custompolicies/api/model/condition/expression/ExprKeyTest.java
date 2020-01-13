package com.redhat.cloud.custompolicies.api.model.condition.expression;

import org.hawkular.alerts.api.model.event.Event;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExprKeyTest {

    @Test
    public void compareMixedEventFields() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 1);
        event.setFacts(factMap);
        event.setCategory("timeless");
        Map<String, String> tags = new HashMap<>();
        tags.put("c", "f");
        event.setTags(tags);

        String expr = "facts.a = 'b' AND category = 'timeless' AND tags.c = 'f'";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "category != 'timeless' AND tags.c = 'f'";
        assertFalse(ExprParser.evaluate(event, expr));
    }

    @Test
    public void testInnerMapReading() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 1);
        Map<String, String> tags = new HashMap<>();
        tags.put("c", "f");
        event.setTags(tags);

        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("d", "e");

        factMap.put("c", innerMap);
        event.setFacts(factMap);

        String expr = "facts.a = 'b' AND facts.c.d = 'e'";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "fact.a.b.e = 'e'";
        assertFalse(ExprParser.evaluate(event, expr));

        expr = "tags.c.f = 'd'";
        assertFalse(ExprParser.evaluate(event, expr));
    }

    @Test
    public void testEscapedKeyName() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("log.category", "b");
        event.setFacts(factMap);

        String expr = "facts.log.category = 'b'";
        assertFalse(ExprParser.evaluate(event, expr));

        expr = "facts.log\\.category = 'b'";
        assertTrue(ExprParser.evaluate(event, expr));
    }
}
