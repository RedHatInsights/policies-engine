package com.redhat.cloud.policies.engine.condition;

import io.quarkus.logging.Log;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import com.redhat.cloud.policies.engine.process.Event;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConditionParser extends ExpressionBaseVisitor<Boolean> {

    private static final String ID = "id";
    private static final String CTIME = "ctime";
    private static final String TEXT = "text";
    private static final String CATEGORY = "category";
    private static final String TAGS = "tags.";
    private static final String FACTS = "facts.";

    private static final Pattern KEY_REGEXP = Pattern.compile("(?<!\\\\)\\.");
    private static final Pattern ESCAPE_CLEANER_REGEXP = Pattern.compile("^(['\"])(.*)\\1$");

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
            // Nothing to do
        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
            // Nothing to do
        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
            // Nothing to do
        }
    }

    // ExpressionBaseVisitor

    static class ExprVisitor extends ExpressionBaseVisitor<Boolean> {
        private final Event value;

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


            // This is the factKey
            if(ctx.key() != null) {
                if(ctx.key().SIMPLETEXT() != null) {
                    key = ctx.key().SIMPLETEXT().getSymbol().getText();
                } else if (ctx.key().STRING() != null) {
                    key = cleanString(ctx.key().STRING().getSymbol().getText());
                }
            } else {
                return false;
            }

            final Object targetValue = decodeKeyToValue(key);
            if(targetValue == null) {
                // Doesn't matter if the key exists or not - the value will not match
                return false;
            }

            String targetValueStr = cleanString(targetValue.toString());
            BigDecimal targetValueDecimal = null;

            // Fact comparison value
            ExpressionParser.ValueContext valueCtx = ctx.value();

            try {
                TerminalNode number = null;
                if(valueCtx != null) {
                    strValue = valueToString(valueCtx);
                    if(valueCtx.NUMBER() != null) {
                        number = valueCtx.NUMBER();
                    } else if (valueCtx.QUOTED_NUMBER() != null) {
                        number = valueCtx.QUOTED_NUMBER();
                    }
                } else if(ctx.numerical_value() != null) {
                    if (ctx.numerical_value().NUMBER() != null) {
                        number = ctx.numerical_value().NUMBER();
                    } else {
                        number = ctx.numerical_value().QUOTED_NUMBER();
                    }
                }

                if(number != null) {
                    decimalValue = new BigDecimal(cleanString(number.getSymbol().getText()));
                    strValue = cleanString(decimalValue.toString());

                    // Convert to BigDecimal supported types
                    if(!(isArray(targetValue))) {
                        targetValueDecimal = convertToBigDecimal(targetValue);
                        if(targetValueDecimal != null) {
                            targetValueStr = cleanString(targetValueDecimal.toString());
                        } else {
                            // We can't do numeric compare
                            decimalValue = null;
                        }
                    }
                }
            } catch(NumberFormatException e) {
                Log.warn("Failed to parse value into a number " + e.getMessage());
                return false;
            }

            // Equality checks
            if(ctx.boolean_operator() != null) {
                final ExpressionParser.Boolean_operatorContext op = ctx.boolean_operator();
                boolean compareResult = false;
                if(isArray(targetValue)) {
                    // If the targetValue is a container (such as with tags) - replace = operator with
                    // contains operation
                    compareResult = arrayContains((Iterable<?>) targetValue, strValue);
                } else if(decimalValue != null) {
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
                ExpressionParser.Numeric_compare_operatorContext op = ctx.numeric_compare_operator();

                if(isArray(targetValue)) {
                    // Do arrayContains basically.. with numericCompare
                    return arrayNumericMatches(decimalValue, (Iterable<?>) targetValue, op);
                } else if(decimalValue == null) {
                    return false;
                }

                return numericCompare(decimalValue, targetValueDecimal, op);
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
                        if(isArray(targetValue)) {
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
                        if (val != null) {
                            validForAll &= targetValueStr.contains(val);
                        }
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
                                if (isArray(targetValue)) {
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
                                if(isArray(targetValue)) {
                                    validForAny |= arrayContains((Iterable) targetValue, val);
                                } else {
                                    validForAny |= targetValueStr.equals(val);
                                }
                            }
                            return validForAny;
                        }
                    }
                }
            }

            // The define only check (negative is thrown out earlier)
            return true;
        }

        private static boolean isArray(Object targetValue) {
            // Tags values are not considered an array in our operators
            return targetValue instanceof Iterable;
        }

        static boolean numericCompare(BigDecimal decimalValue, BigDecimal targetValueDecimal, ExpressionParser.Numeric_compare_operatorContext op) {
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

        private Object decodeKeyToValue(String eventField) {
            eventField = eventField.toLowerCase();
            Object sEventValue = null;
            if (ID.equals(eventField)) {
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
                Collection<String> tagValues = value.getTags(key).stream().map(c -> c.value).collect(Collectors.toSet());
                if(tagValues.size() == 1) {
                    sEventValue = tagValues.iterator().next();
                } else {
                    // No values for the tag
                    if(tagValues.isEmpty()) {
                        return null;
                    }
                    // Multiple values
                    sEventValue = tagValues;
                }
            } else if (eventField.startsWith(FACTS)) {
                if(value.getFacts() == null) {
                    return null;
                }

                String key = eventField.substring(6);

                // Allow matching of keys with dot in them if they're escaped correctly
                String[] subMap = KEY_REGEXP.split(key, 2);
                String innerKey = subMap[0].replace("\\.", ".").toLowerCase();
                Object innerValue = value.getFacts().get(innerKey);

                while(subMap.length > 1) {
                    if(innerValue instanceof Map) {
                        subMap = KEY_REGEXP.split(subMap[1], 2);
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

        static boolean arrayContains(Iterable<?> targetValue, String matcher) {
            boolean anyMatch = false;
            for (Object o : targetValue) {
                anyMatch |= cleanString(o.toString()).contains(matcher);
            }
            return anyMatch;
        }

        static boolean arrayNumericMatches(BigDecimal decimalValue, Iterable<?> targetValue, ExpressionParser.Numeric_compare_operatorContext op) {
            boolean anyMatch = false;
            // Missing op & targetValue..
            for (Object o : targetValue) {
                BigDecimal targetValueDecimal = convertToBigDecimal(o);
                if(targetValueDecimal != null) {
                    anyMatch |= numericCompare(decimalValue, targetValueDecimal, op);
                }
            }

            return anyMatch;
        }
    }

    public static BigDecimal convertToBigDecimal(Object targetValue) {
        BigDecimal targetValueDecimal = null;

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
                targetValueDecimal = new BigDecimal(cleanString(targetValue.toString()));
            } catch (NumberFormatException e) {
                // Only allow String comparison operators
                targetValueDecimal = null;
            }
        }
        return targetValueDecimal;
    }

    public static String valueToString(ExpressionParser.ValueContext value) {
        String strValue = null;
        if (value.STRING() != null) {
            strValue = value.STRING().getSymbol().getText();
            strValue = cleanString(strValue);
        }

        return strValue;
    }

    static String cleanString(String strValue) {
        return ESCAPE_CLEANER_REGEXP.matcher(strValue).replaceAll("$2").toLowerCase();
    }
}
