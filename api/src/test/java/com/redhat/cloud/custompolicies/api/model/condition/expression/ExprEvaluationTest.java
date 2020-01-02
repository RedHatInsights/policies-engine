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
        factMap.put("c", "");

        String expr = "a = b";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a = 'b'";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a != 'c'";
        assertTrue(ExprParser.evaluate(factMap, expr));

        // String comparison, equality should still match?
        expr = "b = '3'";
        assertTrue(ExprParser.evaluate(factMap, expr));

        // Empty string matching
        expr = "c = ''";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "c != ' '";
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
    public void testNumberComparisons() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", 2);
        factMap.put("b", 4.0);
        factMap.put("c", 4.1);

        String expr = "a = 2";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a != 3";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a >= 2";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a < 2.1";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "b = 4.00";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "b = 4";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "b > 3";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "b <= 5";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "c < 3";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "b = 3.99";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "c != 4.1";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void testNullMatching() {
        // Mainly error handling - should return false and not fail
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("c", null);

        String expr = "c = 2";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "c = 'b'";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void testInMatching() {
        // Negations are NotImplementedYet
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");

        String expr = "a IN [b, c, d]";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a IN [b]";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a IN []";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "a IN [c, d]";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void testNotMatching() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 1);

        String expr = "NOT (a IN [c, d])";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "NOT (a != 3)";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "NOT (b >= 2)";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "NOT a = 'b'";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "!(a = 'b')";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }
}
