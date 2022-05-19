package com.redhat.cloud.policies.api.model.condition.expression;

import org.hawkular.alerts.api.model.event.Event;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExprKeyTest {

    @Test
    void compareMixedEventFields() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 1);
        event.setFacts(factMap);
        event.setCategory("timeless");
        event.addTag("c", "f");

        String expr = "facts.a = 'b' AND category = 'timeless' AND tags.c = 'f'";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "category != 'timeless' AND tags.c = 'f'";
        assertFalse(ExprParser.evaluate(event, expr));
    }

    @Test
    void testInnerMapReading() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 1);
        event.addTag("c", "f");

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
    void testEscapedKeyName() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("log.category", "b");
        event.setFacts(factMap);

        String expr = "facts.log.category = 'b'";
        assertFalse(ExprParser.evaluate(event, expr));

        expr = "facts.log\\.category = 'b'";
        assertTrue(ExprParser.evaluate(event, expr));
    }

    @Test
    void testWhitespaceInKeyname() {
        Event event = new Event();
        // tag names are parsed to lower case format
        event.addTag("cost center", "12345");

        // In the query we don't care about the case
        String expr = "'tags.Cost Center' = '12345'";
        assertTrue(ExprParser.evaluate(event, expr));
    }
}
