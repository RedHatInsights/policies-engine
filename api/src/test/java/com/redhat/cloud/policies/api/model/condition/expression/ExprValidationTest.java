package com.redhat.cloud.policies.api.model.condition.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class ExprValidationTest {

    @Test
    void testSimple() {
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
    void testEquals() {
        String expr = "(cores = 1 OR rhelversion = \"auto\")";
        ExprParser.validate(expr);

        expr = "cores = 1 AND rhelversion = 'auto'";
        ExprParser.validate(expr);
    }

    @Test
    void testOnlyNumbersForCompares() {
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
    void testCaseInsensitivityOperators() {
        String expr = "(process_time > 8 and machines <= 7) or (cores >= 2 or day > 1)";
        ExprParser.validate(expr);

        expr = "(process_time > 8 AND machines <= 7) or (cores >= 2 OR day > 1) and (a = 'b')";
        ExprParser.validate(expr);
    }

    @Test
    void testOperatorsWithoutExpression() {
        try {
            String expr = "a = 'b' OR";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        try {
            String expr = "AND c = 'd'";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        try {
            String expr = " OR ";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    void testQuotes() {
        String expr = "machine_name = 'localhost' AND brand = \"RedHat\"";
        ExprParser.validate(expr);

        try {
            expr = "machine_name = 'home";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

        expr = "machine_name = ''";
        ExprParser.validate(expr);

        // Invalid syntax - the Center part doesn't match anything
        try {
            expr = "tags.Cost Center = PnT";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    void testNumericParsing() {
        String expr = "a = 1.4";
        ExprParser.validate(expr);

        expr = "a = 4";
        ExprParser.validate(expr);

        expr = "a < 1";
        ExprParser.validate(expr);

        try {
            // Comma is used in some locales, but we don't accept it
            expr = "a = 1,4";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    void testNegation() {
        String expr = "not (machine_name = 'localhost' OR cores > 9)";
        ExprParser.validate(expr);

        expr = "not a = 'b'";
        ExprParser.validate(expr);

        expr = "not (a = 'b')";
        ExprParser.validate(expr);

        expr = "(a = 'b') and not (c = 'd' or e > 3)";
        ExprParser.validate(expr);

        expr = "!(a = 'b')";
        ExprParser.validate(expr);
    }

    @Test
    void testRejectUnquotedString() {
        try {
            // Unquoted strings are reserved for future use, don't allow them
            String expr = "a = b";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }

    }

    @Test
    void testQuotedNumbers() {
        String expr = "a = \"9.0\"";
        ExprParser.validate(expr);

        String expr2 = "a = '9.0'";
        ExprParser.validate(expr2);
    }

    @Test
    void testRejectWrongQuotedNumbersWithNumericOperator() {
        try {
            // Unquoted strings are reserved for future use, don't allow them
            String expr = "a > 'foobar'";
            ExprParser.validate(expr);
            fail();
        } catch(IllegalArgumentException e) { }
    }

    @Test
    void testQuotedNumbersWithNumericOperator() {
        String expr = "a > \"9.0\"";
        ExprParser.validate(expr);
    }
}
