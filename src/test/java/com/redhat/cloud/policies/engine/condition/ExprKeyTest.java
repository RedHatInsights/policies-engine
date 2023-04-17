package com.redhat.cloud.policies.engine.condition;

import com.redhat.cloud.policies.engine.process.Event;
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
        event.addTag("namespace", "c", "f");

        String expr = "facts.a = 'b' AND category = 'timeless' AND tags.c = 'f'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "category != 'timeless' AND tags.c = 'f'";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    void testInnerMapReading() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 1);
        event.addTag("namespace", "c", "f");

        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("d", "e");

        factMap.put("c", innerMap);
        event.setFacts(factMap);

        String expr = "facts.a = 'b' AND facts.c.d = 'e'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "fact.a.b.e = 'e'";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "tags.c.f = 'd'";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    void testEscapedKeyName() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("log.category", "b");
        event.setFacts(factMap);

        String expr = "facts.log.category = 'b'";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "facts.log\\.category = 'b'";
        assertTrue(ConditionParser.evaluate(event, expr));
    }

    @Test
    void testWhitespaceInKeyname() {
        Event event = new Event();
        // tag names are parsed to lower case format
        event.addTag("namespace", "cost center", "12345");

        // In the query we don't care about the case
        String expr = "'tags.Cost Center' = '12345'";
        assertTrue(ConditionParser.evaluate(event, expr));
    }
}
