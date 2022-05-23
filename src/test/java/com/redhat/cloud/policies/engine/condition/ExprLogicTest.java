package com.redhat.cloud.policies.engine.condition;

import com.redhat.cloud.policies.engine.process.Event;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExprLogicTest {

    @Test
    void testSingleOperators() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "facts.a = 'b' AND facts.b = 3";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a = 'c' AND facts.b = 3";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "facts.a = 'c' OR facts.b = 3";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a = 'b' OR facts.b = 3";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a = 'c' OR facts.b > 4";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    void testSimpleBrackets() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "( facts.a = 'b' AND facts.b = 3 )";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND facts.b = 3)";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(facts.a = 'c' AND facts.b = 3)";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "(facts.a = 'c')";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    void testMultipleOperators() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "(facts.a = 'b' AND facts.b > 2) OR (facts.c = '' AND facts.b < 3)";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "((facts.a = 'b' AND facts.b > 2) OR (facts.c = '' AND facts.b < 3))";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND (facts.b > 2 OR (facts.c = '' AND facts.b < 3)))";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND (facts.b = 2 OR (facts.c = '' AND facts.b >= 3)))";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(((facts.a = 'b') AND (facts.b = 2)) OR (facts.c = '' AND facts.b >= 3))";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(((facts.a = 'b') AND (facts.b = 6)) OR (facts.c = '' AND facts.b < 3))";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    void testNegativeMultipleOperators() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "(facts.a = 'b' AND facts.b > 2) OR NOT (facts.c = '' AND facts.b < 3)";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "((facts.a = 'b' AND facts.b > 2) OR (facts.c = '' AND NOT facts.b > 3))";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND (facts.b > 2 OR (facts.c = '' AND facts.b < 3)))";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND (facts.b = 2 OR (facts.c = '' AND facts.b >= 3)))";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(((facts.a = 'b') AND NOT (facts.b = 2)) OR (facts.c = '' AND facts.b >= 3))";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "(((facts.a = 'b') AND (facts.b = 6)) OR (facts.c = '' AND facts.b < 3))";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "(facts.c = '') AND NOT (facts.b <= 2)";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "!(facts.c = '' AND facts.a = 'b')";
        assertFalse(ConditionParser.evaluate(event, expr));
    }
}
