package com.redhat.cloud.policies.api.model.condition.expression;

import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionBaseVisitor;
import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionLexer;
import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.hawkular.alerts.api.model.event.Event;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Map;

public class ExprParser extends ExpressionBaseVisitor<Boolean> {

    private static final String TENANT_ID = "tenantId";
    private static final String ID = "id";
    private static final String CTIME = "ctime";
    private static final String TEXT = "text";
    private static final String CATEGORY = "category";
    private static final String TAGS = "tags.";
    private static final String FACTS = "facts.";

    private static final String KEY_REGEXP = "(?<!\\\\)\\.";

    static ParseTree createParserTree(String expression, ANTLRErrorListener errorListener) {
        CharStream cs = CharStreams.fromString(expression);
        ExpressionLexer lexer = new ExpressionLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return parser.expression();
    }

    public static void validate(String expression) {
        ThrowingErrorHandler errorListener = new ThrowingErrorHandler();
        createParserTree(expression, errorListener);
    }

    public static boolean evaluate(Event value, String expression) {
        ThrowingErrorHandler errorListener = new ThrowingErrorHandler();
        ParseTree parseTree = createParserTree(expression, errorListener);
        ExprVisitor visitor = new ExprVisitor(value);
        return visitor.visit(parseTree);
    }

    private static class ThrowingErrorHandler implements ANTLRErrorListener {

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object o, int line, int charPos, String msg, RecognitionException e) {
            String errorMsg = String.format("Invalid expression: %s at line %d position %d", msg, line, charPos);
            throw new IllegalArgumentException(errorMsg);
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

        }
    }

    // ExpressionBaseVisitor

    static class ExprVisitor extends ExpressionBaseVisitor<Boolean> {
        private Event value;

        ExprVisitor(Event value) {
            this.value = value;
        }

        @Override
        public Boolean visitExpression(ExpressionParser.ExpressionContext ctx) {
            if(ctx.object() != null) {
                return visitObject(ctx.object());
            }
            return super.visitExpression(ctx);
        }

        @Override
        public Boolean visitObject(ExpressionParser.ObjectContext ctx) {
            if(ctx.expr() != null) {
                if(ctx.negative_expr() != null) {
                    return !visitExpr(ctx.expr());
                }
                return visitExpr(ctx.expr());
            }

            if(ctx.logical_operator() != null) {
                ExpressionParser.Logical_operatorContext op = ctx.logical_operator();
                ExpressionParser.ObjectContext left = ctx.object(0);
                ExpressionParser.ObjectContext right = ctx.object(1);

                if(op.AND() != null) {
                    return visitObject(left) && visitObject(right);
                } else if (op.OR() != null) {
                    return visitObject(left) || visitObject(right);
                }

                return false;
            }

            if(ctx.object() != null && ctx.object().size() == 1) {
                if(ctx.negative_expr() != null) {
                    return !visitObject(ctx.object(0));
                }
                return visitObject(ctx.object(0));
            }
            return false;
        }

        /**
         * @param ctx Expr from left or right
         * @return Return boolean if the comparison was successful
         */
        @Override
        public Boolean visitExpr(ExpressionParser.ExprContext ctx) {
            String key = null;
            String strValue = null;
            BigDecimal decimalValue = null;

            ExpressionParser.ValueContext value = ctx.value();

            // This is the factKey
            if(ctx.key() != null) {
                key = ctx.key().SIMPLETEXT().getSymbol().getText();
            } else {
                return false;
            }

            final Object targetValue = decodeKeyToValue(key);
            if(targetValue == null) {
                // Doesn't matter if the key exists or not - the value will not match
                return false;
            }

            String targetValueStr = targetValue.toString();
            BigDecimal targetValueDecimal = null;

            // Fact comparison value
            try {
                TerminalNode number = null;
                if(value != null) {
                    strValue = valueToString(value);
                    if(value.NUMBER() != null) {
                        number = value.NUMBER();
                    }
                } else if(ctx.numerical_value() != null) {
                    number = ctx.numerical_value().NUMBER();
                }

                if(number != null) {
                    decimalValue = new BigDecimal(number.getSymbol().getText());

                    // Convert to BigDecimal supported types
                    if(targetValue instanceof Long) {
                        targetValueDecimal = new BigDecimal((Long) targetValue);
                    } else if(targetValue instanceof Double) {
                        targetValueDecimal = BigDecimal.valueOf((Double) targetValue);
                    } else if(targetValue instanceof Float) {
                        targetValueDecimal = BigDecimal.valueOf((Float) targetValue);
                    } else if(targetValue instanceof Integer) {
                        targetValueDecimal = new BigDecimal((Integer) targetValue);
                    } else if(targetValue instanceof String) {
                        // Do String comparison
                        try {
                            targetValueDecimal = new BigDecimal(targetValueStr);
                        } catch (NumberFormatException e) {
                            // Only allow String comparison operators
                            targetValueDecimal = null;
                        }
                    } else {
                        return false;
                    }
                    if (targetValueDecimal != null) {
                        targetValueStr = targetValueDecimal.toString();
                    }
                    strValue = decimalValue.toString();
                }

                if(targetValueStr == null) {
                    targetValueStr = targetValue.toString();
                }
            } catch(NumberFormatException e) {
                e.printStackTrace();
                return false;
            }

            // Equality checks
            if(ctx.boolean_operator() != null) {
                final ExpressionParser.Boolean_operatorContext op = ctx.boolean_operator();
                boolean compareResult = false;
                if(decimalValue != null && targetValueDecimal != null) {
                    compareResult = decimalValue.compareTo(targetValueDecimal) == 0;
                } else if(strValue != null) {
                    compareResult = targetValueStr.equals(strValue);
                }

                if(op.EQUAL() != null) {
                    return compareResult;
                } else if(op.NOTEQUAL() != null) {
                    return !compareResult;
                }

                return false;
            }

            // These need to be integers or doubles
            if(ctx.numeric_compare_operator() != null) {
                if(decimalValue == null || targetValueDecimal == null) {
                    return false;
                }
                ExpressionParser.Numeric_compare_operatorContext op = ctx.numeric_compare_operator();
                if(op.GT() != null) {
                    return decimalValue.compareTo(targetValueDecimal) < 0;
                } else if(op.GTE() != null) {
                    return decimalValue.compareTo(targetValueDecimal) <= 0;
                } else if(op.LT() != null) {
                    return decimalValue.compareTo(targetValueDecimal) > 0;
                } else if(op.LTE() != null) {
                    return decimalValue.compareTo(targetValueDecimal) >= 0;
                }

                return false;
            }

            // Operators which support targetArray
            if(ctx.string_compare_operator() != null) {
                // If the value is an array, do exact match
                // If the value is string, do string contains match
                // What if we're talking about a number..?
                if(ctx.value() != null) {
                    // String contains
                    if(ctx.string_compare_operator().CONTAINS() != null) {
                        // Repetitive code, refactor at some point when more array operators are known
                        if(targetValue instanceof Iterable) {
                            return arrayContains((Iterable) targetValue, strValue);
                        } else {
                            return targetValueStr.contains(strValue);
                        }
                    }
                }
                if(ctx.array() != null) {
                    boolean validForAll = true;
                    for (ExpressionParser.ValueContext valueContext : ctx.array().value()) {
                        String val = valueToString(valueContext);
                        validForAll &= targetValueStr.contains(val);
                    }
                    return validForAll;
                }
                return false;
            }

            if(ctx.array_operator() != null) {
                if(ctx.array() != null) {
                    // We have restricted array values to be strings always
                    if(ctx.string_compare_operator() != null) {
                        if(ctx.string_compare_operator().CONTAINS() != null) {
                            boolean validForAll = true;
                            for (ExpressionParser.ValueContext valueContext : ctx.array().value()) {
                                String val = valueToString(valueContext);
                                if (targetValue instanceof Iterable) {
                                    validForAll &= arrayContains((Iterable) targetValue, val);
                                } else {
                                    validForAll &= targetValueStr.contains(val);
                                }
                            }

                            return validForAll;
                        }
                    }
                    else if(ctx.array_operator() != null) {
                        if(ctx.array_operator().IN() != null) {
                            boolean validForAny = false;
                            for (ExpressionParser.ValueContext valueContext : ctx.array().value()) {
                                String val = valueToString(valueContext);
                                validForAny |= targetValueStr.equals(val);
                            }
                            return validForAny;
                        }
                    }
                }
            }

            // The define only check (negative is thrown out earlier)
            return true;
        }

        private Object decodeKeyToValue(String eventField) {
            Object sEventValue = null;
            if (TENANT_ID.equals(eventField)) {
                sEventValue = value.getTenantId();
            } else if (ID.equals(eventField)) {
                sEventValue = value.getId();
            } else if (CTIME.equals(eventField)) {
                sEventValue = value.getCtime();
            } else if (TEXT.equals(eventField)) {
                sEventValue = value.getText();
            } else if (CATEGORY.equals(eventField)) {
                sEventValue = value.getCategory();
            } else if (eventField.startsWith(TAGS)) {
                // We get the key from tags.<key> string
                String key = eventField.substring(5);
                sEventValue = value.getTags().get(key);
            } else if (eventField.startsWith(FACTS)) {
                if(value.getFacts() == null) {
                    return null;
                }

                String key = eventField.substring(6);

                // Allow matching of keys with dot in them if they're escaped correctly
                String[] subMap = key.split(KEY_REGEXP, 2);
                String innerKey = subMap[0].replace("\\.", ".");
                Object innerValue = value.getFacts().get(innerKey);

                while(subMap.length > 1) {
                    if(innerValue instanceof Map) {
                        subMap = subMap[1].split(KEY_REGEXP, 2);
                        innerKey = subMap[0].replace("\\.", ".");
                        innerValue = ((Map) innerValue).get(innerKey);
                    } else {
                        break;
                    }
                }

                sEventValue = innerValue;
            }

            return sEventValue;
        }
    }

    static boolean arrayContains(Iterable<?> targetValue, String matcher) {
        boolean anyMatch = false;
        for (Object o : targetValue) {
            anyMatch |= o.toString().equals(matcher);
        }
        return anyMatch;
    }

    static String valueToString(ExpressionParser.ValueContext value) {
        String strValue = null;
        if (value.STRING() != null) {
            strValue = value.STRING().getSymbol().getText();
            strValue = strValue.replaceAll("^(['\"])(.*)\\1$", "$2");
        }

        return strValue;
    }
}


