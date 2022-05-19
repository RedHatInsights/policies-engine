package com.redhat.cloud.policies.engine.condition;

import org.junit.Test;

import static org.junit.Assert.fail;

public class ExprValidationTest {

    @Test
    public void testSimple() {
        // Extra whitespace is intentional
        String expr = "(cores = 1  OR rhelversion > 8)";
        ConditionParser.validate(expr);

        expr = "cores = 1 OR rhelversion > 8";
        ConditionParser.validate(expr);

        try {
            expr = "(cores = 1  OR rhelversion > 8";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    public void testEquals() {
        String expr = "(cores = 1 OR rhelversion = \"auto\")";
        ConditionParser.validate(expr);

        expr = "cores = 1 AND rhelversion = 'auto'";
        ConditionParser.validate(expr);
    }

    @Test
    public void testOnlyNumbersForCompares() {
        String expr = "(process_time > 8 AND machines <= 7) OR (cores >= 2 OR day > 1)";
        ConditionParser.validate(expr);

        try {
            expr = "cores >= 'home'";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        expr = "reserved_cores >= 1.7";
        ConditionParser.validate(expr);
    }

    @Test
    public void testCaseInsensitivityOperators() {
        String expr = "(process_time > 8 and machines <= 7) or (cores >= 2 or day > 1)";
        ConditionParser.validate(expr);

        expr = "(process_time > 8 AND machines <= 7) or (cores >= 2 OR day > 1) and (a = 'b')";
        ConditionParser.validate(expr);
    }

    @Test
    public void testOperatorsWithoutExpression() {
        try {
            String expr = "a = 'b' OR";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        try {
            String expr = "AND c = 'd'";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        try {
            String expr = " OR ";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    public void testQuotes() {
        String expr = "machine_name = 'localhost' AND brand = \"RedHat\"";
        ConditionParser.validate(expr);

        try {
            expr = "machine_name = 'home";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        expr = "machine_name = ''";
        ConditionParser.validate(expr);

        // Invalid syntax - the Center part doesn't match anything
        try {
            expr = "tags.Cost Center = PnT";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    public void testNumericParsing() {
        String expr = "a = 1.4";
        ConditionParser.validate(expr);

        expr = "a = 4";
        ConditionParser.validate(expr);

        expr = "a < 1";
        ConditionParser.validate(expr);

        try {
            // Comma is used in some locales, but we don't accept it
            expr = "a = 1,4";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    public void testNegation() {
        String expr = "not (machine_name = 'localhost' OR cores > 9)";
        ConditionParser.validate(expr);

        expr = "not a = 'b'";
        ConditionParser.validate(expr);

        expr = "not (a = 'b')";
        ConditionParser.validate(expr);

        expr = "(a = 'b') and not (c = 'd' or e > 3)";
        ConditionParser.validate(expr);

        expr = "!(a = 'b')";
        ConditionParser.validate(expr);
    }

    @Test
    public void testRejectUnquotedString() {
        try {
            // Unquoted strings are reserved for future use, don't allow them
            String expr = "a = b";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

    }

    @Test
    public void testQuotedNumbers() {
        String expr = "a = \"9.0\"";
        ConditionParser.validate(expr);

        String expr2 = "a = '9.0'";
        ConditionParser.validate(expr2);
    }

    @Test
    public void testRejectWrongQuotedNumbersWithNumericOperator() {
        try {
            // Unquoted strings are reserved for future use, don't allow them
            String expr = "a > 'foobar'";
            ConditionParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    public void testQuotedNumbersWithNumericOperator() {
        String expr = "a > \"9.0\"";
        ConditionParser.validate(expr);
    }
}
