package com.redhat.cloud.custompolicies.api.model.condition.expression;

import org.junit.Test;

import static org.junit.Assert.fail;

public class ExprTest {

    @Test
    public void testSimple() {
        // Extra whitespace is intentional
        String expr = "(cores = 1  OR rhelversion > 8)";
        ExprParser.validate(expr);

        expr = "cores = 1 OR rhelversion > 8";
        ExprParser.validate(expr);

        try {
            expr = "(cores = 1  OR rhelversion > 8";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    public void testEquals() {
        String expr = "(cores = 1 OR rhelversion = \"auto\")";
        ExprParser.validate(expr);

        expr = "cores = 1 AND rhelversion = 'auto'";
        ExprParser.validate(expr);
    }

    @Test
    public void testOnlyNumbersForCompares() {
        String expr = "(process_time > 8 AND machines <= 7) OR (cores >= 2 OR day > 1)";
        ExprParser.validate(expr);

        try {
            expr = "cores >= 'home'";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        expr = "reserved_cores >= 1.7";
        ExprParser.validate(expr);
    }

    @Test
    public void testCaseInsensitivityOperators() {
        String expr = "(process_time > 8 and machines <= 7) or (cores >= 2 or day > 1)";
        ExprParser.validate(expr);
    }

    @Test
    public void testQuotes() {
        String expr = "machine_name = 'localhost' AND brand = \"RedHat\"";
        ExprParser.validate(expr);

        try {
            expr = "machine_name = 'home";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    public void testParsingHelper() {
        String expr = "machine_name = 'localhost' AND brand = \"RedHat\"";
//                " AND a = 1";
        ExprParser.validate(expr);
    }

    @Test
    public void theSimplestCase() {
        String expr = "a = 1.4";
        ExprParser.validate(expr);
    }

//    @Test
//    public void testNegation() {
//        // Not supported yet
//        ExprParser exprParser = new ExprParser();
//        String expr = "not (machine_name = 'localhost' OR cores > 9)";
//        assertTrue(exprParser.evaluate(expr));
//    }
}
