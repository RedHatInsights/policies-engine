package com.redhat.cloud.custompolicies.api.model.condition.expression;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExprTest {

    @Test
    public void testSimple() {
        ExprParser exprParser = new ExprParser();
        // Extra whitespace is intentional
        String expr = "(cores = 1  OR rhelversion > 8)";
        exprParser.parse(expr);

        exprParser = new ExprParser();
        expr = "cores = 1 OR rhelversion > 8";
        exprParser.parse(expr);

        try {
            exprParser = new ExprParser();
            expr = "(cores = 1  OR rhelversion > 8";
            exprParser.parse(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    public void testEquals() {
        ExprParser exprParser = new ExprParser();
        String expr = "(cores = 1 OR rhelversion = \"auto\")";
        exprParser.parse(expr);

        exprParser = new ExprParser();
        expr = "cores = 1 AND rhelversion = 'auto'";
        exprParser.parse(expr);
    }

    @Test
    public void testOnlyNumbersForCompares() {
        ExprParser exprParser = new ExprParser();
        String expr = "(process_time > 8 AND machines <= 7) OR (cores >= 2 OR day > 1)";
        exprParser.parse(expr);

        try {
            exprParser = new ExprParser();
            expr = "cores >= 'home'";
            exprParser.parse(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        exprParser = new ExprParser();
        expr = "reserved_cores >= 1.7";
        exprParser.parse(expr);
    }

    @Test
    public void testCaseInsensitivityOperators() {
        ExprParser exprParser = new ExprParser();
        String expr = "(process_time > 8 and machines <= 7) or (cores >= 2 or day > 1)";
        exprParser.parse(expr);
    }

    @Test
    public void testQuotes() {
        ExprParser exprParser = new ExprParser();
        String expr = "machine_name = 'localhost' AND brand = \"RedHat\"";
        exprParser.parse(expr);

        try {
            exprParser = new ExprParser();
            expr = "machine_name = 'home";
            exprParser.parse(expr);
            fail();
        } catch(IllegalArgumentException e) { }

    }

//    @Test
//    public void testNegation() {
//        // Not supported yet
//        ExprParser exprParser = new ExprParser();
//        String expr = "not (machine_name = 'localhost' OR cores > 9)";
//        assertTrue(exprParser.parse(expr));
//    }
}
