package com.redhat.cloud.custompolicies.api.model.condition.expression;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExprLogicTest {

    @Test
    public void testSingleOperators() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");

        String expr = "a = 'b' AND b = 3";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a = 'c' AND b = 3";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "a = 'c' OR b = 3";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a = 'b' OR b = 3";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a = 'c' OR b > 4";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void testSimpleBrackets() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");

        String expr = "( a = 'b' AND b = 3 )";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(a = 'b' AND b = 3)";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(a = 'c' AND b = 3)";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "(a = 'c')";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void testMultipleOperators() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");

        String expr = "(a = 'b' AND b > 2) OR (c = '' AND b < 3)";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "((a = 'b' AND b > 2) OR (c = '' AND b < 3))";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(a = 'b' AND (b > 2 OR (c = '' AND b < 3)))";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(a = 'b' AND (b = 2 OR (c = '' AND b >= 3)))";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(((a = 'b') AND (b = 2)) OR (c = '' AND b >= 3))";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(((a = 'b') AND (b = 6)) OR (c = '' AND b < 3))";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void testNegativeMultipleOperators() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");

        String expr = "(a = 'b' AND b > 2) OR NOT (c = '' AND b < 3)";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "((a = 'b' AND b > 2) OR (c = '' AND NOT b > 3))";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(a = 'b' AND (b > 2 OR (c = '' AND b < 3)))";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(a = 'b' AND (b = 2 OR (c = '' AND b >= 3)))";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(((a = 'b') AND NOT (b = 2)) OR (c = '' AND b >= 3))";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "(((a = 'b') AND (b = 6)) OR (c = '' AND b < 3))";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "(c = '') AND NOT (b <= 2)";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "!(c = '' AND a = 'b')";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }
}
