package com.redhat.cloud.custompolicies.api.model.condition.expression;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExprValueTest {

    @Test
    public void testListTargetWithWrongOperators() {
        Map<String, Object> factMap = new HashMap<>();
        List<String> aList = new ArrayList<>(3);
        aList.add("b");
        aList.add("c");
        aList.add("d");
        factMap.put("a", aList);

        String expr = "a = 'b'";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "a > 3";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

//    @Test
    // NotImplementedYet
    public void matchingArray() {
        Map<String, Object> factMap = new HashMap<>();
        List<String> aList = new ArrayList<>(3);
        aList.add("b");
        aList.add("c");
        aList.add("d");
        factMap.put("a", aList);

        String expr = "a = [b, c, d]";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a = [b, c]";
        assertFalse(ExprParser.evaluate(factMap, expr));

        expr = "a = [b, c, a]";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void containsInString() {
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b c");

        String expr = "a contains b";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a contains [b, c]";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a contains [b]";
        assertTrue(ExprParser.evaluate(factMap, expr));

        // Empty array matches always
        expr = "a contains []";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a contains [b, e]";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }

    @Test
    public void containsInArray() {
        Map<String, Object> factMap = new HashMap<>();
        List<String> aList = new ArrayList<>(3);
        aList.add("b");
        aList.add("c");
        aList.add("d");
        factMap.put("a", aList);

        String expr = "a contains b";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a contains [b, c]";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a contains [b]";
        assertTrue(ExprParser.evaluate(factMap, expr));

        // Empty array matches always
        expr = "a contains []";
        assertTrue(ExprParser.evaluate(factMap, expr));

        expr = "a contains [b, e]";
        assertFalse(ExprParser.evaluate(factMap, expr));
    }
}
