package com.redhat.cloud.custompolicies.api.model.condition.expression;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExprEvaluationTest {

    @Test
    public void testStrComparisons() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);

        String expr = "a = b";
        assertTrue(ExprParser.evaluate(factMap, expr));

        System.out.println("First");

        expr = "a = 'b'";
        assertTrue(ExprParser.evaluate(factMap, expr));

        System.out.println("Second");

        expr = "a != 'c'";
        assertTrue(ExprParser.evaluate(factMap, expr));

        // String comparison, equality should still match?
        expr = "b = '3'";
        assertTrue(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void testImpossibleStrComparisons() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 4);

        String expr = "a > 3";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "a < 3";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "a <= 3";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "a >= 3";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void testIntegerComparisons() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", 4);
        factMap.put("b", 4);
    }
}
