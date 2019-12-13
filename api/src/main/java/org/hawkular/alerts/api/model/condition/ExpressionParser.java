/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.alerts.api.model.condition;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author hrupp
 */
class ExpressionParser {

    /**
     * Evaluate the facts vs the given conditions
     *
     * @param facts
     * @return
     */
    static boolean evaluateConditions(Map<String, Object> facts, String expression) {
        if (facts == null) {
            return false;
        }
        if (expression == null || expression.isEmpty()) {
            return true;
        }

        if (expression.contains("(")) {
            // We evaluate left to right
            int lpos = expression.indexOf("(");
            int rpos = expression.lastIndexOf(")");

            String subExpression = expression.substring(lpos + 1, rpos);
            boolean inParens = evaluateConditions(facts, subExpression);
            if (lpos == 0 && rpos == expression.length() - 1) {
                // This consumed the entire expression
                return inParens;
            }

            if (lpos == 0) {
                // Parens start left, check if we have anything right to the closing one
                if (expression.length() > rpos) {
                    subExpression = inParens + " " + expression.substring(rpos + 1);
                    return evaluateConditions(facts, subExpression);
                }
            } else { // TODO parens in the middle
                // Parens are on the right
                subExpression = expression.substring(0, lpos - 1) + " " + inParens;
                return evaluateConditions(facts, subExpression);
            }
        }

        Oper op = Oper.NONE;
        int pos = expression.indexOf(" OR ");
        if (pos != -1) {
            op = Oper.OR;
        } else {
            pos = expression.indexOf(" AND ");
            if (pos != -1) {
                op = Oper.AND;
            }
        }

        if (!op.equals(Oper.NONE)) {
            String leftCond = expression.substring(0, pos);
            String rightCond = expression.substring(pos + (op.equals(Oper.OR) ? 4 : 5));
            boolean leftResult = evalOneCondition(facts, leftCond);
            boolean rightResult = evalOneCondition(facts, rightCond);

            return op.equals(Oper.AND) ? leftResult && rightResult : leftResult || rightResult;
        }

        return evalOneCondition(facts, expression);
    }

    static boolean evalOneCondition(Map<String, Object> facts, String condition) {
        condition = condition.trim();

        if (condition.equalsIgnoreCase("true")) {
            return true;
        }

        if (condition.equalsIgnoreCase("false")) {
            return false;
        }

        // Simple case for now  Fact compare value
        int pos = findComparatorIndex(condition);
        if (pos == -1) {
            throw new IllegalStateException("No comparator found");
        }
        String factName = condition.substring(0, pos);
        factName = stripQuoteSigns(factName);
        int afterComparatorIndex = condition.indexOf(" ", pos + 1);
        if (afterComparatorIndex == -1) {
            throw new IllegalStateException("Comparision lacking right side");
        }
        String comparatorString = condition.substring(pos, afterComparatorIndex);
        String value = condition.substring(pos + comparatorString.length() + 1);

        if (!facts.containsKey(factName)) {
            return false;
        }
        Object factValue = facts.get(factName);

        Comparator comparator = Comparator.fromString(comparatorString);

        boolean result;
        if (factValue instanceof String[]) {
            String[] valueArray = (String[]) factValue;
            if (comparator.equals(Comparator.CONTAINS)) {
                for (String item : valueArray) {
                    if (value.equals(item)) {
                        return true;
                    }
                }
            }
            return false;
        } else if (isStringValue(value)) {
            value = stripQuoteSigns(value);
            result = handleStringValue(value, (String) factValue, comparator);
        } else {
            result = handleIntValue(Integer.parseInt(value), factValue, comparator);
        }

        return result;
    }

    private static boolean handleIntValue(int numValue, Object factValue, Comparator comparator) {
        int valFromFact;

        if (factValue instanceof BigDecimal) {
            valFromFact = ((BigDecimal) factValue).intValueExact();
        } else {
            valFromFact = (Integer) factValue;
        }

        switch (comparator) {
            case EQUALS:
                return numValue == valFromFact;
            case NOT_EQUALS:
                return numValue != valFromFact;
            case LESS:
                return valFromFact < numValue;
            case MORE:
                return valFromFact > numValue;
            case LESS_THAN:
                return valFromFact <= numValue;
            case MORE_THAN:
                return valFromFact >= numValue;
            default:
                return false;
        }
    }

    private static boolean handleStringValue(String value, String factValue, Comparator comparator) {
        switch (comparator) {
            case EQUALS:
                return value.equals(factValue);
            case NOT_EQUALS:
                return !(value.equals(factValue));
            case MORE:
                return factValue.startsWith(value) && !factValue.equals(value);
            case MORE_THAN:
                return factValue.startsWith(value);
            case CONTAINS:
                return factValue.contains(value);
            default:
                return false;
        }
    }

    private static boolean isStringValue(String value) {
        return value.contains("\'") || value.contains("\"");
    }

    // We need to remove spaces and quotation marks around the fact name
    private static String stripQuoteSigns(String factName) {
        String t = factName.trim();
        return t.replace("\'", "").replace("\"", "");
    }

    private static int findComparatorIndex(String expression) {
        for (Comparator c : Comparator.values()) {
            if (expression.contains(c.comparator)) {
                return expression.indexOf(c.comparator);
            }
        }

        return -1;
    }

    private enum Comparator {
        EQUALS("=="),
        NOT_EQUALS("!="),
        LESS("<"),
        MORE(">"),
        LESS_THAN("<="),
        MORE_THAN(">="),
        CONTAINS("?");   // Case sensitive

        String comparator;

        Comparator(String comparator) {
            this.comparator = comparator;
        }

        public static Comparator fromString(String comparatorString) {
            for (Comparator c : Comparator.values()) {
                if (c.comparator.equalsIgnoreCase(comparatorString)) {
                    return c;
                }
            }
            throw new IllegalArgumentException("Unknown comparator " + comparatorString);
        }

        public String getComparator() {
            return comparator;
        }
    }

    private enum Oper { // TODO need better name
        NONE,
        AND,
        OR;
    }
}
