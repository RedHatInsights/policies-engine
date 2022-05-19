package com.redhat.cloud.policies.engine.condition;

import com.redhat.cloud.policies.engine.process.Event;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExprEvaluationTest {

    @Test
    public void testStrComparisons() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "facts.a = 'b'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a = 'b'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.A = 'B'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a != 'c'";
        assertTrue(ConditionParser.evaluate(event, expr));

        // String comparison, equality should still match?
        expr = "facts.b = '3'";
        assertTrue(ConditionParser.evaluate(event, expr));

        // Empty string matching
        expr = "facts.c = ''";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.c != ' '";
        assertTrue(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void testImpossibleStrComparisons() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 4);
        event.setFacts(factMap);

        String expr = "a > 3";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "a < 3";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "a <= 3";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "a >= 3";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void testNumberComparisons() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", 2);
        factMap.put("b", 4.0);
        factMap.put("c", 4.1);
        event.setFacts(factMap);

        String expr = "facts.a = 2";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a != 3";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a >= 2";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a < 2.1";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.b = 4.00";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.b = 4";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.b > 3";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.b <= 5";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.c < 3";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "facts.b = 3.99";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "facts.c != 4.1";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void testNullMatching() {
        // Mainly error handling - should return false and not fail
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("c", null);
        event.setFacts(factMap);

        String expr = "facts.c = 2";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "facts.c = 'b'";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void testInMatching() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        event.setFacts(factMap);

        String expr = "facts.a IN ['b', 'c', 'd']";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a IN ['b']";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a IN []";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "facts.a IN ['c', 'd']";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void testNotMatching() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 1);
        event.setFacts(factMap);

        String expr = "NOT (facts.a IN ['c', 'd'])";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "NOT (facts.a != 3)";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "NOT (facts.b >= 2)";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "NOT facts.a = 'b'";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "!(facts.a = 'b')";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void testDefine() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 1);
        event.setFacts(factMap);

        String expr = "facts.a";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.c";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "(facts.c)";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "NOT facts.c";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a = 'b' AND facts.b";
        assertTrue(ConditionParser.evaluate(event, expr));
    }
}
